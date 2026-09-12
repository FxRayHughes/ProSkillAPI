/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.ItemMechanic
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

import com.rit.sucy.text.TextFormatter;
import com.sucy.skill.SkillAPI;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Gives an item to each player target
 */
@SkillNode(
        key = "item",
        name = "Item",
        nameZh = "物品",
        description = "Gives each player target the item defined by the settings.",
        descriptionZh = "给每个玩家目标发放按配置构造的物品，非玩家目标会被跳过。材质经兼容层解析，1.13 之后的方块/物品拆分与旧版数据值差异会自动处理，材质无法识别时整个节点直接失败。物品栏放不下时多余的物品会丢失（不会掉落在地）。注意：只要目标列表非空就返回成功，无法据此判断玩家是否真的收到了物品。")
public class ItemMechanic extends MechanicComponent
{
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Material",
            labelZh = "材质",
            tooltip = "[material] The type of item to give to the player",
            tooltipZh = "要给出的物品材质，内部转大写并把空格换成下划线后经兼容层解析；无法解析时整个节点返回失败。默认 Arrow。",
            options = {"Acacia Door", "Acacia Door Item", "Acacia Fence", "Acacia Fence Gate", "Acacia Stairs", "Activator Rail", "Air", "Anvil", "Apple", "Armor Stand", "Arrow", "Baked Potato", "Banner", "Barrier", "Beacon", "Bed", "Bed Block", "Bedrock", "Beetroot", "Beetroot Block", "Beetroot Seeds", "Beetroot Soup", "Birch Door", "Birch Door Item", "Birch Fence", "Birch Fence Gate", "Birch Wood Stairs", "Black Glazed Terracotta", "Black Shulker Box", "Blaze Powder", "Blaze Rod", "Blue Glazed Terracotta", "Blue Shulker Box", "Boat", "Boat Acacia", "Boat Birch", "Boat Dark Oak", "Boat Jungle", "Boat Spruce", "Bone", "Bone Block", "Book", "Book And Quill", "Bookshelf", "Bow", "Bowl", "Bread", "Brewing Stand", "Brewing Stand Item", "Brick", "Brick Stairs", "Brown Glazed Terracotta", "Brown Mushroom", "Brown Shulker Box", "Bucket", "Burning Furnace", "Cactus", "Cake", "Cake Block", "Carpet", "Carrot", "Carrot Item", "Carrot Stick", "Cauldron", "Cauldron Item", "Chainmail Boots", "Chainmail Chestplate", "Chainmail Helmet", "Chainmail Leggings", "Chest", "Chorus Flower", "Chorus Fruit", "Chorus Fruit Popped", "Chorus Plant", "Clay", "Clay Ball", "Clay Brick", "Coal", "Coal Block", "Coal Ore", "Cobble Wall", "Cobblestone", "Cobblestone Stairs", "Cocoa", "Command", "Command Chain", "Command Minecart", "Command Repeating", "Compass", "Concrete", "Concrete Powder", "Cooked Beef", "Cooked Chicken", "Cooked Fish", "Cooked Mutton", "Cooked Rabbit", "Cookie", "Crops", "Cyan Glazed Terracotta", "Cyan Shulker Box", "Dark Oak Door", "Dark Oak Door Item", "Dark Oak Fence", "Dark Oak Fence Gate", "Dark Oak Stairs", "Daylight Detector", "Daylight Detector Inverted", "Dead Bush", "Detector Rail", "Diamond", "Diamond Axe", "Diamond Barding", "Diamond Block", "Diamond Boots", "Diamond Chestplate", "Diamond Helmet", "Diamond Hoe", "Diamond Leggings", "Diamond Ore", "Diamond Pickaxe", "Diamond Spade", "Diamond Sword", "Diode", "Diode Block Off", "Diode Block On", "Dirt", "Dispenser", "Double Plant", "Double Step", "Double Stone Slab2", "Dragon Egg", "Dragons Breath", "Dropper", "Egg", "Elytra", "Emerald", "Emerald Block", "Emerald Ore", "Empty Map", "Enchanted Book", "Enchantment Table", "End Bricks", "End Crystal", "End Gateway", "End Rod", "Ender Chest", "Ender Pearl", "Ender Portal", "Ender Portal Frame", "Ender Stone", "Exp Bottle", "Explosive Minecart", "Eye Of Ender", "Feather", "Fence", "Fence Gate", "Fermented Spider Eye", "Fire", "Fireball", "Firework", "Firework Charge", "Fishing Rod", "Flint", "Flint And Steel", "Flower Pot", "Flower Pot Item", "Frosted Ice", "Furnace", "Ghast Tear", "Glass", "Glass Bottle", "Glowing Redstone Ore", "Glowstone", "Glowstone Dust", "Gold Axe", "Gold Barding", "Gold Block", "Gold Boots", "Gold Chestplate", "Gold Helmet", "Gold Hoe", "Gold Ingot", "Gold Leggings", "Gold Nugget", "Gold Ore", "Gold Pickaxe", "Gold Plate", "Gold Record", "Gold Spade", "Gold Sword", "Golden Apple", "Golden Carrot", "Grass", "Grass Path", "Gravel", "Gray Glazed Terracotta", "Gray Shulker Box", "Green Glazed Terracotta", "Green Record", "Green Shulker Box", "Grilled Pork", "Hard Clay", "Hay Block", "Hopper", "Hopper Minecart", "Huge Mushroom 1", "Huge Mushroom 2", "Ice", "Ink Sack", "Iron Axe", "Iron Barding", "Iron Block", "Iron Boots", "Iron Chestplate", "Iron Door", "Iron Door Block", "Iron Fence", "Iron Helmet", "Iron Hoe", "Iron Ingot", "Iron Leggings", "Iron Nugget", "Iron Ore", "Iron Pickaxe", "Iron Plate", "Iron Spade", "Iron Sword", "Iron Trapdoor", "Item Frame", "Jack O Lantern", "Jukebox", "Jungle Door", "Jungle Door Item", "Jungle Fence", "Jungle Fence Gate", "Jungle Wood Stairs", "Knowledge Book", "Ladder", "Lapis Block", "Lapis Ore", "Lava", "Lava Bucket", "Leash", "Leather", "Leather Boots", "Leather Chestplate", "Leather Helmet", "Leather Leggings", "Leaves", "Leaves 2", "Lever", "Light Blue Glazed Terracotta", "Light Blue Shulker Box", "Lime Glazed Terracotta", "Lime Shulker Box", "Lingering Potion", "Log", "Log 2", "Long Grass", "Magenta Glazed Terracotta", "Magenta Shulker Box", "Magma", "Magma Cream", "Map", "Melon", "Melon Block", "Melon Seeds", "Melon Stem", "Milk Bucket", "Minecart", "Mob Spawner", "Monster Egg", "Monster Eggs", "Mossy Cobblestone", "Mushroom Soup", "Mutton", "Mycel", "Name Tag", "Nether Brick", "Nether Brick Item", "Nether Brick Stairs", "Nether Fence", "Nether Stalk", "Nether Star", "Nether Wart Block", "Nether Warts", "Netherrack", "Note Block", "Observer", "Obsidian", "Orange Glazed Terracotta", "Orange Shulker Box", "Packed Ice", "Painting", "Paper", "Pink Glazed Terracotta", "Pink Shulker Box", "Piston Base", "Piston Extension", "Piston Moving Piece", "Piston Sticky Base", "Poisonous Potato", "Pork", "Portal", "Potato", "Potato Item", "Potion", "Powered Minecart", "Powered Rail", "Prismarine", "Prismarine Crystals", "Prismarine Shard", "Pumpkin", "Pumpkin Pie", "Pumpkin Seeds", "Pumpkin Stem", "Purple Glazed Terracotta", "Purple Shulker Box", "Purpur Block", "Purpur Double Slab", "Purpur Pillar", "Purpur Slab", "Purpur Stairs", "Quartz", "Quartz Block", "Quartz Ore", "Quartz Stairs", "Rabbit", "Rabbit Foot", "Rabbit Hide", "Rabbit Stew", "Rails", "Raw Beef", "Raw Chicken", "Raw Fish", "Record 10", "Record 11", "Record 12", "Record 3", "Record 4", "Record 5", "Record 6", "Record 7", "Record 8", "Record 9", "Red Glazed Terracotta", "Red Mushroom", "Red Nether Brick", "Red Rose", "Red Sandstone", "Red Sandstone Stairs", "Red Shulker Box", "Redstone", "Redstone Block", "Redstone Comparator", "Redstone Comparator Off", "Redstone Comparator On", "Redstone Lamp Off", "Redstone Lamp On", "Redstone Ore", "Redstone Torch Off", "Redstone Torch On", "Redstone Wire", "Rotten Flesh", "Saddle", "Sand", "Sandstone", "Sandstone Stairs", "Sapling", "Sea Lantern", "Seeds", "Shears", "Shield", "Shulker Shell", "Sign", "Sign Post", "Silver Glazed Terracotta", "Silver Shulker Box", "Skull", "Skull Item", "Slime Ball", "Slime Block", "Smooth Brick", "Smooth Stairs", "Snow", "Snow Ball", "Snow Block", "Soil", "Soul Sand", "Speckled Melon", "Spectral Arrow", "Spider Eye", "Splash Potion", "Sponge", "Spruce Door", "Spruce Door Item", "Spruce Fence", "Spruce Fence Gate", "Spruce Wood Stairs", "Stained Clay", "Stained Glass", "Stained Glass Pane", "Standing Banner", "Stationary Lava", "Stationary Water", "Step", "Stick", "Stone", "Stone Axe", "Stone Button", "Stone Hoe", "Stone Pickaxe", "Stone Plate", "Stone Slab2", "Stone Spade", "Stone Sword", "Storage Minecart", "String", "Structure Block", "Structure Void", "Sugar", "Sugar Cane", "Sugar Cane Block", "Sulphur", "Thin Glass", "Tipped Arrow", "Tnt", "Torch", "Totem", "Trap Door", "Trapped Chest", "Tripwire", "Tripwire Hook", "Vine", "Wall Banner", "Wall Sign", "Watch", "Water", "Water Bucket", "Water Lily", "Web", "Wheat", "White Glazed Terracotta", "White Shulker Box", "Wood", "Wood Axe", "Wood Button", "Wood Door", "Wood Double Step", "Wood Hoe", "Wood Pickaxe", "Wood Plate", "Wood Spade", "Wood Stairs", "Wood Step", "Wood Sword", "Wooden Door", "Wool", "Workbench", "Written Book", "Yellow Flower", "Yellow Glazed Terracotta", "Yellow Shulker Box"},
            optionsZh = {"金合欢木门", "金合欢木门物品", "金合欢木栅栏", "金合欢木栅栏", "金合欢木楼梯", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "箭", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "桦木门", "桦木门物品", "桦木栅栏", "桦木栅栏", "桦木木头楼梯", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "金合欢木", "桦木", "深色橡木", "丛林木", "云杉", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "弓", "其他材质", "其他材质", "其他材质", "物品", "其他材质", "楼梯", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "物品", "其他材质", "其他材质", "物品", "靴子", "胸甲", "头盔", "护腿", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "楼梯", "其他材质", "命令", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "深色橡木门", "深色橡木门物品", "深色橡木栅栏", "深色橡木栅栏", "深色橡木楼梯", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "斧", "其他材质", "其他材质", "靴子", "胸甲", "头盔", "其他材质", "护腿", "其他材质", "镐", "其他材质", "剑", "其他材质", "其他材质", "其他材质", "泥土", "其他材质", "其他材质", "脚步", "石头", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "石头", "其他材质", "其他材质", "其他材质", "其他材质", "栅栏", "栅栏", "其他材质", "火焰", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "物品", "其他材质", "其他材质", "其他材质", "玻璃", "玻璃", "其他材质", "其他材质", "其他材质", "斧", "其他材质", "其他材质", "靴子", "胸甲", "头盔", "其他材质", "其他材质", "护腿", "其他材质", "其他材质", "镐", "其他材质", "其他材质", "其他材质", "剑", "其他材质", "其他材质", "草方块", "草方块", "沙砾", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "斧", "其他材质", "其他材质", "靴子", "胸甲", "门", "门", "栅栏", "头盔", "其他材质", "其他材质", "护腿", "其他材质", "其他材质", "镐", "其他材质", "其他材质", "剑", "其他材质", "物品", "其他材质", "其他材质", "丛林木门", "丛林木门物品", "丛林木栅栏", "丛林木栅栏", "丛林木木头楼梯", "其他材质", "其他材质", "其他材质", "其他材质", "熔岩", "熔岩", "其他材质", "其他材质", "靴子", "胸甲", "头盔", "护腿", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "药水", "其他材质", "其他材质", "草方块", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "物品", "楼梯", "栅栏", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "物品", "药水效果", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "台阶", "其他材质", "台阶", "楼梯", "其他材质", "其他材质", "其他材质", "楼梯", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "楼梯", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "沙子", "其他材质", "楼梯", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "物品", "其他材质", "其他材质", "其他材质", "楼梯", "其他材质", "其他材质", "其他材质", "其他材质", "沙子", "其他材质", "箭", "其他材质", "药水", "其他材质", "云杉门", "云杉门物品", "云杉栅栏", "云杉栅栏", "云杉木头楼梯", "其他材质", "玻璃", "玻璃", "其他材质", "熔岩", "水", "脚步", "其他材质", "石头", "石头斧", "石头", "石头", "石头镐", "石头", "石头", "石头", "石头剑", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "玻璃", "箭", "其他材质", "其他材质", "其他材质", "门", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "水", "水", "水", "其他材质", "其他材质", "其他材质", "其他材质", "木头", "木头斧", "木头", "木头门", "木头脚步", "木头", "木头镐", "木头", "木头", "木头楼梯", "木头脚步", "木头剑", "门", "羊毛", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质"},
            defaultValue = "Arrow")
    private static final String MATERIAL = "material";
    @SkillField(
            kind = FieldKind.IntValue,
            label = "Amount",
            labelZh = "数量",
            tooltip = "[amount] The quantity of the item to give to the player",
            tooltipZh = "给出的数量，直接作为物品堆叠数量，超过上限时由服务器处理。默认 1。",
            defaultValue = "1")
    private static final String AMOUNT   = "amount";
    @SkillField(
            kind = FieldKind.IntValue,
            label = "Durability",
            labelZh = "耐久度",
            tooltip = "[data] The durability value of the item to give to the player",
            tooltipZh = "耐久/损伤值。服务器配置为旧式耐久时直接写入 durability，否则写入物品的 damage 属性。默认 0。",
            defaultValue = "0")
    private static final String DATA     = "data";
    @SkillField(
            kind = FieldKind.IntValue,
            label = "Data",
            labelZh = "数据值",
            tooltip = "[byte] The data value of the item to give to the player for things such as egg type or wool color",
            tooltipZh = "旧版数据值，用于羊毛颜色、木头种类、刷怪蛋类型等变种。启用技能自定义模型数据时，该值改为写入 CustomModelData（配合材质包用）；未扁平化的旧版本上则写入 MaterialData，且对没有耐久的方块类物品会再写回 durability 以保留颜色/种类。默认 0。",
            defaultValue = "0")
    private static final String BYTE     = "byte";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Custom",
            labelZh = "自定义",
            tooltip = "[custom] Whether or not to apply a custom name/lore to the item",
            tooltipZh = "是否给物品套用自定义名称与描述。默认「否」；为「是」时才会读取「名称」与「描述」两项。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "False")
    private static final String CUSTOM   = "custom";
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Name",
            labelZh = "名称",
            tooltip = "[name] The name of the item",
            tooltipZh = "物品显示名，支持颜色代码；仅在「自定义」为「是」时生效，且留空时不改名。",
            defaultValue = "Name")
    private static final String NAME     = "name";
    @SkillField(
            kind = FieldKind.StringListValue,
            label = "Lore",
            labelZh = "物品描述",
            tooltip = "[lore] The lore text for the item (the text below the name)",
            tooltipZh = "物品描述（名称下方的文本），逐行填写并支持颜色代码；仅在「自定义」为「是」时生效。",
            defaultValue = "")
    private static final String LORE     = "lore";

    @Override
    public String getKey() {
        return "item";
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
        String mat = settings.getString(MATERIAL, "arrow").toUpperCase().replace(" ", "_");
        Material material;
        try
        {
            // Legacy item variants are represented by distinct materials after 1.13.
            material = com.sucy.skill.api.util.MaterialCompat.resolve(mat, settings.getInt(BYTE, 0), true);
            if (material == null) return false;
        }
        catch (Exception ex)
        {
            return false;
        }
        int amount = settings.getInt(AMOUNT, 1);
        int durability = settings.getInt(DATA, 0);
        int data = settings.getInt(BYTE, 0);

        ItemStack item = new ItemStack(material, amount);

        ItemMeta meta = item.getItemMeta();
        if (settings.getString(CUSTOM, "false").toLowerCase().equals("true"))
        {
            String name = TextFormatter.colorString(settings.getString(NAME, ""));
            if (name.length() > 0)
            {
                meta.setDisplayName(name);
            }
            List<String> lore = TextFormatter.colorStringList(settings.getStringList(LORE));
            meta.setLore(lore);
        }
        if (SkillAPI.getSettings().useSkillModelData()) {
            meta.setCustomModelData(data);
        } else if (!com.sucy.skill.api.util.MaterialCompat.isFlattened()) {
            item.setData(new MaterialData(material, (byte) data));
        }
        if (SkillAPI.getSettings().useOldDurability()) {
            item.setItemMeta(meta);
            item.setDurability((short) durability);
        } else {
            if (meta instanceof Damageable) {
                ((Damageable) meta).setDamage(durability);
            }
            item.setItemMeta(meta);
        }

        // Pre-flattening non-tools store their variant in the durability field;
        // applying metadata/damage above must not erase wool colors or wood types.
        if (!com.sucy.skill.api.util.MaterialCompat.isFlattened() && material.getMaxDurability() == 0) {
            item.setDurability((short) data);
        }

        boolean worked = false;
        for (LivingEntity target : targets)
        {
            if (target instanceof Player)
            {
                worked = ((Player) target).getInventory().addItem(item).isEmpty() || worked;
            }
        }
        return targets.size() > 0;
    }
}
