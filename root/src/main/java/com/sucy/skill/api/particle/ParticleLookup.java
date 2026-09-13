/**
 * SkillAPI
 * com.sucy.skill.api.particle.ParticleLookup
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Steven Sucy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
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
package com.sucy.skill.api.particle;

import org.bukkit.Particle;
import com.cryptomorin.xseries.particles.XParticle;

import java.util.HashMap;

public class ParticleLookup
{
    private static final HashMap<String, ParticleType> BY_EDITOR = new HashMap<>();
    private static final HashMap<String, ParticleType> BY_OLD    = new HashMap<>();

    public static Particle find(String key)
    {
        if (key == null || key.trim().isEmpty()) return null;
        // XSeries normalizes renamed particle constants across server versions.
        try {
            Particle particle = XParticle.of(key).map(XParticle::get).orElse(null);
            if (particle != null) return particle;
        } catch (Throwable ignored) {
            // Keep the legacy editor/particle table as a fallback.
        }
        try {
            return Particle.valueOf(key.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            key = key.toLowerCase();
            ParticleType type = BY_EDITOR.get(key);
            if (type == null) {
                try {
                    type = getByName(key);
                } catch (IllegalArgumentException ignored) {
                    // Unknown config text may still be a modern Bukkit/Paper particle alias.
                }
            }
            if (type == null)
                type = BY_OLD.get(key);
            return type == null ? SpigotParticles.findParticle(key) : SpigotParticles.findParticle(type.name());
        }
    }

    public static ParticleType getByEditor(String editorKey)
    {
        return BY_EDITOR.get(editorKey.toLowerCase());
    }

    public static ParticleType getByOld(String oldName)
    {
        return BY_OLD.get(oldName.toLowerCase());
    }

    public static ParticleType getByName(String name)
    {
        return ParticleType.valueOf(name.toUpperCase().replace(" ", "_"));
    }

    public static void register(ParticleType type)
    {
        BY_EDITOR.put(type.editorKey(), type);
        if (type.oldName() != null)
            BY_OLD.put(type.oldName().toLowerCase(), type);
    }
}
