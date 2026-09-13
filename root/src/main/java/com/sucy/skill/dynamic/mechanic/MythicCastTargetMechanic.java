/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.ValueAddMechanic
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
package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.hook.MythicMobsHook;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * 释放一个mm技能
 */
@SkillNode(
        key = "mythic cast target",
        name = "Mythic Cast Target",
        nameZh = "神话生物施放目标",
        description = "施法者释放某个Mythic技能对目标们",
        descriptionZh = "由本技能的施法者释放一个 MythicMobs 技能，并把当前目标列表整体作为该 MM 技能的目标传入。与「神话生物施放」相反：那个是让目标自己放技能，这个是施法者放技能打目标。需要服务器安装 MythicMobs。未填写技能名称时不执行；填了就直接返回成功，且目标列表为空时也会照常调用（等于放一个没有目标的技能）。",
        requiresPlugins = {"MythicMobs"})
public class MythicCastTargetMechanic extends MechanicComponent {

    @SkillField(
            kind = FieldKind.StringValue,
            label = "SkillName",
            labelZh = "技能名称",
            tooltip = "[skillname] 唯一识别标签 {uuid} 会被进行替换为 施法者的UUID",
            tooltipZh = "要释放的 MythicMobs 技能名，须与 MM 配置中的技能标识一致；写 {uuid} 会替换为施法者的 UUID。此项为必填，缺失则整个节点不执行。",
            defaultValue = "技能名")
    private static final String SKILLNAME = "skillname";

    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Power",
            labelZh = "强度",
            tooltip = "[power] 技能等级",
            tooltipZh = "传给 MythicMobs 的技能强度（power），转为浮点后传入，用于 MM 内部按强度缩放的效果。数值随技能等级/属性变化，默认 1。")
    private static final String POWER = "power";

    @Override
    public String getKey() {
        return "mythic cast target";
    }


    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        if (!settings.has(SKILLNAME)) {
            return false;
        }

        String key = settings.getString(SKILLNAME, "").replace("{uuid}", caster.getUniqueId().toString());
        float power = (float) parseValues(caster, POWER, level, 1);
        Collection<Entity> etarget = new ArrayList<>(targets);
        MythicMobsHook.castSkill(caster, key, etarget, power);
        return true;
    }
}
