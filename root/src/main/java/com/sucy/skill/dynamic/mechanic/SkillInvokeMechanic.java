package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.player.PlayerData;
import com.sucy.skill.api.skills.Skill;
import com.sucy.skill.api.skills.SkillShot;
import com.sucy.skill.dynamic.DynamicSkill;
import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Calls another registered skill while preserving the caller's ownership rules.
 *
 * <p>Player casters route through {@link PlayerData#cast(String)} so permission,
 * resource cost, cooldown, and player skill ownership checks remain authoritative.
 * Non-player casters can only invoke a {@link SkillShot} and use the current
 * dynamic level. A per-thread depth counter rejects recursive chains at depth
 * twelve and the boolean result is always written to {@code invoke-result},
 * including lookup and recursion failures.</p>
 */
@SkillNode(key = "skill invoke", name = "Skill Invoke", nameZh = "调用技能",
        descriptionZh = "玩家按正常施法规则调用；非玩家使用当前等级，结果写入 invoke-result。")
public final class SkillInvokeMechanic extends MechanicComponent {
    @SkillField(kind = FieldKind.StringValue, label = "Skill", labelZh = "目标技能",
            tooltipZh = "必须填写已注册技能名。", defaultValue = "")
    private static final String NAME = "skill";
    /** Per-call-thread recursion guard; removing the zero frame prevents thread retention. */
    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);
    /** @return legacy component key used by dynamic skill loading */
    @Override public String getKey() { return "skill invoke"; }

    @Override public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        String name = settings.getString(NAME, "");
        Skill called = SkillAPI.getSkill(name);
        if (called == null || DEPTH.get() >= 12) {
            DynamicSkill.getCastData(caster).put("invoke-result", false);
            return false;
        }
        DEPTH.set(DEPTH.get() + 1);
        try {
            boolean result;
            if (caster instanceof Player) {
                PlayerData data = SkillAPI.getPlayerData((Player) caster);
                result = data != null && data.getSkill(name) != null && data.cast(name);
            } else result = called instanceof SkillShot && ((SkillShot) called).cast(caster, level);
            DynamicSkill.getCastData(caster).put("invoke-result", result);
            return result;
        } finally {
            int depth = DEPTH.get() - 1;
            if (depth == 0) DEPTH.remove();
            else DEPTH.set(depth);
        }
    }
}
