package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.api.Settings;
import com.sucy.skill.dynamic.DynamicSkill;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Map;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * SkillAPI © 2018
 * com.sucy.skill.dynamic.trigger.BlockBreakTrigger
 */
@SkillNode(
        key = "ENVIRONMENT_DAMAGE",
        name = "Environment Damage",
        nameZh = "环境伤害时",
        description = "Applies skill effects when a player takes environmental damage.",
        descriptionZh = "生物受到伤害时按伤害原因触发，施法者与初始目标都是受伤者自己；受伤方不是 LivingEntity 时不触发。注意它挂的是 Bukkit 通用伤害事件，type 填 any 会把近战攻击（ENTITY_ATTACK）之类也一并收进来，并不只有环境伤害。伤害值写入 api-taken；技能执行完后节点里的即时增益会回写到本次伤害上，因此可用来做减伤/抗性。已被其他插件取消的伤害不触发。",
        container = true)
public class EnvironmentalTrigger implements Trigger<EntityDamageEvent> {
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Type",
            labelZh = "类型",
            tooltip = "[type] The source of damage to apply for",
            tooltipZh = "限定伤害原因。any 表示不限；否则把填写值的空格换成下划线并转大写后与 Bukkit 的 DamageCause 名比较（Block Explosion → BLOCK_EXPLOSION）。代码在键缺失时按 any 处理，但编辑器新建节点预填的是 FALL，两者默认值不一致，照默认保存会变成只认摔落伤害。",
            options = {"Block Explosion", "Contact", "Cramming", "Custom", "Dragon Breath", "Drowning", "Entity Attack", "Entity Explosion", "Entity Sweep Attack", "Fall", "Falling Block", "Fire", "Fire Tick", "Fly Into Wall", "Hot Floor", "Lava", "Lightning", "Magic", "Melting", "Poison", "Projectile", "Starvation", "Suffocation", "Suicide", "Thorns", "Void", "Wither"},
            optionsZh = {"爆炸", "可选值2", "可选值3", "自定义", "可选值5", "可选值6", "实体", "实体爆炸", "实体", "可选值10", "可选值11", "火焰", "火焰", "可选值14", "可选值15", "熔岩", "可选值17", "可选值18", "可选值19", "可选值20", "投射物", "可选值22", "可选值23", "可选值24", "可选值25", "可选值26", "可选值27"},
            defaultValue = "FALL")
    private static final String TYPE = "type";


    /** {@inheritDoc} */
    @Override
    public String getKey() {
        return "ENVIRONMENT_DAMAGE";
    }

    /** {@inheritDoc} */
    @Override
    public Class<EntityDamageEvent> getEvent() {
        return EntityDamageEvent.class;
    }

    /** {@inheritDoc} */
    @Override
    public boolean shouldTrigger(final EntityDamageEvent event, final int level, final Settings settings) {
        final String type = settings.getString(TYPE, "any").replace(' ', '_').toUpperCase();
        return type.equalsIgnoreCase("ANY") || type.equalsIgnoreCase(event.getCause().name());
    }

    /** {@inheritDoc} */
    @Override
    public void setValues(final EntityDamageEvent event, final Map<String, Object> data) {
        data.put("api-taken", event.getDamage());
    }

    /** {@inheritDoc} */
    @Override
    public LivingEntity getCaster(final EntityDamageEvent event) {
        if (event.getEntity() instanceof LivingEntity) {
            return (LivingEntity) event.getEntity();
        } else {
            return null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public LivingEntity getTarget(final EntityDamageEvent event, final Settings settings) {
        return getCaster(event);
    }

    /**
     * Handles applying other effects after the skill resolves
     *
     * @param event event details
     * @param skill skill to resolve
     */
    @Override
    public void postProcess(final EntityDamageEvent event, final DynamicSkill skill) {
        final double damage = skill.applyImmediateBuff(event.getDamage());
        event.setDamage(damage);
    }
}
