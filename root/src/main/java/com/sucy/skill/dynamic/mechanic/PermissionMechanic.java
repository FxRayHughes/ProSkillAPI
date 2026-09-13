/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.PermissionMechanic
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Steven Sucy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software") to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.api.util.FlagManager;
import com.sucy.skill.hook.PluginChecker;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Applies a flag to each target
 */
@SkillNode(
        key = "permission",
        name = "Permission",
        nameZh = "权限",
        description = "Grants each player target a permission for a limited duration. This mechanic requires Vault with an accompanying permissions plugin in order to work.",
        descriptionZh = "在一段时间内给玩家目标临时授予权限。实现方式不是直接调用权限插件，而是给目标挂一个「perm:权限节点」形式的限时标记（FlagManager），由插件的权限桥接逻辑读取。需要 Vault 处于激活状态，未装 Vault、未配置 perm 或目标列表为空时返回 false。已经拥有该权限的目标会被跳过，不重复挂标记。",
        container = true,
        requiresPlugins = {"Vault"})
public class PermissionMechanic extends MechanicComponent {
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Permission",
            labelZh = "权限",
            tooltip = "[perm] The permission to give to the player",
            tooltipZh = "要临时授予的权限节点字符串。未配置该键时节点直接失效。",
            defaultValue = "plugin.perm.key")
    private static final String PERM    = "perm";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Seconds",
            labelZh = "秒数",
            tooltip = "[seconds] How long in seconds to give the permission to the player",
            tooltipZh = "权限持续秒数，随技能等级缩放，默认 3 秒；内部乘 20 转成 tick 作为标记时长。")
    private static final String SECONDS = "seconds";

    @Override
    public String getKey() {
        return "permission";
    }

    /**
     * Executes the component
     *
     * @param caster  caster of the skill
     * @param level   level of the skill
     * @param targets targets to apply to
     *
     * @return true if applied to something, false otherwise
     */
    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        if (targets.size() == 0 || !settings.has(PERM) || !PluginChecker.isVaultActive()) {
            return false;
        }

        String key = settings.getString(PERM, "");
        double seconds = parseValues(caster, SECONDS, level, 3.0);
        int ticks = (int) (seconds * 20);
        for (LivingEntity target : targets) {
            if (!target.hasPermission(key)) {
                FlagManager.addFlag(target, "perm:" + key, ticks);
            }
        }
        return targets.size() > 0;
    }
}
