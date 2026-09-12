/**
 * SkillAPI
 * com.sucy.skill.api.event.ParticleProjectileLaunchEvent
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
package com.sucy.skill.api.event;

import com.sucy.skill.dynamic.mechanic.ParticleAnimationArmorStandMechanic;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * <p>粒子投射物发射时的事件.</p>
 */
public class ParticleAnimationExpireEvent extends Event
{
    private static final HandlerList handlers = new HandlerList();

    private final ParticleAnimationArmorStandMechanic.ParticleTask particleTask;

    /**
     * <p>Initializes a new event.</p>
     * @param particleTask
     */

    /**
     * <p>Initializes a new event.</p>
     *
     * @param particleTask
     */
    public ParticleAnimationExpireEvent(ParticleAnimationArmorStandMechanic.ParticleTask particleTask)
    {
        this.particleTask = particleTask;
    }

    /**
     * <p>Bukkit method for taking care of the event handlers.</p>
     *
     * @return list of event handlers
     */
    @Override
    public HandlerList getHandlers()
    {
        return handlers;
    }

    /**
     * <p>Bukkit method for taking care of the event handlers.</p>
     *
     * @return list of event handlers
     */
    public static HandlerList getHandlerList()
    {
        return handlers;
    }

    public ParticleAnimationArmorStandMechanic.ParticleTask getParticleTask() {
        return particleTask;
    }
}
