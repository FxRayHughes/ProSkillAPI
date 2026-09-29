package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.entity.LivingEntity;

import java.util.List;

/**
 * Calls a named group with the current caster, level, targets, and cast data.
 *
 * <p>A group is loaded as a declaration and is not independently subscribed
 * to Bukkit events. This mechanic is the explicit call boundary. Values written
 * by the group remain in the current cast context, and the caller's children
 * run after the group returns. The skill-level group recursion guard prevents a
 * group from re-entering itself forever.</p>
 */
@SkillNode(key = "run group", name = "Run Group", nameZh = "运行共享组",
        descriptionZh = "执行一个根级 GROUP 的子树，返回后继续执行本节点的子节点。", container = true)
public final class RunGroupMechanic extends MechanicComponent {
    @SkillField(kind = FieldKind.StringValue, label = "Group Key", labelZh = "共享组键")
    private static final String GROUP = "group";
    /** @return legacy component key used by dynamic skill loading */
    @Override public String getKey() { return "run group"; }
    @Override public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        boolean groupRan = skill.executeGroup(settings.getString(GROUP, ""), caster, level, targets);
        // Calls behave like functions: a following branch can use values written by the group.
        return executeChildren(caster, level, targets) || groupRan;
    }
}
