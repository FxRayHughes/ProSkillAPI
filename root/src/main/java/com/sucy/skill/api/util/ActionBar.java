/**
 * SkillAPI
 * com.sucy.skill.api.util.ActionBar
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
package com.sucy.skill.api.util;

import com.rit.sucy.text.TextFormatter;
import com.sucy.skill.log.Logger;
import com.sucy.skill.nms.NmsProvider;
import org.bukkit.entity.Player;

/**
 * Handles sending text to players using the action bar.
 */
public class ActionBar
{
    /**
     * Checks whether or not the action bar is supported
     *
     * @return true if supported, false otherwise
     */
    public static boolean isSupported()
    {
        return NmsProvider.bridge().isActionBarSupported();
    }

    /**
     * Shows an action bar message to the given player
     *
     * @param player  player to show the message to
     * @param message message to show
     */
    public static void show(Player player, String message)
    {
        if (!isSupported()) return;

        if (!NmsProvider.bridge().sendActionBar(player, TextFormatter.colorString(message))) {
            Logger.bug("Failed to apply Action Bar");
        }
    }
}
