/**
 * SkillAPI
 * com.sucy.skill.cmd.CmdPoints
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
package com.sucy.skill.cmd;

import com.rit.sucy.commands.CommandManager;
import com.rit.sucy.commands.ConfigurableCommand;
import com.rit.sucy.commands.IFunction;
import com.rit.sucy.config.Filter;
import com.rit.sucy.version.VersionManager;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.player.PlayerData;
import com.sucy.skill.api.player.PlayerSkill;
import com.sucy.skill.api.skills.Skill;
import com.sucy.skill.language.RPGFilter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * A command that gives a player class experience
 */
public class CmdSkillUpdate implements IFunction {
    private static final String NOT_PLAYER = "not-player";
    private static final String NOT_NUMBER = "not-number";
    private static final String NOT_POSITIVE = "not-positive";
    private static final String DISABLED = "world-disabled";

    /**
     * Runs the command
     *
     * @param cmd    command that was executed
     * @param plugin plugin reference
     * @param sender sender of the command
     * @param args   argument list
     */

    //class skillup 技能 等级 ID
    @Override
    public void execute(ConfigurableCommand cmd, Plugin plugin, CommandSender sender, String[] args) {
        // Disabled world
        if (sender instanceof Player && !SkillAPI.getSettings().isWorldEnabled(((Player) sender).getWorld()) && args.length == 1) {
            cmd.sendMessage(sender, DISABLED, "&4You cannot use this command in this world");
        } else if (args.length >= 3) {
            String skill = args[0];
            int level = Integer.parseInt(args[1]);
            Player player = null;
            try {
                player = Bukkit.getPlayerExact(args[2]);
            } catch (Exception e) {
                if (sender instanceof Player) {
                    player = (Player) sender;
                }
            }
            PlayerData playerData = SkillAPI.getPlayerData(player);
            if (playerData == null) {
                return;
            }
            PlayerSkill playerSkill = playerData.getSkill(skill);
            for (int i = 1; i < level; i++) {
                playerData.forceUpSkill(playerSkill);
            }
        } else {
            CommandManager.displayUsage(cmd, sender);
        }
    }
}
