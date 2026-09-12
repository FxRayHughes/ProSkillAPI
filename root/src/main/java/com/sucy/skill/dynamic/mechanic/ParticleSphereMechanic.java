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
 * 空心球体粒子
 * 以目标为中心，在球面上均匀分布粒子（中心不填充）
 */
@SkillNode(
        key = "particle sphere",
        name = "Particle Sphere",
        nameZh = "粒子球体",
        description = "Draws a hollow sphere of particles evenly distributed over the surface around each target.",
        descriptionZh = "以每个目标为球心画一个空心球壳：用黄金角螺旋（费波那契球）铺点，点在球面上近似等面积分布，球内不填充。"
                + "与 particle 机制的 Sphere 排布不同：那个是在半径内随机撒点（实心感），本节点是确定性的球面均匀描边，"
                + "同一配置每次画出的点位完全相同。"
                + "内部把 particles 固定为 1、radius 固定为 0，因此每个点只出一个粒子；"
                + "若额外配了 arrangement，仍会在每个点上按该排布重复铺一遍。"
                + "目标列表为空时返回 false。注意：粒子数量没有上限保护，填得过大会直接吃 tick。")
public class ParticleSphereMechanic extends MechanicComponent {

    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Radius",
            labelZh = "半径",
            tooltip = "[sphere-radius] Radius of the sphere in blocks",
            tooltipZh = "球壳半径，单位方块，随技能等级缩放。所有粒子都落在这个半径的球面上。",
            defaultValue = "2")
    private static final String RADIUS = "sphere-radius";

    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Particle Count",
            labelZh = "粒子数量",
            tooltip = "[particle-count] How many particles form the sphere surface",
            tooltipZh = "构成球面的粒子点数，随技能等级缩放。低于 4 时按 4 处理。"
                    + "半径越大需要越多点才能看出球形（点数不随半径自动增加）；"
                    + "此处没有上限保护，与目标数相乘后总量会很快失控。",
            defaultValue = "80")
    private static final String COUNT = "particle-count";

    @SkillField(
            kind = FieldKind.DoubleValue,
            label = "Height Offset",
            labelZh = "高度偏移",
            tooltip = "[height-offset] Vertical offset of the sphere center from the target's feet in blocks",
            tooltipZh = "球心相对目标脚部的垂直偏移，单位方块。默认 1 约等于把球心放在身体中部；"
                    + "填 0 时球心贴地、下半球会埋进地里。不随技能等级缩放。",
            defaultValue = "1")
    private static final String HEIGHT_OFFSET = "height-offset";

    @Override
    public String getKey() {
        return "particle sphere";
    }

    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        if (targets.isEmpty()) return false;

        double radius = parseValues(caster, RADIUS, level, 2.0);
        // 黄金角螺旋至少要 4 个点才成球面；上限走统一钳制，
        // 否则填个 100000 会直接卡住 tick。
        int particleCount = ParticleGeometry.samples(parseValues(caster, COUNT, level, 80));
        if (particleCount < 4) particleCount = 4;
        double heightOffset = settings.getDouble(HEIGHT_OFFSET, 1.0);

        Settings particleSettings = new Settings(settings);
        particleSettings.set("particles", 1, 0);
        particleSettings.set("radius", 0, 0);
        particleSettings.set("level", level);

        for (LivingEntity target : targets) {
            Location center = target.getLocation().add(0, heightOffset, 0);
            // 使用黄金角螺旋法均匀分布球面点
            double goldenAngle = Math.PI * (3.0 - Math.sqrt(5.0));
            for (int i = 0; i < particleCount; i++) {
                // y 从 -1 到 1 均匀分布
                double y = 1.0 - (2.0 * i / (particleCount - 1));
                double r = Math.sqrt(1.0 - y * y);
                double theta = goldenAngle * i;
                double x = r * Math.cos(theta);
                double z = r * Math.sin(theta);
                Location p = center.clone().add(x * radius, y * radius, z * radius);
                ParticleHelper.play(p, particleSettings);
            }
        }
        return true;
    }
}
