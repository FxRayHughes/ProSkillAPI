/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.ValueMultiplyMechanic
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

import com.sucy.skill.SkillAPI;
import com.sucy.skill.dynamic.DynamicSkill;
import com.sucy.skill.hook.PlaceholderAPIHook;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.util.HashMap;
import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * 使JS运算的内容施加给value
 */
@SkillNode(
        key = "value script",
        name = "Value Script",
        nameZh = "数值脚本",
        description = "使用脚本来定义一个变量",
        descriptionZh = "执行一段 JavaScript，把返回值存入施法者 cast data。脚本可用的变量：api（SkillAPI 实例）、caster（施法者）、skill_level（当前等级）、attribute_[属性名]（属性系统启用时注入）、value_[键名]（当前 cast data 每个键的值），施法者是玩家时还注入 player，并且脚本文本会先经 PlaceholderAPI 替换。前提条件：JVM 必须带 JavaScript 引擎——Java 15 起 Nashorn 已从 JDK 移除，需自行提供实现，否则引擎为 null 会直接报错。脚本抛异常时会包装成 RuntimeException 向外抛出，可能中断整条技能。存入的值是脚本返回值原型（未必是 Double），若被 Value Condition 读取需自行保证是数字。",
        requiresCapabilities = {SkillNode.Capability.SCRIPT_ENGINE})
public class ValueScriptMechanic extends MechanicComponent {
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Key",
            labelZh = "引用键",
            tooltip = "[key] The unique key to store the value under. This key can be used in place of attribute values to use the stored value.",
            tooltipZh = "cast data 里的键名，支持 {uuid} 替换为施法者 UUID。存入脚本的返回值，类型取决于脚本本身。",
            defaultValue = "value")
    private static final String KEY = "key";
    @SkillField(
            kind = FieldKind.StringValue,
            label = "脚本内容",
            labelZh = "技能节点",
            tooltip = "[script] 内置的变量:attribute_[变量名] value_[value数据] api player caster 玩家类型自动替换papi",
            tooltipZh = "要执行的 JavaScript 表达式或语句块，返回值即为存入的内容。可用内置变量：attribute_[属性名]、value_[cast data 键名]、skill_level、api、caster，玩家施法时还有 player 且整段文本会先做 PlaceholderAPI 替换。",
            defaultValue = "script")
    private static final String SCRIPT = "script";

    @Override
    public String getKey() {
        return "value script";
    }

    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        if (targets.size() == 0 || !settings.has(KEY)) {
            return false;
        }

        String key = settings.getString(KEY, "").replace("{uuid}", caster.getUniqueId().toString());
        String script = settings.getString(SCRIPT, "");
        ScriptEngine engine = SkillAPI.scriptEngineManager.getEngineByName("JavaScript");
        if (caster instanceof Player) {
            Player player = (Player) caster;
            script = PlaceholderAPIHook.format(script, player);
            engine.put("player", player);
        }
        parseEngine(caster, level, engine);
        HashMap<String, Object> data = DynamicSkill.getCastData(caster);
        data.forEach((a, b) -> engine.put("value_" + a, b));
        engine.put("api", SkillAPI.singleton);
        engine.put("caster", caster);
        try {
            data.put(key, engine.eval(script));
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
        return true;
    }
}
