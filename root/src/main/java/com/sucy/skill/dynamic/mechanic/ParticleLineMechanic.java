package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.api.Settings;
import com.sucy.skill.api.util.ParticleHelper;
import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import java.util.List;

/**
 * 粒子连线机制 - 从施法者到每个目标画粒子直线
 */
@SkillNode(
        key = "particle line",
        name = "Particle Line",
        nameZh = "粒子直线",
        description = "Draws a straight line of particles from the caster to each target.",
        descriptionZh = "从施法者到每一个目标各画一条粒子直线，起点是施法者、终点是目标，两端都抬高「高度偏移」格。"
                + "采样点数按 起终点距离×密度 取整，至少 2 段（即至少 3 个点，含首尾）。"
                + "与 particle point line 的区别：那个是相对目标朝向的两个固定端点，本节点端点跟着施法者与目标实时走。"
                + "内部把 particles 固定为 1、radius 固定为 0，因此每个采样点只出一个粒子；"
                + "但若额外配了 arrangement，仍会在每个采样点上按该排布重复铺一遍，点数会成倍上升。"
                + "目标列表为空时返回 false 且不绘制。施法者与目标不在同一世界时会抛异常。")
public class ParticleLineMechanic extends MechanicComponent {

    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Density",
            labelZh = "密度",
            tooltip = "[density] Samples per block of line length",
            tooltipZh = "每格线长采样多少个粒子点，随技能等级缩放。总点数为 距离×密度 取整后再加 1，"
                    + "结果不足 2 时按 2 段处理；此处没有上限保护，填得过大会明显吃 tick。",
            defaultValue = "4")
    private static final String DENSITY = "density";

    @SkillField(
            kind = FieldKind.DoubleValue,
            label = "Height Offset",
            labelZh = "高度偏移",
            tooltip = "[height-offset] Vertical offset from the feet of both endpoints in blocks",
            tooltipZh = "起点与终点同时相对脚部抬高的格数，正值上移、负值下移。默认 1 约等于胸口高度，"
                    + "填 0 会贴地。不随技能等级缩放。",
            defaultValue = "1")
    private static final String HEIGHT = "height-offset";

    @Override
    public String getKey() {
        return "particle line";
    }

    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        if (targets.size() == 0) {
            return false;
        }

        double density = parseValues(caster, DENSITY, level, 4.0);
        double heightOffset = settings.getDouble(HEIGHT, 1.0);

        boolean drawn = false;

        Settings particleSettings = new Settings(settings);
        particleSettings.set("particles", 1, 0);
        particleSettings.set("radius", 0, 0);
        particleSettings.set("level", level);

        Location casterLoc = caster.getLocation().add(0, heightOffset, 0);

        for (LivingEntity target : targets) {
            Location targetLoc = target.getLocation().add(0, heightOffset, 0);
            // 汇总实际绘制结果：跨世界或距离为 0 的目标被跳过，
            // 全部目标都画不出来时返回 false，让技能树能据此走 else 分支。
            if (drawLine(casterLoc, targetLoc, density, particleSettings)) {
                drawn = true;
            }
        }

        return drawn;
    }

    /**
     * 在两点间画一条等距采样的粒子线。
     *
     * @return 是否真的画了；跨世界或距离为 0 时返回 false
     */
    private boolean drawLine(Location from, Location to, double density, Settings settings) {
        // 跨世界时 Location.distance 会抛异常；技能可能在传送途中结算，不能让它崩掉。
        double distance = ParticleGeometry.safeDistance(from, to);
        if (distance <= 0) {
            return false;
        }
        // 点数走统一钳制（上限 16384），避免密度填大后卡住 tick。
        int points = Math.max(2, ParticleGeometry.samples(distance * density));
        double dx = (to.getX() - from.getX()) / points;
        double dy = (to.getY() - from.getY()) / points;
        double dz = (to.getZ() - from.getZ()) / points;

        Location point = from.clone();
        for (int i = 0; i <= points; i++) {
            ParticleHelper.play(point, settings);
            point.add(dx, dy, dz);
        }
        return true;
    }
}
