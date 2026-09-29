package com.sucy.skill.dynamic.signal;

import com.rit.sucy.config.parse.DataSection;
import com.sucy.skill.dynamic.DynamicSkill;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Validates v1 tree signal contracts before registration and bounds synchronous dispatch.
 * Its state belongs to one Bukkit server process and is discarded on plugin reload.
 */
public final class SignalManager {
    private static final int MAX_DEPTH = 12;
    private static final int MAX_DELIVERIES = 256;
    private static final Map<String, Map<String, String>> CONTRACTS = new HashMap<>();
    private static final Map<String, Rate> RATES = new HashMap<>();
    /** Receivers can be loaded before their sender; resolve these after the batch. */
    private static final List<Declaration> PENDING_RECEIVERS = new ArrayList<>();
    private static final ThreadLocal<Chain> CHAIN = new ThreadLocal<>();

    /** Keeps scan order independent so a receiver may appear before its sender. */
    private static final class Declaration {
        final String channel;
        final boolean sender;
        final Map<String, String> contract;
        final String location;

        private Declaration(String channel, boolean sender, Map<String, String> contract, String location) {
            this.channel = channel;
            this.sender = sender;
            this.contract = contract;
            this.location = location;
        }
    }

    private SignalManager() { }

    private static final class Chain {
        int depth;
        int deliveries;
    }

    private static final class Rate {
        long second;
        int count;
    }

    public static void clear() {
        CONTRACTS.clear();
        RATES.clear();
        PENDING_RECEIVERS.clear();
        CHAIN.remove();
    }

    /** Recurses through native children and rejects incompatible channel declarations atomically. */
    public static void scan(String skill, DataSection components) {
        if (components == null) return;
        Map<String, Map<String, String>> proposed = new HashMap<>(CONTRACTS);
        List<Declaration> declarations = new ArrayList<>();
        List<Declaration> pending = new ArrayList<>();
        collectDeclarations(skill, components, declarations, 0);
        // Sender arguments are the source of truth for new documents. Legacy
        // senders with no arguments may still carry an explicit contract.
        for (Declaration declaration : declarations) {
            if (!declaration.sender) continue;
            Map<String, String> earlier = proposed.putIfAbsent(declaration.channel, declaration.contract);
            if (earlier != null && !earlier.equals(declaration.contract))
                throw new IllegalArgumentException("Conflicting signal contract on '" + declaration.channel + "': " + declaration.location);
        }
        // Receivers can omit the duplicated map when a sender already defined
        // the channel. Explicit receiver contracts remain checked for typos.
        for (Declaration declaration : declarations) {
            if (declaration.sender) continue;
            Map<String, String> earlier = proposed.get(declaration.channel);
            if (earlier == null) {
                if (declaration.contract.isEmpty()) {
                    // The sender may live in another skill that has not been
                    // loaded yet. Defer this one check until the batch ends.
                    pending.add(declaration);
                } else {
                    proposed.put(declaration.channel, declaration.contract);
                }
            } else if (!declaration.contract.isEmpty() && !earlier.equals(declaration.contract)) {
                throw new IllegalArgumentException("Conflicting signal contract on '" + declaration.channel + "': " + declaration.location);
            }
        }
        CONTRACTS.clear();
        CONTRACTS.putAll(proposed);
        // Commit deferred declarations only after every declaration in this
        // skill has passed validation; a failed scan must leave no stale state.
        PENDING_RECEIVERS.addAll(pending);
    }

    /**
     * Resolves receiver declarations deferred by {@link #scan(String, DataSection)}.
     * Registration calls this once after all dynamic skill files are scanned,
     * so channel contracts remain independent of filesystem iteration order.
     */
    public static void validatePending() {
        for (Declaration declaration : PENDING_RECEIVERS) {
            if (!CONTRACTS.containsKey(declaration.channel)) {
                throw new IllegalArgumentException("Missing signal contract: " + declaration.location);
            }
        }
        PENDING_RECEIVERS.clear();
    }

    private static void collectDeclarations(String skill, DataSection children,
                                             List<Declaration> declarations, int depth) {
        if (children == null) return;
        if (depth > 100) throw new IllegalArgumentException("Signal tree too deep: " + skill);
        for (String key : children.keys()) {
            DataSection node = children.getSection(key);
            if (node == null) continue;
            String name = key.replaceAll("-.+", "").toLowerCase(Locale.ROOT);
            if (name.replace(' ', '_').equals("signal_received") || name.equals("signal emit")) {
                DataSection data = node.getSection("data");
                String channel = data == null ? "" : data.getString("channel", "").trim();
                if (channel.isEmpty()) throw new IllegalArgumentException("Missing signal channel: " + skill + "/" + key);
                Object rawContract = data == null ? null : data.get("contract");
                Map<String, String> contract = contract(rawContract);
                boolean sender = name.equals("signal emit");
                if (sender) {
                    Object rawArguments = data == null ? null : data.get("arguments");
                    Map<String, String> derived = deriveContract(rawArguments);
                    // An explicit map is accepted only as a legacy consistency
                    // check. Legacy YAML often stores every scalar as text, so
                    // a declared number/boolean is retained when its value is
                    // safely convertible; new typed rows still use the derived map.
                    if (!contract.isEmpty() && !contract.equals(derived)
                            && !legacyContractAccepts(contract, rawArguments))
                        throw new IllegalArgumentException("Signal arguments do not match contract: " + skill + "/" + key);
                    contract = contract.isEmpty() ? derived : contract;
                }
                declarations.add(new Declaration(channel, sender, contract, skill + "/" + key));
            }
            collectDeclarations(skill, node.getSection("children"), declarations, depth + 1);
        }
    }

    /** Validates legacy string scalars without allowing missing or extra fields. */
    private static boolean legacyContractAccepts(Map<String, String> contract, Object rawArguments) {
        Map<String, Object> values = map(rawArguments);
        if (!values.keySet().equals(contract.keySet())) return false;
        for (Map.Entry<String, String> field : contract.entrySet()) {
            Object value = values.get(field.getKey());
            if (value == null) return false;
            try {
                switch (field.getValue()) {
                    case "number":
                        if (!Double.isFinite(Double.parseDouble(value.toString()))) return false;
                        break;
                    case "boolean":
                        if (!"true".equalsIgnoreCase(value.toString()) && !"false".equalsIgnoreCase(value.toString())) return false;
                        break;
                    case "text":
                        break;
                    default:
                        return false;
                }
            } catch (NumberFormatException exception) {
                return false;
            }
        }
        return true;
    }

    /** DataSection and YAML maps differ in representation, but share a string-key contract. */
    public static Map<String, String> contract(Object raw) {
        Map<String, Object> values = map(raw);
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            String type = String.valueOf(entry.getValue()).toLowerCase(Locale.ROOT);
            if (!type.equals("number") && !type.equals("text") && !type.equals("boolean"))
                throw new IllegalArgumentException("Invalid signal parameter type: " + entry.getKey());
            result.put(entry.getKey(), type);
        }
        return result;
    }

    /** Infers the wire type from scalar argument values when a sender omits a contract. */
    public static Map<String, String> deriveContract(Object raw) {
        Map<String, Object> values = map(raw);
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof Number && Double.isFinite(((Number) value).doubleValue()))
                result.put(entry.getKey(), "number");
            else if (value instanceof Boolean)
                result.put(entry.getKey(), "boolean");
            else if (value instanceof String)
                result.put(entry.getKey(), "text");
            else
                throw new IllegalArgumentException("Signal argument must be text, number or boolean: " + entry.getKey());
        }
        return result;
    }

    public static Map<String, Object> map(Object raw) {
        if (raw == null) return Collections.emptyMap();
        Map<String, Object> result = new LinkedHashMap<>();
        if (raw instanceof DataSection) {
            DataSection section = (DataSection) raw;
            for (String key : section.keys()) result.put(key, section.get(key));
        } else if (raw instanceof Map) {
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) raw).entrySet())
                result.put(String.valueOf(entry.getKey()), entry.getValue());
        } else throw new IllegalArgumentException("Signal contract/arguments must be a mapping");
        return result;
    }

    /** Resolve typed values only after contract validation; unknowns cannot silently become zero. */
    public static Map<String, Object> arguments(LivingEntity sender, String channel, Object raw) {
        Map<String, String> contract = CONTRACTS.get(channel);
        if (contract == null) throw new IllegalArgumentException("Undeclared signal channel: " + channel);
        Map<String, Object> supplied = map(raw);
        if (!supplied.keySet().equals(contract.keySet()))
            throw new IllegalArgumentException("Signal arguments do not match contract: " + channel);
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, String> field : contract.entrySet()) {
            Object value = supplied.get(field.getKey());
            if (value instanceof String && ((String) value).startsWith("{") && ((String) value).endsWith("}")) {
                String key = ((String) value).substring(1, ((String) value).length() - 1);
                value = DynamicSkill.getCastData(sender).get(key);
            }
            if (value == null) throw new IllegalArgumentException("Missing signal value: " + field.getKey());
            switch (field.getValue()) {
                case "number":
                    double number = Double.parseDouble(value.toString());
                    if (!Double.isFinite(number)) throw new IllegalArgumentException("Non-finite signal number");
                    result.put(field.getKey(), number);
                    break;
                case "boolean":
                    if (!"true".equalsIgnoreCase(value.toString()) && !"false".equalsIgnoreCase(value.toString()))
                        throw new IllegalArgumentException("Invalid signal boolean");
                    result.put(field.getKey(), Boolean.parseBoolean(value.toString()));
                    break;
                default: result.put(field.getKey(), value.toString());
            }
        }
        return result;
    }

    /** Save only the signal namespace, allowing ordinary cast data writes to survive a receiver. */
    public static Map<String, Object> push(SignalEvent event) {
        Map<String, Object> data = DynamicSkill.getCastData(event.getRecipient());
        Map<String, Object> old = new HashMap<>();
        for (String key : event.getArguments().keySet()) {
            String name = "signal-" + key;
            if (data.containsKey(name)) old.put(name, data.get(name));
        }
        for (String key : new String[] {"signal-channel", "signal-sender"})
            if (data.containsKey(key)) old.put(key, data.get(key));
        return old;
    }

    public static void pop(SignalEvent event, Map<String, Object> old) {
        Map<String, Object> data = DynamicSkill.getCastData(event.getRecipient());
        for (String key : event.getArguments().keySet()) data.remove("signal-" + key);
        data.remove("signal-channel");
        data.remove("signal-sender");
        data.putAll(old);
    }

    /** Only loaded living entities participate; broadcast never loads a chunk. */
    public static boolean emit(LivingEntity sender, List<LivingEntity> targets, String channel,
                               String scope, Map<String, Object> arguments) {
        if (sender == null || !CONTRACTS.containsKey(channel)) return false;
        Chain chain = CHAIN.get();
        boolean outer = chain == null;
        if (outer) chain = new Chain();
        if (chain.depth >= MAX_DEPTH || chain.deliveries >= MAX_DELIVERIES) return false;
        String rateKey = sender.getUniqueId() + "/" + channel;
        long second = System.currentTimeMillis() / 1000;
        // Bound sustained loops without suppressing legitimate same-tick sibling emits.
        Rate rate = RATES.computeIfAbsent(rateKey, ignored -> new Rate());
        if (rate.second != second) { rate.second = second; rate.count = 0; }
        if (++rate.count > 128) return false;
        if (RATES.size() > 4096) RATES.entrySet().removeIf(entry -> entry.getValue().second < second - 1);
        if (outer) CHAIN.set(chain);
        chain.depth++;
        try {
            Set<UUID> seen = new LinkedHashSet<>();
            // Keep the candidate list bounded before iterating a world. A server-wide
            // signal must not allocate one entry for every loaded mob just to discard
            // all but the first MAX_DELIVERIES recipients below.
            List<LivingEntity> recipients = new ArrayList<>(MAX_DELIVERIES - chain.deliveries);
            int scanBudget = MAX_DELIVERIES - chain.deliveries;
            switch (scope.toLowerCase(Locale.ROOT)) {
                case "self": recipients.add(sender); break;
                case "target":
                    for (LivingEntity target : targets) {
                        if (recipients.size() >= scanBudget) break;
                        recipients.add(target);
                    }
                    break;
                case "world": addWorld(sender.getWorld(), recipients, scanBudget); break;
                case "server":
                    for (World world : Bukkit.getWorlds()) {
                        if (recipients.size() >= scanBudget) break;
                        addWorld(world, recipients, scanBudget - recipients.size());
                    }
                    break;
                default: throw new IllegalArgumentException("Unknown signal scope: " + scope);
            }
            boolean delivered = false;
            for (LivingEntity recipient : recipients) {
                if (recipient == null || !recipient.isValid() || !seen.add(recipient.getUniqueId())) continue;
                if (++chain.deliveries > MAX_DELIVERIES) break;
                Bukkit.getPluginManager().callEvent(new SignalEvent(channel, sender, recipient, arguments));
                delivered = true;
            }
            return delivered;
        } finally {
            chain.depth--;
            if (outer) CHAIN.remove();
        }
    }

    /** Adds at most the remaining per-chain scan budget; never loads another chunk. */
    private static void addWorld(World world, List<LivingEntity> result, int limit) {
        if (limit <= 0) return;
        int startSize = result.size();
        for (Entity entity : world.getEntities()) {
            if (result.size() - startSize >= limit) break;
            if (entity instanceof LivingEntity) result.add((LivingEntity) entity);
        }
    }
}
