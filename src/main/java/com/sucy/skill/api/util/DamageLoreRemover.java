/**
 * SkillAPI
 * com.sucy.skill.api.util.DamageLoreRemover
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

import com.sucy.skill.nms.NmsProvider;
import org.bukkit.inventory.ItemStack;

/**
 * <p>Utility class for removing vanilla damage lore lines from items.</p>
 */
public class DamageLoreRemover
{
    /**
     * <p>Removes the vanilla damage lore from tools.</p>
     * <p>If you pass in something other than a tool this will do nothing.</p>
     * <p>Version modules decide whether NMS tags or Bukkit item flags are safest.</p>
     *
     * @param item tool to remove the lore from
     *
     * @return the tool without the damage lore
     */
    public static ItemStack removeAttackDmg(ItemStack item)
    {
        return NmsProvider.bridge().removeAttackDmg(item);
    }
}
