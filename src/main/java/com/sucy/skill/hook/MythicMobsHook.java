package com.sucy.skill.hook;

import io.lumine.xikage.mythicmobs.MythicMobs;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Collection;

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
        MythicMobs.inst().getAPIHelper().castSkill(caster, skillName, power);
    }

    public static void castSkill(LivingEntity caster, String skillName, Collection<Entity> targets, Float power) {
        MythicMobs.inst().getAPIHelper().castSkill(caster, skillName, power);
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


}
