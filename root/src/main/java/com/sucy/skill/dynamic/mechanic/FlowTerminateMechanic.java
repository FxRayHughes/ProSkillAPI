package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.dynamic.FlowControl;
import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.entity.LivingEntity;

import java.util.List;

/**
 * Stops this branch or all remaining siblings in the current root invocation.
 *
 * <p>The branch scope only returns false, which prevents this node from
 * executing children and lets the parent traversal continue with its normal
 * sibling rules. The skill scope marks the current {@link FlowControl} frame;
 * the trigger executor observes that marker and stops later siblings belonging
 * to the same root without affecting a nested skill or signal frame.</p>
 */
@SkillNode(key = "flow terminate", name = "Flow Terminate", nameZh = "终止执行",
        descriptionZh = "分支模式不执行子节点；技能模式使当前起点的其余节点停止。")
public final class FlowTerminateMechanic extends MechanicComponent {
    @SkillField(kind = FieldKind.ListValue, label = "Scope", labelZh = "终止范围",
            options = {"branch", "skill"}, optionsZh = {"当前分支", "本次技能"}, defaultValue = "branch")
    private static final String SCOPE = "scope";
    /** @return legacy component key used by dynamic skill loading */
    @Override public String getKey() { return "flow terminate"; }
    @Override public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        if ("skill".equalsIgnoreCase(settings.getString(SCOPE, "branch"))) FlowControl.stopSkill();
        return false;
    }
}
