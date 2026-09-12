package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.Settings;
import com.sucy.skill.api.util.ParticleHelper;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

@SkillNode(
        key = "particle animation",
        name = "Particle Animation",
        nameZh = "粒子动画",
        description = "Plays an animated particle effect at the location of each target over time by applying various transformations.",
        descriptionZh = "为每个目标启动一个定时任务，随时间对一个偏移向量反复做旋转与平移，每帧在目标当前位置加上该偏移播放粒子，从而画出旋转、扩张/收缩、升降的动画轨迹。每次任务触发会连续推进 steps 帧，因此实际帧数为 steps × duration × 20；只有 frequency 取 0.05（1 tick）时现实耗时才等于 duration 秒，frequency 越大整段动画拖得越长。水平/垂直位移按三角波往返（radAt/heightAt），走完一个来回算两个半程。particles 与 radius 随等级缩放。目标列表为空时返回 false。")
public class ParticleAnimationMechanic extends MechanicComponent {
    @SkillField(
            kind = FieldKind.DoubleValue,
            label = "Forward Offset",
            labelZh = "前方偏移",
            tooltip = "[forward] How far forward in front of the target in blocks to play the particles. A negative value will go behind.",
            tooltipZh = "偏移向量的初始前向分量（格），负值向后。会被 start 起始角与目标朝向一起旋转。不随等级缩放。",
            defaultValue = "0")
    private static final String FORWARD = "forward";
    @SkillField(
            kind = FieldKind.DoubleValue,
            label = "Upward Offset",
            labelZh = "上方偏移",
            tooltip = "[upward] How far above the target in blocks to play the particles. A negative value will go below.",
            tooltipZh = "偏移向量的初始高度（格），负值向下；每帧都会被重置为 upward 加上垂直位移量。不随等级缩放。",
            defaultValue = "0")
    private static final String UPWARD = "upward";
    @SkillField(
            kind = FieldKind.DoubleValue,
            label = "Right Offset",
            labelZh = "右方偏移",
            tooltip = "[right] How far to the right of the target to play the particles. A negative value will go to the left.",
            tooltipZh = "偏移向量的初始右向分量（格），负值向左。不随等级缩放。",
            defaultValue = "0")
    private static final String RIGHT = "right";
    @SkillField(
            kind = FieldKind.IntValue,
            label = "Steps",
            labelZh = "步数",
            tooltip = "[steps] The number of times to play particles and apply translations each application.",
            tooltipZh = "每次任务触发连续推进的帧数，默认 1。它同时把总帧数放大 steps 倍，调大会让动画更密集而不是更快结束。",
            defaultValue = "1")
    private static final String STEPS = "steps";
    @SkillField(
            kind = FieldKind.DoubleValue,
            label = "Frequency",
            labelZh = "频率",
            tooltip = "[frequency] How often to apply the animation in seconds. 0.05 is the fastest (1 tick). Lower than that will act the same.",
            tooltipZh = "任务触发间隔秒数，内部乘 20 取整为 tick。注意代码读取时的默认值是 1.0 秒（即 20 tick），与标注的 0.05 不一致；要达到逐 tick 播放必须显式写 0.05。",
            defaultValue = "0.05")
    private static final String FREQ = "frequency";
    @SkillField(
            kind = FieldKind.IntValue,
            label = "Angle",
            labelZh = "角度",
            tooltip = "[angle] How far the animation should rotate over the duration in degrees",
            tooltipZh = "整段动画累计旋转的角度数，默认 0；每帧旋转量为 angle/总帧数，正负决定旋向。",
            defaultValue = "0")
    private static final String ANGLE = "angle";
    @SkillField(
            kind = FieldKind.IntValue,
            label = "Start Angle",
            labelZh = "起始角度",
            tooltip = "[start] The starting orientation of the animation. Horizontal translations and the forward/right offsets will be based off of this.",
            tooltipZh = "动画的起始朝向角度，前/右偏移与水平位移都以此为基准先旋转一次。",
            defaultValue = "0")
    private static final String START = "start";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Duration",
            labelZh = "持续时间",
            tooltip = "[duration] How long the animation should last for in seconds",
            tooltipZh = "动画时长秒数，随技能等级缩放，默认 3 秒；乘 20 再乘 steps 得到总帧数。")
    private static final String DURATION = "duration";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "H-Translation",
            labelZh = "水平平移",
            tooltip = "[h-translation] How far the animation moves horizontally relative to the center over a cycle. Positive values make it expand from the center while negative values make it contract.",
            tooltipZh = "一个半程内偏移点相对中心的水平移动距离，随等级缩放。正值向外扩张，负值向内收缩。")
    private static final String H_TRANS = "h-translation";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "V-Translation",
            labelZh = "垂直平移",
            tooltip = "[v-translation] How far the animation moves vertically over a cycle. Positive values make it rise while negative values make it sink.",
            tooltipZh = "一个半程内偏移点的垂直移动距离，随等级缩放。正值上升，负值下沉。")
    private static final String V_TRANS = "v-translation";
    @SkillField(
            kind = FieldKind.IntValue,
            label = "H-Cycles",
            labelZh = "水平周期数",
            tooltip = "[h-cycles] How many times to move the animation position throughout the animation. Every other cycle moves it back to where it started. For example, two cycles would move it out and then back in.",
            tooltipZh = "水平位移在整段动画中的往返段数，默认 1。每段与上一段方向相反，例如填 2 表示先扩张再收回。",
            defaultValue = "1")
    private static final String H_CYCLES = "h-cycles";
    @SkillField(
            kind = FieldKind.IntValue,
            label = "V-Cycles",
            labelZh = "垂直周期数",
            tooltip = "[v-cycles] How many times to move the animation position throughout the animation. Every other cycle moves it back to where it started. For example, two cycles would move it up and then back down.",
            tooltipZh = "垂直位移在整段动画中的往返段数，默认 1。填 2 表示先升起再落回。",
            defaultValue = "1")
    private static final String V_CYCLES = "v-cycles";

    @Override
    public String getKey() {
        return "particle animation";
    }

    /**
     * Executes the component
     *
     * @param caster  caster of the skill
     * @param level   level of the skill
     * @param targets targets to apply to
     * @return true if applied to something, false otherwise
     */
    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        if (targets.size() == 0) {
            return false;
        }

        final Settings copy = new Settings(settings);
        copy.set(ParticleHelper.PARTICLES_KEY, parseValues(caster, ParticleHelper.PARTICLES_KEY, level, 1), 0);
        copy.set(ParticleHelper.RADIUS_KEY, parseValues(caster, ParticleHelper.RADIUS_KEY, level, 0), 0);
        copy.set("level", level);
        new ParticleTask(caster, targets, level, copy);
        return targets.size() > 0;
    }

    private class ParticleTask extends BukkitRunnable {

        private List<LivingEntity> targets;
        private double[] rots;
        private Vector offset;
        private Vector dir;

        private double forward;
        private double right;
        private double upward;

        private int steps;
        private int freq;
        private int angle;
        private int startAngle;
        private int duration;
        private int life;
        private int hc;
        private int vc;
        private int hl;
        private int vl;
        private double ht;
        private double vt;
        private double cos;
        private double sin;

        private Settings settings;

        ParticleTask(LivingEntity caster, List<LivingEntity> targets, int level, Settings settings) {
            this.targets = targets;
            this.settings = settings;

            this.forward = settings.getDouble(FORWARD, 0);
            this.upward = settings.getDouble(UPWARD, 0);
            this.right = settings.getDouble(RIGHT, 0);

            this.steps = settings.getInt(STEPS, 1);
            this.freq = (int) (settings.getDouble(FREQ, 1.0) * 20);
            this.angle = settings.getInt(ANGLE, 0);
            this.startAngle = settings.getInt(START, 0);
            this.duration = steps * (int) (20 * parseValues(caster, DURATION, level, 3.0));
            this.life = 0;
            this.ht = parseValues(caster, H_TRANS, level, 0);
            this.vt = parseValues(caster, V_TRANS, level, 0);
            this.hc = settings.getInt(H_CYCLES, 1);
            this.vc = settings.getInt(V_CYCLES, 1);
            this.hl = duration / hc;
            this.vl = duration / vc;

            this.cos = Math.cos(angle * Math.PI / (180 * duration));
            this.sin = Math.sin(angle * Math.PI / (180 * duration));

            rots = new double[targets.size() * 2];
            for (int i = 0; i < targets.size(); i++) {
                Vector dir = targets.get(i).getLocation().getDirection().setY(0).normalize();
                rots[i * 2] = dir.getX();
                rots[i * 2 + 1] = dir.getZ();
            }
            this.dir = new Vector(1, 0, 0);
            this.offset = new Vector(forward, upward, right);

            double sc = Math.cos(startAngle * Math.PI / 180);
            double ss = Math.sin(startAngle * Math.PI / 180);
            rotate(offset, sc, ss);
            rotate(dir, sc, ss);

            SkillAPI.schedule(this, 0, freq);
        }

        @Override
        public void run() {
            for (int i = 0; i < steps; i++) {
                // Play the effect
                int j = 0;
                for (LivingEntity target : targets) {
                    Location loc = target.getLocation();

                    rotate(offset, rots[j], rots[j + 1]);
                    loc.add(offset);
                    ParticleHelper.play(loc, settings);
                    loc.subtract(offset);
                    rotate(offset, rots[j++], -rots[j++]);
                }

                // Update the lifespan of the animation
                this.life++;

                // Apply transformations
                rotate(offset, cos, sin);
                rotate(dir, cos, sin);

                double dx = radAt(this.life) - radAt(this.life - 1);
                offset.setX(offset.getX() + dx * dir.getX());
                offset.setZ(offset.getZ() + dx * dir.getZ());
                offset.setY(upward + heightAt(this.life));
            }

            if (this.life >= this.duration) {
                cancel();
            }
        }

        private double heightAt(int step) {
            return vt * (vl - Math.abs(vl - step % (2 * vl))) / vl;
        }

        private double radAt(int step) {
            return ht * (hl - Math.abs(hl - step % (2 * hl))) / hl;
        }

        private void rotate(Vector vec, double cos, double sin) {
            double x = vec.getX() * cos - vec.getZ() * sin;
            vec.setZ(vec.getX() * sin + vec.getZ() * cos);
            vec.setX(x);
        }
    }
}