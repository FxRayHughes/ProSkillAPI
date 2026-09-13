package com.sucy.skill.api.particle;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * SkillAPI © 2018
 * com.sucy.skill.api.particle.SpigotParticles
 */
public class SpigotParticles {
    private static boolean error = true;

    public static void play(final Location loc, final String particle, final float dx, final float dy, final float dz, final int count, final float speed, final double distance, final Material material, final int data) {
        Particle effect = findParticle(particle);
        if (effect == null) return;
        try {
            ArrayList<Player> players = new ArrayList<>();
            double range = distance * distance;
            for (Player player : loc.getWorld().getPlayers()) {
                if (player.getLocation().distanceSquared(loc) < range) {
                    players.add(player);
                }
            }

            com.sucy.skill.api.particle.Particle.play(
                    players, effect, loc.getX(), loc.getY(), loc.getZ(),
                    count, dx, dy, dz, speed, material, data);
        } catch (final Exception ex) {
            if (error) {
                ex.printStackTrace();
                error = false;
            }
        }
    }

    private static final Map<String, Particle> CONVERSION = getter();

    /**
     * Finds a Bukkit particle by current API name, legacy Bukkit name, or the
     * editor-facing SkillAPI name. Paper 26.2 renamed many constants, while old
     * configs still store names like "block crack" and "red dust".
     */
    public static Particle findParticle(String particle) {
        String normalized = particle.toUpperCase().replace(' ', '_');
        // Legacy Bukkit names describe the effect, while modern APIs renamed
        // or split several of them. Prefer the semantic modern target before
        // resolving the raw name (for example SPELL would otherwise select a
        // modern payload-bearing particle and fail without Particle.Spell).
        String legacyTarget = legacyTarget(normalized);
        Particle effect = resolve(legacyTarget == null ? normalized : legacyTarget);
        if (effect != null) {
            return effect;
        }
        return CONVERSION.get(particle.toLowerCase().replace('_', ' '));
    }

    private static String legacyTarget(String name) {
        switch (name) {
            case "SPELL": return "EFFECT";
            case "SPELL_INSTANT": return "INSTANT_EFFECT";
            case "SPELL_MOB":
            case "SPELL_MOB_AMBIENT": return "ENTITY_EFFECT";
            case "SPELL_WITCH": return "WITCH";
            case "REDSTONE": return "DUST";
            case "ITEM_CRACK": return "ITEM";
            case "BLOCK_CRACK":
            case "BLOCK_DUST": return "BLOCK";
            default: return null;
        }
    }

    public static Map<String, Particle> getter() {
        HashMap<String, Particle> map = new HashMap<>();
        put(map, "angry villager", "ANGRY_VILLAGER", "VILLAGER_ANGRY");
        put(map, "block crack", "BLOCK", "BLOCK_CRACK");
        put(map, "block dust", "BLOCK", "BLOCK_DUST");
        put(map, "bubble", "BUBBLE", "WATER_BUBBLE");
        put(map, "cloud", "CLOUD");
        put(map, "crit", "CRIT");
        put(map, "damage indicator", "DAMAGE_INDICATOR");
        put(map, "death", "UNDERWATER", "SUSPENDED");
        put(map, "death suspend", "UNDERWATER", "SUSPENDED_DEPTH");
        put(map, "depth suspend", "UNDERWATER", "SUSPENDED_DEPTH");
        put(map, "dragon breath", "DRAGON_BREATH");
        put(map, "drip lava", "DRIPPING_LAVA", "DRIP_LAVA");
        put(map, "drip water", "DRIPPING_WATER", "DRIP_WATER");
        put(map, "enchantment table", "ENCHANT", "ENCHANTMENT_TABLE");
        put(map, "end rod", "END_ROD");
        put(map, "ender signal", "PORTAL");
        put(map, "explode", "EXPLOSION", "EXPLOSION_NORMAL");
        put(map, "firework spark", "FIREWORK", "FIREWORKS_SPARK");
        put(map, "flame", "FLAME");
        put(map, "footstep", "CLOUD");
        put(map, "happy villager", "HAPPY_VILLAGER", "VILLAGER_HAPPY");
        put(map, "heart", "HEART");
        put(map, "huge explosion", "EXPLOSION_EMITTER", "EXPLOSION_HUGE");
        put(map, "hurt", "DAMAGE_INDICATOR");
        put(map, "icon crack", "ITEM", "ITEM_CRACK");
        put(map, "instant spell", "INSTANT_EFFECT", "SPELL_INSTANT");
        put(map, "large explode", "EXPLOSION_EMITTER", "EXPLOSION_LARGE");
        put(map, "large smoke", "LARGE_SMOKE", "SMOKE_LARGE");
        put(map, "lava", "LAVA");
        put(map, "magic crit", "ENCHANTED_HIT", "CRIT_MAGIC");
        put(map, "mob appearance", "ELDER_GUARDIAN", "MOB_APPEARANCE");
        put(map, "mob spell", "ENTITY_EFFECT", "SPELL_MOB");
        put(map, "mob spell ambient", "ENTITY_EFFECT", "SPELL_MOB_AMBIENT");
        put(map, "mobspawner flames", "FLAME");
        put(map, "note", "NOTE");
        put(map, "portal", "PORTAL");
        put(map, "potion break", "EFFECT", "SPELL");
        put(map, "red dust", "DUST", "REDSTONE");
        put(map, "sheep eat", "ELDER_GUARDIAN", "MOB_APPEARANCE");
        put(map, "slime", "ITEM_SLIME", "SLIME");
        put(map, "smoke", "SMOKE", "SMOKE_NORMAL");
        put(map, "snowball poof", "ITEM_SNOWBALL", "SNOWBALL");
        put(map, "snow shovel", "POOF", "SNOW_SHOVEL");
        put(map, "spell", "EFFECT", "SPELL");
        put(map, "splash", "SPLASH", "WATER_SPLASH");
        put(map, "sweep attack", "SWEEP_ATTACK");
        put(map, "suspend", "UNDERWATER", "SUSPENDED");
        put(map, "town aura", "MYCELIUM", "TOWN_AURA");
        put(map, "water drop", "RAIN", "WATER_DROP");
        put(map, "water wake", "FISHING", "WATER_WAKE");
        put(map, "witch magic", "WITCH", "SPELL_WITCH");
        put(map, "wolf hearts", "HEART");

        for (Particle value : Particle.values()) {
            map.put(value.name(), value);
            map.put(value.name().toLowerCase(), value);
            map.put(value.name().toLowerCase().replace("_", " "), value);
        }
        return map;
    }

    /**
     * Adds the first particle name supported by the running Bukkit/Paper API.
     * The ordered list keeps modern Paper names first and falls back to older
     * Bukkit names on legacy servers.
     */
    private static void put(Map<String, Particle> map, String key, String... names) {
        Particle particle = resolve(names);
        if (particle != null) {
            map.put(key, particle);
        }
    }

    private static Particle resolve(String... names) {
        for (String name : names) {
            try {
                // Prefer the current Bukkit enum when the target name is an
                // exact modern constant. XSeries aliases EFFECT to SPELL on
                // some versions, which would reintroduce the typed-payload
                // error for legacy SPELL configurations.
                return Particle.valueOf(name);
            } catch (IllegalArgumentException ignored) {
                Particle resolved = com.cryptomorin.xseries.particles.XParticle.of(name)
                        .map(com.cryptomorin.xseries.particles.XParticle::get).orElse(null);
                if (resolved != null) return resolved;
            }
        }
        return null;
    }
}
