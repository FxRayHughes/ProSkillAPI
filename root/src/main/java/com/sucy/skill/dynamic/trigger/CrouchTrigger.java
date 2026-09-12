package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.api.Settings;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import java.util.Map;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * SkillAPI © 2018
 * com.sucy.skill.dynamic.trigger.BlockBreakTrigger
 */
@SkillNode(
        key = "CROUCH",
        name = "Crouch",
        nameZh = "下蹲时",
        description = "Applies skill effects when a player starts or stops crouching using the shift key.",
        descriptionZh = "玩家按下或松开潜行键（Shift）时触发，施法者与初始目标都是这名玩家。默认只在“开始潜行”这一下触发，不是潜行期间持续触发；想做持续效果需要自己配合状态标记或延时节点。",
        container = true)
public class CrouchTrigger implements Trigger<PlayerToggleSneakEvent> {
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Type",
            labelZh = "类型",
            tooltip = "[type] Whether or not you want to apply components when crouching or not crouching",
            tooltipZh = "选择在潜行的哪个时点触发：Start Crouching 只在按下时触发，Stop Crouching 只在松开时触发，Both 两者都触发。未配置时按 Start Crouching 处理；填了无法识别的值会等同于 Start Crouching，而不会报错。",
            options = {"Start Crouching", "Stop Crouching", "Both"},
            optionsZh = {"开始下蹲", "停止下蹲", "两者"},
            defaultValue = "Start Crouching")
    private static final String TYPE = "type";


    /** {@inheritDoc} */
    @Override
    public String getKey() {
        return "CROUCH";
    }

    /** {@inheritDoc} */
    @Override
    public Class<PlayerToggleSneakEvent> getEvent() {
        return PlayerToggleSneakEvent.class;
    }

    /** {@inheritDoc} */
    @Override
    public boolean shouldTrigger(final PlayerToggleSneakEvent event, final int level, final Settings settings) {
        final String type = settings.getString(TYPE, "start crouching");
        return type.equalsIgnoreCase("both") || event.isSneaking() != type.equalsIgnoreCase("stop crouching");
    }

    /** {@inheritDoc} */
    @Override
    public void setValues(final PlayerToggleSneakEvent event, final Map<String, Object> data) { }

    /** {@inheritDoc} */
    @Override
    public LivingEntity getCaster(final PlayerToggleSneakEvent event) {
        return event.getPlayer();
    }

    /** {@inheritDoc} */
    @Override
    public LivingEntity getTarget(final PlayerToggleSneakEvent event, final Settings settings) {
        return event.getPlayer();
    }
}
