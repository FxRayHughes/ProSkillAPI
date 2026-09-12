package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.api.Settings;
import com.sucy.skill.api.util.ParticleHelper;
import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * 正弦波粒子线
 * 从施法者到每个目标画一条正弦波形粒子线
 */
@SkillNode(
        key = "particle sine wave",
        name = "Particle Sine Wave",
        nameZh = "粒子正弦波",
        description = "Draws a sine wave of particles from the origin to each target.",
        descriptionZh = "从起点到每个目标画一条正弦波形的粒子线：先沿两点连线均匀取点，再让每个点沿垂直于连线的方向按正弦偏移。"
                + "采样点数为 距离×密度 取整，至少 2 段（含首尾故实为 点数+1 个粒子）。"
                + "波形完整周期数为 距离÷波长，所以拉远后波形会变密而不是被拉长。"
                + "内部把 particles 固定为 1、radius 固定为 0；若额外配了 arrangement，仍会在每个采样点上按该排布重复铺一遍。"
                + "点数经统一钳制（上限 16384），密度填过大不会卡住服务器。"
                + "波长填 0 视作不起振、按直线绘制；起点与目标重合或跨世界时该目标跳过，"
                + "全部目标都画不出来才返回 false。")
public class ParticleSineWaveMechanic extends MechanicComponent {

    private static final Vector UP = new Vector(0, 1, 0);

    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Amplitude",
            labelZh = "振幅",
            tooltip = "[amplitude] Peak offset of the wave from the center line in blocks",
            tooltipZh = "波峰离中线的最大偏移，单位方块，随技能等级缩放。0 等于画直线，"
                    + "负值只是把波形上下（或左右）翻转，观感与正值相同。",
            defaultValue = "1")
    private static final String AMPLITUDE = "amplitude";

    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Wavelength",
            labelZh = "波长",
            tooltip = "[wavelength] Length of one full wave cycle in blocks",
            tooltipZh = "一个完整波形占多少格，单位方块，随技能等级缩放。值越小波越密。"
                    + "周期数按 总距离÷波长 计算，因此同一配置在不同距离下波形密度一致。"
                    + "填 0 视作不起振，按直线绘制而非失效。",
            defaultValue = "3")
    private static final String WAVELENGTH = "wavelength";

    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Density",
            labelZh = "密度",
            tooltip = "[density] Samples per block along the wave",
            tooltipZh = "沿主轴每格采样多少个粒子点，随技能等级缩放。密度过低（相对波长）会让波形看起来断裂或走形，"
                    + "建议每个波长至少有 8 个点。总点数上限 16384，超出会被钳制。",
            defaultValue = "4")
    private static final String DENSITY = "density";

    @SkillField(
            kind = FieldKind.DoubleValue,
            label = "Phase",
            labelZh = "相位",
            tooltip = "[phase] Starting phase of the wave in degrees",
            tooltipZh = "波形起始相位，单位度。用于让多条波线错开（例如两条相差 180 度可组成双螺旋）。"
                    + "0 时起点正好在中线上。不随技能等级缩放。",
            defaultValue = "0")
    private static final String PHASE = "phase";

    @SkillField(
            kind = FieldKind.DoubleValue,
            label = "Height Offset",
            labelZh = "高度偏移",
            tooltip = "[height-offset] Vertical offset from the feet of both endpoints in blocks",
            tooltipZh = "起点与终点同时相对脚部抬高的格数，正值上移、负值下移。默认 1 约等于胸口高度。不随技能等级缩放。",
            defaultValue = "1")
    private static final String HEIGHT_OFFSET = "height-offset";

    @SkillField(
            kind = FieldKind.DoubleValue,
            label = "Forward Offset",
            labelZh = "前方偏移",
            tooltip = "[forward] Start offset along the origin's horizontal facing in blocks",
            tooltipZh = "起点沿视线水平前方的偏移格数，负值向后。只影响起点，终点始终是目标本体。"
                    + "常用来把波形起点挪到身前（例如手部位置）。不随技能等级缩放。",
            defaultValue = "0")
    private static final String FORWARD = "forward";

    @SkillField(
            kind = FieldKind.DoubleValue,
            label = "Right Offset",
            labelZh = "右方偏移",
            tooltip = "[right] Start offset along the origin's side axis in blocks",
            tooltipZh = "起点沿侧向轴的偏移格数。该轴由朝向与竖直向上做叉积得出，正值实际偏向左侧。"
                    + "只影响起点。不随技能等级缩放。",
            defaultValue = "0")
    private static final String RIGHT = "right";

    @SkillField(
            kind = FieldKind.ListValue,
            label = "Wave Axis",
            labelZh = "波动轴",
            tooltip = "[wave-axis] Which perpendicular axis the wave oscillates along",
            tooltipZh = "波形沿哪个垂直轴摆动。horizontal 为左右摆（贴地看是蛇形），vertical 为上下摆（侧面看是波浪）。"
                    + "填其他值按 horizontal 处理。主轴接近竖直（|Y|>0.99）时水平参考轴退化为世界 X 轴。",
            options = {"horizontal", "vertical"},
            optionsZh = {"水平摆动", "竖直摆动"},
            defaultValue = "horizontal")
    private static final String WAVE_AXIS = "wave-axis";

    @SkillField(
            kind = FieldKind.ListValue,
            label = "Origin",
            labelZh = "起点来源",
            tooltip = "[origin] Whether the wave starts at the caster or at the target",
            tooltipZh = "波线起点取施法者还是目标自身。caster 为从施法者射向目标；"
                    + "target 为起点终点都在目标身上，配合前方 / 右方 / 起点抬高偏移做出目标自身的短波线。"
                    + "填其他值按 caster 处理。视线接近正上 / 正下时朝向退化，此时按世界南向兜底。",
            options = {"caster", "target"},
            optionsZh = {"施法者", "目标"},
            defaultValue = "caster")
    private static final String ORIGIN = "origin";

    @SkillField(
            kind = FieldKind.DoubleValue,
            label = "Start Up",
            labelZh = "起点额外抬高",
            tooltip = "[start-up] Extra height added to the start point, target origin only",
            tooltipZh = "在「高度偏移」之上再给起点加的高度，单位方块。仅在起点来源为 target 时生效，"
                    + "两种起点来源都生效。不随技能等级缩放。",
            defaultValue = "0")
    private static final String START_UP = "start-up";

    @Override
    public String getKey() {
        return "particle sine wave";
    }

    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        if (targets.isEmpty()) return false;

        double amplitude = parseValues(caster, AMPLITUDE, level, 1.0);
        double wavelength = parseValues(caster, WAVELENGTH, level, 3.0);
        double density = parseValues(caster, DENSITY, level, 4.0);
        double phase = Math.toRadians(settings.getDouble(PHASE, 0));
        double heightOffset = settings.getDouble(HEIGHT_OFFSET, 1.0);
        double forward = settings.getDouble(FORWARD, 0);
        double right = settings.getDouble(RIGHT, 0);
        String axis = settings.getString(WAVE_AXIS, "horizontal").toLowerCase();
        String origin = settings.getString(ORIGIN, "caster").toLowerCase();
        double startUp = settings.getDouble(START_UP, 0);

        boolean drawn = false;

        Settings particleSettings = new Settings(settings);
        particleSettings.set("particles", 1, 0);
        particleSettings.set("radius", 0, 0);
        particleSettings.set("level", level);

        for (LivingEntity target : targets) {
            Location startLoc;
            if (origin.equals("target")) {
                startLoc = target.getLocation();
                Vector dir = ParticleGeometry.normalizeOrDefault(startLoc.getDirection().setY(0));
                Vector rightDir = dir.clone().crossProduct(UP);
                startLoc.add(dir.multiply(forward)).add(0, heightOffset + startUp, 0).add(rightDir.multiply(right));
            } else {
                startLoc = caster.getLocation();
                // 正上/正下看时压平后是零长向量，直接 normalize 会得 NaN 让整条波线消失。
                Vector dir = ParticleGeometry.normalizeOrDefault(startLoc.getDirection().setY(0));
                Vector rightDir = dir.clone().crossProduct(UP);
                // start-up 对两种起点都生效，避免只有 target 模式能抬高。
                startLoc.add(dir.multiply(forward)).add(0, heightOffset + startUp, 0)
                        .add(rightDir.multiply(right));
            }

            Location targetLoc = target.getLocation().add(0, heightOffset, 0);
            // 汇总实际绘制结果：全部目标都画不出来（跨世界、距离为 0）时返回 false，
            // 让技能树能据此走 else 分支，而不是假装成功。
            if (drawSineWave(startLoc, targetLoc, amplitude, wavelength, phase,
                    density, axis, particleSettings)) {
                drawn = true;
            }
        }
        return drawn;
    }

    /**
     * 在两点之间画一条正弦波。
     *
     * @return 是否真的画了；跨世界、距离为 0 或点数超上限时返回 false
     */
    private boolean drawSineWave(Location from, Location to, double amplitude,
            double wavelength, double phase, double density, String axis, Settings settings) {

        // 跨世界时 Location.distance 会抛异常；技能可能在传送途中结算，不能让它崩掉。
        double distance = ParticleGeometry.safeDistance(from, to);
        if (distance <= 0) {
            return false;
        }
        // 点数走统一钳制（1..16384），避免密度填大后卡住 tick。
        int points = ParticleGeometry.samples(distance * density);
        if (points < 2) {
            points = 2;
        }

        // 主轴方向（从起点到终点）。两点重合已被 distance <= 0 挡掉，这里仍走安全归一化。
        Vector dir = ParticleGeometry.normalizeOrDefault(to.toVector().subtract(from.toVector()));
        // 计算垂直于主轴的两个方向
        Vector perpH; // 水平垂直方向
        Vector perpV; // 垂直方向（始终向上）

        if (Math.abs(dir.getY()) > 0.99) {
            // 如果主轴几乎竖直，用 X 轴作为水平参考
            perpH = new Vector(1, 0, 0);
        } else {
            perpH = ParticleGeometry.normalizeOrDefault(dir.clone().crossProduct(UP));
        }
        perpV = ParticleGeometry.normalizeOrDefault(dir.clone().crossProduct(perpH));

        // 波长为 0 会让 cycles 变成无穷、sin 得 NaN，整条波线不可见。
        // 视作"不起振"：按直线画，而不是静默失效。
        double cycles = (Double.isFinite(wavelength) && wavelength > 0) ? distance / wavelength : 0;

        for (int i = 0; i <= points; i++) {
            double t = (double) i / points;
            double angle = t * Math.PI * 2 * cycles + phase;
            double offset = amplitude * Math.sin(angle);

            // 沿主轴的基础位置
            double x = from.getX() + (to.getX() - from.getX()) * t;
            double y = from.getY() + (to.getY() - from.getY()) * t;
            double z = from.getZ() + (to.getZ() - from.getZ()) * t;

            // 根据波动轴施加偏移
            Vector perp;
            if (axis.equals("vertical")) {
                perp = perpV;
            } else {
                perp = perpH;
            }

            Location point = new Location(from.getWorld(),
                    x + perp.getX() * offset,
                    y + perp.getY() * offset,
                    z + perp.getZ() * offset);
            ParticleHelper.play(point, settings);
        }
        return true;
    }
}
