/**
 * SkillAPI
 * com.sucy.skill.nms.MinecraftVersion
 *
 * The MIT License (MIT)
 */
package com.sucy.skill.nms;

import org.bukkit.Bukkit;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Numeric view of the running server core version.
 *
 * <p>Version detection deliberately reads three independent sources because no
 * single one survived the whole 1.8 - 26.x range:</p>
 * <ul>
 *     <li>{@code Bukkit.getMinecraftVersion()} exists on modern Paper only.</li>
 *     <li>{@code Bukkit.getBukkitVersion()} exists everywhere but carries the
 *     {@code -R0.1-SNAPSHOT} suffix and, on relocated cores, an unrelated
 *     build tag.</li>
 *     <li>The {@code v1_12_R1} CraftBukkit package suffix only exists before
 *     the 1.20.5 package flattening.</li>
 * </ul>
 *
 * <p>Both the historic {@code 1.MINOR.PATCH} scheme and the newer
 * {@code MAJOR.MINOR} scheme are parsed. Ordering compares major first, so any
 * non-1 major sorts above every {@code 1.x} release without needing a hard
 * coded cut-off list.</p>
 */
public final class MinecraftVersion implements Comparable<MinecraftVersion> {
    /** Used when no source could be parsed; sorts below every real release. */
    public static final MinecraftVersion UNKNOWN = new MinecraftVersion(0, 0, 0, "unknown");

    private static final Pattern VERSION = Pattern.compile("(\\d+)\\.(\\d+)(?:\\.(\\d+))?");
    private static final Pattern LEGACY_PACKAGE = Pattern.compile("^v(\\d+)_(\\d+)_R(\\d+)$");

    private static volatile MinecraftVersion current;

    private final int major;
    private final int minor;
    private final int patch;
    private final String raw;

    private MinecraftVersion(int major, int minor, int patch, String raw) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        this.raw = raw;
    }

    /**
     * @return version of the running server, detected once per process
     */
    public static MinecraftVersion current() {
        MinecraftVersion value = current;
        if (value == null) {
            synchronized (MinecraftVersion.class) {
                value = current;
                if (value == null) {
                    value = detect();
                    current = value;
                }
            }
        }
        return value;
    }

    /**
     * Parses an arbitrary version string.
     *
     * @param value version text such as {@code 1.20.4} or {@code 26.2}
     * @return parsed version, or {@link #UNKNOWN} when no numbers were found
     */
    public static MinecraftVersion parse(String value) {
        if (value == null) {
            return UNKNOWN;
        }
        Matcher legacyPackage = LEGACY_PACKAGE.matcher(value.trim());
        if (legacyPackage.matches()) {
            return new MinecraftVersion(
                    Integer.parseInt(legacyPackage.group(1)),
                    Integer.parseInt(legacyPackage.group(2)),
                    0,
                    value);
        }
        Matcher matcher = VERSION.matcher(value);
        if (!matcher.find()) {
            return UNKNOWN;
        }
        return new MinecraftVersion(
                Integer.parseInt(matcher.group(1)),
                Integer.parseInt(matcher.group(2)),
                matcher.group(3) == null ? 0 : Integer.parseInt(matcher.group(3)),
                value);
    }

    /**
     * @param major major component
     * @param minor minor component
     * @return true when this version is the given release or newer
     */
    public boolean isAtLeast(int major, int minor) {
        return isAtLeast(major, minor, 0);
    }

    /**
     * @param major major component
     * @param minor minor component
     * @param patch patch component
     * @return true when this version is the given release or newer
     */
    public boolean isAtLeast(int major, int minor, int patch) {
        return compareTo(new MinecraftVersion(major, minor, patch, null)) >= 0;
    }

    /**
     * @param major major component
     * @param minor minor component
     * @return true when this version is strictly older than the given release
     */
    public boolean isBefore(int major, int minor) {
        return !isAtLeast(major, minor);
    }

    /**
     * @return true when the version could not be detected at all
     */
    public boolean isUnknown() {
        return major == 0;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getPatch() {
        return patch;
    }

    /**
     * @return the original text the version was parsed from
     */
    public String getRaw() {
        return raw;
    }

    @Override
    public int compareTo(MinecraftVersion other) {
        if (major != other.major) {
            return major < other.major ? -1 : 1;
        }
        if (minor != other.minor) {
            return minor < other.minor ? -1 : 1;
        }
        return patch == other.patch ? 0 : (patch < other.patch ? -1 : 1);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MinecraftVersion)) {
            return false;
        }
        return compareTo((MinecraftVersion) other) == 0;
    }

    @Override
    public int hashCode() {
        return (major * 31 + minor) * 31 + patch;
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + patch;
    }

    private static MinecraftVersion detect() {
        MinecraftVersion value = fromPaperAccessor();
        if (!value.isUnknown()) {
            return value;
        }
        value = fromBukkitVersion();
        if (!value.isUnknown()) {
            return value;
        }
        return fromCraftBukkitPackage();
    }

    private static MinecraftVersion fromPaperAccessor() {
        try {
            Method accessor = Bukkit.class.getMethod("getMinecraftVersion");
            Object value = accessor.invoke(null);
            return value == null ? UNKNOWN : parse(String.valueOf(value));
        } catch (Throwable ignored) {
            // Spigot and older Paper builds do not expose this accessor.
            return UNKNOWN;
        }
    }

    private static MinecraftVersion fromBukkitVersion() {
        try {
            return parse(Bukkit.getBukkitVersion());
        } catch (Throwable ignored) {
            // Bukkit is not initialized (unit tests, shaded tooling).
            return UNKNOWN;
        }
    }

    private static MinecraftVersion fromCraftBukkitPackage() {
        try {
            String name = Bukkit.getServer().getClass().getPackage().getName();
            return parse(name.substring(name.lastIndexOf('.') + 1));
        } catch (Throwable ignored) {
            // Flattened cores no longer carry a versioned package suffix.
            return UNKNOWN;
        }
    }
}
