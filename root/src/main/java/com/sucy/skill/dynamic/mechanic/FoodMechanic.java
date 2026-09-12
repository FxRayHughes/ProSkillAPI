package com.sucy.skill.dynamic.mechanic;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * SkillAPI © 2017
 * com.sucy.skill.dynamic.mechanic.FoodMechanic
 */
@SkillNode(
        key = "food",
        name = "Food",
        nameZh = "饥饿度",
        description = "Adds or removes to a player's hunger and saturation",
        descriptionZh = "增减玩家目标的饥饿值与饱和度，非玩家目标会被跳过。饥饿值在加减后被限制在 0-20，饱和度被限制在 0 到「当前饥饿值」之间（原版规则：饱和度不能超过饥饿值）。两个数值填负数即为扣除。")
public class FoodMechanic extends MechanicComponent {
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Food",
            labelZh = "饥饿度",
            tooltip = "[food] The amount of food to give. Use a negative number to lower the food meter.",
            tooltipZh = "饥饿值变化量，与玩家当前饥饿值相加后截断到 0-20 区间；取整后使用，负数表示扣除饥饿值。数值随技能等级/属性变化，默认 1。")
    private static final String FOOD = "food";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Saturation",
            labelZh = "饱和度",
            tooltip = "[saturation] How much saturation to give. Use a negative number to lower saturation. This is the hidden value that determines how long until food starts going down.",
            tooltipZh = "饱和度变化量，与当前饱和度相加后截断到 0 到当前饥饿值之间。饱和度是隐藏值，决定饥饿值开始下降前能维持多久。数值随技能等级/属性变化，默认 1。")
    private static final String SATURATION = "saturation";

    @Override
    public String getKey() {
        return "food";
    }

    /**
     * Executes the component
     *
     * @param caster  caster of the skill
     * @param level   level of the skill
     * @param targets targets to apply to
     *
     * @return true if applied to something, false otherwise
     */
    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        double food = parseValues(caster, FOOD, level, 1.0);
        double saturation = parseValues(caster, SATURATION, level, 1.0);
        for (LivingEntity target : targets) {
            if (target instanceof Player) {
                Player player = (Player) target;
                player.setFoodLevel(Math.min(20, Math.max(0, (int) food + player.getFoodLevel())));
                player.setSaturation(Math.min(
                        player.getFoodLevel(),
                        Math.max(0, player.getSaturation() + (float) saturation)));
            }
        }
        return targets.size() > 0;
    }
}
