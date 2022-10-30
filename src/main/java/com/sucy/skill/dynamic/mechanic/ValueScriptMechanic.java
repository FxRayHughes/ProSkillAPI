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

/**
 * 使JS运算的内容施加给value
 */
public class ValueScriptMechanic extends MechanicComponent {
    private static final String KEY = "key";
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

        String key = settings.getString(KEY).replace("{uuid}", caster.getUniqueId().toString());
        String script = settings.getString(SCRIPT);
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
