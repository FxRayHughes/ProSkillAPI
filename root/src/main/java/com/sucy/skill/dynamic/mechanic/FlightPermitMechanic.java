package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;

/** Changes the permission bit independently of the player's current flight toggle. */
@SkillNode(key = "flight permit", name = "Flight Permit", nameZh = "允许飞行",
        descriptionZh = "设置玩家能否开始飞行；撤销时同时结束正在进行的飞行。")
public final class FlightPermitMechanic extends MechanicComponent {
    @SkillField(kind = FieldKind.BooleanValue, label = "Permit", labelZh = "允许", defaultValue = "True")
    private static final String PERMIT = "permit";
    @Override public String getKey() { return "flight permit"; }
    @Override public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        boolean permit = settings.getBool(PERMIT, true);
        boolean changed = false;
        for (LivingEntity target : targets) if (target instanceof Player) {
            Player player = (Player) target;
            if (!permit && player.isFlying()) player.setFlying(false);
            player.setAllowFlight(permit);
            changed = true;
        }
        return changed;
    }
}
