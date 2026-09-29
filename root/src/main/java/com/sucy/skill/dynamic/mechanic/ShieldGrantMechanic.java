package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.combat.shield.ShieldManager;
import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.entity.LivingEntity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Applies an independent shield layer to each current living target.
 *
 * <p>Each invocation validates capacity, duration, ratio, and per-hit limits
 * in {@link ShieldManager}; invalid values fail the target without corrupting
 * existing layers. Stacking is scoped by the configured key and granting
 * caster, while priority and filters are evaluated later when a damage event
 * is previewed. The mechanic returns true when at least one target accepted a
 * layer, which lets a parent branch distinguish a rejected grant from a
 * successful application.</p>
 */
@SkillNode(key = "shield grant", name = "Shield Grant", nameZh = "施加护盾",
        descriptionZh = "为目标增加独立护盾层；先按优先级和创建顺序消耗，过期或死亡时清理。", container = false)
public class ShieldGrantMechanic extends MechanicComponent {
    @SkillField(kind = FieldKind.StringValue, label = "Layer Key", labelZh = "护盾键",
            tooltipZh = "用于后续读取、调整和移除同类护盾。", defaultValue = "shield")
    private static final String KEY = "key";
    @SkillField(kind = FieldKind.AttributeValue, label = "Capacity", labelZh = "容量",
            tooltipZh = "护盾可吸收的生命伤害，必须大于零。", defaultValue = "10")
    private static final String CAPACITY = "capacity";
    @SkillField(kind = FieldKind.AttributeValue, label = "Duration", labelZh = "持续秒数",
            tooltipZh = "-1 表示持续到实体失效或被移除。", defaultValue = "10")
    private static final String DURATION = "duration";
    @SkillField(kind = FieldKind.IntValue, label = "Priority", labelZh = "优先级",
            tooltipZh = "数值越大越先承受伤害。", defaultValue = "0")
    private static final String PRIORITY = "priority";
    @SkillField(kind = FieldKind.DoubleValue, label = "Absorb Ratio", labelZh = "吸收比例",
            tooltipZh = "0 到 1；每层按剩余伤害的这一比例吸收。", defaultValue = "1")
    private static final String RATIO = "ratio";
    @SkillField(kind = FieldKind.DoubleValue, label = "Per Hit Limit", labelZh = "单次上限",
            tooltipZh = "0 表示只受剩余容量限制。", defaultValue = "0")
    private static final String HIT_LIMIT = "per-hit-limit";
    @SkillField(kind = FieldKind.ListValue, label = "Stacking", labelZh = "重复施加",
            tooltipZh = "叠加独立层、刷新剩余容量或替换旧层。",
            options = {"stack", "refresh", "replace"}, optionsZh = {"叠加", "刷新", "替换"}, defaultValue = "stack")
    private static final String STACKING = "stacking";
    @SkillField(kind = FieldKind.StringListValue, label = "Damage Kinds", labelZh = "伤害大类",
            tooltipZh = "留空匹配全部；可填 physical、skill、environment、true。")
    private static final String KINDS = "damage-kinds";
    @SkillField(kind = FieldKind.StringListValue, label = "Damage Causes", labelZh = "伤害原因",
            tooltipZh = "留空匹配全部；填写 Bukkit DamageCause 名。")
    private static final String CAUSES = "damage-causes";
    @SkillField(kind = FieldKind.StringListValue, label = "Classifications", labelZh = "技能分类",
            tooltipZh = "留空匹配全部；仅对有分类的技能伤害生效。")
    private static final String CLASSES = "classifications";

    /** @return legacy component key used by dynamic skill loading */
    @Override public String getKey() { return "shield grant"; }

    @Override public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        double capacity = parseValues(caster, CAPACITY, level, 10);
        double seconds = parseValues(caster, DURATION, level, 10);
        if (!Double.isFinite(seconds) || seconds < -1 || seconds > Long.MAX_VALUE / 1000) return false;
        long duration = seconds < 0 ? -1 : Math.round(seconds * 20);
        boolean applied = false;
        for (LivingEntity target : targets) {
            if (target == null || target.isDead()) continue;
            applied |= ShieldManager.add(target, caster, settings.getString(KEY, "shield"),
                    capacity, duration, settings.getInt(PRIORITY, 0),
                    settings.getDouble(RATIO, 1), settings.getDouble(HIT_LIMIT, 0),
                    settings.getString(STACKING, "stack"), set(KINDS), set(CAUSES), set(CLASSES)) != null;
        }
        return applied;
    }

    private Set<String> set(String key) { return new HashSet<>(settings.getStringList(key)); }
}
