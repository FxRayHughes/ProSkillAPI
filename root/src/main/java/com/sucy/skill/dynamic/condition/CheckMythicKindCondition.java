package com.sucy.skill.dynamic.condition;

import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.hook.MythicMobsHook;
import org.bukkit.entity.LivingEntity;

/** Matches MythicMobs' configured mob identity through the existing version bridge. */
@SkillNode(key = "check mythic kind", name = "Check Mythic Kind", nameZh = "检查 Mythic 怪物类型",
        descriptionZh = "匹配 MythicMobs 配置键；未安装依赖或目标不是 Mythic 怪物时不通过。",
        container = true, requiresPlugins = {"MythicMobs"})
public final class CheckMythicKindCondition extends ConditionComponent {
    @SkillField(kind = FieldKind.StringValue, label = "Mob Key", labelZh = "怪物配置键")
    private static final String KIND = "kind";
    @Override public String getKey() { return "check mythic kind"; }
    @Override boolean test(LivingEntity caster, int level, LivingEntity target) {
        String expected = settings.getString(KIND, "");
        String actual = target == null ? null : MythicMobsHook.getMobKind(target);
        return !expected.isEmpty() && expected.equals(actual);
    }
}
