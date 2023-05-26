package com.sucy.skill.hook.mechanic;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.skills.Skill;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.skills.placeholders.parsers.PlaceholderInt;
import io.lumine.xikage.mythicmobs.skills.placeholders.parsers.PlaceholderString;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class MythicDamageMechanic extends SkillMechanic implements ITargetedEntitySkill {

    protected Double value;
    protected PlaceholderString classifier;

    protected PlaceholderString type;
    protected Boolean trueDamage;

    public static String SKILL_NAME = "MythicMobs_Cast_Damage";

    protected Skill skill = new Skill(SKILL_NAME, "Dynamic", Material.ICE, 1) {

    };

    public MythicDamageMechanic(String line, MythicLineConfig mlc) {
        super(line, mlc);
        this.value = mlc.getDouble(new String[]{"value", "v"}, 0.0);
        this.classifier = PlaceholderString.of(mlc.getString(new String[]{"classifier", "class", "c"}, ""));
        this.type = PlaceholderString.of(mlc.getString(new String[]{"type", "t"}, ""));
        this.trueDamage = mlc.getBoolean(new String[]{"true"}, false);
    }

    @Override
    public boolean castAtEntity(SkillMetadata data, AbstractEntity target) {
        if (target.isDead()) {
            return false;
        }
        if (target.isLiving() && target.getHealth() <= 0.0d) {
            return false;
        }
        String pString = type.get(data).toLowerCase();
        boolean percent = pString.equals("multiplier") || pString.equals("percent");
        boolean missing = pString.equals("percent missing");
        boolean left = pString.equals("percent left");
        boolean trueDmg = trueDamage;
        double damage = value;
        Entity caster = data.getCaster().getEntity().getBukkitEntity();
        if (!(caster instanceof LivingEntity)) {
            return false;
        }
        Entity targetSkill = target.getBukkitEntity();
        if (!(targetSkill instanceof LivingEntity)) {
            return false;
        }
        LivingEntity other = (LivingEntity) caster;
        String classification = classifier.get(data);
        if (damage < 0) {
            return false;
        }
        if (target.isDead()) {
            return false;
        }
        double amount;
        if (percent) {
            amount = damage * target.getMaxHealth() / 100;
        } else if (missing) {
            amount = damage * (target.getMaxHealth() - target.getHealth()) / 100;
        } else if (left) {
            amount = damage * target.getHealth() / 100;
        } else {
            amount = damage;
        }
        Bukkit.getScheduler().runTask(SkillAPI.singleton, () -> {
            if (trueDmg) {
                skill.trueDamage((LivingEntity) targetSkill, amount, other);
            } else {
                skill.damage((LivingEntity) targetSkill, amount, other, classification, true);
            }
        });
        return true;
    }

}
