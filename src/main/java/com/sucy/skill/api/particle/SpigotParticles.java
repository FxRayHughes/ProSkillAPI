package com.sucy.skill.api.particle;

import com.google.common.collect.ImmutableList;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;

import java.util.HashMap;
import java.util.Map;

/**
 * SkillAPI © 2018
 * com.sucy.skill.api.particle.SpigotParticles
 */
public class SpigotParticles {
    private static boolean error = true;

    public static void play(final Location loc, final String particle, final float dx, final float dy, final float dz, final int count, final float speed, final double distance, final Material material, final int data) {
        Particle effect;
        try {
            effect = Particle.valueOf(particle.toUpperCase().replace(' ', '_'));
        } catch (IllegalArgumentException e) {
            effect = CONVERSION.get(particle.toLowerCase().replace('_', ' '));
        }
        if (effect == null) return;
        try {
            final Object packet = com.sucy.skill.api.particle.Particle.make(
                    effect.name(), loc.getX(), loc.getY(), loc.getZ(), dx, dy, dz, speed, count, material, 0);
            com.sucy.skill.api.particle.Particle.send(loc, ImmutableList.of(packet), distance);
        } catch (final Exception ex) {
            if (error) {
                ex.printStackTrace();
                error = false;
            }
        }
    }

    private static final Map<String, Particle> CONVERSION = getter();

    public static Map<String, Particle> getter() {
        HashMap<String, Particle> map = new HashMap<>();
        map.put("angry villager", Particle.VILLAGER_ANGRY);
        map.put("block crack", Particle.BLOCK_CRACK);
        map.put("bubble", Particle.WATER_BUBBLE);
        map.put("cloud", Particle.CLOUD);
        map.put("crit", Particle.CRIT);
        map.put("damage indicator", Particle.DAMAGE_INDICATOR);
        map.put("death", Particle.SUSPENDED);
        map.put("death suspend", Particle.SUSPENDED_DEPTH);
        map.put("dragon breath", Particle.DRAGON_BREATH);
        map.put("drip lava", Particle.DRIP_LAVA);
        map.put("drip water", Particle.DRIP_WATER);
        map.put("enchantment table", Particle.ENCHANTMENT_TABLE);
        map.put("end rod", Particle.END_ROD);
        map.put("ender signal", Particle.PORTAL);
        map.put("explode", Particle.EXPLOSION_NORMAL);
        map.put("firework spark", Particle.FIREWORKS_SPARK);
        map.put("flame", Particle.FLAME);
        map.put("footstep", Particle.CLOUD);
        map.put("happy villager", Particle.VILLAGER_HAPPY);
        map.put("heart", Particle.HEART);
        map.put("huge explosion", Particle.EXPLOSION_HUGE);
        map.put("hurt", Particle.DAMAGE_INDICATOR);
        map.put("icon crack", Particle.ITEM_CRACK);
        map.put("instant spell", Particle.SPELL_INSTANT);
        map.put("large explode", Particle.EXPLOSION_LARGE);
        map.put("large smoke", Particle.SMOKE_LARGE);
        map.put("lava", Particle.LAVA);
        map.put("magic crit", Particle.CRIT_MAGIC);
        map.put("mob spell", Particle.SPELL_MOB);
        map.put("mob spell ambient", Particle.SPELL_MOB_AMBIENT);
        map.put("mobspawner flames", Particle.FLAME);
        map.put("note", Particle.NOTE);
        map.put("portal", Particle.PORTAL);
        map.put("potion break", Particle.SPELL);
        map.put("red dust", Particle.REDSTONE);
        map.put("sheep eat", Particle.MOB_APPEARANCE);
        map.put("slime", Particle.SLIME);
        map.put("smoke", Particle.SMOKE_NORMAL);
        map.put("snowball poof", Particle.SNOWBALL);
        map.put("snow shovel", Particle.SNOW_SHOVEL);
        map.put("spell", Particle.SPELL);
        map.put("splash", Particle.WATER_SPLASH);
        map.put("sweep attack", Particle.SWEEP_ATTACK);
        map.put("suspend", Particle.SUSPENDED);
        map.put("town aura", Particle.TOWN_AURA);
        map.put("water drop", Particle.WATER_DROP);
        map.put("water wake", Particle.WATER_WAKE);
        map.put("witch magic", Particle.SPELL_WITCH);
        map.put("wolf hearts", Particle.HEART);

        for (Particle value : Particle.values()) {
            map.put(value.name(), value);
            map.put(value.name().toLowerCase(), value);
            map.put(value.name().toLowerCase().replace("_", " "), value);
        }
        return map;
    }
}
