package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.dynamic.DynamicSkill;
import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.hook.MythicMobsHook;
import com.sucy.skill.hook.PluginChecker;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;

/**
 * 读取 MM 怪物配置中的自定义属性字段，存入 castData 供后续组件使用。
 * 玩家目标会回退到 SkillAPI 的 PlayerData.getAttribute(...)，便于在同一被动里兼容双方。
 */
@SkillNode(
        key = "value mob attribute",
        name = "Value Mob Attribute",
        nameZh = "数值怪物属性",
        description = "Stores an attribute value read from a MythicMobs mob config (or a player's attribute) as a cast data value.",
        descriptionZh = "只看第一个目标：若它是玩家，读该玩家的 SkillAPI 属性点数；若是 MythicMobs 怪物，"
                + "则按属性名去怪物配置里读同名字段（属性名直接写在怪物 yml 里，如「暴击率: 30」）。"
                + "结果以 Double 存入施法者 cast data 的指定键，供后续节点用 {键名} 引用。"
                + "怪物侧读到 0 时会被当作「没配这个属性」，改用默认值写入；玩家侧读到 0 则原样写入 0。"
                + "读取过程中的任何异常都被吞掉并回落到默认值。"
                + "未配置引用键或属性名、或目标列表为空时返回 false 且不写入。"
                + "与 value attribute 的区别：那个只认玩家属性，本节点多了 MythicMobs 怪物配置这条来源。",
        requiresPlugins = {"MythicMobs"})
public class ValueMobAttributeMechanic extends MechanicComponent {

    @SkillField(
            kind = FieldKind.StringValue,
            label = "Key",
            labelZh = "引用键",
            tooltip = "[key] The unique key to store the value under",
            tooltipZh = "cast data 里的键名，后续节点用 {键名} 取值。存入的类型为 Double。"
                    + "同名键会被覆盖，多次读取不同属性时要用不同键名。",
            defaultValue = "attribute")
    private static final String KEY = "key";

    @SkillField(
            kind = FieldKind.StringValue,
            label = "Attribute",
            labelZh = "属性",
            tooltip = "[attribute] The attribute name to read from the mob config or the player's attributes",
            tooltipZh = "要读的属性名。目标是怪物时，这个名字必须与 MythicMobs 怪物 yml 里的字段名逐字一致（支持中文键名）；"
                    + "目标是玩家时，则要与服务器属性配置里的属性键一致。名字对不上时读到 0，进而回落到默认值。",
            defaultValue = "暴击率")
    private static final String ATTR = "attribute";

    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Default",
            labelZh = "默认值",
            tooltip = "[default] Fallback value used when the attribute cannot be read",
            tooltipZh = "读不到属性时写入的兜底数值，随技能等级缩放。"
                    + "怪物侧读到 0（含没配该字段、不是 MM 怪物、未安装 MythicMobs）时都会用这个值。",
            defaultValue = "0")
    private static final String DEFAULT = "default";

    @Override
    public String getKey() {
        return "value mob attribute";
    }

    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        if (!settings.has(KEY) || !settings.has(ATTR) || targets.isEmpty()) {
            return false;
        }

        String storeKey = settings.getString(KEY);
        String attrName = settings.getString(ATTR);
        double fallback = parseValues(caster, DEFAULT, level, 0.0);

        LivingEntity target = targets.get(0);
        double value = fallback;

        if (target instanceof Player) {
            try {
                value = SkillAPI.getPlayerData((Player) target).getAttribute(attrName);
            } catch (Exception ignored) {
            }
        } else if (PluginChecker.isMythicMobsActive()) {
            try {
                // 读到就用，包括 0。原实现用 v != 0 判断"是否读到"，会把怪物配置里
                // 明确写的 0 当成未配置而回退默认值，与上面玩家分支的语义也不一致。
                value = MythicMobsHook.getMobAttribute(target, attrName);
            } catch (Exception ignored) {
            }
        }

        HashMap<String, Object> data = DynamicSkill.getCastData(caster);
        data.put(storeKey, value);
        return true;
    }
}
