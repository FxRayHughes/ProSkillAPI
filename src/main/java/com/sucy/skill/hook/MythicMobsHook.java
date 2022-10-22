package com.sucy.skill.hook;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.util.MythicUtil;
import io.lumine.xikage.mythicmobs.utils.promise.Promise;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockIterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * SkillAPI © 2017
 * com.sucy.skill.hook.MythicMobsHook
 */
public class MythicMobsHook {

    public static void taunt(final LivingEntity target, final LivingEntity source, final double amount) {
        if (amount > 0) {
            MythicMobs.inst().getAPIHelper().addThreat(target, source, amount);
        } else if (amount < 0) {
            MythicMobs.inst().getAPIHelper().reduceThreat(target, source, -amount);
        }
    }

    public static boolean isMonster(final LivingEntity target) {
        return MythicMobs.inst().getAPIHelper().isMythicMob(target);
    }

    public static boolean castSkill(LivingEntity caster, String skillName) {
        return MythicMobs.inst().getAPIHelper().castSkill(caster, skillName);
    }

    public static void castSkill(LivingEntity caster, String skillName, Float power) {
        LivingEntity target = getTargetedEntity(caster);
        ArrayList<Entity> targets = new ArrayList<>();
        targets.add(target);
        MythicMobs.inst().getAPIHelper().castSkill(caster, skillName, caster, caster.getLocation(), targets, null, power);
    }

    public static void castSkill(LivingEntity caster, String skillName, Collection<Entity> targets, Float power) {
        ArrayList<Location> locations = new ArrayList<>();
        for (Entity target : targets) {
            locations.add(target.getLocation());
        }
        if (locations.stream().findFirst().isPresent()) {
            MythicMobs.inst().getAPIHelper().castSkill(caster, skillName, locations.stream().findFirst().get(), targets, locations, power);
        } else {
            MythicMobs.inst().getAPIHelper().castSkill(caster, skillName, null, targets, locations, power);
        }
    }

    public static LivingEntity getTargetedEntity(LivingEntity player) {
        int range = 32;
        List<Entity> ne;
        if (Bukkit.isPrimaryThread()) {

            ne = player.getNearbyEntities(range, range, range);
        } else {
            try {
                ne = Promise.supplyingSync(() -> player.getNearbyEntities(range, range, range)).get();
            } catch (ExecutionException | InterruptedException var21) {
                var21.printStackTrace();
                return null;
            }
        }

        List<LivingEntity> entities = new ArrayList<>();

        for (Entity o : ne) {
            if (o instanceof LivingEntity) {
                entities.add((LivingEntity) o);
            }
        }

        BlockIterator bi;
        try {
            bi = new BlockIterator(player, range);
        } catch (IllegalStateException var20) {
            return null;
        }

        label93:
        while (bi.hasNext()) {
            Block b = bi.next();
            int bx = b.getX();
            int by = b.getY();
            int bz = b.getZ();
            Material material = b.getType();
            if (material != Material.BARRIER && (material.isOccluding() || material.isSolid())) {
                break;
            }

            Iterator<LivingEntity> var18 = entities.iterator();

            while (true) {
                double ey;
                LivingEntity e;
                do {
                    double ez;
                    do {
                        do {
                            double ex;
                            do {
                                do {
                                    do {
                                        if (!var18.hasNext()) {
                                            continue label93;
                                        }

                                        e = var18.next();
                                        Location l = e.getLocation();
                                        ex = l.getX();
                                        ey = l.getY();
                                        ez = l.getZ();
                                    } while (!((double) bx - 0.75 <= ex));
                                } while (!(ex <= (double) bx + 1.75));
                            } while (!((double) bz - 0.75 <= ez));
                        } while (!(ez <= (double) bz + 1.75));
                    } while (!((double) (by - 1) <= ey));
                } while (!(ey <= (double) by + 2.5));

                if (!(e instanceof Player) || ((Player) e).getGameMode() != GameMode.CREATIVE) {
                    return e;
                }

            }
        }

        return null;
    }


}
