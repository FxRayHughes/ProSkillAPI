package com.sucy.skill.cmd;

import com.rit.sucy.commands.ConfigurableCommand;
import com.rit.sucy.commands.IFunction;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.skills.Skill;
import com.sucy.skill.api.skills.SkillCastAPI;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CmdForceEntityCast implements IFunction {

    @Override
    public void execute(ConfigurableCommand cmd, Plugin plugin, CommandSender sender, String[] args) {
        List<String> list = Arrays.asList(args);
        if (sender instanceof Player) {
            list = PlaceholderAPI.setPlaceholders(((Player) sender), list);
        }
        UUID uuid = UUID.fromString(list.get(0));
        Entity getter = Bukkit.getEntity(uuid);
        if (getter == null || getter.isDead()) {
            return;
        }
        if (!(getter instanceof LivingEntity)) {
            return;
        }
        LivingEntity entity = (LivingEntity) getter;
        String name = list.get(1);
        int level = 1;
        if (list.size() == 3) {
            level = Integer.parseInt(list.get(2));
        }
        Skill skill = SkillAPI.getSkill(name);
        if (skill == null) {
            return;
        }
        SkillCastAPI.cast(entity, skill, level);
    }
}
