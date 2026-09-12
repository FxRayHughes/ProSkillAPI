package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.api.Settings;
import com.sucy.skill.api.util.ParticleHelper;
import com.sucy.skill.dynamic.meta.FieldKind;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.SkillNode;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 粒子链机制 - 按距离排序在施法者和目标之间画链式连线
 */
@SkillNode(
        key = "particle chain",
        name = "Particle Chain",
        nameZh = "粒子链",
        description = "Draws a chain of particle lines from the caster through every target, nearest first.",
        descriptionZh = "把目标按到施法者的距离由近到远排序，然后依次串起来：施法者→最近目标→次近目标→……，形成一条折线链。"
                + "与 particle line 的区别：那个是从施法者分别放射到每个目标（星形），本节点是首尾相接的一条链（闪电链效果）。"
                + "每段折线的采样点数按 该段长度×密度 取整，至少 2 段；相邻两段在拐点处会各画一个点，因此拐点粒子会重叠。"
                + "所有端点都抬高「高度偏移」格。内部把 particles 固定为 1、radius 固定为 0，每个采样点只出一个粒子；"
                + "若额外配了 arrangement，仍会在每个采样点上按该排布重复铺一遍。"
                + "目标列表为空时返回 false。目标与施法者不在同一世界时会抛异常。")
public class ParticleChainMechanic extends MechanicComponent {

    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Density",
            labelZh = "密度",
            tooltip = "[density] Samples per block of chain segment length",
            tooltipZh = "每格链段长度采样多少个粒子点，随技能等级缩放。每段点数为 段长×密度 取整后再加 1，"
                    + "结果不足 2 时按 2 段处理；此处没有上限保护，目标多且密度大时点数会累加得很快。",
            defaultValue = "4")
    private static final String DENSITY = "density";

    @SkillField(
            kind = FieldKind.DoubleValue,
            label = "Height Offset",
            labelZh = "高度偏移",
            tooltip = "[height-offset] Vertical offset from the feet of every chain node in blocks",
            tooltipZh = "链上每个端点（施法者与各目标）相对脚部抬高的格数，正值上移、负值下移。"
                    + "默认 1 约等于胸口高度，填 0 会贴地。不随技能等级缩放；"
                    + "该偏移对排序无影响，排序用的是脚部原始位置。",
            defaultValue = "1")
    private static final String HEIGHT = "height-offset";

    @Override
    public String getKey() {
        return "particle chain";
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

        // 按距离施法者的远近排序目标
        Location casterLoc = caster.getLocation().add(0, heightOffset, 0);
        List<LivingEntity> sorted = new ArrayList<>(targets);
        sorted.sort(Comparator.comparingDouble(
                (LivingEntity e) -> e.getLocation().distanceSquared(caster.getLocation())
        ));

        // 从施法者开始，连线到第一个目标，再到第二个...
        Location prev = casterLoc.clone();
        for (LivingEntity target : sorted) {
            Location targetLoc = target.getLocation().add(0, heightOffset, 0);
            // 汇总实际绘制结果：跨世界或距离为 0 的目标被跳过，
            // 全部目标都画不出来时返回 false，让技能树能据此走 else 分支。
            if (drawLine(prev, targetLoc, density, particleSettings)) {
                drawn = true;
            }
            prev = targetLoc;
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
