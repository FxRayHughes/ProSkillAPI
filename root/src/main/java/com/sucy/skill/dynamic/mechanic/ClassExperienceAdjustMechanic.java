package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.enums.ExpSource;
import com.sucy.skill.api.player.PlayerClass;
import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;

/** Uses the class XP methods so cancellable gain and loss events remain effective. */
@SkillNode(key = "class experience adjust", name = "Class Experience Adjust", nameZh = "调整职业经验",
        descriptionZh = "对目标主职业增加或减少指定点数，继续经过已有经验事件与升级逻辑。")
public final class ClassExperienceAdjustMechanic extends MechanicComponent {
    @SkillField(kind = FieldKind.AttributeValue, label = "Delta", labelZh = "经验变化", defaultValue = "1")
    private static final String DELTA = "delta";
    @Override public String getKey() { return "class experience adjust"; }
    @Override public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        double delta = parseValues(caster, DELTA, level, 1);
        if (!Double.isFinite(delta) || delta == 0) return false;
        boolean applied = false;
        for (LivingEntity target : targets) {
            if (!(target instanceof Player)) continue;
            PlayerClass playerClass = SkillAPI.getPlayerData((Player) target).getMainClass();
            if (playerClass == null) continue;
            if (delta > 0) playerClass.giveExp(delta, ExpSource.SPECIAL, true);
            else if (playerClass.getRequiredExp() > 0)
                playerClass.loseExp(-delta / playerClass.getRequiredExp());
            applied = true;
        }
        return applied;
    }
}
