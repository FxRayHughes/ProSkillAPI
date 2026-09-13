/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.FlagMechanic
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

import com.sucy.skill.dynamic.FlagKeys;
import com.sucy.skill.api.util.FlagManager;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Applies a flag to each target
 */
@SkillNode(
        key = "flag",
        name = "Flag",
        nameZh = "标记",
        description = "Marks the target with a flag for a duration. Flags can be checked by other triggers, spells or the related for interesting synergies and effects.",
        descriptionZh = "给每个目标打上一个带时限的自定义标记，供「标记条件」等节点后续检查，用来做叠层、状态、连招判定。标记键里的 {uuid} 会被替换成施法者 UUID，可用于区分不同施法者的标记。未填写引用键或目标列表为空时不执行。")
public class FlagMechanic extends MechanicComponent {
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Key",
            labelZh = "引用键",
            tooltip = "[key] The unique string for the flag. Use the same key when checking it in a Flag Condition.",
            tooltipZh = "标记的唯一字符串，检查标记的节点必须使用完全相同的键。其中 {uuid} 会被替换为施法者的 UUID，{player} 会被替换为施法者名称（非玩家实体退回实体类型名）；用它们可让每个施法者拥有独立的标记。UUID 不随改名变化，名称可读但玩家改名后旧标记会失联。此项为必填，缺失则整个节点不执行。",
            defaultValue = "key")
    private static final String KEY = "key";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Seconds",
            labelZh = "秒数",
            tooltip = "[seconds] The duration the flag should be set for. To set one indefinitely, use Flag Toggle.",
            tooltipZh = "标记持续秒数，内部乘 20 转为 tick，到期自动移除。若要设置永不过期的标记请改用「标记切换」。数值随技能等级/属性变化，默认 3 秒。")
    private static final String SECONDS = "seconds";

    @Override
    public String getKey() {
        return "flag";
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
        if (targets.size() == 0 || !settings.has(KEY)) {
            return false;
        }

        String key = FlagKeys.resolve(settings.getString(KEY, ""), caster);
        double seconds = parseValues(caster, SECONDS, level, 3.0);
        int ticks = (int) (seconds * 20);
        for (LivingEntity target : targets) {
            FlagManager.addFlag(target, key, ticks);
        }
        return targets.size() > 0;
    }
}
