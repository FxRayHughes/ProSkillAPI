package com.sucy.skill.combat.shield;

import com.sucy.skill.SkillAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Owns transient, independently consumable shields. All mutations occur on the
 * Bukkit main thread; a preview may be discarded when another plugin cancels a hit.
 */
public final class ShieldManager {
    private static final int MAX_LAYERS_PER_ENTITY = 32;
    private static final Map<UUID, List<Layer>> LAYERS = new HashMap<>();
    private static BukkitTask sweepTask;
    private static long sequence;

    private ShieldManager() { }

    /** A stable snapshot of a single layer's configuration and live capacity. */
    public static final class Layer {
        public final UUID id = UUID.randomUUID();
        public final UUID sourceId;
        public final String key;
        public final double maximum;
        public final int priority;
        public final double ratio;
        public final double perHitLimit;
        public final long expiresAt;
        public final Set<String> kinds;
        public final Set<String> causes;
        public final Set<String> classifications;
        private final long order;
        private double remaining;

        private Layer(UUID sourceId, String key, double capacity, int priority,
                      double ratio, double perHitLimit, long expiresAt,
                      Set<String> kinds, Set<String> causes, Set<String> classifications) {
            this.sourceId = sourceId;
            this.key = key;
            this.maximum = capacity;
            this.remaining = capacity;
            this.priority = priority;
            this.ratio = ratio;
            this.perHitLimit = perHitLimit;
            this.expiresAt = expiresAt;
            this.kinds = normalized(kinds);
            this.causes = normalized(causes);
            this.classifications = normalized(classifications);
            this.order = sequence++;
        }

        public double getRemaining() { return remaining; }
    }

    /** A proposed hit is immutable until the underlying damage event survives cancellation. */
    public static final class Resolution {
        private final LivingEntity target;
        private final double original;
        private final Map<UUID, Double> allocations;

        private Resolution(LivingEntity target, double original, Map<UUID, Double> allocations) {
            this.target = target;
            this.original = original;
            this.allocations = allocations;
        }

        public double getAbsorbed() {
            double total = 0;
            for (double amount : allocations.values()) total += amount;
            return total;
        }

        public double getRemainingDamage() { return Math.max(0, original - getAbsorbed()); }
    }

    public static void start(SkillAPI plugin) {
        clear();
        // A periodic sweep also expires shields on entities that never take another hit.
        sweepTask = Bukkit.getScheduler().runTaskTimer(plugin, ShieldManager::sweep, 20L, 20L);
    }

    public static void clear() {
        if (sweepTask != null) sweepTask.cancel();
        sweepTask = null;
        LAYERS.clear();
        sequence = 0;
    }

    /** Duration -1 means until removed or the entity leaves the world. */
    public static Layer add(LivingEntity target, LivingEntity source, String key, double capacity,
                            long durationTicks, int priority, double ratio, double perHitLimit,
                            String stacking, Set<String> kinds, Set<String> causes,
                            Set<String> classifications) {
        if (target == null || key == null || key.trim().isEmpty()
                || !Double.isFinite(capacity) || capacity <= 0
                || !Double.isFinite(ratio) || ratio <= 0 || ratio > 1
                || !Double.isFinite(perHitLimit) || perHitLimit < 0
                || durationTicks < -1) return null;
        if (durationTicks > 0 && durationTicks > (Long.MAX_VALUE - System.currentTimeMillis()) / 50L)
            return null;
        List<Layer> list = LAYERS.computeIfAbsent(target.getUniqueId(), ignored -> new ArrayList<>());
        expire(target, list);
        String mode = stacking == null ? "stack" : stacking.toLowerCase();
        List<Layer> replaced = new ArrayList<>();
        if ("replace".equals(mode) || "refresh".equals(mode)) {
            for (Iterator<Layer> it = list.iterator(); it.hasNext();) {
                Layer old = it.next();
                if (old.key.equals(key) && equalsSource(old.sourceId, source)) {
                    if ("refresh".equals(mode)) capacity = Math.max(capacity, old.remaining);
                    it.remove();
                    replaced.add(old);
                }
            }
        }
        if (list.size() >= MAX_LAYERS_PER_ENTITY) return null;
        long expiresAt = durationTicks < 0 ? Long.MAX_VALUE
                : System.currentTimeMillis() + durationTicks * 50L;
        Layer layer = new Layer(source == null ? null : source.getUniqueId(), key, capacity,
                priority, ratio, perHitLimit, expiresAt, kinds, causes, classifications);
        list.add(layer);
        // Events run after list mutation: a listener may add or remove another layer.
        for (Layer old : replaced) fire(ShieldEvent.Kind.REMOVED, target, old, old.remaining);
        fire(ShieldEvent.Kind.APPLIED, target, layer, capacity);
        return layer;
    }

    public static Resolution preview(LivingEntity target, double damage, String kind,
                                     String cause, String classification) {
        Map<UUID, Double> allocations = new HashMap<>();
        if (target == null || !Double.isFinite(damage) || damage <= 0)
            return new Resolution(target, Double.isFinite(damage) ? Math.max(0, damage) : 0, allocations);
        List<Layer> list = LAYERS.get(target.getUniqueId());
        if (list == null) return new Resolution(target, damage, allocations);
        expire(target, list);
        List<Layer> ordered = new ArrayList<>(list);
        ordered.sort(Comparator.comparingInt((Layer layer) -> layer.priority).reversed()
                .thenComparingLong(layer -> layer.order));
        double remaining = damage;
        for (Layer layer : ordered) {
            if (remaining <= 0) break;
            if (!matches(layer.kinds, kind) || !matches(layer.causes, cause)
                    || !matches(layer.classifications, classification)) continue;
            double limit = layer.perHitLimit == 0 ? Double.POSITIVE_INFINITY : layer.perHitLimit;
            double absorbed = Math.min(layer.remaining, Math.min(limit, remaining * layer.ratio));
            if (absorbed <= 0) continue;
            allocations.put(layer.id, absorbed);
            remaining -= absorbed;
        }
        return new Resolution(target, damage, allocations);
    }

    public static void commit(Resolution resolution) {
        if (resolution == null || resolution.target == null) return;
        List<Layer> list = LAYERS.get(resolution.target.getUniqueId());
        if (list == null) return;
        List<Layer> absorbedLayers = new ArrayList<>();
        List<Double> amounts = new ArrayList<>();
        List<Layer> depleted = new ArrayList<>();
        List<Double> depletionAmounts = new ArrayList<>();
        for (Iterator<Layer> it = list.iterator(); it.hasNext();) {
            Layer layer = it.next();
            Double planned = resolution.allocations.get(layer.id);
            if (planned == null) continue;
            double absorbed = Math.min(layer.remaining, planned);
            layer.remaining -= absorbed;
            absorbedLayers.add(layer);
            amounts.add(absorbed);
            if (layer.remaining <= 0.000001) {
                layer.remaining = 0;
                it.remove();
                depleted.add(layer);
                depletionAmounts.add(absorbed);
            }
        }
        if (list.isEmpty()) LAYERS.remove(resolution.target.getUniqueId());
        for (int i = 0; i < absorbedLayers.size(); i++)
            fire(ShieldEvent.Kind.ABSORBED, resolution.target, absorbedLayers.get(i), amounts.get(i));
        for (int i = 0; i < depleted.size(); i++)
            fire(ShieldEvent.Kind.DEPLETED, resolution.target, depleted.get(i), depletionAmounts.get(i));
    }

    public static double remaining(LivingEntity target, String key) {
        if (target == null) return 0;
        List<Layer> list = LAYERS.get(target.getUniqueId());
        if (list == null) return 0;
        expire(target, list);
        double total = 0;
        for (Layer layer : list) if (key == null || key.isEmpty() || layer.key.equals(key))
            total += layer.remaining;
        return total;
    }

    /** Adjusts live capacity of matching layers without changing their original maximum. */
    public static int adjust(LivingEntity target, String key, double delta) {
        if (target == null || !Double.isFinite(delta)) return 0;
        List<Layer> list = LAYERS.get(target.getUniqueId());
        if (list == null) return 0;
        expire(target, list);
        int changed = 0;
        List<Layer> depleted = new ArrayList<>();
        List<Double> depletionAmounts = new ArrayList<>();
        for (Iterator<Layer> it = list.iterator(); it.hasNext();) {
            Layer layer = it.next();
            if (key != null && !key.isEmpty() && !layer.key.equals(key)) continue;
            double before = layer.remaining;
            layer.remaining = Math.max(0, Math.min(layer.maximum, before + delta));
            if (layer.remaining != before) changed++;
            if (layer.remaining == 0) {
                it.remove();
                depleted.add(layer);
                depletionAmounts.add(before);
            }
        }
        if (list.isEmpty()) LAYERS.remove(target.getUniqueId());
        for (int i = 0; i < depleted.size(); i++)
            fire(ShieldEvent.Kind.DEPLETED, target, depleted.get(i), depletionAmounts.get(i));
        return changed;
    }

    public static int count(LivingEntity target, String key) {
        if (target == null) return 0;
        List<Layer> list = LAYERS.get(target.getUniqueId());
        if (list == null) return 0;
        expire(target, list);
        int count = 0;
        for (Layer layer : list) if (key == null || key.isEmpty() || layer.key.equals(key)) count++;
        return count;
    }

    public static int remove(LivingEntity target, String key) {
        if (target == null) return 0;
        List<Layer> list = LAYERS.get(target.getUniqueId());
        if (list == null) return 0;
        int removed = 0;
        List<Layer> removedLayers = new ArrayList<>();
        for (Iterator<Layer> it = list.iterator(); it.hasNext();) {
            Layer layer = it.next();
            if (key == null || key.isEmpty() || layer.key.equals(key)) {
                it.remove();
                removed++;
                removedLayers.add(layer);
            }
        }
        if (list.isEmpty()) LAYERS.remove(target.getUniqueId());
        for (Layer layer : removedLayers) fire(ShieldEvent.Kind.REMOVED, target, layer, layer.remaining);
        return removed;
    }

    private static void sweep() {
        // Snapshot keys because expiry callbacks may mutate the global layer map.
        for (UUID id : new ArrayList<>(LAYERS.keySet())) {
            List<Layer> layers = LAYERS.get(id);
            if (layers == null) continue;
            Entity entity = Bukkit.getEntity(id);
            if (!(entity instanceof LivingEntity) || entity.isDead() || !entity.isValid()) {
                LAYERS.remove(id);
                continue;
            }
            expire((LivingEntity) entity, layers);
            if (layers.isEmpty()) LAYERS.remove(id);
        }
    }

    private static void expire(LivingEntity target, List<Layer> list) {
        long now = System.currentTimeMillis();
        List<Layer> expired = new ArrayList<>();
        for (Iterator<Layer> it = list.iterator(); it.hasNext();) {
            Layer layer = it.next();
            if (layer.expiresAt <= now) {
                it.remove();
                expired.add(layer);
            }
        }
        for (Layer layer : expired) fire(ShieldEvent.Kind.EXPIRED, target, layer, layer.remaining);
    }

    private static boolean equalsSource(UUID sourceId, LivingEntity source) {
        return sourceId == null ? source == null : source != null && sourceId.equals(source.getUniqueId());
    }

    private static Set<String> normalized(Set<String> values) {
        if (values == null || values.isEmpty()) return Collections.emptySet();
        Set<String> result = new HashSet<>();
        for (String value : values) if (value != null && !value.trim().isEmpty())
            result.add(value.trim().toLowerCase());
        return Collections.unmodifiableSet(result);
    }

    private static boolean matches(Set<String> values, String actual) {
        return values.isEmpty() || values.contains(actual == null ? "" : actual.toLowerCase());
    }

    private static void fire(ShieldEvent.Kind kind, LivingEntity target, Layer layer, double amount) {
        Bukkit.getPluginManager().callEvent(new ShieldEvent(kind, target, layer.sourceId,
                layer.id, layer.key, amount, layer.remaining));
    }
}
