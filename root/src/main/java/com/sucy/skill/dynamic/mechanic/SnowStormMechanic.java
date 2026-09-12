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

import org.bukkit.entity.LivingEntity;
import ray.mintcat.rayskillapiaddor.command.impl.CommandParticle;

import java.util.List;
import java.util.UUID;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * 释放一个mm技能
 */
@SkillNode(
        key = "snow storm",
        name = "Snow Storm",
        nameZh = "雪风暴",
        description = "播放一个雪暴粒子 [需要有龙之核心与外置包]",
        descriptionZh = "调用外置的龙之核心粒子指令，为每个目标播放一个由 .json 文件定义的特效（如雪暴），需要额外的龙之核心与外置资源包，缺少依赖时不会生效。未配置 id 时返回 false；只要配置了 id 就返回 true，不校验目标数量，因此目标为空也算成功。id 中的 {uuid} 会被替换成施法者 UUID，{random} 会被替换成一个随机 UUID。")
public class SnowStormMechanic extends MechanicComponent {

    @SkillField(
            kind = FieldKind.StringValue,
            label = "特效名称",
            labelZh = "技能节点",
            tooltip = "[name] 这里是特效名称不需要写.json",
            tooltipZh = "特效文件名，不需要写 .json 后缀。",
            defaultValue = "null")
    private static final String NAME = "name";
    @SkillField(
            kind = FieldKind.StringValue,
            label = "ID",
            labelZh = "标识",
            tooltip = "[id] 用来识别特效的ID,{uuid}会自动替换为施法者的UUID",
            tooltipZh = "用于识别、复用或覆盖该特效实例的 ID。支持占位符：{uuid} 替换为施法者 UUID，{random} 替换为随机 UUID（后者未在原提示中说明）。未配置此项节点直接失效。",
            defaultValue = "null")
    private static final String ID = "id";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "播放时间",
            labelZh = "技能节点",
            tooltip = "[time] 即使播放时间结束 也不会让特效立即结束",
            tooltipZh = "特效播放时长，随技能等级缩放，默认 1。时间到了特效不会被立刻强制中断，而是自然收尾。")
    private static final String TIME = "time";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "俯仰角生效",
            labelZh = "技能节点",
            tooltip = "[look] 开启后特效会随着玩家的视角改变而上下倾斜",
            tooltipZh = "开启后特效随玩家视角俯仰而上下倾斜，默认 False。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "False")
    private static final String LOOK = "look";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "播放点X偏移",
            labelZh = "参数7",
            tooltip = "[ax] 播放特效的中心点将会向玩家前/后偏移",
            tooltipZh = "特效中心点沿玩家前/后方向的偏移，随技能等级缩放，默认 1（注意默认不是 0）。")
    private static final String AX = "ax";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "播放点Y偏移",
            labelZh = "参数8",
            tooltip = "[ay] 播放特效的中心点将会向玩家上/下偏移",
            tooltipZh = "特效中心点沿上/下方向的偏移，随技能等级缩放，默认 1。")
    private static final String AY = "ay";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "播放点Z偏移",
            labelZh = "参数9",
            tooltip = "[az] 播放特效的中心点将会向玩家左/右偏移",
            tooltipZh = "特效中心点沿左/右方向的偏移，随技能等级缩放，默认 1。")
    private static final String AZ = "az";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "播放点俯仰角偏移",
            labelZh = "技能节点",
            tooltip = "[ayaw] 额外的向左右进行偏移",
            tooltipZh = "额外的左右角度偏移，随技能等级缩放，默认 1。")
    private static final String AYAW = "ayaw";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "播放点偏航角偏移",
            labelZh = "技能节点",
            tooltip = "[apith] !需要开启偏航角生效! 对偏航角进行偏移",
            tooltipZh = "俯仰角方向的额外偏移，需要配合角度生效开关才有意义，随技能等级缩放，默认 1。")
    private static final String APITH = "apith";

    @Override
    public String getKey() {
        return "snow storm";
    }


    /**
     * @param caster  caster of the skill
     * @param level   level of the skill
     * @param targets targets to execute on
     */
    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        if (!settings.has(ID)) {
            return false;
        }
        String name = settings.getString(NAME);
        String id = settings.getString(ID).replace("{uuid}", caster.getUniqueId().toString()).replace("{random}", UUID.randomUUID().toString());
        int time = (int) parseValues(caster, TIME, level, 1);
        boolean look = Boolean.parseBoolean(settings.getString(LOOK));
        double ax = parseValues(caster, AX, level, 1);
        double ay = parseValues(caster, AY, level, 1);
        double az = parseValues(caster, AZ, level, 1);
        double ayaw = parseValues(caster, AYAW, level, 1);
        double apith = parseValues(caster, APITH, level, 1);
        for (LivingEntity target : targets) {
            CommandParticle.INSTANCE.send(target, name, id,
                    time, look, ax, ay, az, ayaw, apith
            );
        }
        return true;
    }
}
