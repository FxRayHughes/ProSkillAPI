/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.PotionProjectileMechanic
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
package com.sucy.skill.dynamic.mechanic;

import com.cryptomorin.xseries.XPotion;
import com.rit.sucy.version.VersionManager;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.dynamic.TempEntity;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LingeringPotion;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.sucy.skill.listener.MechanicListener.POTION_PROJECTILE;
import static com.sucy.skill.listener.MechanicListener.SKILL_CASTER;
import static com.sucy.skill.listener.MechanicListener.SKILL_LEVEL;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Heals each target
 */
@SkillNode(
        key = "potion projectile",
        name = "Potion Projectile",
        nameZh = "药水投射物",
        description = "Drops a splash potion from each target that does not apply potion effects by default. This will apply child elements when the potion lands. The targets supplied will be everything hit by the potion. If nothing is hit by the potion, the target will be the location it landed.",
        descriptionZh = "让每个目标投出一个喷溅药水（1.9+ 且开启 linger 时改为滞留药水）。药水本身默认不施加药水效果，只作为视觉与落点判定；落地后执行子节点，子节点的目标是被药水波及的实体按 group 过滤后的结果，若过滤后为空则用落点生成临时实体。type 不是合法的 PotionType 时直接返回 false。构造药水物品时用反射直接写入 ItemMeta，失败则回退成普通 POTION 物品。",
        container = true)
public class PotionProjectileMechanic extends MechanicComponent
{
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Type",
            labelZh = "类型",
            tooltip = "[type] The type of the potion to use for the visuals",
            tooltipZh = "决定药水外观颜色的药水类型（必须是合法 PotionType）。代码里的兜底默认值是 slowness，与标注的 Fire Resistance 不一致；填了非法值节点直接失效。",
            options = {"Absorption", "Blindness", "Confusion", "Damage Resistance", "Fast Digging", "Fire Resistance", "Glowing", "Harm", "Heal", "Health Boost", "Hunger", "Increase Damage", "Invisibility", "Jump", "Levitation", "Luck", "Night Vision", "Poison", "Regeneration", "Saturation", "Slow", "Slow Digging", "Speed", "Unluck", "Water Breathing", "Weakness", "Wither"},
            optionsZh = {"可选值1", "可选值2", "可选值3", "可选值4", "可选值5", "火焰", "可选值7", "可选值8", "可选值9", "可选值10", "可选值11", "可选值12", "可选值13", "可选值14", "可选值15", "可选值16", "可选值17", "可选值18", "可选值19", "饱和度", "可选值21", "可选值22", "速度", "可选值24", "水", "可选值26", "可选值27"},
            defaultValue = "Fire Resistance")
    private static final String POTION = "type";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Group",
            labelZh = "目标阵营",
            tooltip = "[group] The alignment of entities to hit",
            tooltipZh = "命中后保留哪些阵营作为子节点目标：Enemy（默认）、Ally 或 Both（不过滤）。过滤依据是施法者对其能否攻击。",
            options = {"Ally", "Enemy", "Both"},
            optionsZh = {"友方", "敌方", "两者"},
            defaultValue = "Enemy")
    private static final String ALLY   = "group";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Linger",
            labelZh = "滞留",
            tooltip = "[linger] Whether or not the potion should be a lingering potion (for 1.9+ only)",
            tooltipZh = "是否使用滞留药水，默认 False。仅在服务端为 1.9 及以上时生效，低版本会自动退回喷溅药水。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "False")
    private static final String LINGER = "linger";

    @Override
    public String getKey() {
        return "potion projectile";
    }

    /**
     * Executes the component
     *
     * @param caster  caster of the skill
     * @param level   level of the skill
     * @param targets targets to apply to
     *
     * @return true if applied to something, false otherwise
     */
    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets)
    {
        // Get common values
        String potion = settings.getString(POTION, "slowness");
        boolean linger = settings.getString(LINGER, "false").toLowerCase().equals("true") && VersionManager.isVersionAtLeast(VersionManager.V1_9_0);
        // XSeries handles legacy aliases and modern names without linking a
        // version-specific Bukkit PotionType constant at configuration load time.
        PotionType type = XPotion.of(potion).map(XPotion::getPotionType).orElse(null);
        if (type == null) return false;

        Potion p = new Potion(type, 1);
        ItemStack item;
        try
        {
            Material potionMaterial = com.sucy.skill.api.util.MaterialCompat.resolve(
                    linger ? "LINGERING_POTION" : "SPLASH_POTION", 0, true);
            if (potionMaterial == null) return false;
            item = new ItemStack(potionMaterial);
            Field meta = ItemStack.class.getDeclaredField("meta");
            meta.setAccessible(true);
            PotionMeta potionMeta = (PotionMeta) item.getItemMeta();
            potionMeta.setDisplayName("lol");
            meta.set(item, potionMeta);
        }
        catch (Exception ex)
        {
            item = new ItemStack(Material.POTION);
        }
        p.apply(item);

        // Fire from each target
        for (LivingEntity target : targets)
        {
            ThrownPotion thrown = target.launchProjectile(linger ? LingeringPotion.class : ThrownPotion.class);
            SkillAPI.setMeta(thrown, SKILL_LEVEL, level);
            SkillAPI.setMeta(thrown, SKILL_CASTER, caster);
            SkillAPI.setMeta(thrown, POTION_PROJECTILE, this);
            thrown.setItem(item);
        }

        return targets.size() > 0;
    }

    /**
     * The callback for the projectiles that applies child components
     *
     * @param entity potion effect
     * @param hit    the entity hit by the projectile, if any
     */
    public void callback(Entity entity, Collection<LivingEntity> hit)
    {
        ArrayList<LivingEntity> targets = new ArrayList<LivingEntity>(hit);
        String group = settings.getString(ALLY, "enemy").toLowerCase();
        boolean both = group.equals("both");
        boolean ally = group.equals("ally");
        LivingEntity caster = (LivingEntity) SkillAPI.getMeta(entity, SKILL_CASTER);
        int level = SkillAPI.getMetaInt(entity, SKILL_LEVEL);
        Location loc = entity.getLocation();
        for (int i = 0; i < targets.size(); i++)
        {
            if (!both && SkillAPI.getSettings().canAttack(caster, targets.get(i)) == ally)
            {
                targets.remove(i);
                i--;
            }
        }
        if (targets.size() == 0)
        {
            LivingEntity locTarget = TempEntity.create(loc);
            targets.add(locTarget);
        }
        executeChildren(caster, level, targets);
    }
}
