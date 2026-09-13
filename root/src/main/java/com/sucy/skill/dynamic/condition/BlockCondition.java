/**
 * SkillAPI
 * com.sucy.skill.dynamic.condition.BlockCondition
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
package com.sucy.skill.dynamic.condition;

import com.rit.sucy.config.parse.DataSection;
import com.sucy.skill.dynamic.DynamicSkill;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;

import java.util.Set;
import java.util.stream.Collectors;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

@SkillNode(
        key = "block",
        name = "Block",
        nameZh = "检查方块",
        description = "Applies child components if the target is currently standing on a block of the given type.",
        descriptionZh = "检查目标脚下或脚部所在方块的材质。类型以“In Block”结尾时查目标所在的那一格，否则查正下方一格；类型以“Not”开头时把结果取反。材质名转大写、空格换下划线后与方块枚举名精确比对。",
        container = true)
public class BlockCondition extends ConditionComponent {
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Material",
            labelZh = "材质",
            tooltip = "[material] The type of the block to require the targets to stand on",
            tooltipZh = "允许的方块材质列表，任一命中即算匹配。名称转大写并把空格替换为下划线，须与服务端 Material 枚举名一致。",
            options = {"Acacia Door", "Acacia Door Item", "Acacia Fence", "Acacia Fence Gate", "Acacia Stairs", "Activator Rail", "Air", "Anvil", "Apple", "Armor Stand", "Arrow", "Baked Potato", "Banner", "Barrier", "Beacon", "Bed", "Bed Block", "Bedrock", "Beetroot", "Beetroot Block", "Beetroot Seeds", "Beetroot Soup", "Birch Door", "Birch Door Item", "Birch Fence", "Birch Fence Gate", "Birch Wood Stairs", "Black Glazed Terracotta", "Black Shulker Box", "Blaze Powder", "Blaze Rod", "Blue Glazed Terracotta", "Blue Shulker Box", "Boat", "Boat Acacia", "Boat Birch", "Boat Dark Oak", "Boat Jungle", "Boat Spruce", "Bone", "Bone Block", "Book", "Book And Quill", "Bookshelf", "Bow", "Bowl", "Bread", "Brewing Stand", "Brewing Stand Item", "Brick", "Brick Stairs", "Brown Glazed Terracotta", "Brown Mushroom", "Brown Shulker Box", "Bucket", "Burning Furnace", "Cactus", "Cake", "Cake Block", "Carpet", "Carrot", "Carrot Item", "Carrot Stick", "Cauldron", "Cauldron Item", "Chainmail Boots", "Chainmail Chestplate", "Chainmail Helmet", "Chainmail Leggings", "Chest", "Chorus Flower", "Chorus Fruit", "Chorus Fruit Popped", "Chorus Plant", "Clay", "Clay Ball", "Clay Brick", "Coal", "Coal Block", "Coal Ore", "Cobble Wall", "Cobblestone", "Cobblestone Stairs", "Cocoa", "Command", "Command Chain", "Command Minecart", "Command Repeating", "Compass", "Concrete", "Concrete Powder", "Cooked Beef", "Cooked Chicken", "Cooked Fish", "Cooked Mutton", "Cooked Rabbit", "Cookie", "Crops", "Cyan Glazed Terracotta", "Cyan Shulker Box", "Dark Oak Door", "Dark Oak Door Item", "Dark Oak Fence", "Dark Oak Fence Gate", "Dark Oak Stairs", "Daylight Detector", "Daylight Detector Inverted", "Dead Bush", "Detector Rail", "Diamond", "Diamond Axe", "Diamond Barding", "Diamond Block", "Diamond Boots", "Diamond Chestplate", "Diamond Helmet", "Diamond Hoe", "Diamond Leggings", "Diamond Ore", "Diamond Pickaxe", "Diamond Spade", "Diamond Sword", "Diode", "Diode Block Off", "Diode Block On", "Dirt", "Dispenser", "Double Plant", "Double Step", "Double Stone Slab2", "Dragon Egg", "Dragons Breath", "Dropper", "Egg", "Elytra", "Emerald", "Emerald Block", "Emerald Ore", "Empty Map", "Enchanted Book", "Enchantment Table", "End Bricks", "End Crystal", "End Gateway", "End Rod", "Ender Chest", "Ender Pearl", "Ender Portal", "Ender Portal Frame", "Ender Stone", "Exp Bottle", "Explosive Minecart", "Eye Of Ender", "Feather", "Fence", "Fence Gate", "Fermented Spider Eye", "Fire", "Fireball", "Firework", "Firework Charge", "Fishing Rod", "Flint", "Flint And Steel", "Flower Pot", "Flower Pot Item", "Frosted Ice", "Furnace", "Ghast Tear", "Glass", "Glass Bottle", "Glowing Redstone Ore", "Glowstone", "Glowstone Dust", "Gold Axe", "Gold Barding", "Gold Block", "Gold Boots", "Gold Chestplate", "Gold Helmet", "Gold Hoe", "Gold Ingot", "Gold Leggings", "Gold Nugget", "Gold Ore", "Gold Pickaxe", "Gold Plate", "Gold Record", "Gold Spade", "Gold Sword", "Golden Apple", "Golden Carrot", "Grass", "Grass Path", "Gravel", "Gray Glazed Terracotta", "Gray Shulker Box", "Green Glazed Terracotta", "Green Record", "Green Shulker Box", "Grilled Pork", "Hard Clay", "Hay Block", "Hopper", "Hopper Minecart", "Huge Mushroom 1", "Huge Mushroom 2", "Ice", "Ink Sack", "Iron Axe", "Iron Barding", "Iron Block", "Iron Boots", "Iron Chestplate", "Iron Door", "Iron Door Block", "Iron Fence", "Iron Helmet", "Iron Hoe", "Iron Ingot", "Iron Leggings", "Iron Nugget", "Iron Ore", "Iron Pickaxe", "Iron Plate", "Iron Spade", "Iron Sword", "Iron Trapdoor", "Item Frame", "Jack O Lantern", "Jukebox", "Jungle Door", "Jungle Door Item", "Jungle Fence", "Jungle Fence Gate", "Jungle Wood Stairs", "Knowledge Book", "Ladder", "Lapis Block", "Lapis Ore", "Lava", "Lava Bucket", "Leash", "Leather", "Leather Boots", "Leather Chestplate", "Leather Helmet", "Leather Leggings", "Leaves", "Leaves 2", "Lever", "Light Blue Glazed Terracotta", "Light Blue Shulker Box", "Lime Glazed Terracotta", "Lime Shulker Box", "Lingering Potion", "Log", "Log 2", "Long Grass", "Magenta Glazed Terracotta", "Magenta Shulker Box", "Magma", "Magma Cream", "Map", "Melon", "Melon Block", "Melon Seeds", "Melon Stem", "Milk Bucket", "Minecart", "Mob Spawner", "Monster Egg", "Monster Eggs", "Mossy Cobblestone", "Mushroom Soup", "Mutton", "Mycel", "Name Tag", "Nether Brick", "Nether Brick Item", "Nether Brick Stairs", "Nether Fence", "Nether Stalk", "Nether Star", "Nether Wart Block", "Nether Warts", "Netherrack", "Note Block", "Observer", "Obsidian", "Orange Glazed Terracotta", "Orange Shulker Box", "Packed Ice", "Painting", "Paper", "Pink Glazed Terracotta", "Pink Shulker Box", "Piston Base", "Piston Extension", "Piston Moving Piece", "Piston Sticky Base", "Poisonous Potato", "Pork", "Portal", "Potato", "Potato Item", "Potion", "Powered Minecart", "Powered Rail", "Prismarine", "Prismarine Crystals", "Prismarine Shard", "Pumpkin", "Pumpkin Pie", "Pumpkin Seeds", "Pumpkin Stem", "Purple Glazed Terracotta", "Purple Shulker Box", "Purpur Block", "Purpur Double Slab", "Purpur Pillar", "Purpur Slab", "Purpur Stairs", "Quartz", "Quartz Block", "Quartz Ore", "Quartz Stairs", "Rabbit", "Rabbit Foot", "Rabbit Hide", "Rabbit Stew", "Rails", "Raw Beef", "Raw Chicken", "Raw Fish", "Record 10", "Record 11", "Record 12", "Record 3", "Record 4", "Record 5", "Record 6", "Record 7", "Record 8", "Record 9", "Red Glazed Terracotta", "Red Mushroom", "Red Nether Brick", "Red Rose", "Red Sandstone", "Red Sandstone Stairs", "Red Shulker Box", "Redstone", "Redstone Block", "Redstone Comparator", "Redstone Comparator Off", "Redstone Comparator On", "Redstone Lamp Off", "Redstone Lamp On", "Redstone Ore", "Redstone Torch Off", "Redstone Torch On", "Redstone Wire", "Rotten Flesh", "Saddle", "Sand", "Sandstone", "Sandstone Stairs", "Sapling", "Sea Lantern", "Seeds", "Shears", "Shield", "Shulker Shell", "Sign", "Sign Post", "Silver Glazed Terracotta", "Silver Shulker Box", "Skull", "Skull Item", "Slime Ball", "Slime Block", "Smooth Brick", "Smooth Stairs", "Snow", "Snow Ball", "Snow Block", "Soil", "Soul Sand", "Speckled Melon", "Spectral Arrow", "Spider Eye", "Splash Potion", "Sponge", "Spruce Door", "Spruce Door Item", "Spruce Fence", "Spruce Fence Gate", "Spruce Wood Stairs", "Stained Clay", "Stained Glass", "Stained Glass Pane", "Standing Banner", "Stationary Lava", "Stationary Water", "Step", "Stick", "Stone", "Stone Axe", "Stone Button", "Stone Hoe", "Stone Pickaxe", "Stone Plate", "Stone Slab2", "Stone Spade", "Stone Sword", "Storage Minecart", "String", "Structure Block", "Structure Void", "Sugar", "Sugar Cane", "Sugar Cane Block", "Sulphur", "Thin Glass", "Tipped Arrow", "Tnt", "Torch", "Totem", "Trap Door", "Trapped Chest", "Tripwire", "Tripwire Hook", "Vine", "Wall Banner", "Wall Sign", "Watch", "Water", "Water Bucket", "Water Lily", "Web", "Wheat", "White Glazed Terracotta", "White Shulker Box", "Wood", "Wood Axe", "Wood Button", "Wood Door", "Wood Double Step", "Wood Hoe", "Wood Pickaxe", "Wood Plate", "Wood Spade", "Wood Stairs", "Wood Step", "Wood Sword", "Wooden Door", "Wool", "Workbench", "Written Book", "Yellow Flower", "Yellow Glazed Terracotta", "Yellow Shulker Box"},
            optionsZh = {"金合欢木门", "金合欢木门物品", "金合欢木栅栏", "金合欢木栅栏", "金合欢木楼梯", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "箭", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "桦木门", "桦木门物品", "桦木栅栏", "桦木栅栏", "桦木木头楼梯", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "金合欢木", "桦木", "深色橡木", "丛林木", "云杉", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "弓", "其他材质", "其他材质", "其他材质", "物品", "其他材质", "楼梯", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "物品", "其他材质", "其他材质", "物品", "靴子", "胸甲", "头盔", "护腿", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "楼梯", "其他材质", "命令", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "深色橡木门", "深色橡木门物品", "深色橡木栅栏", "深色橡木栅栏", "深色橡木楼梯", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "斧", "其他材质", "其他材质", "靴子", "胸甲", "头盔", "其他材质", "护腿", "其他材质", "镐", "其他材质", "剑", "其他材质", "其他材质", "其他材质", "泥土", "其他材质", "其他材质", "脚步", "石头", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "石头", "其他材质", "其他材质", "其他材质", "其他材质", "栅栏", "栅栏", "其他材质", "火焰", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "物品", "其他材质", "其他材质", "其他材质", "玻璃", "玻璃", "其他材质", "其他材质", "其他材质", "斧", "其他材质", "其他材质", "靴子", "胸甲", "头盔", "其他材质", "其他材质", "护腿", "其他材质", "其他材质", "镐", "其他材质", "其他材质", "其他材质", "剑", "其他材质", "其他材质", "草方块", "草方块", "沙砾", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "斧", "其他材质", "其他材质", "靴子", "胸甲", "门", "门", "栅栏", "头盔", "其他材质", "其他材质", "护腿", "其他材质", "其他材质", "镐", "其他材质", "其他材质", "剑", "其他材质", "物品", "其他材质", "其他材质", "丛林木门", "丛林木门物品", "丛林木栅栏", "丛林木栅栏", "丛林木木头楼梯", "其他材质", "其他材质", "其他材质", "其他材质", "熔岩", "熔岩", "其他材质", "其他材质", "靴子", "胸甲", "头盔", "护腿", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "药水", "其他材质", "其他材质", "草方块", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "物品", "楼梯", "栅栏", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "物品", "药水效果", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "台阶", "其他材质", "台阶", "楼梯", "其他材质", "其他材质", "其他材质", "楼梯", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "楼梯", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "沙子", "其他材质", "楼梯", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "物品", "其他材质", "其他材质", "其他材质", "楼梯", "其他材质", "其他材质", "其他材质", "其他材质", "沙子", "其他材质", "箭", "其他材质", "药水", "其他材质", "云杉门", "云杉门物品", "云杉栅栏", "云杉栅栏", "云杉木头楼梯", "其他材质", "玻璃", "玻璃", "其他材质", "熔岩", "水", "脚步", "其他材质", "石头", "石头斧", "石头", "石头", "石头镐", "石头", "石头", "石头", "石头剑", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "玻璃", "箭", "其他材质", "其他材质", "其他材质", "门", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "水", "水", "水", "其他材质", "其他材质", "其他材质", "其他材质", "木头", "木头斧", "木头", "木头门", "木头脚步", "木头", "木头镐", "木头", "木头", "木头楼梯", "木头脚步", "木头剑", "门", "羊毛", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质"},
            defaultValue = "Dirt")
    private static final String MATERIAL = "material";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Type",
            labelZh = "类型",
            tooltip = "[standing] Specifies which block to check and whether or not it should match the selected mateiral. \"On Block\" is directly below the player while \"In Block\" is the block a player's feet are in.",
            tooltipZh = "同时决定查哪一格和是否取反：以“not”开头表示取反，以“in block”结尾表示查目标所在方块，否则查正下方一格。",
            options = {"On Block", "Not On Block", "In Block", "Not In Block"},
            optionsZh = {"可选值1", "可选值2", "可选值3", "可选值4"},
            defaultValue = "On Block")
    private static final String STANDING = "standing";

    private Set<String> types;
    private boolean negated;
    private boolean in;

    @Override
    public String getKey() {
        return "block";
    }

    @Override
    public void load(DynamicSkill skill, DataSection config) {
        super.load(skill, config);
        final String type = settings.getString(STANDING, "On Block").toLowerCase();
        negated = type.startsWith("not");
        in = type.endsWith("in block");
        types = settings.getStringList(MATERIAL).stream()
                .map(s -> s.toUpperCase().replace(' ', '_'))
                .collect(Collectors.toSet());
    }

    @Override
    boolean test(final LivingEntity caster, final int level, final LivingEntity target) {
        final Block in = target.getLocation().getBlock();
        final Block tested = this.in ? in : in.getRelative(BlockFace.DOWN);
        return negated != types.contains(tested.getType().name());
    }
}
