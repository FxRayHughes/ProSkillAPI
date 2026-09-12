/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.BlockMechanic
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

import com.sucy.skill.SkillAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.LivingEntity;
import org.bukkit.material.MaterialData;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Mechanic that changes blocks for a duration before
 * returning them to what they were
 */
@SkillNode(
        key = "block",
        name = "Block",
        nameZh = "方块",
        description = "Changes blocks to the given type of block for a limited duration.",
        descriptionZh = "把目标周围一片区域的方块临时替换成指定方块，到时自动还原成原样（含原方块状态）。区域可选球形或长方体，中心点按前/上/右偏移从目标位置算出。多个技能覆盖同一格时用引用计数，只有最后一个到期才真正还原，不会互相踩踏。配置里的方块名解析不出来或不是方块时直接返回 false，不会静默铺成冰。服务端配置的过滤方块名单里的方块不会被替换。")
public class BlockMechanic extends MechanicComponent {
    private static final Vector UP = new Vector(0, 1, 0);

    @SkillField(
            kind = FieldKind.ListValue,
            label = "Shape",
            labelZh = "形状",
            tooltip = "[shape] The shape of the region to change the blocks for",
            tooltipZh = "区域形状。Sphere 用「半径」，Cuboid 用「宽/高/深」三项。",
            options = {"Sphere", "Cuboid"},
            optionsZh = {"球形", "形状2"},
            defaultValue = "Sphere")
    private static final String SHAPE   = "shape";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Type",
            labelZh = "类型",
            tooltip = "[type] The type of blocks to replace. Air or any would be for making obstacles while solid would change the environment",
            tooltipZh = "限定哪些方块会被替换：Solid 只换实心方块（改地形用），Air 只换空气（凭空造墙用），Any 全都换。",
            options = {"Air", "Any", "Solid"},
            optionsZh = {"可选值1", "任意", "可选值3"},
            defaultValue = "Solid")
    private static final String TYPE    = "type";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Radius",
            labelZh = "半径",
            tooltip = "[radius] The radius of the sphere region in blocks",
            tooltipZh = "球形区域半径，单位格，默认 3。随技能等级缩放。仅在形状为 Sphere 时生效。")
    private static final String RADIUS  = "radius";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Width (X)",
            labelZh = "参数12",
            tooltip = "[width] The width of the cuboid in blocks",
            tooltipZh = "长方体在 X 轴的宽度，单位格，默认 5。随技能等级缩放。仅在形状为 Cuboid 时生效。")
    private static final String WIDTH   = "width";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Height (Y)",
            labelZh = "参数13",
            tooltip = "[height] The height of the cuboid in blocks",
            tooltipZh = "长方体在 Y 轴的高度，单位格，默认 5。随技能等级缩放。仅在形状为 Cuboid 时生效。")
    private static final String HEIGHT  = "height";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Depth (Z)",
            labelZh = "参数14",
            tooltip = "[depth] The depth of the cuboid in blocks",
            tooltipZh = "长方体在 Z 轴的深度，单位格，默认 5。随技能等级缩放。仅在形状为 Cuboid 时生效。")
    private static final String DEPTH   = "depth";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Block",
            labelZh = "方块",
            tooltip = "[block] The type of block to turn the region into",
            tooltipZh = "替换成的方块类型。会先经 MaterialCompat 做旧版名称/数据值转换；解析失败或不是方块则整个节点不执行。",
            options = {"Acacia Door", "Acacia Door Item", "Acacia Fence", "Acacia Fence Gate", "Acacia Stairs", "Activator Rail", "Air", "Anvil", "Apple", "Armor Stand", "Arrow", "Baked Potato", "Banner", "Barrier", "Beacon", "Bed", "Bed Block", "Bedrock", "Beetroot", "Beetroot Block", "Beetroot Seeds", "Beetroot Soup", "Birch Door", "Birch Door Item", "Birch Fence", "Birch Fence Gate", "Birch Wood Stairs", "Black Glazed Terracotta", "Black Shulker Box", "Blaze Powder", "Blaze Rod", "Blue Glazed Terracotta", "Blue Shulker Box", "Boat", "Boat Acacia", "Boat Birch", "Boat Dark Oak", "Boat Jungle", "Boat Spruce", "Bone", "Bone Block", "Book", "Book And Quill", "Bookshelf", "Bow", "Bowl", "Bread", "Brewing Stand", "Brewing Stand Item", "Brick", "Brick Stairs", "Brown Glazed Terracotta", "Brown Mushroom", "Brown Shulker Box", "Bucket", "Burning Furnace", "Cactus", "Cake", "Cake Block", "Carpet", "Carrot", "Carrot Item", "Carrot Stick", "Cauldron", "Cauldron Item", "Chainmail Boots", "Chainmail Chestplate", "Chainmail Helmet", "Chainmail Leggings", "Chest", "Chorus Flower", "Chorus Fruit", "Chorus Fruit Popped", "Chorus Plant", "Clay", "Clay Ball", "Clay Brick", "Coal", "Coal Block", "Coal Ore", "Cobble Wall", "Cobblestone", "Cobblestone Stairs", "Cocoa", "Command", "Command Chain", "Command Minecart", "Command Repeating", "Compass", "Concrete", "Concrete Powder", "Cooked Beef", "Cooked Chicken", "Cooked Fish", "Cooked Mutton", "Cooked Rabbit", "Cookie", "Crops", "Cyan Glazed Terracotta", "Cyan Shulker Box", "Dark Oak Door", "Dark Oak Door Item", "Dark Oak Fence", "Dark Oak Fence Gate", "Dark Oak Stairs", "Daylight Detector", "Daylight Detector Inverted", "Dead Bush", "Detector Rail", "Diamond", "Diamond Axe", "Diamond Barding", "Diamond Block", "Diamond Boots", "Diamond Chestplate", "Diamond Helmet", "Diamond Hoe", "Diamond Leggings", "Diamond Ore", "Diamond Pickaxe", "Diamond Spade", "Diamond Sword", "Diode", "Diode Block Off", "Diode Block On", "Dirt", "Dispenser", "Double Plant", "Double Step", "Double Stone Slab2", "Dragon Egg", "Dragons Breath", "Dropper", "Egg", "Elytra", "Emerald", "Emerald Block", "Emerald Ore", "Empty Map", "Enchanted Book", "Enchantment Table", "End Bricks", "End Crystal", "End Gateway", "End Rod", "Ender Chest", "Ender Pearl", "Ender Portal", "Ender Portal Frame", "Ender Stone", "Exp Bottle", "Explosive Minecart", "Eye Of Ender", "Feather", "Fence", "Fence Gate", "Fermented Spider Eye", "Fire", "Fireball", "Firework", "Firework Charge", "Fishing Rod", "Flint", "Flint And Steel", "Flower Pot", "Flower Pot Item", "Frosted Ice", "Furnace", "Ghast Tear", "Glass", "Glass Bottle", "Glowing Redstone Ore", "Glowstone", "Glowstone Dust", "Gold Axe", "Gold Barding", "Gold Block", "Gold Boots", "Gold Chestplate", "Gold Helmet", "Gold Hoe", "Gold Ingot", "Gold Leggings", "Gold Nugget", "Gold Ore", "Gold Pickaxe", "Gold Plate", "Gold Record", "Gold Spade", "Gold Sword", "Golden Apple", "Golden Carrot", "Grass", "Grass Path", "Gravel", "Gray Glazed Terracotta", "Gray Shulker Box", "Green Glazed Terracotta", "Green Record", "Green Shulker Box", "Grilled Pork", "Hard Clay", "Hay Block", "Hopper", "Hopper Minecart", "Huge Mushroom 1", "Huge Mushroom 2", "Ice", "Ink Sack", "Iron Axe", "Iron Barding", "Iron Block", "Iron Boots", "Iron Chestplate", "Iron Door", "Iron Door Block", "Iron Fence", "Iron Helmet", "Iron Hoe", "Iron Ingot", "Iron Leggings", "Iron Nugget", "Iron Ore", "Iron Pickaxe", "Iron Plate", "Iron Spade", "Iron Sword", "Iron Trapdoor", "Item Frame", "Jack O Lantern", "Jukebox", "Jungle Door", "Jungle Door Item", "Jungle Fence", "Jungle Fence Gate", "Jungle Wood Stairs", "Knowledge Book", "Ladder", "Lapis Block", "Lapis Ore", "Lava", "Lava Bucket", "Leash", "Leather", "Leather Boots", "Leather Chestplate", "Leather Helmet", "Leather Leggings", "Leaves", "Leaves 2", "Lever", "Light Blue Glazed Terracotta", "Light Blue Shulker Box", "Lime Glazed Terracotta", "Lime Shulker Box", "Lingering Potion", "Log", "Log 2", "Long Grass", "Magenta Glazed Terracotta", "Magenta Shulker Box", "Magma", "Magma Cream", "Map", "Melon", "Melon Block", "Melon Seeds", "Melon Stem", "Milk Bucket", "Minecart", "Mob Spawner", "Monster Egg", "Monster Eggs", "Mossy Cobblestone", "Mushroom Soup", "Mutton", "Mycel", "Name Tag", "Nether Brick", "Nether Brick Item", "Nether Brick Stairs", "Nether Fence", "Nether Stalk", "Nether Star", "Nether Wart Block", "Nether Warts", "Netherrack", "Note Block", "Observer", "Obsidian", "Orange Glazed Terracotta", "Orange Shulker Box", "Packed Ice", "Painting", "Paper", "Pink Glazed Terracotta", "Pink Shulker Box", "Piston Base", "Piston Extension", "Piston Moving Piece", "Piston Sticky Base", "Poisonous Potato", "Pork", "Portal", "Potato", "Potato Item", "Potion", "Powered Minecart", "Powered Rail", "Prismarine", "Prismarine Crystals", "Prismarine Shard", "Pumpkin", "Pumpkin Pie", "Pumpkin Seeds", "Pumpkin Stem", "Purple Glazed Terracotta", "Purple Shulker Box", "Purpur Block", "Purpur Double Slab", "Purpur Pillar", "Purpur Slab", "Purpur Stairs", "Quartz", "Quartz Block", "Quartz Ore", "Quartz Stairs", "Rabbit", "Rabbit Foot", "Rabbit Hide", "Rabbit Stew", "Rails", "Raw Beef", "Raw Chicken", "Raw Fish", "Record 10", "Record 11", "Record 12", "Record 3", "Record 4", "Record 5", "Record 6", "Record 7", "Record 8", "Record 9", "Red Glazed Terracotta", "Red Mushroom", "Red Nether Brick", "Red Rose", "Red Sandstone", "Red Sandstone Stairs", "Red Shulker Box", "Redstone", "Redstone Block", "Redstone Comparator", "Redstone Comparator Off", "Redstone Comparator On", "Redstone Lamp Off", "Redstone Lamp On", "Redstone Ore", "Redstone Torch Off", "Redstone Torch On", "Redstone Wire", "Rotten Flesh", "Saddle", "Sand", "Sandstone", "Sandstone Stairs", "Sapling", "Sea Lantern", "Seeds", "Shears", "Shield", "Shulker Shell", "Sign", "Sign Post", "Silver Glazed Terracotta", "Silver Shulker Box", "Skull", "Skull Item", "Slime Ball", "Slime Block", "Smooth Brick", "Smooth Stairs", "Snow", "Snow Ball", "Snow Block", "Soil", "Soul Sand", "Speckled Melon", "Spectral Arrow", "Spider Eye", "Splash Potion", "Sponge", "Spruce Door", "Spruce Door Item", "Spruce Fence", "Spruce Fence Gate", "Spruce Wood Stairs", "Stained Clay", "Stained Glass", "Stained Glass Pane", "Standing Banner", "Stationary Lava", "Stationary Water", "Step", "Stick", "Stone", "Stone Axe", "Stone Button", "Stone Hoe", "Stone Pickaxe", "Stone Plate", "Stone Slab2", "Stone Spade", "Stone Sword", "Storage Minecart", "String", "Structure Block", "Structure Void", "Sugar", "Sugar Cane", "Sugar Cane Block", "Sulphur", "Thin Glass", "Tipped Arrow", "Tnt", "Torch", "Totem", "Trap Door", "Trapped Chest", "Tripwire", "Tripwire Hook", "Vine", "Wall Banner", "Wall Sign", "Watch", "Water", "Water Bucket", "Water Lily", "Web", "Wheat", "White Glazed Terracotta", "White Shulker Box", "Wood", "Wood Axe", "Wood Button", "Wood Door", "Wood Double Step", "Wood Hoe", "Wood Pickaxe", "Wood Plate", "Wood Spade", "Wood Stairs", "Wood Step", "Wood Sword", "Wooden Door", "Wool", "Workbench", "Written Book", "Yellow Flower", "Yellow Glazed Terracotta", "Yellow Shulker Box"},
            optionsZh = {"金合欢木门", "金合欢木门物品", "金合欢木栅栏", "金合欢木栅栏", "金合欢木楼梯", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "箭", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "桦木门", "桦木门物品", "桦木栅栏", "桦木栅栏", "桦木木头楼梯", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "金合欢木", "桦木", "深色橡木", "丛林木", "云杉", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "弓", "其他材质", "其他材质", "其他材质", "物品", "其他材质", "楼梯", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "物品", "其他材质", "其他材质", "物品", "靴子", "胸甲", "头盔", "护腿", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "楼梯", "其他材质", "命令", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "深色橡木门", "深色橡木门物品", "深色橡木栅栏", "深色橡木栅栏", "深色橡木楼梯", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "斧", "其他材质", "其他材质", "靴子", "胸甲", "头盔", "其他材质", "护腿", "其他材质", "镐", "其他材质", "剑", "其他材质", "其他材质", "其他材质", "泥土", "其他材质", "其他材质", "脚步", "石头", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "石头", "其他材质", "其他材质", "其他材质", "其他材质", "栅栏", "栅栏", "其他材质", "火焰", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "物品", "其他材质", "其他材质", "其他材质", "玻璃", "玻璃", "其他材质", "其他材质", "其他材质", "斧", "其他材质", "其他材质", "靴子", "胸甲", "头盔", "其他材质", "其他材质", "护腿", "其他材质", "其他材质", "镐", "其他材质", "其他材质", "其他材质", "剑", "其他材质", "其他材质", "草方块", "草方块", "沙砾", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "斧", "其他材质", "其他材质", "靴子", "胸甲", "门", "门", "栅栏", "头盔", "其他材质", "其他材质", "护腿", "其他材质", "其他材质", "镐", "其他材质", "其他材质", "剑", "其他材质", "物品", "其他材质", "其他材质", "丛林木门", "丛林木门物品", "丛林木栅栏", "丛林木栅栏", "丛林木木头楼梯", "其他材质", "其他材质", "其他材质", "其他材质", "熔岩", "熔岩", "其他材质", "其他材质", "靴子", "胸甲", "头盔", "护腿", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "药水", "其他材质", "其他材质", "草方块", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "物品", "楼梯", "栅栏", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "物品", "药水效果", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "台阶", "其他材质", "台阶", "楼梯", "其他材质", "其他材质", "其他材质", "楼梯", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "楼梯", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "沙子", "其他材质", "楼梯", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "物品", "其他材质", "其他材质", "其他材质", "楼梯", "其他材质", "其他材质", "其他材质", "其他材质", "沙子", "其他材质", "箭", "其他材质", "药水", "其他材质", "云杉门", "云杉门物品", "云杉栅栏", "云杉栅栏", "云杉木头楼梯", "其他材质", "玻璃", "玻璃", "其他材质", "熔岩", "水", "脚步", "其他材质", "石头", "石头斧", "石头", "石头", "石头镐", "石头", "石头", "石头", "石头剑", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "玻璃", "箭", "其他材质", "其他材质", "其他材质", "门", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "水", "水", "水", "其他材质", "其他材质", "其他材质", "其他材质", "木头", "木头斧", "木头", "木头门", "木头脚步", "木头", "木头镐", "木头", "木头", "木头楼梯", "木头脚步", "木头剑", "门", "羊毛", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质"},
            defaultValue = "Ice")
    private static final String BLOCK   = "block";
    @SkillField(
            kind = FieldKind.IntValue,
            label = "Block Data",
            labelZh = "方块数据",
            tooltip = "[data] The block data to apply, mostly applicable for things like signs, woods, steps, or the similar",
            tooltipZh = "旧版方块数据值（台阶朝向、木头种类这类）。仅在未扁平化的旧版本服务端上生效，1.13+ 会被忽略。",
            defaultValue = "0")
    private static final String DATA    = "data";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Seconds",
            labelZh = "秒数",
            tooltip = "[seconds] How long the blocks should be replaced for",
            tooltipZh = "方块替换的持续秒数，默认 5，随技能等级缩放。内部乘 20 换算成 tick，到点后还原。")
    private static final String SECONDS = "seconds";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Forward Offset",
            labelZh = "前方偏移",
            tooltip = "[forward] How far forward in front of the target the region should be in blocks. A negative value will put it behind.",
            tooltipZh = "区域中心相对目标朝向的前方偏移格数，负值放到身后。随技能等级缩放。")
    private static final String FORWARD = "forward";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Upward Offset",
            labelZh = "上方偏移",
            tooltip = "[upward] How far above the target the region should be in blocks. A negative value will put it below.",
            tooltipZh = "区域中心相对目标的上方偏移格数，负值放到下方。随技能等级缩放。")
    private static final String UPWARD  = "upward";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Right Offset",
            labelZh = "右方偏移",
            tooltip = "[right] How far to the right the region should be of the target. A negative value will put it to the left.",
            tooltipZh = "区域中心相对目标朝向的右侧偏移格数，负值放到左侧。随技能等级缩放。")
    private static final String RIGHT   = "right";

    private static final HashMap<Location, Integer>    pending  = new HashMap<Location, Integer>();
    private static final HashMap<Location, BlockState> original = new HashMap<Location, BlockState>();

    private final Map<Integer, List<RevertTask>> tasks = new HashMap<>();

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
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        if (targets.size() == 0) { return false; }

        // Convert legacy variants before replacement; invalid config must not place ICE silently.
        Material block = com.sucy.skill.api.util.MaterialCompat.resolve(
                settings.getString(BLOCK, "ICE"), settings.getInt(DATA, 0), false);
        if (block == null || !block.isBlock()) return false;

        boolean sphere = settings.getString(SHAPE, "sphere").toLowerCase().equals("sphere");
        int ticks = (int) (20 * parseValues(caster, SECONDS, level, 5));
        byte data = (byte) settings.getInt(DATA, 0);

        String type = settings.getString(TYPE, "solid").toLowerCase();
        boolean solid = type.equals("solid");
        boolean air = type.equals("air");

        double forward = parseValues(caster, FORWARD, level, 0);
        double upward = parseValues(caster, UPWARD, level, 0);
        double right = parseValues(caster, RIGHT, level, 0);

        List<Block> blocks = new ArrayList<Block>();
        World w = caster.getWorld();

        // Grab blocks in a sphere
        if (sphere) {
            double radius = parseValues(caster, RADIUS, level, 3);
            double x, y, z, dx, dy, dz;
            double rSq = radius * radius;
            for (LivingEntity t : targets) {
                // Get the center with offsets included
                Location loc = t.getLocation();
                Vector dir = t.getLocation().getDirection().setY(0).normalize();
                Vector nor = dir.clone().crossProduct(UP);
                loc.add(dir.multiply(forward).add(nor.multiply(right)));
                loc.add(0, upward, 0);

                x = loc.getBlockX();
                y = loc.getBlockY();
                z = loc.getBlockZ();

                // Get all blocks within the radius of the center
                for (int i = (int) (x - radius) + 1; i < (int) (x + radius); i++) {
                    for (int j = (int) (y - radius) + 1; j < (int) (y + radius); j++) {
                        for (int k = (int) (z - radius) + 1; k < (int) (z + radius); k++) {
                            dx = x - i;
                            dy = y - j;
                            dz = z - k;
                            if (dx * dx + dy * dy + dz * dz < rSq) {
                                Block b = w.getBlockAt(i, j, k);
                                if ((!solid || b.getType().isSolid())
                                        && (!air || b.getType() == Material.AIR)
                                        && !SkillAPI.getSettings().getFilteredBlocks().contains(b.getType())) {
                                    blocks.add(b);
                                }
                            }
                        }
                    }
                }
            }
        }

        // Grab blocks in a cuboid
        else {
            // Cuboid options
            double width = (parseValues(caster, WIDTH, level, 5) - 1) / 2;
            double height = (parseValues(caster, HEIGHT, level, 5) - 1) / 2;
            double depth = (parseValues(caster, DEPTH, level, 5) - 1) / 2;
            double x, y, z;

            for (LivingEntity t : targets) {
                // Get the location with offsets included
                Location loc = t.getLocation();
                Vector dir = t.getLocation().getDirection().setY(0).normalize();
                Vector nor = dir.clone().crossProduct(UP);
                loc.add(dir.multiply(forward).add(nor.multiply(right)));
                loc.add(0, upward, 0);

                x = loc.getX();
                y = loc.getY();
                z = loc.getZ();

                // Get all blocks in the area
                for (double i = x - width; i <= x + width + 0.01; i++) {
                    for (double j = y - height; j <= y + height + 0.01; j++) {
                        for (double k = z - depth; k <= z + depth + 0.01; k++) {
                            Block b = w.getBlockAt((int) i, (int) j, (int) k);
                            if ((!solid || b.getType().isSolid())
                                    && (!air || b.getType() == Material.AIR)
                                    && !SkillAPI.getSettings().getFilteredBlocks().contains(b.getType())) {
                                blocks.add(b);
                            }
                        }
                    }
                }
            }
        }

        // Change blocks
        ArrayList<Location> states = new ArrayList<Location>();
        for (Block b : blocks) {
            // Increment the counter
            Location loc = b.getLocation();
            if (pending.containsKey(loc)) {
                pending.put(loc, pending.get(loc) + 1);
            } else {
                pending.put(loc, 1);
                original.put(loc, b.getState());
            }

            states.add(b.getLocation());
            BlockState state = b.getState();
            state.setType(block);
            if (!com.sucy.skill.api.util.MaterialCompat.isFlattened()) {
                state.setData(new MaterialData(block, data));
            }
            state.update(true, false);
        }

        // Revert after duration
        final RevertTask task = new RevertTask(caster, states);
        task.runTaskLater(Bukkit.getPluginManager().getPlugin("SkillAPI"), ticks);
        tasks.computeIfAbsent(caster.getEntityId(), ArrayList::new).add(task);

        return true;
    }

    @Override
    public String getKey() {
        return "block";
    }

    @Override
    protected void doCleanUp(final LivingEntity caster) {
        final List<RevertTask> casterTasks = tasks.remove(caster.getEntityId());
        if (casterTasks != null) {
            casterTasks.forEach(task -> {
                task.revert();
                task.cancel();
            });
        }
    }

    /**
     * Checks whether or not the location is modified by a block mechanic
     *
     * @param loc location to check
     *
     * @return true if modified, false otherwise
     */
    public static boolean isPending(Location loc) {
        return pending.containsKey(loc);
    }

    /**
     * Reverts block changes after a duration
     */
    private class RevertTask extends BukkitRunnable {
        private ArrayList<Location> locs;
        private LivingEntity        caster;

        RevertTask(final LivingEntity caster, final ArrayList<Location> locs) {
            this.caster = caster;
            this.locs = locs;
        }

        @Override
        public void run() {
            revert();
            tasks.get(caster.getEntityId()).remove(this);
        }

        private void revert() {
            for (Location loc : locs) {
                int count = pending.remove(loc);

                if (count == 1) {
                    original.remove(loc).update(true, false);
                } else {
                    pending.put(loc, count - 1);
                }
            }
        }
    }
}
