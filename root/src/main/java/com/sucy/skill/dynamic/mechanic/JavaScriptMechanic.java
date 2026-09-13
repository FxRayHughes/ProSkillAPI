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

import com.sucy.skill.SkillAPI;
import org.bukkit.entity.LivingEntity;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;

@SkillNode(
        key = "js",
        name = "JavaScript",
        nameZh = "执行脚本",
        requiresCapabilities = {SkillNode.Capability.SCRIPT_ENGINE},
        descriptionZh = "执行外部 JavaScript 脚本文件：读取插件目录下 javascripts/<引用键>.js，向脚本注入 api（插件实例）、caster（施法者）、targets（当前目标列表）三个变量后求值，并把脚本中名为 eval 的变量作为本节点的返回值。需要 JVM 提供可用的 JavaScript 引擎——Java 15 起已移除内置 Nashorn，须自行加入引擎实现，否则会失败。脚本文件缺失或执行报错会抛出运行时异常，而不是静默失败。引用键在编辑器中无对应字段，需直接写进节点的 YAML 设置。")
public class JavaScriptMechanic extends MechanicComponent {
    private static final String KEY = "key";

    @Override
    public String getKey() {
        return "js";
    }

    /**
     * Executes the component
     *
     * @param caster  caster of the skill
     * @param level   level of the skill
     * @param targets targets to apply to
     * @return true if applied to something, false otherwise
     */
    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        if (targets.size() == 0 || !settings.has(KEY)) {
            return false;
        }
        String key = settings.getString(KEY, "").replace("{uuid}", caster.getUniqueId().toString());
        try(FileReader reader = new FileReader(SkillAPI.singleton.getDataFolder() + "/javascripts/" + key + ".js")) {
            ScriptEngine engine = SkillAPI.scriptEngineManager.getEngineByName("JavaScript");
            engine.put("api",SkillAPI.singleton);
            engine.put("caster", caster);
            engine.put("targets", targets);
            engine.eval(reader);
            return (boolean) engine.get("eval");
        } catch (ScriptException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
