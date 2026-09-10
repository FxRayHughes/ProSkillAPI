package com.sucy.skill.dynamic;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.dynamic.condition.*;
import com.sucy.skill.dynamic.custom.CustomComponent;
import com.sucy.skill.dynamic.custom.CustomEffectComponent;
import com.sucy.skill.dynamic.custom.EditorOption;
import com.sucy.skill.dynamic.mechanic.*;
import com.sucy.skill.dynamic.target.*;
import com.sucy.skill.dynamic.trigger.*;
import com.sucy.skill.serialization.gson.GsonUtils;
import org.bukkit.event.Event;
import org.bukkit.plugin.EventExecutor;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SkillAPI © 2018
 * com.sucy.skill.dynamic.ComponentRegistry
 */
public class ComponentRegistry {

    static final Map<ComponentType, Map<String, Class<?>>> COMPONENTS = new EnumMap<>(ComponentType.class);

    static final Map<String, Trigger<?>> TRIGGERS = new HashMap<>();
    public static final Map<Trigger<?>, EventExecutor> EXECUTORS = new HashMap<>();

    public static Trigger<?> getTrigger(final String key) {
        return TRIGGERS.get(key.toUpperCase().replace(' ', '_'));
    }

    static EffectComponent getComponent(final ComponentType type, final String key) {
        final Class<?> componentClass = COMPONENTS.get(type).get(key.toLowerCase());
        if (componentClass == null) {
            throw new IllegalArgumentException("Invalid component key - " + key);
        }
        try {
            return (EffectComponent) componentClass.newInstance();
        } catch (final Exception ex) {
            throw new IllegalArgumentException("Invalid component - does not have a default constructor");
        }
    }

    static EventExecutor getExecutor(final Trigger<?> trigger) {
        return EXECUTORS.get(trigger);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Event> void register(final Trigger<T> trigger) {
        if (getTrigger(trigger.getKey()) != null) {
            throw new IllegalArgumentException("Trigger with key " + trigger.getKey() + " already exists");
        } else if (trigger.getKey().contains("-")) {
            throw new IllegalArgumentException(trigger.getKey() + " is not a valid key: must not contain dashes");
        }
        TRIGGERS.put(trigger.getKey(), trigger);
        EXECUTORS.put(trigger, (listener, event) -> {
            if (!trigger.getEvent().isInstance(event)) return;
            ((TriggerHandler) listener).apply((T) event, trigger);
        });
    }

    public static void register(final CustomEffectComponent component) {
        register((EffectComponent) component);
    }

    public static void save() {
        final File file = new File(SkillAPI.getPlugin(SkillAPI.class).getDataFolder(), "tool-config.json");
        try {
            List<Map<String, Object>> values = new ArrayList<Map<String, Object>>();
            TRIGGERS.values().forEach(trigger -> appendMap(trigger, values));
            COMPONENTS.forEach((type, map) ->
                    map.keySet().forEach(key -> appendMap(getComponent(type, key), values)));
            // GsonUtils is the single JSON writer for generated editor data,
            // avoiding hand-built JSON that breaks on quotes or backslashes.
            GsonUtils.writeJson(file, values);
        } catch (Exception var4) {
            var4.printStackTrace();
        }
    }

    private static void appendMap(final Object obj, final List<Map<String, Object>> values) {
        if (obj instanceof CustomComponent) {
            values.add(toMap((CustomComponent) obj));
        }
    }

    public static void append(final Object obj, final StringBuilder builder) {
        if (!(obj instanceof CustomComponent)) {
            return;
        }

        final CustomComponent component = (CustomComponent) obj;
        // Keep the public helper for binary/source compatibility, but route
        // its output through GsonUtils so special characters remain valid JSON.
        builder.append(GsonUtils.toJson(toMap(component))).append(',');
    }

    private static Map<String, Object> toMap(final CustomComponent component) {
        Map<String, Object> value = new LinkedHashMap<String, Object>();
        value.put("type", component.getType().name());
        value.put("key", component.getKey());
        value.put("display", component.getDisplayName());
        value.put("container", component.isContainer());
        value.put("description", component.getDescription());

        List<Map<String, Object>> options = new ArrayList<Map<String, Object>>();
        for (EditorOption option : component.getOptions()) {
            Map<String, Object> optionValue = new LinkedHashMap<String, Object>();
            optionValue.put("type", option.type);
            optionValue.put("key", option.key);
            optionValue.put("display", option.name);
            optionValue.put("description", option.description);
            option.extra.forEach((key, rawValue) -> {
                optionValue.put(key, parseExtraValue(rawValue));
            });
            options.add(optionValue);
        }
        value.put("options", options);
        return value;
    }

    private static Object parseExtraValue(String rawValue) {
        try {
            Object parsed = GsonUtils.fromJson(rawValue, Object.class);
            return parsed == null ? rawValue : parsed;
        } catch (RuntimeException ignored) {
            // A third-party editor option may contain plain text rather than a
            // JSON literal; retain it instead of making tool-config generation fail.
            return rawValue;
        }
    }

    public static void register(final EffectComponent component) {
        COMPONENTS.computeIfAbsent(component.getType(), t -> new HashMap<>())
                .put(component.getKey().toLowerCase(), component.getClass());
    }

    /**
     * Registers a component supplied by a folded optional module. The method is
     * separate from core bootstrap registration so integration modules can be
     * installed conditionally after Bukkit confirms their provider plugin exists.
     */
    public static void registerModuleComponent(final EffectComponent component) {
        register(component);
    }

    static {

        // Triggers
        register(new BlockBreakTrigger());
        register(new BlockPlaceTrigger());
        register(new CrouchTrigger());
        register(new DeathTrigger());
        register(new EnvironmentalTrigger());
        register(new KillTrigger());
        register(new LandTrigger());
        register(new LaunchTrigger());
        register(new MoveTrigger());
        register(new PhysicalDealtTrigger());
        register(new PhysicalTakenTrigger());
        register(new SkillDealtTrigger());
        register(new SkillTakenTrigger());
        register(new PlayerInteractTrigger());
        register(new SwapHandItemsTrigger());

        // Targets
        register(new AreaTarget());
        register(new ConeTarget());
        register(new LinearTarget());
        register(new LocationTarget());
        register(new NearestTarget());
        register(new OffsetTarget());
        register(new RememberTarget());
        register(new SelfTarget());
        register(new SingleTarget());

        // Conditions
        register(new ArmorCondition());
        register(new AttributeCondition());
        register(new BiomeCondition());
        register(new BlockCondition());
        register(new CastLevelCondition());
        register(new CeilingCondition());
        register(new ChanceCondition());
        register(new ClassCondition());
        register(new ClassLevelCondition());
        register(new CombatCondition());
        register(new CrouchCondition());
        register(new DirectionCondition());
        register(new ElevationCondition());
        register(new ElseCondition());
        register(new EntityTypeCondition());
        register(new FireCondition());
        register(new FlagCondition());
        register(new FoodCondition());
        register(new GroundCondition());
        register(new HealthCondition());
        register(new InventoryCondition());
        register(new ItemCondition());
        register(new LightCondition());
        register(new LoreCondition());
        register(new ManaCondition());
        register(new MountedCondition());
        register(new MountingCondition());
        register(new NameCondition());
        register(new OffhandCondition());
        register(new PermissionCondition());
        register(new PotionCondition());
        register(new SkillLevelCondition());
        register(new SlotCondition());
        register(new StatusCondition());
        register(new TimeCondition());
        register(new ToolCondition());
        register(new ValueCondition());
        register(new WaterCondition());
        register(new WeatherCondition());
        register(new DataCondition());

        // Mechanics
        register(new DataSetMechanic());
        register(new DataEditMechanic());
        register(new ArmorMechanic());
        register(new ArmorStandMechanic());
        register(new ArmorStandPoseMechanic());
        register(new AttributeMechanic());
        register(new BlockMechanic());
        register(new BuffMechanic());
        register(new CancelEffectMechanic());
        register(new CancelMechanic());
        register(new ChannelMechanic());
        register(new CleanseMechanic());
        register(new CommandMechanic());
        register(new CooldownMechanic());
        register(new DamageMechanic());
        register(new DamageBuffMechanic());
        register(new DamageLoreMechanic());
        register(new DefenseBuffMechanic());
        register(new DelayMechanic());
        register(new DisguiseMechanic());
        register(new DurabilityMechanic());
        register(new ExplosionMechanic());
        register(new FireMechanic());
        register(new FlagMechanic());
        register(new FlagClearMechanic());
        register(new FlagToggleMechanic());
        register(new FoodMechanic());
        register(new ForgetTargetsMechanic());
        register(new HealMechanic());
        register(new HealthSetMechanic());
        register(new HeldItemMechanic());
        register(new ImmunityMechanic());
        register(new InterruptMechanic());
        register(new ItemMechanic());
        register(new ItemProjectileMechanic());
        register(new KetherMechanic());
        register(new ItemRemoveMechanic());
        register(new JavaScriptMechanic());
        register(new LaunchMechanic());
        register(new LightningMechanic());
        register(new ManaMechanic());
        register(new MessageMechanic());
        register(new ParticleMechanic());
        register(new ParticleAnimationArmorStandMechanic());
        register(new ParticleAnimationMechanic());
        register(new ParticleEffectMechanic());
        register(new ParticleProjectileMechanic());
        register(new PassiveMechanic());
        register(new PermissionMechanic());
        register(new PotionMechanic());
        register(new PotionProjectileMechanic());
        register(new ProjectileMechanic());
        register(new PurgeMechanic());
        register(new PushMechanic());
        register(new RememberTargetsMechanic());
        register(new RepeatMechanic());
        register(new SpeedMechanic());
        register(new SoundMechanic());
        register(new StatusMechanic());
        register(new TauntMechanic());
        register(new TriggerMechanic());
        register(new ValueAddMechanic());
        register(new ValueAttributeMechanic());
        register(new ValueCopyMechanic());
        register(new ValueDistanceMechanic());
        register(new ValueHealthMechanic());
        register(new ValueLocationMechanic());
        register(new ValueLoreMechanic());
        register(new ValueLoreSlotMechanic());
        register(new ValueManaMechanic());
        register(new ValueMultiplyMechanic());
        register(new ValuePlaceholderMechanic());
        register(new ValueRandomMechanic());
        register(new ValueSetMechanic());
        register(new ValueScriptMechanic());
        register(new WarpMechanic());
        register(new WarpLocMechanic());
        register(new WarpRandomMechanic());
        register(new WarpSwapMechanic());
        register(new WarpTargetMechanic());
        register(new WarpValueMechanic());
        register(new WolfMechanic());
        register(new MythicCastMechanic());
        register(new MythicCastTargetMechanic());
        register(new SnowStormMechanic());
    }
}
