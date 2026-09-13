package com.sucy.skill.cmd;

import com.rit.sucy.commands.ConfigurableCommand;
import com.rit.sucy.commands.SenderType;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * MCCore command with Bukkit's native completion hook.
 *
 * Keeping this class on top of ConfigurableCommand preserves MCCore's command
 * tree and permission checks on old servers, while overriding only the Bukkit
 * completion method makes suggestions available without version-specific NMS.
 */
public class ChineseConfigurableCommand extends ConfigurableCommand {
    /** Creates a container command while retaining MCCore's registration contract. */
    public ChineseConfigurableCommand(JavaPlugin plugin, String name, SenderType senderType) {
        super(plugin, name, senderType);
    }

    /** Creates an executable command with Chinese help metadata and legacy permission checks. */
    public ChineseConfigurableCommand(JavaPlugin plugin, String name, SenderType senderType,
                                      com.rit.sucy.commands.IFunction function, String description,
                                      String args, String permission) {
        super(plugin, name, senderType, function, description, args, permission);
    }

    /** Supplies permission-filtered child names and online player names to Bukkit. */
    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (!canUseCommand(sender)) return Collections.emptyList();
        String prefix = args.length == 0 ? "" : args[args.length - 1].toLowerCase(Locale.ROOT);
        List<String> result = new ArrayList<>();
        if (args.length <= 1) {
            for (String name : getUsableCommands(sender)) {
                if (name.toLowerCase(Locale.ROOT).startsWith(prefix)) result.add(name);
            }
        } else {
            ConfigurableCommand child = getSubCommand(args[0]);
            if (child != null && child instanceof ChineseConfigurableCommand) {
                return child.tabComplete(sender, alias, tail(args));
            }
            // Player-name completion is safe across Bukkit/Paper generations.
            for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase(Locale.ROOT).startsWith(prefix)) result.add(player.getName());
            }
        }
        Collections.sort(result);
        return result;
    }

    /** Removes the consumed subcommand before delegating completion. */
    private static String[] tail(String[] args) {
        String[] result = new String[Math.max(0, args.length - 1)];
        if (result.length > 0) System.arraycopy(args, 1, result, 0, result.length);
        return result;
    }
}
