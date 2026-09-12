package com.sucy.skill.hook.mythic.v4;

import com.sucy.skill.hook.mythic.MythicBridges;
import com.sucy.skill.hook.mythic.SkillBridge;
import io.lumine.xikage.mythicmobs.adapters.AbstractEntity;
import io.lumine.xikage.mythicmobs.io.MythicLineConfig;
import io.lumine.xikage.mythicmobs.skills.ITargetedEntitySkill;
import io.lumine.xikage.mythicmobs.skills.SkillMechanic;
import io.lumine.xikage.mythicmobs.skills.SkillMetadata;
import io.lumine.xikage.mythicmobs.skills.placeholders.parsers.PlaceholderString;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

/**
 * MythicMobs 4 的 {@code damageType} 机制：按 SkillAPI 的伤害体系结算伤害。
 * <p>
 * type 支持 multiplier/percent（按最大生命百分比）、percent missing（按已损失生命）、
 * percent left（按剩余生命），其余按固定值。
 */
public class V4DamageMechanic extends SkillMechanic implements ITargetedEntitySkill {

    protected Double value;
    protected PlaceholderString classifier;
    protected PlaceholderString type;
    protected Boolean trueDamage;

    public V4DamageMechanic(String line, MythicLineConfig mlc) {
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
        if (value < 0) {
            return false;
        }
        final SkillBridge bridge = MythicBridges.skills();
        if (bridge == null) {
            return false;
        }

        final Entity caster = data.getCaster().getEntity().getBukkitEntity();
        if (!(caster instanceof LivingEntity)) {
            return false;
        }
        final Entity victim = target.getBukkitEntity();
        if (!(victim instanceof LivingEntity)) {
            return false;
        }

        final String pString = type.get(data).toLowerCase();
        final double amount;
        if (pString.equals("multiplier") || pString.equals("percent")) {
            amount = value * target.getMaxHealth() / 100;
        } else if (pString.equals("percent missing")) {
            amount = value * (target.getMaxHealth() - target.getHealth()) / 100;
        } else if (pString.equals("percent left")) {
            amount = value * target.getHealth() / 100;
        } else {
            amount = value;
        }

        bridge.damage((LivingEntity) victim, amount, (LivingEntity) caster, classifier.get(data), trueDamage);
        return true;
    }
}
