package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.dynamic.DynamicSkill;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * SkillAPI © 2018
 * com.sucy.skill.dynamic.mechanic.ValueCopyMechanic
 */
@SkillNode(
        key = "value copy",
        name = "Value Copy",
        nameZh = "数值复制",
        description = "Copies a stored value from the caster to the target or vice versa",
        descriptionZh = "在施法者与目标的 cast data 之间搬运一个键的值，按原样复制（可以是数字、位置、实体列表等任意类型，不限于数字）。to-target 为真时把施法者的值复制给每个目标；为假时把第一个目标的值复制给施法者。源键不存在时该次复制静默跳过，但整个节点仍返回 true。目标为空或未配置 key 时返回 false。",
        requiresNodes = {"value set"})
public class ValueCopyMechanic extends MechanicComponent {
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Key",
            labelZh = "引用键",
            tooltip = "[key] The unique key to store the value under. This key can be used in place of attribute values to use the stored value.",
            tooltipZh = "源键名，从来源方的 cast data 读取，支持 {uuid} 替换为施法者 UUID。",
            defaultValue = "value")
    private static final String KEY       = "key";
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Destination",
            labelZh = "目标位置",
            tooltip = "[destination] The key to copy the original value to",
            tooltipZh = "写入到接收方 cast data 的目标键名；留空时默认与源键同名。注意此项不做 {uuid} 替换。",
            defaultValue = "value")
    private static final String TARGET    = "destination";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "To target",
            labelZh = "朝向目标",
            tooltip = "[to-target] The amount to add to the value",
            tooltipZh = "复制方向。True 表示施法者 → 每个目标；False 表示第一个目标 → 施法者。默认 True。（原英文提示写的是“要加多少”，属于文案复制错误，实际是方向开关。）",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "True")
    private static final String TO_TARGET = "to-target";

    @Override
    public String getKey() {
        return "value copy";
    }

    @Override
    public boolean execute(
            final LivingEntity caster, final int level, final List<LivingEntity> targets) {

        if (targets.size() == 0 || !settings.has(KEY)) {
            return false;
        }

        final String key = settings.getString(KEY).replace("{uuid}", caster.getUniqueId().toString());;
        final String destination = settings.getString(TARGET, key);
        final boolean toTarget = settings.getString(TO_TARGET, "true").equalsIgnoreCase("true");

        if (toTarget) {
            targets.forEach(target -> apply(caster, target, key, destination));
        } else {
            apply(targets.get(0), caster, key, destination);
        }

        return true;
    }

    private boolean apply(final LivingEntity from, final LivingEntity to, final String key, final String destination) {
        final Object value = DynamicSkill.getCastData(from).get(key);
        if (value == null) return false;
        DynamicSkill.getCastData(to).put(destination, value);
        return true;
    }
}
