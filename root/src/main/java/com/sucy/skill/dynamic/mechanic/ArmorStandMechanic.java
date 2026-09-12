package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.armorstand.ArmorStandInstance;
import com.sucy.skill.api.armorstand.ArmorStandManager;
import com.sucy.skill.api.attribute.AttributeAPI;
import com.sucy.skill.api.skills.PassiveSkill;
import com.sucy.skill.api.skills.Skill;
import com.sucy.skill.api.skills.SkillCastAPI;
import com.sucy.skill.listener.MechanicListener;
import com.sucy.skill.task.RemoveTask;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

import static com.sucy.skill.dynamic.mechanic.WolfMechanic.LEVEL;
import static com.sucy.skill.dynamic.mechanic.WolfMechanic.SKILL_META;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Summons an armor stand that can be used as a marker or for item display. Applies child components on the armor stand
 */
@SkillNode(
        key = "armor stand",
        name = "Armor Stand",
        nameZh = "盔甲架",
        description = "Summons an armor stand that can be used as a marker or for item display (check Armor Mechanic for latter). Applies child components on the armor stand",
        descriptionZh = "在每个目标身上（按前/上/右偏移换算出的位置）生成一个盔甲架，并把这些盔甲架当作新目标交给子节点执行——常用来做定位锚点、特效载体或物品展示。盔甲架会以「引用键」注册到 ArmorStandManager，同一目标同一键位同时只能存在一个。持续时间到点后由 RemoveTask 统一 remove 掉盔甲架，并清掉它身上的 flag/buff/施法数据。",
        container = true)
public class ArmorStandMechanic extends MechanicComponent {
    private static final Vector UP = new Vector(0, 1, 0);

    @SkillField(
            kind = FieldKind.StringValue,
            label = "Armor Stand Key",
            labelZh = "盔甲架引用键",
            tooltip = "[key] The key to refer to the armorstand by. Only one armorstand of each key can be active per target at the time",
            tooltipZh = "盔甲架的引用标识。同一个目标下每个键只能有一个存活的盔甲架，重复生成会顶掉旧的。未填时默认用当前技能名，而不是字面的 default。",
            defaultValue = "default")
    private static final String KEY = "key";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Duration",
            labelZh = "持续时间",
            tooltip = "[duration] How long the armorstand lasts before being deleted",
            tooltipZh = "盔甲架存活时长，单位秒，默认 5。随技能等级缩放，内部乘 20 换算成 tick 后交给 RemoveTask 删除。")
    private static final String DURATION = "duration";
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Name",
            labelZh = "名称",
            tooltip = "[name] The name the armor stand displays",
            tooltipZh = "盔甲架的自定义名称，其中 {player} 会替换成施法者名字。",
            defaultValue = "Armor Stand")
    private static final String NAME = "name";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Name visible",
            labelZh = "显示名称",
            tooltip = "[name-visible] Whether or not the armorstand's name should be visible from afar",
            tooltipZh = "名称是否隔远也常显。为 false 时只有准星指到才会显示。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "False")
    private static final String NAME_VISIBLE = "name-visible";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Follow target",
            labelZh = "跟随目标",
            tooltip = "[follow] Whether or not the armorstand should follow the target",
            tooltipZh = "是否持续跟随目标移动。为 true 时按前/上/右偏移与目标保持相对位置，为 false 则生成后钉在原地。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "False")
    private static final String FOLLOW = "follow";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Apply gravity",
            labelZh = "受重力影响",
            tooltip = "[gravity] Whether or not the armorstand should be affected by gravity",
            tooltipZh = "是否受重力影响。注意代码里未配置时的实际默认值是 false（不掉落），与编辑器标注的 True 不一致。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "True")
    private static final String GRAVITY = "gravity";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Small",
            labelZh = "小型",
            tooltip = "[tiny] Whether or not the armorstand should be small",
            tooltipZh = "是否使用小型盔甲架模型。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "False")
    private static final String SMALL = "tiny";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Show arms",
            labelZh = "显示手臂",
            tooltip = "[arms] Whether or not the armorstand should display its arms",
            tooltipZh = "是否显示手臂。要用盔甲架手持物品做展示时需要开启。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "False")
    private static final String ARMS = "arms";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Show base plate",
            labelZh = "显示底座",
            tooltip = "[base] Whether or not the armorstand should display its base plate",
            tooltipZh = "是否显示底座石板。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "False")
    private static final String BASE = "base";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Visible",
            labelZh = "可见",
            tooltip = "[visible] Whether or not the armorstand should be visible",
            tooltipZh = "盔甲架本体是否可见。做纯粹的特效锚点时通常设 false，此时装备的物品仍然可见。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "True")
    private static final String VISIBLE = "visible";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Marker",
            labelZh = "标记实体",
            tooltip = "[marker] Setting this to true will remove the armor stand's hitbox",
            tooltipZh = "为 true 时移除碰撞箱，盔甲架不可选中也不阻挡移动。setMarker 在过旧的服务端不存在，代码用 NoSuchMethodError 静默兜底。注意未配置时代码实际默认 false，与编辑器标注的 True 不一致。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "True")
    private static final String MARKER = "marker";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Forward Offset",
            labelZh = "前方偏移",
            tooltip = "[forward] How far forward in front of the target the armorstand should be in blocks. A negative value will put it behind.",
            tooltipZh = "生成点相对目标朝向的前方偏移格数，负值放到身后。随技能等级缩放。")
    private static final String FORWARD = "forward";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Upward Offset",
            labelZh = "上方偏移",
            tooltip = "[upward] How far above the target the armorstand should be in blocks. A negative value will put it below.",
            tooltipZh = "生成点相对目标的上方偏移格数，负值放到下方。随技能等级缩放。")
    private static final String UPWARD = "upward";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Right Offset",
            labelZh = "右方偏移",
            tooltip = "[right] How far to the right the armorstand should be of the target. A negative value will put it to the left.",
            tooltipZh = "生成点相对目标朝向的右侧偏移格数，负值放到左侧。随技能等级缩放。")
    private static final String RIGHT = "right";

    @SkillField(
            kind = FieldKind.StringListValue,
            label = "Skills 一行一个",
            labelZh = "技能列表",
            tooltip = "[skills] 这会视为盔甲架释放的技能 你可以在后续给他属性，但造成伤害/治疗会以召唤者为伤害/治疗源(伤害/治疗属性来自于召唤者)",
            tooltipZh = "让盔甲架去释放的技能名，一行一个。盔甲架被打上「主人 = 施法者」的标记，因此这些技能造成的伤害/治疗按施法者的属性计算、也算作施法者的输出。",
            defaultValue = "")
    private static final String SKILLS = "skills";

    @Override
    public String getKey() {
        return "armor stand";
    }

    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        String key = settings.getString(KEY, skill.getName());
        int duration = (int) (20 * parseValues(caster, DURATION, level, 5));
        String name = settings.getString(NAME, "Armor Stand");
        boolean nameVisible = settings.getBool(NAME_VISIBLE, false);
        boolean follow = settings.getBool(FOLLOW, false);
        boolean gravity = settings.getBool(GRAVITY, false);
        boolean small = settings.getBool(SMALL, false);
        boolean arms = settings.getBool(ARMS, false);
        boolean base = settings.getBool(BASE, false);
        boolean visible = settings.getBool(VISIBLE, true);
        boolean marker = settings.getBool(MARKER, false);
        double forward = parseValues(caster, FORWARD, level, 0);
        double upward = parseValues(caster, UPWARD, level, 0);
        double right = parseValues(caster, RIGHT, level, 0);

        List<String> skills = settings.getStringList(SKILLS);

        List<LivingEntity> armorStands = new ArrayList<>();
        for (LivingEntity target : targets) {
            Location loc = target.getLocation().clone();
            Vector dir = loc.getDirection().setY(0).normalize();
            Vector side = dir.clone().crossProduct(UP);
            loc.add(dir.multiply(forward)).add(0, upward, 0).add(side.multiply(right));

            ArmorStand armorStand = target.getWorld().spawn(loc, ArmorStand.class, as -> {
                try {
                    as.setMarker(marker);
                    as.setInvulnerable(true);
                } catch (NoSuchMethodError ignored) {
                }
                try {
                    as.setSilent(true);
                } catch (NoSuchMethodError ignored) {
                }
                as.setGravity(gravity);
                as.setCustomName(name.replace("{player}", caster.getName()));
                as.setCustomNameVisible(nameVisible);
                as.setSmall(small);
                as.setArms(arms);
                as.setBasePlate(base);
                as.setVisible(visible);
            });
            SkillAPI.setMeta(armorStand, MechanicListener.ARMOR_STAND, true);
            //设置一下主人
            SkillAPI.setMeta(armorStand, AttributeAPI.FX_SKILL_API_MASTER, caster.getUniqueId());

            for (String skillName : skills) {
                Skill skill = SkillAPI.getSkill(skillName);
                if (skill != null) {
                    SkillCastAPI.cast(armorStand, skill, level);
                }
            }

            armorStands.add(armorStand);

            ArmorStandInstance instance;
            if (follow) {
                instance = new ArmorStandInstance(armorStand, target, forward, upward, right);
            } else {
                instance = new ArmorStandInstance(armorStand, target);
            }
            ArmorStandManager.register(instance, target, key);
        }
        executeChildren(caster, level, armorStands);
        new RemoveTask(armorStands, duration);
        return targets.size() > 0;
    }
}
