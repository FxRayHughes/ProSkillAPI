package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.List;

/** Draws regular polygon edges; density is measured in samples per block of edge length. */
@SkillNode(
        key = "particle polygon",
        name = "Particle Polygon",
        nameZh = "粒子多边形",
        description = "Draws the edges of a regular polygon around each target.",
        descriptionZh = "以每个目标为中心，在指定平面上画一个正多边形的边框。半径是中心到顶点的距离（外接圆），"
                + "因此边中点到中心的距离为 半径×cos(π/边数)，比半径小。"
                + "每条边按「密度」等距采样，且总点数（每边点数×边数）被限制在 16384 以内，超出则不绘制。")
public class ParticlePolygonMechanic extends MechanicComponent {

    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Radius",
            labelZh = "半径",
            tooltip = "[polygon-radius] Distance from the center to each vertex, in blocks",
            tooltipZh = "中心到顶点的距离（外接圆半径），单位方块。随技能等级缩放。"
                    + "边中点会更靠近中心，距离为 半径×cos(π/边数)。",
            defaultValue = "1")
    private static final String RADIUS = "polygon-radius";

    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Sides",
            labelZh = "边数",
            tooltip = "[sides] Number of polygon sides",
            tooltipZh = "多边形的边数，随技能等级缩放。小于 3 时不绘制（无法构成多边形）。",
            defaultValue = "3")
    private static final String SIDES = "sides";

    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Density",
            labelZh = "密度",
            tooltip = "[density] Samples per block of edge length",
            tooltipZh = "每格边长采样多少个粒子点，随技能等级缩放。"
                    + "每边实际点数按边长×密度向上取整；总点数（每边点数×边数）超过 16384 时整个图形不绘制。",
            defaultValue = "10")
    private static final String DENSITY = "density";

    @SkillField(
            kind = FieldKind.ListValue,
            label = "Plane",
            labelZh = "平面",
            tooltip = "[plane] Which plane the polygon lies in",
            tooltipZh = "多边形所在的平面。XZ 为水平（最常用），XY 与 YZ 为竖直。填其他值按 XZ 处理。",
            options = {"XZ", "XY", "YZ"},
            optionsZh = {"XZ 水平", "XY 竖直", "YZ 竖直"},
            defaultValue = "XZ")
    private static final String PLANE = "plane";

    @SkillField(
            kind = FieldKind.DoubleValue,
            label = "Rotation",
            labelZh = "起始旋转",
            tooltip = "[rotation] Starting angle offset in degrees",
            tooltipZh = "起始角偏移，单位度。用于旋转整个多边形的朝向。",
            defaultValue = "0")
    private static final String ROTATION = "rotation";

    @SkillField(
            kind = FieldKind.DoubleValue,
            label = "Height Offset",
            labelZh = "高度偏移",
            tooltip = "[height-offset] Vertical offset from the target's feet in blocks",
            tooltipZh = "相对目标脚部的垂直偏移，单位方块。正值上移，负值下移。",
            defaultValue = "0")
    private static final String HEIGHT_OFFSET = "height-offset";

    @Override
    public String getKey() {
        return "particle polygon";
    }

    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        int sides = ParticleGeometry.samples(parseValues(caster, SIDES, level, 3));
        double radius = parseValues(caster, RADIUS, level, 1);
        double density = parseValues(caster, DENSITY, level, 10);
        if (sides < 3 || !Double.isFinite(radius) || radius <= 0 || density <= 0) return false;
        // Edge length of a regular polygon, used to keep sample spacing uniform.
        int steps = ParticleGeometry.samples(Math.max(1, 2 * radius * Math.sin(Math.PI / sides) * density));
        if (steps == 0 || (long) steps * sides > 16384) return false;

        String plane = settings.getString(PLANE, "XZ");
        double rotation = Math.toRadians(settings.getDouble(ROTATION, 0));
        double heightOffset = settings.getDouble(HEIGHT_OFFSET, 0);

        for (LivingEntity target : targets) {
            Location center = target.getLocation().add(0, heightOffset, 0);
            for (int i = 0; i < sides; i++) {
                Vector start = ParticleGeometry.radial(plane, radius, rotation + Math.PI * 2 * i / sides);
                Vector end = ParticleGeometry.radial(plane, radius, rotation + Math.PI * 2 * (i + 1) / sides);
                ParticleGeometry.line(center, start, end, steps, false, settings);
            }
        }
        return !targets.isEmpty();
    }
}
