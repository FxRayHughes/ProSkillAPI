package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.api.Settings;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.Map;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * SkillAPI © 2018
 * com.sucy.skill.dynamic.trigger.BlockBreakTrigger
 */
@SkillNode(
        key = "DEATH",
        name = "Death",
        nameZh = "死亡时",
        description = "Applies skill effects when a player dies.",
        descriptionZh = "持有该技能的生物死亡时触发，施法者是死亡者本身。虽然英文说明只提玩家，代码走的是通用的实体死亡事件，怪物只要身上激活了这个技能同样会触发。死亡事件不可取消，所以这里无法阻止死亡，只能做死亡时的结算（掉落提示、复仇标记等）。",
        container = true)
public class DeathTrigger implements Trigger<EntityDeathEvent> {
    @SkillField(
            kind = FieldKind.StringValue,
            label = "killer",
            tooltipZh = "填 true 时把初始目标切换成击杀者，并且要求击杀者存在（必须是玩家造成的最后一击），否则整个技能不触发；填 false（默认）时初始目标是死亡者自己。该键只能手写进 YAML，编辑器面板里没有对应输入项。")
    private static final String KILLER = "killer";


    /** {@inheritDoc} */
    @Override
    public String getKey() {
        return "DEATH";
    }

    /** {@inheritDoc} */
    @Override
    public Class<EntityDeathEvent> getEvent() {
        return EntityDeathEvent.class;
    }

    /** {@inheritDoc} */
    @Override
    public boolean shouldTrigger(final EntityDeathEvent event, final int level, final Settings settings) {
        return !isTargetingKiller(settings) || event.getEntity().getKiller() != null;
    }

    /** {@inheritDoc} */
    @Override
    public void setValues(final EntityDeathEvent event, final Map<String, Object> data) { }

    /** {@inheritDoc} */
    @Override
    public LivingEntity getCaster(final EntityDeathEvent event) {
        return event.getEntity();
    }

    /** {@inheritDoc} */
    @Override
    public LivingEntity getTarget(final EntityDeathEvent event, final Settings settings) {
        return isTargetingKiller(settings) ? event.getEntity().getKiller() : event.getEntity();
    }

    private boolean isTargetingKiller(final Settings settings) {
        return settings.getString(KILLER, "false").equalsIgnoreCase("true");
    }
}
