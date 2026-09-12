package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.api.util.ParticleHelper;
import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import java.util.List;

/** Draws an evenly sampled circumference using the legacy ring semantics. */
@SkillNode(
        key = "particle ring",
        name = "Particle Ring",
        nameZh = "粒子环",
        description = "Draws an evenly spaced ring of particles around each target.",
        descriptionZh = "以每个目标为圆心，在指定平面上画一圈等角分布的粒子。与 particle 机制的 Circle 排布不同："
                + "这里是等角均匀描边，点数固定为「粒子数量」，不做随机填充。"
                + "点数被限制在 1..16384 以保护 tick，超出则不绘制。")
public class ParticleRingMechanic extends MechanicComponent {

    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Radius",
            labelZh = "半径",
            tooltip = "[ring-radius] Radius of the ring in blocks",
            tooltipZh = "环的半径，单位方块。随技能等级缩放。",
            defaultValue = "1")
    private static final String RADIUS = "ring-radius";

    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Particle Count",
            labelZh = "粒子数量",
            tooltip = "[particle-count] How many particles form the ring",
            tooltipZh = "构成环的粒子点数，等角分布。随技能等级缩放；有效范围 1..16384，超出则整圈不绘制。",
            defaultValue = "32")
    private static final String COUNT = "particle-count";

    @SkillField(
            kind = FieldKind.ListValue,
            label = "Plane",
            labelZh = "平面",
            tooltip = "[plane] Which plane the ring lies in",
            tooltipZh = "环所在的平面。XZ 为水平圈（最常用），XY 与 YZ 为竖直圈。填其他值按 XZ 处理。",
            options = {"XZ", "XY", "YZ"},
            optionsZh = {"XZ 水平", "XY 竖直", "YZ 竖直"},
            defaultValue = "XZ")
    private static final String PLANE = "plane";

    @SkillField(
            kind = FieldKind.DoubleValue,
            label = "Rotation",
            labelZh = "起始旋转",
            tooltip = "[rotation] Starting angle offset in degrees",
            tooltipZh = "起始角偏移，单位度。用于让多层环错开，避免点位重叠。",
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

    @SkillField(
            kind = FieldKind.StringValue,
            label = "Particle",
            labelZh = "粒子类型",
            tooltip = "[particle] Particle type to render",
            tooltipZh = "使用的粒子类型名。填不存在的名称时该次绘制静默失效。",
            defaultValue = "invalid")
    private static final String PARTICLE = "particle";

    @Override
    public String getKey() {
        return "particle ring";
    }

    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        double radius = parseValues(caster, RADIUS, level, 1);
        int count = ParticleGeometry.samples(parseValues(caster, COUNT, level, 32));
        if (!Double.isFinite(radius) || radius < 0 || count == 0) return false;
        String plane = settings.getString(PLANE, "XZ");
        double rotation = Math.toRadians(settings.getDouble(ROTATION, 0));
        String particle = settings.getString(PARTICLE, "invalid");
        for (LivingEntity target : targets) {
            Location center = target.getLocation().add(0, settings.getDouble(HEIGHT_OFFSET, 0), 0);
            for (int i = 0; i < count; i++) {
                Location point = center.clone().add(ParticleGeometry.radial(plane,
                        radius, rotation + Math.PI * 2 * i / count));
                ParticleHelper.play(point, particle, settings);
            }
        }
        return !targets.isEmpty();
    }
}
