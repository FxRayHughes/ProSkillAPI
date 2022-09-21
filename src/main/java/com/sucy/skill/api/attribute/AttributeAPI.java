package com.sucy.skill.api.attribute;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.event.AttributeGetEvent;
import com.sucy.skill.api.player.PlayerClass;
import com.sucy.skill.api.player.PlayerData;
import com.sucy.skill.dynamic.EffectComponent;
import com.sucy.skill.manager.AttributeManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;

public class AttributeAPI {


    /**
     * 获取实体属性 所有实体获取属性环节都会走这个方法
     * 来实现给MM怪物等获取属性或者是兼容AP等插件
     *
     * @param entity 获取属性的实体
     * @param key    属性名
     * @return 属性值
     */
    public static Integer getAttribute(LivingEntity entity, String key) {
        //触发Event 让其他插件可篡改
        int total = 0;
        key = ChatColor.stripColor(key).toLowerCase();
        if (entity instanceof Player && SkillAPI.getPlayerData((Player) entity) != null) {
            PlayerData data = SkillAPI.getPlayerData((Player) entity);
            if (data.getAttributes().containsKey(key)) {
                total += data.getAttributes().get(key);
            }
            if (data.bonusAttrib.containsKey(key)) {
                total += data.bonusAttrib.get(key);
            }
            total += data.getAddAttribute(key);
            for (PlayerClass playerClass : data.getClasses()) {
                total += playerClass.getData().getAttribute(key, playerClass.getLevel());
            }
            total = Math.max(0, total);
        }
        AttributeGetEvent event = new AttributeGetEvent(entity, key, total);
        Bukkit.getPluginManager().callEvent(event);
        return event.getValue();
    }


    /**
     * Scales a dynamic skill's value using global modifiers
     *
     * @param component component holding the value
     * @param key       key of the value
     * @param value     unmodified value
     * @return the modified value
     */
    public static double scaleDynamic(LivingEntity entity, EffectComponent component, String key, double value) {
        final AttributeManager manager = SkillAPI.getAttributeManager();
        if (manager == null) {
            return value;
        }

        final List<AttributeManager.Attribute> matches = manager.forComponent(component, key);
        if (matches == null) {
            return value;
        }

        for (final AttributeManager.Attribute attribute : matches) {
            int amount = getAttribute(entity, key);
            if (amount > 0) {
                value = attribute.modify(component, key, value, amount);
            }
        }
        return value;
    }

    /**
     * Scales a stat value using the player's attributes
     *
     * @param stat  stat key
     * @param value base value
     * @return modified value
     */
    public static double scaleStat(LivingEntity entity, final String stat, final double value) {
        final AttributeManager manager = SkillAPI.getAttributeManager();
        if (manager == null) {
            return value;
        }

        final List<AttributeManager.Attribute> matches = manager.forStat(stat);
        if (matches == null) {
            return value;
        }

        double modified = value;
        for (final AttributeManager.Attribute attribute : matches) {
            int amount = getAttribute(entity, attribute.getKey());
            if (amount > 0) {
                modified = attribute.modifyStat(stat, modified, amount);
            }
        }
        return modified;
    }

}
