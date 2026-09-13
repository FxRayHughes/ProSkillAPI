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
        // The command targets an entity by UUID; reject incomplete or malformed
        // input here so Bukkit reports a useful usage error instead of a
        // command-wide IllegalArgumentException.
        int skillIndex = sender instanceof Player ? 0 : 1;
        if (args.length <= skillIndex) {
            sender.sendMessage(sender instanceof Player
                    ? "用法: /class mustcast <技能> [等级]"
                    : "用法: /class mustcast <实体UUID> <技能> [等级]");
            return;
        }
        List<String> list = Arrays.asList(args);
        if (sender instanceof Player) {
            list = PlaceholderAPI.setPlaceholders(((Player) sender), list);
        }
        // Players are the natural casting carrier when they issue the command;
        // consoles must still provide an entity UUID explicitly.
        Entity getter;
        if (sender instanceof Player) {
            getter = (Player) sender;
        } else {
            UUID uuid;
            try {
                uuid = UUID.fromString(list.get(0));
            } catch (IllegalArgumentException ex) {
                sender.sendMessage("实体UUID格式无效: " + list.get(0));
                return;
            }
            getter = Bukkit.getEntity(uuid);
        }
        if (getter == null || getter.isDead()) {
            return;
        }
        if (!(getter instanceof LivingEntity)) {
            return;
        }
        LivingEntity entity = (LivingEntity) getter;
        String name = list.get(skillIndex);
        int level = 1;
        if (list.size() > skillIndex + 1) {
            try {
                level = Integer.parseInt(list.get(skillIndex + 1));
            } catch (NumberFormatException ex) {
                sender.sendMessage("技能等级必须是数字: " + list.get(skillIndex + 1));
                return;
            }
        }
        Skill skill = SkillAPI.getSkill(name);
        if (skill == null) {
            return;
        }
        SkillCastAPI.cast(entity, skill, level);
    }
}
