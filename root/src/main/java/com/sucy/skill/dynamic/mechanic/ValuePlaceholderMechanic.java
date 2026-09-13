package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.dynamic.DynamicSkill;
import com.sucy.skill.hook.PlaceholderAPIHook;
import com.sucy.skill.hook.PluginChecker;
import com.sucy.skill.log.Logger;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * SkillAPI © 2018
 * com.sucy.skill.dynamic.mechanic.ValuePlaceholderMechanic
 */
@SkillNode(
        key = "value placeholder",
        name = "Value Placeholder",
        nameZh = "数值占位符",
        description = "Uses a placeholder string and stores it as a value for the caster",
        descriptionZh = "用 PlaceholderAPI 解析占位符，把结果存入施法者 cast data。要求 PlaceholderAPI 已启用且第一个目标是玩家（占位符按该目标玩家解析，而键存在施法者身上），否则返回 false。数字模式下解析结果无法转成数字时会记录一条告警日志并返回 false。",
        requiresPlugins = {"PlaceholderAPI"})
public class ValuePlaceholderMechanic extends MechanicComponent
{
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Key",
            labelZh = "引用键",
            tooltip = "[key] The unique key to store the value under. This key can be used in place of attribute values to use the stored value.",
            tooltipZh = "cast data 里的键名，支持 {uuid} 替换为施法者 UUID。",
            defaultValue = "value")
    private static final String KEY  = "key";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Type",
            labelZh = "类型",
            tooltip = "[type] The type of value to store. Number values require numeric placeholders. String values can be used in messages or commands.",
            tooltipZh = "存储类型。判定只看首字母：以 S 开头（String）时按字符串原样存入，可用于消息或命令；其他情况按数字解析存 Double，解析失败则告警并返回 false。默认 Number。注意字符串类型的值不能被 Value Condition 当数值比较。",
            options = {"Number", "String"},
            optionsZh = {"可选值1", "可选值2"},
            defaultValue = "Number")
    private static final String TYPE = "type";
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Placeholder",
            labelZh = "占位符",
            tooltip = "[placeholder] The placeholder string to use. Can contain multiple placeholders if using the String type.",
            tooltipZh = "要解析的占位符文本，如 %player_food_level%。字符串模式下可包含多个占位符与普通文字；数字模式下整体必须能解析为一个数字。",
            defaultValue = "%player_food_level%")
    private static final String PLACEHOLDER = "placeholder";

    @Override
    public String getKey() {
        return "value placeholder";
    }

    /**
     * Executes the component
     *
     * @param caster  caster of the skill
     * @param level   level of the skill
     * @param targets targets to apply to
     *
     * @return true if applied to something, false otherwise
     */
    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets)
    {
        if (!PluginChecker.isPlaceholderAPIActive()) {
            return false;
        }

        if (targets.get(0) instanceof Player)
        {
            final String key = settings.getString(KEY, "").replace("{uuid}", caster.getUniqueId().toString());
            final String placeholder = settings.getString(PLACEHOLDER, "");
            final String type = settings.getString(TYPE, "STRING").toUpperCase();

            final String value = PlaceholderAPIHook.format(placeholder, (Player)targets.get(0));

            // NUMBER
            if (type.charAt(0) == 'S') { // STRING
                DynamicSkill.getCastData(caster).put(key, value);
            } else {
                try {
                    DynamicSkill.getCastData(caster).put(key, Double.parseDouble(value));
                } catch (final Exception ex) {
                    Logger.invalid(placeholder + " is not a valid numeric placeholder - PlaceholderAPI returned " + value);
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
