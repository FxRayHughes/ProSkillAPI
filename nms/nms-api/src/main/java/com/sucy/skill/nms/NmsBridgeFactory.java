/**
 * SkillAPI
 * com.sucy.skill.nms.NmsBridgeFactory
 *
 * The MIT License (MIT)
 */
package com.sucy.skill.nms;

/**
 * Declares one bridge implementation and the cores it can serve.
 *
 * <p>Factories are looked up by class name so a distribution that does not ship
 * a given module simply skips it. Each factory answers for itself instead of a
 * central version table, which keeps the version knowledge next to the code it
 * protects.</p>
 */
public interface NmsBridgeFactory {
    /**
     * @return stable identifier used by logs and the manual override property
     */
    String id();

    /**
     * Decides whether this implementation can serve the running core. Besides
     * the version range, implementations should probe for the classes and
     * methods they actually use so a relocated or stripped core falls through
     * to the next candidate instead of failing at first use.
     *
     * @param version detected core version
     * @return true when this implementation should be used
     */
    boolean supports(MinecraftVersion version);

    /**
     * @return a new bridge instance; only called after {@link #supports} passed
     */
    NmsBridge create();
}
