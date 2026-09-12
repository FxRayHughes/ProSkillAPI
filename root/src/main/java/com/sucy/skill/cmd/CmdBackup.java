/**
 * SkillAPI
 * com.sucy.skill.cmd.CmdBackup
 * <p>
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2016 Steven Sucy
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
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

import com.rit.sucy.commands.ConfigurableCommand;
import com.rit.sucy.commands.IFunction;
import com.rit.sucy.config.Filter;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.data.io.IOManager;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;

/**
 * Backs up the active player JSON store into local JSON files.
 */
public class CmdBackup implements IFunction
{
    private static final String BACKUP = "backup";
    private static final String FAILED = "failed";
    private static final String DONE   = "done";

    /**
     * Executes the command
     *
     * @param command owning command
     * @param plugin  plugin reference
     * @param sender  sender of the command
     * @param args    arguments
     */
    @Override
    public void execute(ConfigurableCommand command, Plugin plugin, CommandSender sender, String[] args)
    {
        final SkillAPI api = (SkillAPI) plugin;
        command.sendMessage(sender, BACKUP, "&2Starting backup asynchronously...");
        new BackupTask(api, command, sender).runTaskAsynchronously(api);
    }

    /**
     * The task for backing up player JSON data
     */
    private class BackupTask extends BukkitRunnable
    {
        private final ConfigurableCommand cmd;
        private final SkillAPI            api;
        private final CommandSender       sender;

        /**
         * @param api SkillAPI reference
         */
        BackupTask(SkillAPI api, ConfigurableCommand cmd, CommandSender sender)
        {
            this.api = api;
            this.cmd = cmd;
            this.sender = sender;
        }

        /**
         * Runs the backup task, backing up the active player repository locally
         */
        @Override
        public void run()
        {
            int count = 0;
            try
            {
                final File file = new File(api.getDataFolder(), "players");
                IOManager manager = api.getIOManager();
                // Backups use the same JSON document as the active backend so
                // restore tooling never creates a second YAML persistence path.
                count = manager.backupTo(file);
                cmd.sendMessage(sender, DONE, "&2SQL database backup has finished successfully");
            }
            catch (Exception ex)
            {
                cmd.sendMessage(
                    sender,
                    FAILED,
                    "&4SQL database backup failed - backed up {amount} entries",
                    Filter.AMOUNT.setReplacement(count + "")
                );
                ex.printStackTrace();
            }
        }
    }
}
