package com.sucy.skill.dynamic.trigger;

import com.sucy.skill.api.Settings;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.List;
import java.util.Map;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * SkillAPI © 2018
 * com.sucy.skill.dynamic.trigger.BlockBreakTrigger
 */
@SkillNode(
        key = "BLOCK_PLACE",
        name = "Block Place",
        nameZh = "方块放置时",
        description = "Applies skill effects when a player places a block matching the given details",
        descriptionZh = "玩家放置方块时触发，施法者与初始目标都是这名玩家。判定与写入的数值针对刚放下的那个方块：类型写入 api-block-type、坐标写入 api-block-loc。已被其他插件取消的放置不会触发。",
        container = true)
public class BlockPlaceTrigger implements Trigger<BlockPlaceEvent> {
    @SkillField(
            kind = FieldKind.MultiListValue,
            label = "Material",
            labelZh = "材质",
            tooltip = "[material] The type of block expected to be placed",
            tooltipZh = "限定哪些方块才触发。留空或包含 Any 表示不限；否则把填写值里的空格换成下划线后与方块真实类型名做忽略大小写比较。通配只认首字母大写的 Any，写成小写 any 会永不触发。",
            options = {"Any", "Acacia Door", "Acacia Door Item", "Acacia Fence", "Acacia Fence Gate", "Acacia Stairs", "Activator Rail", "Air", "Anvil", "Apple", "Armor Stand", "Arrow", "Baked Potato", "Banner", "Barrier", "Beacon", "Bed", "Bed Block", "Bedrock", "Beetroot", "Beetroot Block", "Beetroot Seeds", "Beetroot Soup", "Birch Door", "Birch Door Item", "Birch Fence", "Birch Fence Gate", "Birch Wood Stairs", "Black Glazed Terracotta", "Black Shulker Box", "Blaze Powder", "Blaze Rod", "Blue Glazed Terracotta", "Blue Shulker Box", "Boat", "Boat Acacia", "Boat Birch", "Boat Dark Oak", "Boat Jungle", "Boat Spruce", "Bone", "Bone Block", "Book", "Book And Quill", "Bookshelf", "Bow", "Bowl", "Bread", "Brewing Stand", "Brewing Stand Item", "Brick", "Brick Stairs", "Brown Glazed Terracotta", "Brown Mushroom", "Brown Shulker Box", "Bucket", "Burning Furnace", "Cactus", "Cake", "Cake Block", "Carpet", "Carrot", "Carrot Item", "Carrot Stick", "Cauldron", "Cauldron Item", "Chainmail Boots", "Chainmail Chestplate", "Chainmail Helmet", "Chainmail Leggings", "Chest", "Chorus Flower", "Chorus Fruit", "Chorus Fruit Popped", "Chorus Plant", "Clay", "Clay Ball", "Clay Brick", "Coal", "Coal Block", "Coal Ore", "Cobble Wall", "Cobblestone", "Cobblestone Stairs", "Cocoa", "Command", "Command Chain", "Command Minecart", "Command Repeating", "Compass", "Concrete", "Concrete Powder", "Cooked Beef", "Cooked Chicken", "Cooked Fish", "Cooked Mutton", "Cooked Rabbit", "Cookie", "Crops", "Cyan Glazed Terracotta", "Cyan Shulker Box", "Dark Oak Door", "Dark Oak Door Item", "Dark Oak Fence", "Dark Oak Fence Gate", "Dark Oak Stairs", "Daylight Detector", "Daylight Detector Inverted", "Dead Bush", "Detector Rail", "Diamond", "Diamond Axe", "Diamond Barding", "Diamond Block", "Diamond Boots", "Diamond Chestplate", "Diamond Helmet", "Diamond Hoe", "Diamond Leggings", "Diamond Ore", "Diamond Pickaxe", "Diamond Spade", "Diamond Sword", "Diode", "Diode Block Off", "Diode Block On", "Dirt", "Dispenser", "Double Plant", "Double Step", "Double Stone Slab2", "Dragon Egg", "Dragons Breath", "Dropper", "Egg", "Elytra", "Emerald", "Emerald Block", "Emerald Ore", "Empty Map", "Enchanted Book", "Enchantment Table", "End Bricks", "End Crystal", "End Gateway", "End Rod", "Ender Chest", "Ender Pearl", "Ender Portal", "Ender Portal Frame", "Ender Stone", "Exp Bottle", "Explosive Minecart", "Eye Of Ender", "Feather", "Fence", "Fence Gate", "Fermented Spider Eye", "Fire", "Fireball", "Firework", "Firework Charge", "Fishing Rod", "Flint", "Flint And Steel", "Flower Pot", "Flower Pot Item", "Frosted Ice", "Furnace", "Ghast Tear", "Glass", "Glass Bottle", "Glowing Redstone Ore", "Glowstone", "Glowstone Dust", "Gold Axe", "Gold Barding", "Gold Block", "Gold Boots", "Gold Chestplate", "Gold Helmet", "Gold Hoe", "Gold Ingot", "Gold Leggings", "Gold Nugget", "Gold Ore", "Gold Pickaxe", "Gold Plate", "Gold Record", "Gold Spade", "Gold Sword", "Golden Apple", "Golden Carrot", "Grass", "Grass Path", "Gravel", "Gray Glazed Terracotta", "Gray Shulker Box", "Green Glazed Terracotta", "Green Record", "Green Shulker Box", "Grilled Pork", "Hard Clay", "Hay Block", "Hopper", "Hopper Minecart", "Huge Mushroom 1", "Huge Mushroom 2", "Ice", "Ink Sack", "Iron Axe", "Iron Barding", "Iron Block", "Iron Boots", "Iron Chestplate", "Iron Door", "Iron Door Block", "Iron Fence", "Iron Helmet", "Iron Hoe", "Iron Ingot", "Iron Leggings", "Iron Nugget", "Iron Ore", "Iron Pickaxe", "Iron Plate", "Iron Spade", "Iron Sword", "Iron Trapdoor", "Item Frame", "Jack O Lantern", "Jukebox", "Jungle Door", "Jungle Door Item", "Jungle Fence", "Jungle Fence Gate", "Jungle Wood Stairs", "Knowledge Book", "Ladder", "Lapis Block", "Lapis Ore", "Lava", "Lava Bucket", "Leash", "Leather", "Leather Boots", "Leather Chestplate", "Leather Helmet", "Leather Leggings", "Leaves", "Leaves 2", "Lever", "Light Blue Glazed Terracotta", "Light Blue Shulker Box", "Lime Glazed Terracotta", "Lime Shulker Box", "Lingering Potion", "Log", "Log 2", "Long Grass", "Magenta Glazed Terracotta", "Magenta Shulker Box", "Magma", "Magma Cream", "Map", "Melon", "Melon Block", "Melon Seeds", "Melon Stem", "Milk Bucket", "Minecart", "Mob Spawner", "Monster Egg", "Monster Eggs", "Mossy Cobblestone", "Mushroom Soup", "Mutton", "Mycel", "Name Tag", "Nether Brick", "Nether Brick Item", "Nether Brick Stairs", "Nether Fence", "Nether Stalk", "Nether Star", "Nether Wart Block", "Nether Warts", "Netherrack", "Note Block", "Observer", "Obsidian", "Orange Glazed Terracotta", "Orange Shulker Box", "Packed Ice", "Painting", "Paper", "Pink Glazed Terracotta", "Pink Shulker Box", "Piston Base", "Piston Extension", "Piston Moving Piece", "Piston Sticky Base", "Poisonous Potato", "Pork", "Portal", "Potato", "Potato Item", "Potion", "Powered Minecart", "Powered Rail", "Prismarine", "Prismarine Crystals", "Prismarine Shard", "Pumpkin", "Pumpkin Pie", "Pumpkin Seeds", "Pumpkin Stem", "Purple Glazed Terracotta", "Purple Shulker Box", "Purpur Block", "Purpur Double Slab", "Purpur Pillar", "Purpur Slab", "Purpur Stairs", "Quartz", "Quartz Block", "Quartz Ore", "Quartz Stairs", "Rabbit", "Rabbit Foot", "Rabbit Hide", "Rabbit Stew", "Rails", "Raw Beef", "Raw Chicken", "Raw Fish", "Record 10", "Record 11", "Record 12", "Record 3", "Record 4", "Record 5", "Record 6", "Record 7", "Record 8", "Record 9", "Red Glazed Terracotta", "Red Mushroom", "Red Nether Brick", "Red Rose", "Red Sandstone", "Red Sandstone Stairs", "Red Shulker Box", "Redstone", "Redstone Block", "Redstone Comparator", "Redstone Comparator Off", "Redstone Comparator On", "Redstone Lamp Off", "Redstone Lamp On", "Redstone Ore", "Redstone Torch Off", "Redstone Torch On", "Redstone Wire", "Rotten Flesh", "Saddle", "Sand", "Sandstone", "Sandstone Stairs", "Sapling", "Sea Lantern", "Seeds", "Shears", "Shield", "Shulker Shell", "Sign", "Sign Post", "Silver Glazed Terracotta", "Silver Shulker Box", "Skull", "Skull Item", "Slime Ball", "Slime Block", "Smooth Brick", "Smooth Stairs", "Snow", "Snow Ball", "Snow Block", "Soil", "Soul Sand", "Speckled Melon", "Spectral Arrow", "Spider Eye", "Splash Potion", "Sponge", "Spruce Door", "Spruce Door Item", "Spruce Fence", "Spruce Fence Gate", "Spruce Wood Stairs", "Stained Clay", "Stained Glass", "Stained Glass Pane", "Standing Banner", "Stationary Lava", "Stationary Water", "Step", "Stick", "Stone", "Stone Axe", "Stone Button", "Stone Hoe", "Stone Pickaxe", "Stone Plate", "Stone Slab2", "Stone Spade", "Stone Sword", "Storage Minecart", "String", "Structure Block", "Structure Void", "Sugar", "Sugar Cane", "Sugar Cane Block", "Sulphur", "Thin Glass", "Tipped Arrow", "Tnt", "Torch", "Totem", "Trap Door", "Trapped Chest", "Tripwire", "Tripwire Hook", "Vine", "Wall Banner", "Wall Sign", "Watch", "Water", "Water Bucket", "Water Lily", "Web", "Wheat", "White Glazed Terracotta", "White Shulker Box", "Wood", "Wood Axe", "Wood Button", "Wood Door", "Wood Double Step", "Wood Hoe", "Wood Pickaxe", "Wood Plate", "Wood Spade", "Wood Stairs", "Wood Step", "Wood Sword", "Wooden Door", "Wool", "Workbench", "Written Book", "Yellow Flower", "Yellow Glazed Terracotta", "Yellow Shulker Box"},
            optionsZh = {"任意", "金合欢木门", "金合欢木门物品", "金合欢木栅栏", "金合欢木栅栏", "金合欢木楼梯", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "箭", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "桦木门", "桦木门物品", "桦木栅栏", "桦木栅栏", "桦木木头楼梯", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "金合欢木", "桦木", "深色橡木", "丛林木", "云杉", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "弓", "其他材质", "其他材质", "其他材质", "物品", "其他材质", "楼梯", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "物品", "其他材质", "其他材质", "物品", "靴子", "胸甲", "头盔", "护腿", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "楼梯", "其他材质", "命令", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "深色橡木门", "深色橡木门物品", "深色橡木栅栏", "深色橡木栅栏", "深色橡木楼梯", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "斧", "其他材质", "其他材质", "靴子", "胸甲", "头盔", "其他材质", "护腿", "其他材质", "镐", "其他材质", "剑", "其他材质", "其他材质", "其他材质", "泥土", "其他材质", "其他材质", "脚步", "石头", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "石头", "其他材质", "其他材质", "其他材质", "其他材质", "栅栏", "栅栏", "其他材质", "火焰", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "物品", "其他材质", "其他材质", "其他材质", "玻璃", "玻璃", "其他材质", "其他材质", "其他材质", "斧", "其他材质", "其他材质", "靴子", "胸甲", "头盔", "其他材质", "其他材质", "护腿", "其他材质", "其他材质", "镐", "其他材质", "其他材质", "其他材质", "剑", "其他材质", "其他材质", "草方块", "草方块", "沙砾", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "斧", "其他材质", "其他材质", "靴子", "胸甲", "门", "门", "栅栏", "头盔", "其他材质", "其他材质", "护腿", "其他材质", "其他材质", "镐", "其他材质", "其他材质", "剑", "其他材质", "物品", "其他材质", "其他材质", "丛林木门", "丛林木门物品", "丛林木栅栏", "丛林木栅栏", "丛林木木头楼梯", "其他材质", "其他材质", "其他材质", "其他材质", "熔岩", "熔岩", "其他材质", "其他材质", "靴子", "胸甲", "头盔", "护腿", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "药水", "其他材质", "其他材质", "草方块", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "物品", "楼梯", "栅栏", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "物品", "药水效果", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "台阶", "其他材质", "台阶", "楼梯", "其他材质", "其他材质", "其他材质", "楼梯", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "楼梯", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "沙子", "其他材质", "楼梯", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "物品", "其他材质", "其他材质", "其他材质", "楼梯", "其他材质", "其他材质", "其他材质", "其他材质", "沙子", "其他材质", "箭", "其他材质", "药水", "其他材质", "云杉门", "云杉门物品", "云杉栅栏", "云杉栅栏", "云杉木头楼梯", "其他材质", "玻璃", "玻璃", "其他材质", "熔岩", "水", "脚步", "其他材质", "石头", "石头斧", "石头", "石头", "石头镐", "石头", "石头", "石头", "石头剑", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "玻璃", "箭", "其他材质", "其他材质", "其他材质", "门", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "水", "水", "水", "其他材质", "其他材质", "其他材质", "其他材质", "木头", "木头斧", "木头", "木头门", "木头脚步", "木头", "木头镐", "木头", "木头", "木头楼梯", "木头脚步", "木头剑", "门", "羊毛", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质"})
    private static final String MATERIAL = "material";


    /** {@inheritDoc} */
    @Override
    public String getKey() {
        return "BLOCK_PLACE";
    }

    /** {@inheritDoc} */
    @Override
    public Class<BlockPlaceEvent> getEvent() {
        return BlockPlaceEvent.class;
    }

    /** {@inheritDoc} */
    @Override
    public boolean shouldTrigger(final BlockPlaceEvent event, final int level, final Settings settings) {
        final List<String> types = settings.getStringList(MATERIAL);
        return types.isEmpty() || types.contains("Any")
                || types.stream().anyMatch(mat -> event.getBlock().getType().name().equalsIgnoreCase(mat.replace(' ','_')));
    }

    /** {@inheritDoc} */
    @Override
    public void setValues(final BlockPlaceEvent event, final Map<String, Object> data) {
        data.put("api-block-type", event.getBlock().getType().name());
        data.put("api-block-loc", event.getBlock().getLocation());
    }

    /** {@inheritDoc} */
    @Override
    public LivingEntity getCaster(final BlockPlaceEvent event) {
        return event.getPlayer();
    }

    /** {@inheritDoc} */
    @Override
    public LivingEntity getTarget(final BlockPlaceEvent event, final Settings settings) {
        return event.getPlayer();
    }
}
