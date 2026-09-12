package com.sucy.skill.hook.mythic.v5;

import com.sucy.skill.hook.mythic.MythicBridges;
import com.sucy.skill.hook.mythic.SkillBridge;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderString;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.skills.SkillMechanic;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

/**
 * MythicMobs 5 的 {@code damageType} 机制：按 SkillAPI 的伤害体系结算伤害。
 * <p>
 * type 支持 multiplier/percent（按最大生命百分比）、percent missing（按已损失生命）、
 * percent left（按剩余生命），其余按固定值。
 */
public class V5DamageMechanic extends SkillMechanic implements ITargetedEntitySkill {

    protected Double value;
    protected PlaceholderString classifier;
    protected PlaceholderString type;
    protected Boolean trueDamage;

    public V5DamageMechanic(String line, MythicLineConfig mlc) {
        super(MythicBukkit.inst().getSkillManager(), null, line, mlc);
        this.value = mlc.getDouble(new String[]{"value", "v"}, 0.0);
        this.classifier = PlaceholderString.of(mlc.getString(new String[]{"classifier", "class", "c"}, ""));
        this.type = PlaceholderString.of(mlc.getString(new String[]{"type", "t"}, ""));
        this.trueDamage = mlc.getBoolean(new String[]{"true"}, false);
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata data, AbstractEntity target) {
        if (target.isDead()) {
            return SkillResult.INVALID_TARGET;
        }
        if (target.isLiving() && target.getHealth() <= 0.0d) {
            return SkillResult.INVALID_TARGET;
        }
        if (value < 0) {
            return SkillResult.INVALID_CONFIG;
        }
        final SkillBridge bridge = MythicBridges.skills();
        if (bridge == null) {
            return SkillResult.ERROR;
        }

        final Entity caster = data.getCaster().getEntity().getBukkitEntity();
        if (!(caster instanceof LivingEntity)) {
            return SkillResult.INVALID_TARGET;
        }
        final Entity victim = target.getBukkitEntity();
        if (!(victim instanceof LivingEntity)) {
            return SkillResult.INVALID_TARGET;
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
        return SkillResult.SUCCESS;
    }
}
