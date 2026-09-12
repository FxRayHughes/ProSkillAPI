package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.List;

/** Draws a segment between two offsets expressed in the caster's local frame. */
@SkillNode(
        key = "particle point line",
        name = "Particle Point Line",
        nameZh = "粒子连线",
        description = "Draws a straight line of particles between two points relative to the target's facing.",
        descriptionZh = "在两个点之间画一条等距采样的粒子直线。两端点用「相对目标朝向」的坐标描述："
                + "forward 沿视线水平前方，right 沿侧向轴（叉积方向，正值偏左），upward 直接指定垂直分量。"
                + "含首尾两端点，因此点数为 长度×密度+1；点数被限制在 16384 以内，超出则不绘制。")
public class ParticlePointLineMechanic extends MechanicComponent {

    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Density",
            labelZh = "密度",
            tooltip = "[density] Samples per block of line length",
            tooltipZh = "每格线长采样多少个粒子点，随技能等级缩放。"
                    + "实际点数按 长度×密度 向上取整再加 1（含两端点），超过 16384 时不绘制。",
            defaultValue = "10")
    private static final String DENSITY = "density";

    @SkillField(
            kind = FieldKind.DoubleValue,
            label = "Start Forward",
            labelZh = "起点·前后",
            tooltip = "[start-forward] Start offset along the target's horizontal facing, in blocks",
            tooltipZh = "起点沿目标水平朝向的偏移，单位方块。正值在身前，负值在身后。",
            defaultValue = "0")
    private static final String START_FORWARD = "start-forward";

    @SkillField(
            kind = FieldKind.DoubleValue,
            label = "Start Right",
            labelZh = "起点·左右",
            tooltip = "[start-right] Start offset along the target's right-hand direction, in blocks",
            tooltipZh = "起点沿目标右手方向的偏移，单位方块。按叉积得出的侧向轴偏移；受该轴朝向影响，正值实际偏向目标左侧。",
            defaultValue = "0")
    private static final String START_RIGHT = "start-right";

    @SkillField(
            kind = FieldKind.DoubleValue,
            label = "Start Upward",
            labelZh = "起点·高度",
            tooltip = "[start-upward] Start height above the target's feet, in blocks",
            tooltipZh = "起点相对目标脚部的高度，单位方块。该值直接作为垂直分量，不受朝向俯仰影响。",
            defaultValue = "0")
    private static final String START_UPWARD = "start-upward";

    @SkillField(
            kind = FieldKind.DoubleValue,
            label = "End Forward",
            labelZh = "终点·前后",
            tooltip = "[end-forward] End offset along the target's horizontal facing, in blocks",
            tooltipZh = "终点沿目标水平朝向的偏移，单位方块。正值在身前，负值在身后。",
            defaultValue = "0")
    private static final String END_FORWARD = "end-forward";

    @SkillField(
            kind = FieldKind.DoubleValue,
            label = "End Right",
            labelZh = "终点·左右",
            tooltip = "[end-right] End offset along the target's right-hand direction, in blocks",
            tooltipZh = "终点沿目标右手方向的偏移，单位方块。按叉积得出的侧向轴偏移；受该轴朝向影响，正值实际偏向目标左侧。",
            defaultValue = "0")
    private static final String END_RIGHT = "end-right";

    @SkillField(
            kind = FieldKind.DoubleValue,
            label = "End Upward",
            labelZh = "终点·高度",
            tooltip = "[end-upward] End height above the target's feet, in blocks",
            tooltipZh = "终点相对目标脚部的高度，单位方块。该值直接作为垂直分量，不受朝向俯仰影响。",
            defaultValue = "0")
    private static final String END_UPWARD = "end-upward";

    @Override
    public String getKey() {
        return "particle point line";
    }

    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        double density = parseValues(caster, DENSITY, level, 10);
        if (!Double.isFinite(density) || density <= 0) return false;
        for (LivingEntity target : targets) {
            Location origin = target.getLocation();
            double yaw = Math.toRadians(origin.getYaw());
            Vector forward = new Vector(-Math.sin(yaw), 0, Math.cos(yaw));
            Vector right = forward.clone().crossProduct(new Vector(0, 1, 0));
            Vector start = offset("start", forward, right);
            Vector end = offset("end", forward, right);
            int steps = ParticleGeometry.samples(Math.max(1, start.distance(end) * density));
            if (steps == 0) return false;
            ParticleGeometry.line(origin, start, end, steps, true, settings);
        }
        return !targets.isEmpty();
    }

    /**
     * 把某一端的「前后 / 左右 / 高度」三个配置值合成相对偏移向量。
     *
     * <p>键名由 prefix 拼出（start-forward / end-right 等）。高度直接写入 Y 分量，
     * 不随目标俯仰倾斜，这样竖直线段不会因抬头低头而歪掉。</p>
     */
    private Vector offset(String prefix, Vector forward, Vector right) {
        return forward.clone().multiply(settings.getDouble(prefix + "-forward", 0))
                .add(right.clone().multiply(settings.getDouble(prefix + "-right", 0)))
                .setY(settings.getDouble(prefix + "-upward", 0));
    }
}
