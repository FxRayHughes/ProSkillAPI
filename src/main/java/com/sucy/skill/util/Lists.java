package com.sucy.skill.util;

import com.sucy.skill.SkillAPI;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * SkillAPI © 2018
 * com.sucy.skill.util.Lists
 */
public class Lists {
    public static <T> List<T> asList(final T... items) {
        return new ArrayList<>(Arrays.asList(items));
    }


    public static void test(Player player) {
        PlaceholderAPI.setPlaceholders(player, "");
        Bukkit.getScheduler().runTaskLater(SkillAPI.singleton, (Runnable) () -> {

        },20L);
    }
}
