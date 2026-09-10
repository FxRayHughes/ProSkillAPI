/**
 * SkillAPI
 * com.sucy.skill.dynamic.TempEntity
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
package com.sucy.skill.dynamic;

import com.sucy.skill.api.particle.target.EffectTarget;
import com.sucy.skill.api.particle.target.FixedTarget;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import java.lang.reflect.Proxy;

/**
 * Dummy entity used for targeting a location in the dynamic system.
 *
 * <p>The dynamic system passes targets around as {@link LivingEntity}, but a
 * location is not an entity, so something has to stand in for one. This type is
 * that stand-in, and it is an interface rather than a class for a structural
 * reason: implementing {@code LivingEntity} by hand meant every method Bukkit
 * added to the interface became a compile error here, and every method it
 * removed became dead code. That list had grown past 700 lines of stubs and
 * broke on each API bump - which is exactly the kind of version coupling the
 * nms modules exist to contain.</p>
 *
 * <p>{@link TempEntityHandler} supplies the behaviour through a dynamic proxy
 * instead. Only the handful of calls a location can genuinely answer are
 * implemented; every other method returns the empty value for its return type.
 * The proxy is generated against whatever {@code LivingEntity} the running
 * server declares, so this code compiles and runs unchanged across the whole
 * supported version range.</p>
 *
 * <p>Callers keep using {@code instanceof TempEntity} exactly as before,
 * because the proxy implements this interface, which extends
 * {@code LivingEntity}.</p>
 */
public interface TempEntity extends LivingEntity {
    /**
     * @param loc location this dummy represents
     * @return a dummy entity standing in for the location
     */
    static TempEntity create(Location loc) {
        return create(new FixedTarget(loc));
    }

    /**
     * @param target effect target this dummy follows
     * @return a dummy entity standing in for the target
     */
    static TempEntity create(EffectTarget target) {
        return (TempEntity) Proxy.newProxyInstance(
                TempEntity.class.getClassLoader(),
                new Class<?>[]{TempEntity.class},
                new TempEntityHandler(target));
    }

    /**
     * @return the target this dummy currently represents
     */
    EffectTarget getEffectTarget();
}
