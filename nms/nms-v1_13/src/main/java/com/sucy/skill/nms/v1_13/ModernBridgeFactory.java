package com.sucy.skill.nms.v1_13;

import com.sucy.skill.nms.MinecraftVersion;
import com.sucy.skill.nms.NmsBridgeFactory;

/**
 * Shared support check for the flattened generations.
 *
 * <p>Each modern module owns a half-open version window: it serves everything
 * from its own release up to, but excluding, the next module's release. The
 * windows are declared per module rather than centrally so adding a generation
 * means adding a module, and the module immediately below it only has to have
 * its upper bound adjusted.</p>
 */
public abstract class ModernBridgeFactory implements NmsBridgeFactory {
    private final int major;
    private final int minor;
    private final int nextMajor;
    private final int nextMinor;

    /**
     * @param major first major version served
     * @param minor first minor version served
     * @param nextMajor major version of the next generation, exclusive
     * @param nextMinor minor version of the next generation, exclusive
     */
    protected ModernBridgeFactory(int major, int minor, int nextMajor, int nextMinor) {
        this.major = major;
        this.minor = minor;
        this.nextMajor = nextMajor;
        this.nextMinor = nextMinor;
    }

    @Override
    public String id() {
        return major == 1 ? "v1_" + minor : "v" + major;
    }

    @Override
    public boolean supports(MinecraftVersion version) {
        if (version.isUnknown()) {
            return false;
        }
        return version.isAtLeast(major, minor) && !version.isAtLeast(nextMajor, nextMinor);
    }
}
