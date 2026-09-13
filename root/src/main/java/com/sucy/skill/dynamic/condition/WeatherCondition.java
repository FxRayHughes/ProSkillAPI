package com.sucy.skill.dynamic.condition;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import com.sucy.skill.dynamic.meta.SkillNode;

/**
 * SkillAPI © 2018
 * com.sucy.skill.dynamic.condition.Weather
 */
@SkillNode(
        key = "weather",
        name = "Weather",
        nameZh = "检查天气",
        description = "Applies child components when the target's location has the given weather condition",
        descriptionZh = "检查目标所在位置的天气，结合世界降水状态与当地气温判断。thunder=正在雷暴且气温 <= 1；rain=正在降水且气温在 (0.15, 1]；snow=正在降水且气温 <= 0.15；其他值（含未填）判为晴朗，即没有降水或气温 > 1。类型值比较时转小写。",
        container = true)
public class WeatherCondition extends ConditionComponent {
    private String TYPE = "type";

    @Override
    boolean test(final LivingEntity caster, final int level, final LivingEntity target) {
        final String type = settings.getString(TYPE, "Clear").toLowerCase();
        final World world = target.getWorld();
        final Location loc = target.getLocation();
        final double temperature = loc.getBlock().getTemperature();

        switch (type) {
            case "thunder":
                return world.isThundering() && temperature <= 1;
            case "rain":
                return world.hasStorm() && temperature > 0.15 && temperature <= 1;
            case "snow":
                return world.hasStorm() && temperature <= 0.15;
            default:
                return !world.hasStorm() || temperature > 1;
        }
    }

    @Override
    public String getKey() {
        return "weather";
    }
}
