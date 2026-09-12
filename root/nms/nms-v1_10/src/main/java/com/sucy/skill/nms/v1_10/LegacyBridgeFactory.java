package com.sucy.skill.nms.v1_10;

import com.sucy.skill.nms.MinecraftVersion;
import com.sucy.skill.nms.NmsBridgeFactory;

import org.bukkit.Bukkit;

/**
 * Shared support check for every pre-flattening generation.
 *
 * <p>Each generation declares its own version window, but they all need the
 * same precondition: the versioned {@code net.minecraft.server} package must
 * actually be loadable. Relocated or heavily patched forks keep the version
 * number while removing that package, and on those the reflective bridges would
 * fail on first use rather than at selection time.</p>
 */
public abstract class LegacyBridgeFactory implements NmsBridgeFactory {
    private final int minor;

    /**
     * @param minor the 1.x minor version this factory serves
     */
    protected LegacyBridgeFactory(int minor) {
        this.minor = minor;
    }

    @Override
    public String id() {
        return "v1_" + minor;
    }

    @Override
    public boolean supports(MinecraftVersion version) {
        if (version.isUnknown() || version.getMajor() != 1 || version.getMinor() != minor) {
            return false;
        }
        return hasVersionedNmsPackage();
    }

    private static boolean hasVersionedNmsPackage() {
        try {
            String name = Bukkit.getServer().getClass().getPackage().getName();
            String suffix = name.substring(name.lastIndexOf('.') + 1);
            if (!suffix.startsWith("v")) {
                return false;
            }
            Class.forName("net.minecraft.server." + suffix + ".EntityPlayer");
            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
