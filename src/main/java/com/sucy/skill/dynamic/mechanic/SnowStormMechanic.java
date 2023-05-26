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

/**
 * 释放一个mm技能
 */
public class SnowStormMechanic extends MechanicComponent {

    private static final String NAME = "name";
    private static final String ID = "id";
    private static final String TIME = "time";
    private static final String LOOK = "look";
    private static final String AX = "ax";
    private static final String AY = "ay";
    private static final String AZ = "az";
    private static final String AYAW = "ayaw";
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
