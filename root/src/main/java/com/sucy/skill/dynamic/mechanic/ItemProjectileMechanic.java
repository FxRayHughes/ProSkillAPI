/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.ItemProjectileMechanic
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
import com.sucy.skill.api.particle.EffectPlayer;
import com.sucy.skill.api.particle.target.FollowTarget;
import com.sucy.skill.api.projectile.CustomProjectile;
import com.sucy.skill.api.projectile.ItemProjectile;
import com.sucy.skill.api.projectile.ProjectileCallback;
import com.sucy.skill.cast.CircleIndicator;
import com.sucy.skill.cast.CylinderIndicator;
import com.sucy.skill.cast.IIndicator;
import com.sucy.skill.cast.IndicatorType;
import com.sucy.skill.cast.ProjectileIndicator;
import com.sucy.skill.dynamic.TempEntity;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Launches a projectile using an item as its visual that applies child components upon landing
 */
@SkillNode(
        key = "item projectile",
        name = "Item Projectile",
        nameZh = "物品投射物",
        description = "Launches a projectile using an item as its visual that applies child components upon landing. The target passed on will be the collided target or the location where it landed if it missed.",
        descriptionZh = "以物品为外观发射投射物，命中生物或落地后再执行子节点，子节点收到的目标是被命中的生物，若未命中任何生物则是一个位于落点的临时实体。发射方式分三种：锥形按目标视线方向散射、水平锥形把方向压平到水平面、暴雨从目标上方一定高度的圆形区域内随机落下。发射源是每个当前目标的位置，而不是施法者，因此对多个目标可同时形成多处弹幕。命中判定只对配置的阵营生效。",
        container = true)
public class ItemProjectileMechanic extends MechanicComponent implements ProjectileCallback {
    private static final Vector UP = new Vector(0, 1, 0);

    @SkillField(
            kind = FieldKind.ListValue,
            label = "Item",
            labelZh = "物品",
            tooltip = "[item] The item type to use as a projectile",
            tooltipZh = "作为投射物外观的物品材质，内部转大写并把空格换成下划线后直接按枚举名解析；解析失败时静默回退为 Jack O Lantern（南瓜灯），不会报错。默认 Jack O Lantern。",
            options = {"Acacia Door", "Acacia Door Item", "Acacia Fence", "Acacia Fence Gate", "Acacia Stairs", "Activator Rail", "Air", "Anvil", "Apple", "Armor Stand", "Arrow", "Baked Potato", "Banner", "Barrier", "Beacon", "Bed", "Bed Block", "Bedrock", "Beetroot", "Beetroot Block", "Beetroot Seeds", "Beetroot Soup", "Birch Door", "Birch Door Item", "Birch Fence", "Birch Fence Gate", "Birch Wood Stairs", "Black Glazed Terracotta", "Black Shulker Box", "Blaze Powder", "Blaze Rod", "Blue Glazed Terracotta", "Blue Shulker Box", "Boat", "Boat Acacia", "Boat Birch", "Boat Dark Oak", "Boat Jungle", "Boat Spruce", "Bone", "Bone Block", "Book", "Book And Quill", "Bookshelf", "Bow", "Bowl", "Bread", "Brewing Stand", "Brewing Stand Item", "Brick", "Brick Stairs", "Brown Glazed Terracotta", "Brown Mushroom", "Brown Shulker Box", "Bucket", "Burning Furnace", "Cactus", "Cake", "Cake Block", "Carpet", "Carrot", "Carrot Item", "Carrot Stick", "Cauldron", "Cauldron Item", "Chainmail Boots", "Chainmail Chestplate", "Chainmail Helmet", "Chainmail Leggings", "Chest", "Chorus Flower", "Chorus Fruit", "Chorus Fruit Popped", "Chorus Plant", "Clay", "Clay Ball", "Clay Brick", "Coal", "Coal Block", "Coal Ore", "Cobble Wall", "Cobblestone", "Cobblestone Stairs", "Cocoa", "Command", "Command Chain", "Command Minecart", "Command Repeating", "Compass", "Concrete", "Concrete Powder", "Cooked Beef", "Cooked Chicken", "Cooked Fish", "Cooked Mutton", "Cooked Rabbit", "Cookie", "Crops", "Cyan Glazed Terracotta", "Cyan Shulker Box", "Dark Oak Door", "Dark Oak Door Item", "Dark Oak Fence", "Dark Oak Fence Gate", "Dark Oak Stairs", "Daylight Detector", "Daylight Detector Inverted", "Dead Bush", "Detector Rail", "Diamond", "Diamond Axe", "Diamond Barding", "Diamond Block", "Diamond Boots", "Diamond Chestplate", "Diamond Helmet", "Diamond Hoe", "Diamond Leggings", "Diamond Ore", "Diamond Pickaxe", "Diamond Spade", "Diamond Sword", "Diode", "Diode Block Off", "Diode Block On", "Dirt", "Dispenser", "Double Plant", "Double Step", "Double Stone Slab2", "Dragon Egg", "Dragons Breath", "Dropper", "Egg", "Elytra", "Emerald", "Emerald Block", "Emerald Ore", "Empty Map", "Enchanted Book", "Enchantment Table", "End Bricks", "End Crystal", "End Gateway", "End Rod", "Ender Chest", "Ender Pearl", "Ender Portal", "Ender Portal Frame", "Ender Stone", "Exp Bottle", "Explosive Minecart", "Eye Of Ender", "Feather", "Fence", "Fence Gate", "Fermented Spider Eye", "Fire", "Fireball", "Firework", "Firework Charge", "Fishing Rod", "Flint", "Flint And Steel", "Flower Pot", "Flower Pot Item", "Frosted Ice", "Furnace", "Ghast Tear", "Glass", "Glass Bottle", "Glowing Redstone Ore", "Glowstone", "Glowstone Dust", "Gold Axe", "Gold Barding", "Gold Block", "Gold Boots", "Gold Chestplate", "Gold Helmet", "Gold Hoe", "Gold Ingot", "Gold Leggings", "Gold Nugget", "Gold Ore", "Gold Pickaxe", "Gold Plate", "Gold Record", "Gold Spade", "Gold Sword", "Golden Apple", "Golden Carrot", "Grass", "Grass Path", "Gravel", "Gray Glazed Terracotta", "Gray Shulker Box", "Green Glazed Terracotta", "Green Record", "Green Shulker Box", "Grilled Pork", "Hard Clay", "Hay Block", "Hopper", "Hopper Minecart", "Huge Mushroom 1", "Huge Mushroom 2", "Ice", "Ink Sack", "Iron Axe", "Iron Barding", "Iron Block", "Iron Boots", "Iron Chestplate", "Iron Door", "Iron Door Block", "Iron Fence", "Iron Helmet", "Iron Hoe", "Iron Ingot", "Iron Leggings", "Iron Nugget", "Iron Ore", "Iron Pickaxe", "Iron Plate", "Iron Spade", "Iron Sword", "Iron Trapdoor", "Item Frame", "Jack O Lantern", "Jukebox", "Jungle Door", "Jungle Door Item", "Jungle Fence", "Jungle Fence Gate", "Jungle Wood Stairs", "Knowledge Book", "Ladder", "Lapis Block", "Lapis Ore", "Lava", "Lava Bucket", "Leash", "Leather", "Leather Boots", "Leather Chestplate", "Leather Helmet", "Leather Leggings", "Leaves", "Leaves 2", "Lever", "Light Blue Glazed Terracotta", "Light Blue Shulker Box", "Lime Glazed Terracotta", "Lime Shulker Box", "Lingering Potion", "Log", "Log 2", "Long Grass", "Magenta Glazed Terracotta", "Magenta Shulker Box", "Magma", "Magma Cream", "Map", "Melon", "Melon Block", "Melon Seeds", "Melon Stem", "Milk Bucket", "Minecart", "Mob Spawner", "Monster Egg", "Monster Eggs", "Mossy Cobblestone", "Mushroom Soup", "Mutton", "Mycel", "Name Tag", "Nether Brick", "Nether Brick Item", "Nether Brick Stairs", "Nether Fence", "Nether Stalk", "Nether Star", "Nether Wart Block", "Nether Warts", "Netherrack", "Note Block", "Observer", "Obsidian", "Orange Glazed Terracotta", "Orange Shulker Box", "Packed Ice", "Painting", "Paper", "Pink Glazed Terracotta", "Pink Shulker Box", "Piston Base", "Piston Extension", "Piston Moving Piece", "Piston Sticky Base", "Poisonous Potato", "Pork", "Portal", "Potato", "Potato Item", "Potion", "Powered Minecart", "Powered Rail", "Prismarine", "Prismarine Crystals", "Prismarine Shard", "Pumpkin", "Pumpkin Pie", "Pumpkin Seeds", "Pumpkin Stem", "Purple Glazed Terracotta", "Purple Shulker Box", "Purpur Block", "Purpur Double Slab", "Purpur Pillar", "Purpur Slab", "Purpur Stairs", "Quartz", "Quartz Block", "Quartz Ore", "Quartz Stairs", "Rabbit", "Rabbit Foot", "Rabbit Hide", "Rabbit Stew", "Rails", "Raw Beef", "Raw Chicken", "Raw Fish", "Record 10", "Record 11", "Record 12", "Record 3", "Record 4", "Record 5", "Record 6", "Record 7", "Record 8", "Record 9", "Red Glazed Terracotta", "Red Mushroom", "Red Nether Brick", "Red Rose", "Red Sandstone", "Red Sandstone Stairs", "Red Shulker Box", "Redstone", "Redstone Block", "Redstone Comparator", "Redstone Comparator Off", "Redstone Comparator On", "Redstone Lamp Off", "Redstone Lamp On", "Redstone Ore", "Redstone Torch Off", "Redstone Torch On", "Redstone Wire", "Rotten Flesh", "Saddle", "Sand", "Sandstone", "Sandstone Stairs", "Sapling", "Sea Lantern", "Seeds", "Shears", "Shield", "Shulker Shell", "Sign", "Sign Post", "Silver Glazed Terracotta", "Silver Shulker Box", "Skull", "Skull Item", "Slime Ball", "Slime Block", "Smooth Brick", "Smooth Stairs", "Snow", "Snow Ball", "Snow Block", "Soil", "Soul Sand", "Speckled Melon", "Spectral Arrow", "Spider Eye", "Splash Potion", "Sponge", "Spruce Door", "Spruce Door Item", "Spruce Fence", "Spruce Fence Gate", "Spruce Wood Stairs", "Stained Clay", "Stained Glass", "Stained Glass Pane", "Standing Banner", "Stationary Lava", "Stationary Water", "Step", "Stick", "Stone", "Stone Axe", "Stone Button", "Stone Hoe", "Stone Pickaxe", "Stone Plate", "Stone Slab2", "Stone Spade", "Stone Sword", "Storage Minecart", "String", "Structure Block", "Structure Void", "Sugar", "Sugar Cane", "Sugar Cane Block", "Sulphur", "Thin Glass", "Tipped Arrow", "Tnt", "Torch", "Totem", "Trap Door", "Trapped Chest", "Tripwire", "Tripwire Hook", "Vine", "Wall Banner", "Wall Sign", "Watch", "Water", "Water Bucket", "Water Lily", "Web", "Wheat", "White Glazed Terracotta", "White Shulker Box", "Wood", "Wood Axe", "Wood Button", "Wood Door", "Wood Double Step", "Wood Hoe", "Wood Pickaxe", "Wood Plate", "Wood Spade", "Wood Stairs", "Wood Step", "Wood Sword", "Wooden Door", "Wool", "Workbench", "Written Book", "Yellow Flower", "Yellow Glazed Terracotta", "Yellow Shulker Box"},
            optionsZh = {"金合欢木门", "金合欢木门物品", "金合欢木栅栏", "金合欢木栅栏", "金合欢木楼梯", "物品6", "物品7", "物品8", "物品9", "物品10", "箭", "物品12", "物品13", "物品14", "物品15", "物品16", "物品17", "物品18", "物品19", "物品20", "物品21", "物品22", "桦木门", "桦木门物品", "桦木栅栏", "桦木栅栏", "桦木木头楼梯", "物品28", "物品29", "物品30", "物品31", "物品32", "物品33", "物品34", "金合欢木", "桦木", "深色橡木", "丛林木", "云杉", "物品40", "物品41", "物品42", "物品43", "物品44", "弓", "物品46", "物品47", "物品48", "物品", "物品50", "楼梯", "物品52", "物品53", "物品54", "物品55", "物品56", "物品57", "物品58", "物品59", "物品60", "物品61", "物品", "物品63", "物品64", "物品", "靴子", "胸甲", "头盔", "护腿", "物品70", "物品71", "物品72", "物品73", "物品74", "物品75", "物品76", "物品77", "物品78", "物品79", "物品80", "物品81", "物品82", "楼梯", "物品84", "命令", "物品86", "物品87", "物品88", "物品89", "物品90", "物品91", "物品92", "物品93", "物品94", "物品95", "物品96", "物品97", "物品98", "物品99", "物品100", "深色橡木门", "深色橡木门物品", "深色橡木栅栏", "深色橡木栅栏", "深色橡木楼梯", "物品106", "物品107", "物品108", "物品109", "物品110", "斧", "物品112", "物品113", "靴子", "胸甲", "头盔", "物品117", "护腿", "物品119", "镐", "物品121", "剑", "物品123", "物品124", "物品125", "泥土", "物品127", "物品128", "脚步", "石头", "物品131", "物品132", "物品133", "物品134", "物品135", "物品136", "物品137", "物品138", "物品139", "物品140", "物品141", "物品142", "物品143", "物品144", "物品145", "物品146", "物品147", "物品148", "物品149", "石头", "物品151", "物品152", "物品153", "物品154", "栅栏", "栅栏", "物品157", "火焰", "物品159", "物品160", "物品161", "物品162", "物品163", "物品164", "物品165", "物品", "物品167", "物品168", "物品169", "玻璃", "玻璃", "物品172", "物品173", "物品174", "斧", "物品176", "物品177", "靴子", "胸甲", "头盔", "物品181", "物品182", "护腿", "物品184", "物品185", "镐", "物品187", "物品188", "物品189", "剑", "物品191", "物品192", "草方块", "草方块", "沙砾", "物品196", "物品197", "物品198", "物品199", "物品200", "物品201", "物品202", "物品203", "物品204", "物品205", "物品206", "物品207", "物品208", "物品209", "斧", "物品211", "物品212", "靴子", "胸甲", "门", "门", "栅栏", "头盔", "物品219", "物品220", "护腿", "物品222", "物品223", "镐", "物品225", "物品226", "剑", "物品228", "物品", "物品230", "物品231", "丛林木门", "丛林木门物品", "丛林木栅栏", "丛林木栅栏", "丛林木木头楼梯", "物品237", "物品238", "物品239", "物品240", "熔岩", "熔岩", "物品243", "物品244", "靴子", "胸甲", "头盔", "护腿", "物品249", "物品250", "物品251", "物品252", "物品253", "物品254", "物品255", "药水", "物品257", "物品258", "草方块", "物品260", "物品261", "物品262", "物品263", "物品264", "物品265", "物品266", "物品267", "物品268", "物品269", "物品270", "物品271", "物品272", "物品273", "物品274", "物品275", "物品276", "物品277", "物品278", "物品279", "物品", "楼梯", "栅栏", "物品283", "物品284", "物品285", "物品286", "物品287", "物品288", "物品289", "物品290", "物品291", "物品292", "物品293", "物品294", "物品295", "物品296", "物品297", "物品298", "物品299", "物品300", "物品301", "物品302", "物品303", "物品304", "物品305", "物品", "药水效果", "物品308", "物品309", "物品310", "物品311", "物品312", "物品313", "物品314", "物品315", "物品316", "物品317", "物品318", "物品319", "台阶", "物品321", "台阶", "楼梯", "物品324", "物品325", "物品326", "楼梯", "物品328", "物品329", "物品330", "物品331", "物品332", "物品333", "物品334", "物品335", "物品336", "物品337", "物品338", "物品339", "物品340", "物品341", "物品342", "物品343", "物品344", "物品345", "物品346", "物品347", "物品348", "物品349", "物品350", "楼梯", "物品352", "物品353", "物品354", "物品355", "物品356", "物品357", "物品358", "物品359", "物品360", "物品361", "物品362", "物品363", "物品364", "物品365", "沙子", "物品367", "楼梯", "物品369", "物品370", "物品371", "物品372", "物品373", "物品374", "物品375", "物品376", "物品377", "物品378", "物品379", "物品", "物品381", "物品382", "物品383", "楼梯", "物品385", "物品386", "物品387", "物品388", "沙子", "物品390", "箭", "物品392", "药水", "物品394", "云杉门", "云杉门物品", "云杉栅栏", "云杉栅栏", "云杉木头楼梯", "物品400", "玻璃", "玻璃", "物品403", "熔岩", "水", "脚步", "物品407", "石头", "石头斧", "石头", "石头", "石头镐", "石头", "石头", "石头", "石头剑", "物品417", "物品418", "物品419", "物品420", "物品421", "物品422", "物品423", "物品424", "玻璃", "箭", "物品427", "物品428", "物品429", "门", "物品431", "物品432", "物品433", "物品434", "物品435", "物品436", "物品437", "水", "水", "水", "物品441", "物品442", "物品443", "物品444", "木头", "木头斧", "木头", "木头门", "木头脚步", "木头", "木头镐", "木头", "木头", "木头楼梯", "木头脚步", "木头剑", "门", "羊毛", "物品459", "物品460", "物品461", "物品462", "物品463"},
            defaultValue = "Jack O Lantern")
    private static final String ITEM    = "item";
    @SkillField(
            kind = FieldKind.IntValue,
            label = "Item Data",
            labelZh = "物品数据",
            tooltip = "[item-data] The durability value for the item to use as a projectile, most notably for dyes or colored items like wool",
            tooltipZh = "物品的数据/耐久值，用于染料、羊毛等有颜色变种的物品。启用技能自定义模型数据时改为写入 CustomModelData，否则写入 durability。默认 0。",
            defaultValue = "0")
    private static final String DATA    = "item-data";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Velocity",
            labelZh = "速度",
            tooltip = "[velocity] How fast the projectile is launched. A negative value fires it in the opposite direction.",
            tooltipZh = "投射物初速度，负数会朝反方向发射。暴雨模式下用于控制下落速度。数值随技能等级/属性变化，默认 3。")
    private static final String SPEED   = "velocity";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Angle",
            labelZh = "角度",
            tooltip = "[angle] The angle in degrees of the cone arc to spread projectiles over. If you are only firing one projectile, this does not matter.",
            tooltipZh = "锥形散射的锥角，单位度；只有在同时发射多个投射物时才有区别，单发时该值无意义。暴雨模式不使用此项。数值随技能等级/属性变化，默认 30 度。")
    private static final String ANGLE   = "angle";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Amount",
            labelZh = "数量",
            tooltip = "[amount] The number of projectiles to fire",
            tooltipZh = "每个目标发射的投射物数量，取整后使用；配合锥角决定弹幕密度。数值随技能等级/属性变化，默认 1。")
    private static final String AMOUNT  = "amount";
    private static final String LEVEL   = "skill_level";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Height",
            labelZh = "高度",
            tooltip = "[height] The distance in blocks over the target to rain the projectiles from",
            tooltipZh = "仅暴雨模式使用：投射物从目标上方多少格开始下落。数值随技能等级/属性变化，默认 8 格。")
    private static final String HEIGHT  = "height";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Radius",
            labelZh = "半径",
            tooltip = "[rain-radius] The radius of the rain emission area in blocks",
            tooltipZh = "仅暴雨模式使用：目标上方落点随机分布的圆形区域半径，单位方块。数值随技能等级/属性变化，默认 2 格。")
    private static final String RADIUS  = "rain-radius";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Spread",
            labelZh = "散布",
            tooltip = "[spread] The orientation for firing projectiles. Cone will fire arrows in a cone centered on your reticle. Horizontal cone does the same as cone, just locked to the XZ axis (parallel to the ground). Rain drops the projectiles from above the target. For firing one arrow straight, use \"Cone\"",
            tooltipZh = "发射方式：「锥形」沿目标视线方向按锥角散开（单发即为直射）；「水平锥形」把方向的竖直分量清零后再散开，弹道平行于地面；「暴雨」忽略朝向，从目标上方圆形区域内随机落下。默认锥形。",
            options = {"Cone", "Horizontal Cone", "Rain"},
            optionsZh = {"可选值1", "可选值2", "可选值3"},
            defaultValue = "Cone")
    private static final String SPREAD  = "spread";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Group",
            labelZh = "目标阵营",
            tooltip = "[group] The alignment of targets to hit",
            tooltipZh = "可被投射物命中的阵营，敌方或友方；发射后写入投射物属性，未选中的阵营会被穿过。默认敌方。",
            options = {"Ally", "Enemy"},
            optionsZh = {"友方", "敌方"},
            defaultValue = "Enemy")
    private static final String ALLY    = "group";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Right Offset",
            labelZh = "右方偏移",
            tooltip = "[right] How far to the right of the target the projectile should fire from. A negative value will put it to the left.",
            tooltipZh = "发射点相对目标朝向右侧的偏移格数，负数为向左。仅锥形/水平锥形生效，暴雨模式忽略。数值随技能等级/属性变化，默认 0。")
    private static final String RIGHT   = "right";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Upward Offset",
            labelZh = "上方偏移",
            tooltip = "[upward] How far above the target the projectile should fire from in blocks. A negative value will put it below.",
            tooltipZh = "发射点相对目标的上抬格数，负数为下移；实际发射高度为目标坐标加 0.5 格再加该值。仅锥形/水平锥形生效。数值随技能等级/属性变化，默认 0。")
    private static final String UPWARD  = "upward";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Forward Offset",
            labelZh = "前方偏移",
            tooltip = "[forward] How far forward in front of the target the projectile should fire from in blocks. A negative value will put it behind.",
            tooltipZh = "发射点沿目标水平朝向前移的格数，负数为后移。仅锥形/水平锥形生效，暴雨模式忽略。数值随技能等级/属性变化，默认 0。")
    private static final String FORWARD = "forward";

    @SkillField(
            kind = FieldKind.ListValue,
            label = "Use Effect",
            labelZh = "启用特效",
            tooltip = "[use-effect] Whether or not to use the premium particle effects.",
            tooltipZh = "是否给投射物挂上跟随式粒子特效（付费特效系统）。默认「否」；为「是」时才读取「特效键」。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "False")
    private static final String USE_EFFECT = "use-effect";
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Effect Key",
            labelZh = "特效引用键",
            tooltip = "[effect-key] The key to refer to the effect by. Only one effect of each key can be active at a time.",
            tooltipZh = "特效的引用键，同一个键同时只能有一个特效存在；留空时使用所属技能的名称。仅在「使用特效」为「是」时生效。",
            defaultValue = "default")
    private static final String EFFECT_KEY = "effect-key";

    /**
     * Creates the list of indicators for the skill
     *
     * @param list   list to store indicators in
     * @param caster caster reference
     * @param targets location to base location on
     * @param level  the level of the skill to create for
     */
    @Override
    public void makeIndicators(List<IIndicator> list, Player caster, List<LivingEntity> targets, int level) {
        targets.forEach(target -> {
            // Get common values
            int amount = (int) parseValues(caster, AMOUNT, level, 1.0);
            double speed = parseValues(caster, "velocity", level, 1);
            String spread = settings.getString(SPREAD, "cone").toLowerCase();

            // Apply the spread type
            if (spread.equals("rain")) {
                double radius = parseValues(caster, RADIUS, level, 2.0);

                if (indicatorType == IndicatorType.DIM_2) {
                    IIndicator indicator = new CircleIndicator(radius);
                    indicator.moveTo(target.getLocation().add(0, 0.1, 0));
                    list.add(indicator);
                } else {
                    double height = parseValues(caster, HEIGHT, level, 8.0);
                    IIndicator indicator = new CylinderIndicator(radius, height);
                    indicator.moveTo(target.getLocation());
                    list.add(indicator);
                }
            } else {
                Vector dir = target.getLocation().getDirection();
                if (spread.equals("horizontal cone")) {
                    dir.setY(0);
                    dir.normalize();
                }
                double angle = parseValues(caster, ANGLE, level, 30.0);
                ArrayList<Vector> dirs = CustomProjectile.calcSpread(dir, angle, amount);
                Location loc = caster.getLocation().add(0, caster.getEyeHeight(), 0);
                for (Vector d : dirs) {
                    ProjectileIndicator indicator = new ProjectileIndicator(speed, 0.04);
                    indicator.setDirection(d);
                    indicator.moveTo(loc);
                    list.add(indicator);
                }
            }
        });
    }

    @Override
    public String getKey() {
        return "item projectile";
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
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        Material mat = Material.JACK_O_LANTERN;
        try {
            mat = Material.valueOf(settings.getString(ITEM).toUpperCase().replace(" ", "_"));
        } catch (Exception ex) {
            // Invalid or missing item material
        }
        ItemStack item = new ItemStack(mat);
        int data = settings.getInt(DATA, 0);
        if (SkillAPI.getSettings().useSkillModelData()) {
            ItemMeta meta = item.getItemMeta();
            meta.setCustomModelData(data);
            item.setItemMeta(meta);
        } else {
            item.setDurability((short) data);
        }

        // Get other common values
        double speed = parseValues(caster, SPEED, level, 3.0);
        int amount = (int) parseValues(caster, AMOUNT, level, 1.0);
        String spread = settings.getString(SPREAD, "cone").toLowerCase();
        boolean ally = settings.getString(ALLY, "enemy").toLowerCase().equals("ally");

        // Fire from each target
        for (LivingEntity target : targets) {
            Location loc = target.getLocation();

            // Apply the spread type
            ArrayList<ItemProjectile> list;
            if (spread.equals("rain")) {
                double radius = parseValues(caster, RADIUS, level, 2.0);
                double height = parseValues(caster, HEIGHT, level, 8.0);
                list = ItemProjectile.rain(caster, loc, item, radius, height, speed, amount, this);
            } else {
                Vector dir = target.getLocation().getDirection();

                double right = parseValues(caster, RIGHT, level, 0);
                double upward = parseValues(caster, UPWARD, level, 0);
                double forward = parseValues(caster, FORWARD, level, 0);

                Vector looking = dir.clone().setY(0).normalize();
                Vector normal = looking.clone().crossProduct(UP);
                looking.multiply(forward).add(normal.multiply(right));

                if (spread.equals("horizontal cone")) {
                    dir.setY(0);
                    dir.normalize();
                }
                dir.multiply(speed);
                double angle = parseValues(caster, ANGLE, level, 30.0);
                list = ItemProjectile.spread(
                        caster,
                        dir,
                        loc.add(looking).add(0, 0.5 + upward, 0),
                        item,
                        angle,
                        amount,
                        this
                );
            }

            // Set metadata for when the callback happens
            for (ItemProjectile p : list) {
                SkillAPI.setMeta(p, LEVEL, level);
                p.setAllyEnemy(ally, !ally);
            }

            if (settings.getBool(USE_EFFECT, false)) {
                EffectPlayer player = new EffectPlayer(settings);
                for (CustomProjectile p : list) {
                    player.start(
                            new FollowTarget(p),
                            settings.getString(EFFECT_KEY, skill.getName()),
                            9999,
                            level,
                            true);
                }
            }
        }

        return targets.size() > 0;
    }

    /**
     * The callback for the projectiles that applies child components
     *
     * @param projectile projectile calling back for
     * @param hit        the entity hit by the projectile, if any
     */
    @Override
    public void callback(CustomProjectile projectile, LivingEntity hit) {
        if (hit == null) {
            hit = TempEntity.create(projectile.getLocation());
        }
        ArrayList<LivingEntity> targets = new ArrayList<LivingEntity>();
        targets.add(hit);
        executeChildren(projectile.getShooter(), SkillAPI.getMetaInt(projectile, LEVEL), targets);
        projectile.setCallback(null);
    }
}
