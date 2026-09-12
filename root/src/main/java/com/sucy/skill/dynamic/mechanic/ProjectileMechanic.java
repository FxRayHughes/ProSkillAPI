/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.ProjectileMechanic
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
import com.sucy.skill.api.projectile.CustomProjectile;
import com.sucy.skill.dynamic.TempEntity;
import com.sucy.skill.listener.MechanicListener;
import com.sucy.skill.task.RemoveTask;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Heals each target
 */
@SkillNode(
        key = "projectile",
        name = "Projectile",
        nameZh = "投射物",
        description = "Launches a projectile that applies child components on hit. The target supplied will be the struck target.",
        descriptionZh = "让每个目标发射原版实体投射物（箭、蛋、雪球、火球等），命中后执行子节点；未命中任何实体时用落点生成临时实体作为子节点的目标。cost 可配置为消耗施法者背包里的对应物品，物品不足时整个节点返回 false 且不发射。spread 为 rain 时在目标上方成片生成并向上给一个初速度后传送到位；否则按 angle 做锥形散射。所有投射物会被 RemoveTask 在 range/|velocity| 向上取整的 tick 后统一清除。注意 rain 形态下没有写入触发回调所需的元数据，因此 rain 发射的投射物命中时不会执行子节点。",
        container = true)
public class ProjectileMechanic extends MechanicComponent {
    private static Class<Enum<?>> PICKUP_STATUS_ENUM = null;
    private static final Vector UP = new Vector(0, 1, 0);

    @SkillField(
            kind = FieldKind.ListValue,
            label = "Projectile",
            labelZh = "投射物",
            tooltip = "[projectile] The type of projectile to fire",
            tooltipZh = "投射物类型，默认 Arrow。先按名字反射查找 org.bukkit.entity 下的类，找不到再回退到内置映射表（arrow/egg/ghast fireball/snowball），仍找不到则强制用 Arrow。",
            options = {"Arrow", "Egg", "Snowball", "Fireball", "Large Fireball", "Small fireball"},
            optionsZh = {"箭", "可选值2", "可选值3", "可选值4", "可选值5", "可选值6"},
            defaultValue = "Arrow")
    private static final String PROJECTILE = "projectile";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Velocity",
            labelZh = "速度",
            tooltip = "[velocity] How fast the projectile is launched. A negative value fires it in the opposite direction.",
            tooltipZh = "发射初速度，随等级缩放，默认 2.0。负值向反方向发射；它同时参与存活时间计算（range 除以速度绝对值）。")
    private static final String SPEED = "velocity";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Angle",
            labelZh = "角度",
            tooltip = "[angle] The angle in degrees of the cone arc to spread projectiles over. If you are only firing one projectile, this does not matter.",
            tooltipZh = "锥形散射张角（度），随等级缩放，默认 30。只发射一个时无意义。")
    private static final String ANGLE = "angle";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Amount",
            labelZh = "数量",
            tooltip = "[amount] The number of projectiles to fire",
            tooltipZh = "发射数量，随等级缩放，默认 1。cost 为 All 时也是需要消耗的物品数量。")
    private static final String AMOUNT = "amount";
    private static final String LEVEL = "skill_level";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Height",
            labelZh = "高度",
            tooltip = "[height] The distance in blocks over the target to rain the projectiles from",
            tooltipZh = "spread 为 rain 时投射物在目标上方多少格生成，随等级缩放，默认 8。")
    private static final String HEIGHT = "height";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Radius",
            labelZh = "半径",
            tooltip = "[rain-radius] The radius of the rain emission area in blocks",
            tooltipZh = "spread 为 rain 时的散布半径（格），随等级缩放，默认 2。")
    private static final String RADIUS = "rain-radius";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Spread",
            labelZh = "散布",
            tooltip = "[spread] The orientation for firing projectiles. Cone will fire arrows in a cone centered on your reticle. Horizontal cone does the same as cone, just locked to the XZ axis (parallel to the ground). Rain drops the projectiles from above the target. For firing one arrow straight, use \"Cone\"",
            tooltipZh = "发射形态。Cone 沿准星成锥形散射（单发即直线）；Horizontal Cone 先把方向压平到水平面；Rain 从目标上方成片落下（该形态下子节点回调不会触发）。",
            options = {"Cone", "Horizontal Cone", "Rain"},
            optionsZh = {"可选值1", "可选值2", "可选值3"},
            defaultValue = "Cone")
    private static final String SPREAD = "spread";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Cost",
            labelZh = "消耗",
            tooltip = "[cost] The cost of the skill of the fired item. All will cost the same number of items as the skill fired.",
            tooltipZh = "施法物品消耗。None 不消耗；One 消耗 1 个；All 消耗与发射数量相同的个数。只对玩家生效，非玩家或物品不足时节点直接返回 false。",
            options = {"None", "All", "One"},
            optionsZh = {"无", "全部", "可选值3"},
            defaultValue = "None")
    private static final String COST = "cost";
    private static final String RANGE = "range";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Flaming",
            labelZh = "燃烧",
            tooltip = "[flaming] Whether or not to make the launched projectiles on fire.",
            tooltipZh = "是否让投射物带火，开启后设置 9999 tick 燃烧时间。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "False")
    private static final String FLAMING = "flaming";
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
            tooltipZh = "发射点抬升（格），代码会再加 0.5，负值向下，随等级缩放。rain 形态下不生效。")
    private static final String UPWARD = "upward";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Forward Offset",
            labelZh = "前方偏移",
            tooltip = "[forward] How far forward in front of the target the projectile should fire from in blocks. A negative value will put it behind.",
            tooltipZh = "发射点前向偏移（格），负值向后，随等级缩放。rain 形态下不生效。")
    private static final String FORWARD = "forward";
    //                       Lifespan
    @SkillField(
            kind = FieldKind.IntValue,
            label = "Lifespan",
            labelZh = "存活时间",
            tooltip = "[lifespan] How long in seconds before the projectile will expire in case it doesn't hit anything",
            tooltipZh = "本意是未命中时的存活秒数，但代码把它当成「已存活 tick 数」直接写入实体（setTicksLived），数值越大反而越早被判定过期；且代码读取的默认值是 20 而非标注的 3。实际清除时机主要由 range 与 velocity 决定。",
            defaultValue = "3")
    private static final String LIFESPAN = "lifespan";
    //                     Gravity
    @SkillField(
            kind = FieldKind.IntValue,
            label = "Gravity",
            labelZh = "重力",
            tooltip = "[gravity] 重力",
            tooltipZh = "重力开关，语义与直觉相反：值为 0 时才启用重力，非 0（默认 1）则关闭重力让投射物直线飞行。随等级缩放后取整判断。",
            defaultValue = "1")
    private static final String GRAVITY = "gravity";

    @Override
    public String getKey() {
        return "projectile";
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
        double speed = parseValues(caster, SPEED, level, 2.0);
        double range = parseValues(caster, RANGE, level, 999);
        int lifespan = (int) parseValues(caster, LIFESPAN, level, 20);
        int gravity = (int) parseValues(caster, GRAVITY, level, 0);
        boolean grav = gravity == 0;

        boolean flaming = settings.getString(FLAMING, "false").equalsIgnoreCase("true");
        String spread = settings.getString(SPREAD, "cone").toLowerCase();
        String projectile = settings.getString(PROJECTILE, "arrow").toLowerCase();
        String cost = settings.getString(COST, "none").toLowerCase();
        Class<? extends Projectile> type = getProjectileClass(projectile);
        if (type == null) {
            type = Arrow.class;
        }
        // Cost to cast
        if (cost.equals("one") || cost.equals("all")) {
            Material mat = MATERIALS.get(settings.getString(PROJECTILE, "arrow").toLowerCase());
            if (mat == null || !(caster instanceof Player)) return false;
            Player player = (Player) caster;
            if (cost.equals("one") && !player.getInventory().contains(mat, 1)) {
                return false;
            }
            if (cost.equals("all") && !player.getInventory().contains(mat, amount)) {
                return false;
            }
            if (cost.equals("one")) {
                player.getInventory().removeItem(new ItemStack(mat));
            } else player.getInventory().removeItem(new ItemStack(mat, amount));
        }

        // Fire from each target
        ArrayList<Entity> projectiles = new ArrayList<>();
        for (LivingEntity target : targets) {
            // Apply the spread type
            if (spread.equals("rain")) {
                double radius = parseValues(caster, RADIUS, level, 2.0);
                double height = parseValues(caster, HEIGHT, level, 8.0);

                ArrayList<Location> locs = CustomProjectile.calcRain(target.getLocation(), radius, height, amount);
                for (Location loc : locs) {
                    Projectile p = caster.launchProjectile(type);
                    p.setTicksLived(lifespan);
                    p.setGravity(grav);
                    if (type.getName().contains("Arrow")) {
                        try {
                            // Will fail under 1.12
                            try {
                                //1.14+
                                AbstractArrow arrow = (AbstractArrow) p;
                                arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
                                arrow.setGravity(grav);
                            } catch (NoClassDefFoundError e) {
                                //1.12+
                                Arrow arrow = (Arrow) p;
                                Class<?> pickupStatusClass = Class.forName("org.bukkit.Arrow$PickupStatus");
                                Arrow.class.getMethod("setPickupStatus", pickupStatusClass).invoke(arrow, pickupStatusClass.getMethod("valueOf", String.class).invoke(null, "DISALLOWED"));
                                arrow.setGravity(grav);
                            }
                        } catch (NoSuchMethodError | ClassNotFoundException | NoSuchMethodException |
                                 IllegalAccessException | InvocationTargetException ignored) {
                        }
                    }
                    p.setVelocity(new Vector(0, speed, 0));
                    p.teleport(loc);
                    SkillAPI.setMeta(p, LEVEL, level);
                    if (flaming) p.setFireTicks(9999);
                    projectiles.add(p);
                }
            } else {
                Vector dir = target.getLocation().getDirection();
                if (spread.equals("horizontal cone")) {
                    dir.setY(0);
                    dir.normalize();
                }
                double angle = parseValues(caster, ANGLE, level, 30.0);
                double right = parseValues(caster, RIGHT, level, 0);
                double upward = parseValues(caster, UPWARD, level, 0);
                double forward = parseValues(caster, FORWARD, level, 0);

                Vector looking = target.getLocation().getDirection().setY(0).normalize();
                Vector normal = looking.clone().crossProduct(UP);
                looking.multiply(forward).add(normal.multiply(right));

                ArrayList<Vector> dirs = CustomProjectile.calcSpread(dir, angle, amount);
                for (Vector d : dirs) {
                    Projectile p = caster.launchProjectile(type);
                    p.setTicksLived(lifespan);
                    p.setGravity(grav);
                    if (type.getName().contains("Arrow")) {
                        try {
                            // Will fail under 1.12
                            try {
                                //1.14+
                                AbstractArrow arrow = (AbstractArrow) p;
                                arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
                                arrow.setGravity(grav);
                            } catch (NoClassDefFoundError e) {
                                //1.12+
                                Arrow arrow = (Arrow) p;
                                Class<?> pickupStatusClass = Class.forName("org.bukkit.Arrow$PickupStatus");
                                Arrow.class.getMethod("setPickupStatus", pickupStatusClass).invoke(arrow, pickupStatusClass.getMethod("valueOf", String.class).invoke(null, "DISALLOWED"));
                                arrow.setGravity(grav);
                            }
                        } catch (NoSuchMethodError | ClassNotFoundException | NoSuchMethodException |
                                 IllegalAccessException | InvocationTargetException ignored) {
                        }
                    } else {
                        p.teleport(target.getLocation().add(looking).add(0, upward + 0.5, 0).add(p.getVelocity()).setDirection(d));
                    }
                    p.setVelocity(d.multiply(speed));
                    SkillAPI.setMeta(p, MechanicListener.P_CALL, this);
                    SkillAPI.setMeta(p, LEVEL, level);
                    if (flaming) p.setFireTicks(9999);
                    projectiles.add(p);
                }
            }
        }
        new RemoveTask(projectiles, (int) Math.ceil(range / Math.abs(speed)));

        return targets.size() > 0;
    }

    /**
     * The callback for the projectiles that applies child components
     *
     * @param projectile projectile calling back for
     * @param hit        the entity hit by the projectile, if any
     */
    public void callback(Projectile projectile, LivingEntity hit) {
        if (hit == null)
            hit = TempEntity.create(projectile.getLocation());

        ArrayList<LivingEntity> targets = new ArrayList<LivingEntity>();
        targets.add(hit);
        executeChildren((LivingEntity) projectile.getShooter(), SkillAPI.getMetaInt(projectile, LEVEL), targets);
        SkillAPI.removeMeta(projectile, MechanicListener.P_CALL);
        projectile.remove();
    }

    private static Class<? extends Projectile> getProjectileClass(String projectileName) {
        StringBuilder conditionedName = new StringBuilder();
        for (String word : projectileName.split(" ")) {
            conditionedName.append(word.substring(0, 1).toUpperCase()).append(word.substring(1).toLowerCase());
        }
        try {
            return (Class<? extends Projectile>) Class.forName("org.bukkit.entity." + conditionedName);
        } catch (ClassNotFoundException e) {
            return PROJECTILES.get(projectileName);
        }
    }

    private static final HashMap<String, Class<? extends Projectile>> PROJECTILES = new HashMap<String, Class<? extends Projectile>>() {{
        put("arrow", Arrow.class);
        put("egg", Egg.class);
        put("ghast fireball", LargeFireball.class);
        put("snowball", Snowball.class);
    }};

    private static final HashMap<String, Material> MATERIALS = new HashMap<String, Material>() {{
        put("arrow", Material.ARROW);
        put("egg", Material.EGG);
        put("snowball", snowBall());
    }};

    private static Material snowBall() {
        for (Material material : Material.values()) {
            if (material.name().startsWith("SNOW") && material.name().endsWith("BALL")) {
                return material;
            }
        }
        return Material.SNOW;
    }
}
