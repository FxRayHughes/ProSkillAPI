/**
 * SkillAPI
 * com.sucy.skill.dynamic.condition.NameCondition
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
package com.sucy.skill.dynamic.condition;

import org.bukkit.entity.LivingEntity;

import java.util.regex.Pattern;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * A condition for dynamic skills that requires the target to have a specified potion effect
 */
@SkillNode(
        key = "name",
        name = "Name",
        nameZh = "检查名称",
        description = "Applies child components when the target has a name matching the settings.",
        descriptionZh = "检查目标的自定义名称（CustomName）。关闭正则时按子串包含判断，开启正则时按正则部分匹配；“包含文本”为否则把结果取反。注意只看自定义名称：没有设置自定义名的实体、以及普通玩家都一律不通过；比对时不去除颜色符号。",
        container = true)
public class NameCondition extends ConditionComponent {
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Contains Text",
            labelZh = "包含文本",
            tooltip = "[contains] Whether or not the target should have a name containing the text",
            tooltipZh = "只有值恰好等于“false”（忽略大小写）才要求名称不匹配，其余任何值都要求名称匹配。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "True")
    private static final String CONTAINS = "contains";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Regex",
            labelZh = "正则表达式",
            tooltip = "[regex] Whether or not the text is formatted as regex. If you do not know what regex is, ignore this option",
            tooltipZh = "只有值恰好等于“true”（忽略大小写）才把文本当正则处理（部分匹配），否则按普通子串包含判断。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "False")
    private static final String REGEX    = "regex";
    private static final String STRING   = "str";

    @Override
    boolean test(final LivingEntity caster, final int level, final LivingEntity target) {
        boolean contains = !settings.getString(CONTAINS, "true").toLowerCase().equals("false");
        boolean regex = settings.getString(REGEX, "false").toLowerCase().equals("true");
        String str = settings.getString(STRING, "");

        String name = target.getCustomName();
        return name != null && (regex
                ? Pattern.compile(str).matcher(name).find() == contains
                : name.contains(str) == contains);
    }

    @Override
    public String getKey() {
        return "name";
    }
}
