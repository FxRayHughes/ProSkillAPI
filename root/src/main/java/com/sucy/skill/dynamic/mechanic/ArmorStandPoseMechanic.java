package com.sucy.skill.dynamic.mechanic;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.EulerAngle;

import java.util.Arrays;
import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

@SkillNode(
        key = "armor stand pose",
        name = "Armor Stand Pose",
        nameZh = "盔甲架姿势",
        description = "Sets the pose of an armor stand target. Values should be in the format x,y,z where rotations are in degrees. Example: 0.0,0.0,0.0",
        descriptionZh = "调整目标盔甲架各个部位的姿势角度。只对本身就是盔甲架的目标生效，其他实体被静默跳过；留空或格式不对的部位保持原样。填写格式为 x,y,z 三个数字，实际是直接塞给 Bukkit 的 EulerAngle，按弧度解释（英文说明里写的 degrees 与代码不符）。只要目标列表非空就返回 true，即使一个盔甲架都没命中。")
public class ArmorStandPoseMechanic extends MechanicComponent {
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Head",
            labelZh = "头部",
            tooltip = "[head] The pose values of the head. Leave empty if should be ignored",
            tooltipZh = "头部姿势，格式 x,y,z（弧度）。留空则不改动该部位。")
    private static final String HEAD = "head";
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Body",
            labelZh = "身体",
            tooltip = "[body] The pose values of the body. Leave empty if should be ignored",
            tooltipZh = "身体姿势，格式 x,y,z（弧度）。留空则不改动该部位。")
    private static final String BODY = "body";
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Left Arm",
            labelZh = "左臂",
            tooltip = "[left-arm] The pose values of the left arm. Leave empty if should be ignored",
            tooltipZh = "左臂姿势，格式 x,y,z（弧度）。留空则不改动该部位。")
    private static final String LEFT_ARM = "left-arm";
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Right Arm",
            labelZh = "右臂",
            tooltip = "[right-arm] The pose values of the right arm. Leave empty if should be ignored",
            tooltipZh = "右臂姿势，格式 x,y,z（弧度）。留空则不改动该部位。")
    private static final String RIGHT_ARM = "right-arm";
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Left Leg",
            labelZh = "左腿",
            tooltip = "[left-leg] The pose values of the left leg. Leave empty if should be ignored",
            tooltipZh = "左腿姿势，格式 x,y,z（弧度）。留空则不改动该部位。")
    private static final String LEFT_LEG = "left-leg";
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Right Leg",
            labelZh = "右腿",
            tooltip = "[right-leg] The pose values of the right leg. Leave empty if should be ignored",
            tooltipZh = "右腿姿势，格式 x,y,z（弧度）。留空则不改动该部位。")
    private static final String RIGHT_LEG = "right-leg";

    @Override
    public String getKey() { return "armor stand pose"; }

    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        EulerAngle head = eulerAngle(settings.getString(HEAD, ""));
        EulerAngle body = eulerAngle(settings.getString(BODY, ""));
        EulerAngle leftArm = eulerAngle(settings.getString(LEFT_ARM, ""));
        EulerAngle rightArm = eulerAngle(settings.getString(RIGHT_ARM, ""));
        EulerAngle leftLeg = eulerAngle(settings.getString(LEFT_LEG, ""));
        EulerAngle rightLeg = eulerAngle(settings.getString(RIGHT_LEG, ""));

        for (LivingEntity target : targets) {
            if (!(target instanceof ArmorStand)) continue;
            ArmorStand armorStand = (ArmorStand) target;
            if (head != null) armorStand.setHeadPose(head);
            if (body != null) armorStand.setBodyPose(body);
            if (leftArm != null) armorStand.setLeftArmPose(leftArm);
            if (rightArm != null) armorStand.setRightArmPose(rightArm);
            if (leftLeg != null) armorStand.setLeftLegPose(leftLeg);
            if (rightLeg != null) armorStand.setRightLegPose(rightLeg);
        }
        return targets.size() > 0;
    }

    private static EulerAngle eulerAngle(String string) {
        if (string.equals("")) return null;
        Double[] doubles;
        try {
            doubles = Arrays.stream(string.split(",",3)).map(Double::valueOf).toArray(Double[]::new);
        } catch (NumberFormatException e) { return null; }
        if (doubles.length != 3) return null;
        return new EulerAngle(doubles[0], doubles[1], doubles[2]);
    }
}
