/**
 * SkillAPI
 * com.sucy.skill.manager.CmdManager
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
package com.sucy.skill.manager;

import com.rit.sucy.commands.CommandManager;
import com.rit.sucy.commands.ConfigurableCommand;
import com.rit.sucy.commands.SenderType;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.cmd.*;
import com.sucy.skill.data.Permissions;

/**
 * Sets up commands for the plugin
 */
public class CmdManager {
    public static ConfigurableCommand PROFESS_COMMAND;

    private SkillAPI api;

    /**
     * Initializes a new command manager. This is handled by the API and
     * shouldn't be used by other plugins.
     *
     * @param api SkillAPI reference
     */
    public CmdManager(SkillAPI api) {
        this.api = api;
        this.initialize();
    }

    /**
     * Initializes commands with MCCore's CommandManager
     */
    public void initialize() {
        ConfigurableCommand root = new ChineseConfigurableCommand(api, "class", SenderType.ANYONE);
        root.addSubCommands(
                new ChineseConfigurableCommand(api, "bind", SenderType.PLAYER_ONLY, new CmdBind(), "绑定技能", "<技能>", Permissions.BASIC),
                new ChineseConfigurableCommand(api, "cast", SenderType.PLAYER_ONLY, new CmdCast(), "释放技能", "<技能>", Permissions.BASIC),
                new ChineseConfigurableCommand(api, "changeclass", SenderType.ANYONE, new CmdChangeClass(), "切换职业", "<玩家> <职业组> <职业>", Permissions.FORCE),
                new ChineseConfigurableCommand(api, "clearbind", SenderType.PLAYER_ONLY, new CmdClearBinds(), "清除技能绑定", "", Permissions.BASIC),
                new ChineseConfigurableCommand(api, "customize", SenderType.PLAYER_ONLY, new CmdCustomize(), "打开编辑界面", "", Permissions.GUI),
                new ChineseConfigurableCommand(api, "exp", SenderType.ANYONE, new CmdExp(), "给予经验", "[玩家] <数量> [职业组]", Permissions.LVL),
                new ChineseConfigurableCommand(api, "info", SenderType.ANYONE, new CmdInfo(), "查看职业信息", "[玩家]", Permissions.BASIC),
                new ChineseConfigurableCommand(api, "level", SenderType.ANYONE, new CmdLevel(), "给予等级", "[玩家] <数量> [职业组]", Permissions.LVL),
                new ChineseConfigurableCommand(api, "list", SenderType.ANYONE, new CmdList(), "查看账户", "[玩家]", Permissions.BASIC),
                new ChineseConfigurableCommand(api, "lore", SenderType.PLAYER_ONLY, new CmdLore(), "添加物品描述", "<描述>", Permissions.LORE),
                new ChineseConfigurableCommand(api, "mana", SenderType.ANYONE, new CmdMana(), "给予魔力", "[玩家] <数量>", Permissions.MANA),
                new ChineseConfigurableCommand(api, "options", SenderType.PLAYER_ONLY, new CmdOptions(), "查看转职选项", "", Permissions.BASIC),
                new ChineseConfigurableCommand(api, "points", SenderType.ANYONE, new CmdPoints(), "给予技能点", "[玩家] <数量>", Permissions.POINTS),
                new ChineseConfigurableCommand(api, "setpoints", SenderType.ANYONE, new CmdSkillPoints(), "设置某技能的点数", "<玩家> <数量> <技能>", Permissions.POINTS),
                PROFESS_COMMAND = new ChineseConfigurableCommand(api, "profess", SenderType.PLAYER_ONLY, new CmdProfess(), "转职", "<职业>", Permissions.BASIC),
                new ChineseConfigurableCommand(api, "reload", SenderType.ANYONE, new CmdReload(), "重载插件", "", Permissions.RELOAD),
                new ChineseConfigurableCommand(api, "reset", SenderType.PLAYER_ONLY, new CmdReset(), "重置账户数据", "", Permissions.BASIC),
                new ChineseConfigurableCommand(api, "skill", SenderType.PLAYER_ONLY, new CmdSkill(), "查看技能", "", Permissions.BASIC),
                new ChineseConfigurableCommand(api,"skilluplevel", SenderType.PLAYER_ONLY, new CmdSkillUpdate(), "升级技能","技能 等级 玩家ID", Permissions.LVL),
                new ChineseConfigurableCommand(api, "unbind", SenderType.PLAYER_ONLY, new CmdUnbind(), "解除手持物品绑定", "", Permissions.BASIC),
                new ChineseConfigurableCommand(api, "world", SenderType.PLAYER_ONLY, new CmdWorld(), "传送到世界", "<世界>", Permissions.WORLD),
                new ChineseConfigurableCommand(api, "mustcast", SenderType.ANYONE, new CmdForceEntityCast(), "强制实体释放技能", "<实体UUID> <技能> [等级]", Permissions.BASIC)
        );
        root.addSubCommands(
                new ChineseConfigurableCommand(api, "forceaccount", SenderType.CONSOLE_ONLY, new CmdForceAccount(), "切换玩家账户", "<玩家> <账户ID>", Permissions.FORCE),
                new ChineseConfigurableCommand(api, "forceattr", SenderType.CONSOLE_ONLY, new CmdForceAttr(), "返还或给予属性点", "<玩家> [属性] [数量]", Permissions.FORCE),
                new ChineseConfigurableCommand(api, "forcecast", SenderType.CONSOLE_ONLY, new CmdForceCast(), "强制玩家释放技能", "<玩家> <技能> [等级]", Permissions.FORCE),
                new ChineseConfigurableCommand(api, "forcecastb", SenderType.CONSOLE_ONLY, new CmdForceCastB(), "强制玩家释放技能（忽略冷却）", "<玩家> <技能> [等级]", Permissions.FORCE),
                new ChineseConfigurableCommand(api, "forceprofess", SenderType.CONSOLE_ONLY, new CmdForceProfess(), "强制玩家转职", "<玩家> <职业>", Permissions.FORCE),
                new ChineseConfigurableCommand(api, "forcereset", SenderType.CONSOLE_ONLY, new CmdForceReset(), "重置玩家数据", "<玩家> [账户]", Permissions.FORCE),
                new ChineseConfigurableCommand(api, "forceskill", SenderType.CONSOLE_ONLY, new CmdForceSkill(), "修改技能等级", "<玩家> <up|down|reset> <技能>", Permissions.FORCE)
        );
        if (SkillAPI.getSettings().isOnePerClass()) {
            root.addSubCommand(new ChineseConfigurableCommand(api, "switch", SenderType.PLAYER_ONLY, new CmdSwitch(), "切换职业", "<职业>", Permissions.BASIC));
        } else {
            root.addSubCommand(new ChineseConfigurableCommand(api, "acc", SenderType.PLAYER_ONLY, new CmdAccount(), "切换账户", "<账户ID>", Permissions.BASIC));
        }
        // Player data is always stored in the local SQLite backend, so the
        // maintenance command must not depend on the legacy remote-SQL flag.
        root.addSubCommand(new ChineseConfigurableCommand(api, "backup", SenderType.ANYONE, new CmdBackup(), "备份数据", "", Permissions.BACKUP));
        if (SkillAPI.getSettings().isSkillBarEnabled()) {
            root.addSubCommand(new ChineseConfigurableCommand(api, "bar", SenderType.PLAYER_ONLY, new CmdBar(), "切换技能栏", "", Permissions.BASIC));
        }
        if (SkillAPI.getSettings().isCustomCombosAllowed()) {
            root.addSubCommand(new ChineseConfigurableCommand(api, "combo", SenderType.PLAYER_ONLY, new CmdCombo(), "设置技能连招", "<技能> <连招>", Permissions.BASIC));
        }
        if (SkillAPI.getSettings().isAttributesEnabled()) {
            root.addSubCommand(new ChineseConfigurableCommand(api, "ap", SenderType.ANYONE, new CmdAP(), "给予属性点", "[玩家] <数量>", Permissions.ATTRIB));
            root.addSubCommand(new ChineseConfigurableCommand(api, "attr", SenderType.PLAYER_ONLY, new CmdAttribute(), "打开属性界面", "", Permissions.BASIC));
        }
        CommandManager.registerCommand(root);
    }

    public static String join(String[] args, int start) {
        return join(args, start, args.length - 1);
    }

    public static String join(String[] args, int start, int end) {
        final StringBuilder builder = new StringBuilder(args[start]);
        for (int i = start + 1; i <= end; i++) builder.append(' ').append(args[i]);
        return builder.toString();
    }

    /**
     * Unregisters all commands for SkillAPI from the server
     */
    public void clear() {
        CommandManager.unregisterCommands(api);
    }
}
