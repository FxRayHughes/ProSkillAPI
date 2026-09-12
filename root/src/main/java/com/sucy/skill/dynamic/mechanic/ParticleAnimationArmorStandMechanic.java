/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.ParticleAnimationMechanic
 * <p>
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2014 Steven Sucy
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software") to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.Settings;
import com.sucy.skill.api.event.ParticleAnimationExpireEvent;
import com.sucy.skill.api.event.ParticleAnimationLaunchEvent;
import com.sucy.skill.api.util.ParticleHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Plays a particle effect
 */
@SkillNode(
        key = "particle animation armor stand",
        name = "Particle Animation Armor Stand",
        nameZh = "粒子动画盔甲架",
        description = "Plays an animated particle effect at the location of each target over time by applying various transformations.",
        descriptionZh = "与「粒子动画」同一套轨迹算法，但在构造阶段就把整条轨迹的所有坐标点预先算好存进 step 列表，并抛出 ParticleAnimationLaunchEvent、结束时抛出 ParticleAnimationExpireEvent，供外部插件用盔甲架等实体来渲染这条轨迹。当 armor-stand 不为 none 时，ParticleHelper 会直接跳过粒子播放，也就是说本节点自身不再出粒子，完全依赖事件监听方生成盔甲架。目标列表为空时返回 false。注意运行期循环里把每个目标的朝向分量当成角度传给三角函数（构造期存的是 cos/sin 分量），朝向补偿因此并不准确，实际表现会与「粒子动画」有偏差。")
public class ParticleAnimationArmorStandMechanic extends MechanicComponent {
    @SkillField(
            kind = FieldKind.DoubleValue,
            label = "Forward Offset",
            labelZh = "前方偏移",
            tooltip = "[forward] How far forward in front of the target in blocks to play the particles. A negative value will go behind.",
            tooltipZh = "偏移向量的初始前向分量（格），负值向后。不随等级缩放。",
            defaultValue = "0")
    private static final String FORWARD = "forward";
    @SkillField(
            kind = FieldKind.DoubleValue,
            label = "Upward Offset",
            labelZh = "上方偏移",
            tooltip = "[upward] How far above the target in blocks to play the particles. A negative value will go below.",
            tooltipZh = "偏移向量的初始高度（格），负值向下；每帧被重置为 upward 加垂直位移量。不随等级缩放。",
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
            tooltipZh = "每次触发连续推进的帧数，默认 1，同时把总帧数放大 steps 倍。",
            defaultValue = "1")
    private static final String STEPS = "steps";
    @SkillField(
            kind = FieldKind.DoubleValue,
            label = "Frequency",
            labelZh = "频率",
            tooltip = "[frequency] How often to apply the animation in seconds. 0.05 is the fastest (1 tick). Lower than that will act the same.",
            tooltipZh = "任务触发间隔秒数，内部乘 20 转 tick。代码实际默认值为 1.0 秒，与标注的 0.05 不一致，需逐 tick 时必须显式写 0.05。",
            defaultValue = "0.05")
    private static final String FREQ = "frequency";
    @SkillField(
            kind = FieldKind.IntValue,
            label = "Angle",
            labelZh = "角度",
            tooltip = "[angle] How far the animation should rotate over the duration in degrees",
            tooltipZh = "整段动画累计旋转角度，默认 0；每帧旋转 angle/总帧数。",
            defaultValue = "0")
    private static final String ANGLE = "angle";
    @SkillField(
            kind = FieldKind.IntValue,
            label = "Start Angle",
            labelZh = "起始角度",
            tooltip = "[start] The starting orientation of the animation. Horizontal translations and the forward/right offsets will be based off of this.",
            tooltipZh = "起始朝向角度，前/右偏移与水平位移以此为基准先旋转一次。",
            defaultValue = "0")
    private static final String START = "start";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Duration",
            labelZh = "持续时间",
            tooltip = "[duration] How long the animation should last for in seconds",
            tooltipZh = "动画时长秒数，随等级缩放，默认 3 秒；乘 20 再乘 steps 得总帧数。")
    private static final String DURATION = "duration";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "H-Translation",
            labelZh = "水平平移",
            tooltip = "[h-translation] How far the animation moves horizontally relative to the center over a cycle. Positive values make it expand from the center while negative values make it contract.",
            tooltipZh = "一个半程内的水平移动距离，随等级缩放。正值外扩，负值内收。")
    private static final String H_TRANS = "h-translation";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "V-Translation",
            labelZh = "垂直平移",
            tooltip = "[v-translation] How far the animation moves vertically over a cycle. Positive values make it rise while negative values make it sink.",
            tooltipZh = "一个半程内的垂直移动距离，随等级缩放。正值上升，负值下沉。")
    private static final String V_TRANS = "v-translation";
    @SkillField(
            kind = FieldKind.IntValue,
            label = "H-Cycles",
            labelZh = "水平周期数",
            tooltip = "[h-cycles] How many times to move the animation position throughout the animation. Every other cycle moves it back to where it started. For example, two cycles would move it out and then back in.",
            tooltipZh = "水平位移的往返段数，默认 1，相邻段方向相反。",
            defaultValue = "1")
    private static final String H_CYCLES = "h-cycles";
    @SkillField(
            kind = FieldKind.IntValue,
            label = "V-Cycles",
            labelZh = "垂直周期数",
            tooltip = "[v-cycles] How many times to move the animation position throughout the animation. Every other cycle moves it back to where it started. For example, two cycles would move it up and then back down.",
            tooltipZh = "垂直位移的往返段数，默认 1，相邻段方向相反。",
            defaultValue = "1")
    private static final String V_CYCLES = "v-cycles";

    @SkillField(
            kind = FieldKind.StringValue,
            label = "ArmorStand",
            labelZh = "盔甲架",
            tooltip = "[armor-stand] 是否生成盔甲架代替粒子",
            tooltipZh = "填 none（默认）时正常播放粒子；填其他值时粒子被抑制，改由监听 ParticleAnimationLaunchEvent 的外部插件按预算轨迹生成盔甲架。",
            defaultValue = "none")
    private static final String ARMOR_STAND = "armor-stand";

    @Override
    public String getKey() {
        return "particle animation armor stand";
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
        copy.set(ParticleHelper.ARMOR_STAND, settings.getString(ARMOR_STAND, "none"));
        new ParticleTask(caster, targets, level, copy);
        return targets.size() > 0;
    }

    public class ParticleTask extends BukkitRunnable {

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

        public Location location;

        public Settings settings;

        public UUID uuid;

        public ArrayList<Location> step = new ArrayList<>();

        ParticleTask(LivingEntity caster, List<LivingEntity> targets, int level, Settings settings) {
            this.uuid = UUID.randomUUID();

            this.location = caster.getLocation();

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

            for (int i = 0; i < steps; i++) {
                // Play the effect
                int j = 0;
                for (LivingEntity target : targets) {
                    Location loc = target.getLocation();

                    rotate(offset, rots[j], rots[j + 1]);
                    loc.add(offset);
                    //ParticleHelper.play(loc, settings);
                    Location temp = loc.clone();
                    step.add(temp);
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
            this.dir = new Vector(1, 0, 0);
            this.offset = new Vector(forward, upward, right);
            this.life = 0;

            Bukkit.getPluginManager().callEvent(new ParticleAnimationLaunchEvent(this));
            SkillAPI.schedule(this, 0, freq);
        }

        @Override
        public void run() {
            for (int i = 0; i < steps; i++) {
                // Play the effect
                int j = 0;
                for (LivingEntity target : targets) {
                    Location loc = target.getLocation();

                    // Calculate the target rotation and add that
                    double targetAngle = loc.getYaw();
                    double targetCos;
                    double targetSin;
                    if (false) {
                        targetCos = Math.cos(Math.toRadians(targetAngle));
                        targetSin = Math.sin(Math.toRadians(targetAngle));
                        rotate(offset, targetCos, targetSin);

                        loc.add(offset);
                        ParticleHelper.play(loc, settings);
                        loc.subtract(offset);

                        targetCos = Math.cos(Math.toRadians(-targetAngle));
                        targetSin = Math.sin(Math.toRadians(-targetAngle));
                        rotate(offset, targetCos, targetSin);
                    } else {
                        rotate(offset, Math.cos(Math.toRadians(rots[j])), Math.sin(Math.toRadians(rots[j])));
                        loc.add(offset);
                        ParticleHelper.play(loc, settings);
                        loc.subtract(offset);

                        rotate(offset, Math.cos(Math.toRadians(-rots[j])), Math.sin(Math.toRadians(-rots[j])));
                        j += 1;
                    }
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
                Bukkit.getPluginManager().callEvent(new ParticleAnimationExpireEvent(this));
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
