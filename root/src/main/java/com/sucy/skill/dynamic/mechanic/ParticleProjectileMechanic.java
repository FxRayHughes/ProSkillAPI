/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.ParticleProjectileMechanic
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
import com.sucy.skill.api.particle.EffectPlayer;
import com.sucy.skill.api.particle.target.FollowTarget;
import com.sucy.skill.api.projectile.CustomProjectile;
import com.sucy.skill.api.projectile.ParticleProjectile;
import com.sucy.skill.api.projectile.ProjectileCallback;
import com.sucy.skill.api.util.ParticleHelper;
import com.sucy.skill.cast.CircleIndicator;
import com.sucy.skill.cast.CylinderIndicator;
import com.sucy.skill.cast.IIndicator;
import com.sucy.skill.cast.IndicatorType;
import com.sucy.skill.cast.ProjectileIndicator;
import com.sucy.skill.dynamic.TempEntity;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Heals each target
 */
@SkillNode(
        key = "particle projectile",
        name = "Particle Projectile",
        nameZh = "粒子投射物",
        description = "Launches a projectile using particles as its visual that applies child components upon landing. The target passed on will be the collided target or the location where it landed if it missed.",
        descriptionZh = "从每个目标身上发射用粒子做外观的自定义投射物（非原版实体），落地或命中后执行子节点。子节点收到的目标是被命中的实体；若什么都没打到，则用落点位置生成一个临时实体作为目标传下去。spread 为 rain 时从目标上方 height 处、rain-radius 范围内成片落下；否则按 dir 与 angle 生成锥形散射，发射点还会额外加上前/右偏移与 upward+0.5 的抬升。velocity、particles、radius 随等级缩放。开启 use-effect 后额外挂一个跟随投射物飞行的粒子特效（时长写死 9999 tick，随投射物消失而结束）。",
        container = true)
public class ParticleProjectileMechanic extends MechanicComponent implements ProjectileCallback {
    private static final Vector UP = new Vector(0, 1, 0);

    @SkillField(
            kind = FieldKind.DoubleValue,
            label = "Position",
            labelZh = "位置",
            tooltip = "[position] The height from the ground to start the projectile",
            tooltipZh = "已废弃：该键在代码中只声明未被读取，配置它不会产生任何效果。发射高度实际由 upward 加固定的 0.5 决定。",
            defaultValue = "0")
    private static final String POSITION = "position";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Angle",
            labelZh = "角度",
            tooltip = "[angle] The angle in degrees of the cone arc to spread projectiles over. If you are only firing one projectile, this does not matter.",
            tooltipZh = "锥形散射的张角（度），随等级缩放，默认 30。只发射 1 个投射物时无意义。")
    private static final String ANGLE = "angle";
    @SkillField(
            kind = FieldKind.DoubleValue,
            label = "Packet Amount",
            labelZh = "每点粒子数",
            tooltip = "[amount] Number of particles to play per point. For \"spell\" and \"effect\" particles, set to 0 to control the particle color.",
            tooltipZh = "实际控制的是发射的投射物数量（默认 1），并非标注所说的每点粒子数；随等级缩放。rain 与锥形散射都用它决定生成个数。",
            defaultValue = "1")
    private static final String AMOUNT = "amount";
    private static final String LEVEL = "skill_level";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Height",
            labelZh = "高度",
            tooltip = "[height] The distance in blocks over the target to rain the projectiles from",
            tooltipZh = "spread 为 rain 时的降落起始高度（目标上方格数），随等级缩放，默认 8。")
    private static final String HEIGHT = "height";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Radius",
            labelZh = "半径",
            tooltip = "[rain-radius] The radius of the rain emission area in blocks",
            tooltipZh = "spread 为 rain 时的降落散布半径（格），随等级缩放，默认 2。")
    private static final String RADIUS = "rain-radius";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Spread",
            labelZh = "散布",
            tooltip = "[spread] The orientation for firing projectiles. Cone will fire arrows in a cone centered on your reticle. Horizontal cone does the same as cone, just locked to the XZ axis (parallel to the ground). Rain drops the projectiles from above the target. For firing one arrow straight, use \"Cone\"",
            tooltipZh = "发射形态。Cone 按准星方向成锥形散射（只发一个时即为直线）；Horizontal Cone 把方向压平到水平面后再散射；Rain 改为从目标上方成片落下。",
            options = {"Cone", "Horizontal Cone", "Rain"},
            optionsZh = {"可选值1", "可选值2", "可选值3"},
            defaultValue = "Cone")
    private static final String SPREAD = "spread";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Group",
            labelZh = "目标阵营",
            tooltip = "[group] The alignment of targets to hit",
            tooltipZh = "投射物可命中的阵营，Enemy（默认）或 Ally；内部换算成 allyEnemy 两个开关写入投射物。",
            options = {"Ally", "Enemy"},
            optionsZh = {"友方", "敌方"},
            defaultValue = "Enemy")
    private static final String ALLY = "group";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Right Offset",
            labelZh = "右方偏移",
            tooltip = "[right] How far to the right of the target the projectile should fire from. A negative value will put it to the left.",
            tooltipZh = "发射点相对目标水平朝向的右向偏移（格），负值向左，随等级缩放。rain 形态下不生效。")
    private static final String RIGHT = "right";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Upward Offset",
            labelZh = "上方偏移",
            tooltip = "[upward] How far above the target the projectile should fire from in blocks. A negative value will put it below.",
            tooltipZh = "发射点的抬升（格），代码会再额外加 0.5，负值向下，随等级缩放。rain 形态下不生效。")
    private static final String UPWARD = "upward";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Forward Offset",
            labelZh = "前方偏移",
            tooltip = "[forward] How far forward in front of the target the projectile should fire from in blocks. A negative value will put it behind.",
            tooltipZh = "发射点相对目标水平朝向的前向偏移（格），负值向后，随等级缩放。rain 形态下不生效。")
    private static final String FORWARD = "forward";

    @SkillField(
            kind = FieldKind.ListValue,
            label = "Use Effect",
            labelZh = "启用特效",
            tooltip = "[use-effect] Whether or not to use the premium particle effects.",
            tooltipZh = "是否额外挂一个跟随投射物的粒子特效（EffectPlayer），默认 False。开启后特效以 effect-key 为标识。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "False")
    private static final String USE_EFFECT = "use-effect";
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Effect Key",
            labelZh = "特效引用键",
            tooltip = "[effect-key] The key to refer to the effect by. Only one effect of each key can be active at a time.",
            tooltipZh = "跟随特效的引用键，默认取技能名；同一个键同时只能存在一个特效实例。",
            defaultValue = "default")
    private static final String EFFECT_KEY = "effect-key";

    @SkillField(
            kind = FieldKind.StringValue,
            label = "ArmorStand",
            labelZh = "盔甲架",
            tooltip = "[armor-stand] 是否生成盔甲架代替粒子",
            tooltipZh = "填 none（默认）以外的值时，ParticleHelper 会跳过粒子播放，改由外部插件用盔甲架渲染投射物。",
            defaultValue = "none")
    private static final String ARMOR_STAND = "armor-stand";

    /**
     * Creates the list of indicators for the skill
     *
     * @param list    list to store indicators in
     * @param caster  caster reference
     * @param targets location to base location on
     * @param level   the level of the skill to create for
     */
    @Override
    public void makeIndicators(List<IIndicator> list, Player caster, List<LivingEntity> targets, int level) {
        targets.forEach(target -> {
            // Get common values
            int amount = (int) parseValues(caster, AMOUNT, level, 1.0);
            double speed = parseValues(caster, "velocity", level, 1);
            String spread = settings.getString(SPREAD, "cone").toLowerCase();

            // Apply the spread type
            if (spread.equals("rain")) {
                double radius = parseValues(caster, RADIUS, level, 2.0);

                if (indicatorType == IndicatorType.DIM_2) {
                    IIndicator indicator = new CircleIndicator(radius);
                    indicator.moveTo(target.getLocation().add(0, 0.1, 0));
                    list.add(indicator);
                } else {
                    double height = parseValues(caster, HEIGHT, level, 8.0);
                    IIndicator indicator = new CylinderIndicator(radius, height);
                    indicator.moveTo(target.getLocation());
                    list.add(indicator);
                }
            } else {
                Vector dir = target.getLocation().getDirection();
                if (spread.equals("horizontal cone")) {
                    dir.setY(0);
                    dir.normalize();
                }
                double angle = parseValues(caster, ANGLE, level, 30.0);
                ArrayList<Vector> dirs = CustomProjectile.calcSpread(dir, angle, amount);
                Location loc = caster.getLocation().add(0, caster.getEyeHeight(), 0);
                for (Vector d : dirs) {
                    ProjectileIndicator indicator = new ProjectileIndicator(speed, 0);
                    indicator.setDirection(d);
                    indicator.moveTo(loc);
                    list.add(indicator);
                }
            }
        });
    }

    @Override
    public String getKey() {
        return "particle projectile";
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
        // Get common values
        int amount = (int) parseValues(caster, AMOUNT, level, 1.0);
        String spread = settings.getString(SPREAD, "cone").toLowerCase();
        boolean ally = settings.getString(ALLY, "enemy").toLowerCase().equals("ally");
        settings.set("level", level);

        final Settings copy = new Settings(settings);
        copy.set(ParticleProjectile.SPEED, parseValues(caster, ParticleProjectile.SPEED, level, 1), 0);
        copy.set(ParticleHelper.PARTICLES_KEY, parseValues(caster, ParticleHelper.PARTICLES_KEY, level, 1), 0);
        copy.set(ParticleHelper.RADIUS_KEY, parseValues(caster, ParticleHelper.RADIUS_KEY, level, 0), 0);
        copy.set(ParticleHelper.ARMOR_STAND, settings.getString(ARMOR_STAND, "none"));

        // Fire from each target
        for (LivingEntity target : targets) {
            Location loc = target.getLocation();

            // Apply the spread type
            ArrayList<ParticleProjectile> list;
            if (spread.equals("rain")) {
                double radius = parseValues(caster, RADIUS, level, 2.0);
                double height = parseValues(caster, HEIGHT, level, 8.0);
                list = ParticleProjectile.rain(caster, level, loc, copy, radius, height, amount, this);
            } else {
                Vector dir = target.getLocation().getDirection();

                double right = parseValues(caster, RIGHT, level, 0);
                double upward = parseValues(caster, UPWARD, level, 0);
                double forward = parseValues(caster, FORWARD, level, 0);

                Vector looking = dir.clone().setY(0).normalize();
                Vector normal = looking.clone().crossProduct(UP);
                looking.multiply(forward).add(normal.multiply(right));

                if (spread.equals("horizontal cone")) {
                    dir.setY(0);
                    dir.normalize();
                }
                double angle = parseValues(caster, ANGLE, level, 30.0);
                list = ParticleProjectile.spread(
                        caster,
                        level,
                        dir,
                        loc.add(looking).add(0, upward + 0.5, 0),
                        copy,
                        angle,
                        amount,
                        this
                );
            }

            // Set metadata for when the callback happens
            for (ParticleProjectile p : list) {
                SkillAPI.setMeta(p, LEVEL, level);
                p.setAllyEnemy(ally, !ally);
            }

            if (settings.getBool(USE_EFFECT, false)) {
                EffectPlayer player = new EffectPlayer(settings);
                for (CustomProjectile p : list) {
                    player.start(
                            new FollowTarget(p),
                            settings.getString(EFFECT_KEY, skill.getName()),
                            9999,
                            level,
                            true);
                }
            }
        }

        return targets.size() > 0;
    }

    /**
     * The callback for the projectiles that applies child components
     *
     * @param projectile projectile calling back for
     * @param hit        the entity hit by the projectile, if any
     */
    @Override
    public void callback(CustomProjectile projectile, LivingEntity hit) {
        if (hit == null) {
            hit = TempEntity.create(projectile.getLocation());
        }
        ArrayList<LivingEntity> targets = new ArrayList<>();
        targets.add(hit);
        executeChildren(projectile.getShooter(), SkillAPI.getMetaInt(projectile, LEVEL), targets);
    }
}
