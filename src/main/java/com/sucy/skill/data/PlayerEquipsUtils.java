package com.sucy.skill.data;

import com.rit.sucy.config.parse.NumberParser;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.util.Pair;
import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerEquipsUtils {

    public static Pair<String, Integer> getAttribute(String lore) {
        for (String attr : SkillAPI.getAttributeManager().getLookupKeys()) {
            String oLore = ChatColor.stripColor(lore).toLowerCase();
            if (oLore.contains(attr)) {
                String normalized = SkillAPI.getAttributeManager().normalize(attr);
                int extra = toInt(oLore);
                if (extra <= 0){
                    return null;
                }
                return new Pair<>(normalized, extra);
            }
        }
        return null;
    }

    public static Integer toInt(String a) {
        String regEx = "[^0-9]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(a);
        return NumberParser.parseInt(m.replaceAll("").trim());
    }

}
