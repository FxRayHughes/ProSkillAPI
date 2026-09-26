package com.sucy.skill.api.particle;

import com.rit.sucy.config.parse.DataSection;
import com.sucy.skill.nms.MinecraftVersion;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves the portable particle AST stored in skill YAML to a string understood by the
 * running server. Missing version variants deliberately use CLOUD, a white particle present
 * throughout the supported version range, rather than failing to render the whole skill.
 */
public final class ParticleAstResolver {
    public static final String WHITE_FALLBACK = "CLOUD";
    private static final Pattern VERSION = Pattern.compile("^(\\d+)\\.(\\d+)");

    private ParticleAstResolver() { }

    /** Only these keys use the particle AST protocol; other nested settings stay untouched. */
    public static boolean isParticleKey(String key) {
        return "particle".equals(key) || key.endsWith("-particle-type");
    }

    /**
     * Old scalar values remain readable. A valid AST must contain a version-specific variant;
     * absent, malformed or unsupported variants all resolve to the white fallback.
     */
    public static String resolve(Object value) {
        String selected = resolve(value, MinecraftVersion.current().getRaw());
        // A hand-edited mapping may name a particle absent from the actual Bukkit API.
        return !(value instanceof String) && ParticleLookup.find(selected) == null
                ? WHITE_FALLBACK : selected;
    }

    /** Resolve an explicit version without consulting the running server's particle API. */
    public static String resolve(Object value, String bukkitVersion) {
        if (value instanceof String) {
            String legacy = ((String) value).trim();
            return legacy.isEmpty() ? WHITE_FALLBACK : legacy;
        }
        if (!"particle".equals(child(value, "kind"))) return WHITE_FALLBACK;
        if (bukkitVersion == null) return WHITE_FALLBACK;
        Matcher version = VERSION.matcher(bukkitVersion);
        if (!version.find()) return WHITE_FALLBACK;
        String key = "v" + version.group(1) + "_" + version.group(2);
        Object selected = child(child(value, "versions"), key);
        return selected instanceof String && !((String) selected).trim().isEmpty()
                ? ((String) selected).trim() : WHITE_FALLBACK;
    }

    private static Object child(Object section, String key) {
        if (section instanceof DataSection) return ((DataSection) section).get(key);
        if (section instanceof Map<?, ?>) return ((Map<?, ?>) section).get(key);
        return null;
    }
}
