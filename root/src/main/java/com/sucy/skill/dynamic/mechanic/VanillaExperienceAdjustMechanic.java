package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.api.event.SkillOutcomeEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;

/** Changes Minecraft experience points, not class experience. */
@SkillNode(key = "vanilla experience adjust", name = "Vanilla Experience Adjust", nameZh = "调整原版经验",
        descriptionZh = "对玩家增减原版经验点；不会修改职业经验。")
public final class VanillaExperienceAdjustMechanic extends MechanicComponent {
    @SkillField(kind = FieldKind.AttributeValue, label = "Points", labelZh = "经验点变化", defaultValue = "1")
    private static final String POINTS = "points";
    @Override public String getKey() { return "vanilla experience adjust"; }
    @Override public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        double raw = parseValues(caster, POINTS, level, 1);
        if (!Double.isFinite(raw) || raw < Integer.MIN_VALUE || raw > Integer.MAX_VALUE) return false;
        int points = (int) Math.round(raw);
        boolean changed = false;
        for (LivingEntity target : targets) if (target instanceof Player) {
            Player player = (Player) target;
            int before = player.getTotalExperience();
            player.giveExp(points);
            int after = player.getTotalExperience();
            // The event describes the observed point delta, including vanilla clamping.
            if (after != before) Bukkit.getPluginManager().callEvent(new SkillOutcomeEvent(
                    SkillOutcomeEvent.Phase.VANILLA_XP_CHANGED, caster, player,
                    "experience", "mechanic", after - before, before, after));
            changed = true;
        }
        return changed;
    }
}
