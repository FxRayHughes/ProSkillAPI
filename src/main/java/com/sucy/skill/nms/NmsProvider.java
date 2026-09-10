/**
 * SkillAPI
 * com.sucy.skill.nms.NmsProvider
 *
 * The MIT License (MIT)
 */
package com.sucy.skill.nms;

import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

/**
 * Selects the runtime NMS bridge once per server process.
 *
 * <p>Selection is a registry rather than a single version comparison. Each
 * candidate module answers {@link NmsBridgeFactory#supports} for itself, so the
 * decision combines the detected core version with probes for the classes and
 * methods that candidate actually needs. Candidates are ordered from most
 * specific to most conservative, and the last entry only uses public Bukkit API,
 * which means an unrecognised core still gets a working bridge instead of a
 * hard failure.</p>
 *
 * <p>Set {@code -Dskillapi.nms=<id>} to pin one implementation when a fork needs
 * a different choice than the probes make.</p>
 */
public final class NmsProvider {
    /**
     * Candidate factories, newest generation first.
     *
     * <p>One entry per NMS generation. The windows are disjoint, so the order
     * only matters for the two open-ended entries: {@code v26} claims every
     * major version from 2 upward and is also the fallback for a core whose
     * version could not be detected, which is why it is listed first.</p>
     *
     * <p>Class names are resolved reflectively so a build that omits a module
     * simply skips it instead of failing to load.</p>
     */
    private static final String[] FACTORIES = {
            "com.sucy.skill.nms.v26.V26BridgeFactory",
            "com.sucy.skill.nms.v1_21.V1_21BridgeFactory",
            "com.sucy.skill.nms.v1_20.V1_20BridgeFactory",
            "com.sucy.skill.nms.v1_17.V1_17BridgeFactory",
            "com.sucy.skill.nms.v1_16.V1_16BridgeFactory",
            "com.sucy.skill.nms.v1_13.V1_13BridgeFactory",
            "com.sucy.skill.nms.v1_12.V1_12BridgeFactory",
            "com.sucy.skill.nms.v1_11.V1_11BridgeFactory",
            "com.sucy.skill.nms.v1_10.V1_10BridgeFactory",
            "com.sucy.skill.nms.v1_9.V1_9BridgeFactory",
            "com.sucy.skill.nms.v1_8.V1_8BridgeFactory"
    };

    private static final String OVERRIDE_PROPERTY = "skillapi.nms";

    /** Generation used when no window claims the running core. */
    private static final String FALLBACK_ID = "v1_13";

    private static final NmsBridge BRIDGE = createBridge();

    private NmsProvider() {
    }

    /**
     * @return bridge matching the running Minecraft core
     */
    public static NmsBridge bridge() {
        return BRIDGE;
    }

    /**
     * @return version the bridge selection was based on
     */
    public static MinecraftVersion version() {
        return MinecraftVersion.current();
    }

    private static NmsBridge createBridge() {
        MinecraftVersion version = MinecraftVersion.current();
        String override = System.getProperty(OVERRIDE_PROPERTY);
        List<NmsBridgeFactory> factories = loadFactories();

        if (override != null && !override.trim().isEmpty()) {
            for (NmsBridgeFactory factory : factories) {
                if (factory.id().equalsIgnoreCase(override.trim())) {
                    NmsBridge bridge = instantiate(factory);
                    if (bridge != null) {
                        log("Using NMS bridge \"" + bridge.id() + "\" (forced by -D"
                                + OVERRIDE_PROPERTY + ")");
                        return bridge;
                    }
                }
            }
            warn("Unknown or unusable value for -D" + OVERRIDE_PROPERTY + ": " + override
                    + " - falling back to automatic detection");
        }

        for (NmsBridgeFactory factory : factories) {
            boolean supported;
            try {
                supported = factory.supports(version);
            } catch (Throwable ex) {
                warn("NMS candidate " + factory.id() + " failed its capability probe: " + ex);
                continue;
            }
            if (!supported) {
                continue;
            }
            NmsBridge bridge = instantiate(factory);
            if (bridge != null) {
                log("Detected server core " + version + " - using NMS bridge \"" + bridge.id() + "\"");
                return bridge;
            }
        }

        // No window claimed this core. The realistic cause is a relocated or
        // heavily patched pre-1.13 fork whose versioned NMS package is gone, so
        // its own generation refused itself. Degrade to the API-only bridge:
        // most of it works on any 1.11+ core, and losing packet combos is far
        // better than refusing to enable the plugin.
        NmsBridge fallback = fallbackBridge(factories);
        if (fallback != null) {
            warn("No NMS bridge matches server core " + version
                    + " - falling back to \"" + fallback.id() + "\","
                    + " which uses public API only. Click combos and exact"
                    + " projectile collision may be unavailable.");
            return fallback;
        }

        // Reaching this point means every module was stripped from the jar.
        throw new IllegalStateException(
                "No SkillAPI NMS bridge is available for server core " + version);
    }

    /**
     * @param factories loaded candidates
     * @return the most conservative available bridge, or null when none loaded
     */
    private static NmsBridge fallbackBridge(List<NmsBridgeFactory> factories) {
        for (NmsBridgeFactory factory : factories) {
            if (!FALLBACK_ID.equals(factory.id())) {
                continue;
            }
            return instantiate(factory);
        }
        return factories.isEmpty() ? null : instantiate(factories.get(0));
    }

    private static List<NmsBridgeFactory> loadFactories() {
        List<NmsBridgeFactory> factories = new ArrayList<NmsBridgeFactory>(FACTORIES.length);
        for (String className : FACTORIES) {
            try {
                Object instance = Class.forName(className).newInstance();
                if (instance instanceof NmsBridgeFactory) {
                    factories.add((NmsBridgeFactory) instance);
                }
            } catch (ClassNotFoundException ignored) {
                // Module is not part of this distribution.
            } catch (Throwable ex) {
                warn("Failed to load NMS candidate " + className + ": " + ex);
            }
        }
        return factories;
    }

    private static NmsBridge instantiate(NmsBridgeFactory factory) {
        try {
            return factory.create();
        } catch (Throwable ex) {
            warn("NMS candidate " + factory.id() + " could not be created: " + ex);
            return null;
        }
    }

    private static void log(String message) {
        try {
            Bukkit.getLogger().info("[SkillAPI] " + message);
        } catch (Throwable ignored) {
            // Bukkit is unavailable outside a running server.
        }
    }

    private static void warn(String message) {
        try {
            Bukkit.getLogger().warning("[SkillAPI] " + message);
        } catch (Throwable ignored) {
            // Bukkit is unavailable outside a running server.
        }
    }
}
