package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.api.target.TargetHelper;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;

/**
 * 沿目标水平朝向闪现一段距离，沿途遇到障碍则停在障碍前。
 * <p>
 * 落点解析与 {@link WarpMechanic} 共用 {@link TargetHelper#getOpenLocation}，
 * 区别在于方向取自视线并压平 Y 分量——否则抬头施放会把人送上天。
 */
@SkillNode(
        key = "dash",
        name = "Dash",
        nameZh = "冲刺瞬移",
        descriptionZh = "让每个目标沿自己的水平朝向瞬移一段距离，途中撞到障碍就停在障碍前。方向取自视线但压平了 Y 分量，所以抬头施放不会把人送上天；视线接近垂直导致方向退化时用固定水平方向兜底。落点从眼部高度开始求解以免脚下台阶被当成障碍，落地后会做一次贴地修正，并保留原来的视角朝向。目标列表为空、或距离不大于 0 时返回 false。本节点的两个配置项（distance、walls）在代码里没有 @SkillField 标注，编辑器不展示。")
public class DashMechanic extends MechanicComponent {

    private static final String DISTANCE = "distance";
    private static final String WALL     = "walls";

    /** 视线接近垂直时朝向退化为零向量，此时取一个固定水平方向兜底。 */
    private static final Vector DEFAULT_DIRECTION = new Vector(0, 0, 1);

    @Override
    public String getKey() {
        return "dash";
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
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        if (targets.isEmpty()) {
            return false;
        }

        final double distance = parseValues(caster, DISTANCE, level, 5.0);
        if (distance <= 0) {
            return false;
        }
        final boolean throughWalls = settings.getString(WALL, "false").equalsIgnoreCase("true");

        for (LivingEntity target : targets) {
            final Location origin = target.getLocation();

            Vector direction = origin.getDirection().setY(0);
            if (direction.lengthSquared() < 0.001) {
                direction = DEFAULT_DIRECTION.clone();
            }
            direction.normalize().multiply(distance);

            // 从眼部高度求解，避免脚下的台阶被当成起点障碍
            final Location from = origin.clone().add(0, 1, 0);
            Location loc = TargetHelper.getOpenLocation(from, from.clone().add(direction), throughWalls);
            if (!loc.getBlock().getType().isSolid()
                    && loc.getBlock().getRelative(BlockFace.DOWN).getType().isSolid()) {
                loc.add(0, 1, 0);
            }
            loc = loc.subtract(0, 1, 0);

            // 保留原视角，闪现不应改变玩家的朝向
            loc.setYaw(origin.getYaw());
            loc.setPitch(origin.getPitch());
            target.teleport(loc);
        }
        return true;
    }
}
