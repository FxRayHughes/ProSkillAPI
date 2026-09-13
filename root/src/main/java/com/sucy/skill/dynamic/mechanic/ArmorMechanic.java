package com.sucy.skill.dynamic.mechanic;

import com.rit.sucy.text.TextFormatter;
import com.sucy.skill.SkillAPI;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Sets the specified armor slot of the target to the item defined by the settings
 */
@SkillNode(
        key = "armor",
        name = "Armor",
        nameZh = "盔甲",
        description = "Sets the specified armor slot of the target to the item defined by the settings",
        descriptionZh = "按配置组装一件物品，装备到每个目标的指定装备槽（主手/副手/脚/腿/胸/头）。默认只在该槽为空时才装备；开启「覆盖」会直接顶掉原有物品且原物品被永久丢弃。装备后不会自动归还或清除，需要自己用别的节点收尾。槽位名或材质名无法解析时整个节点直接返回 false（不做任何事）。本节点所有取值都不随技能等级缩放。")
public class ArmorMechanic extends MechanicComponent {
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Slot",
            labelZh = "槽位",
            tooltip = "[slot] The slot number to set the item to",
            tooltipZh = "装备到哪个槽位，取 Bukkit EquipmentSlot 名（Hand/Off Hand/Feet/Legs/Chest/Head）。填了识别不了的值，节点直接失败返回 false，不会退回默认手持。",
            options = {"Hand", "Off Hand", "Feet", "Legs", "Chest", "Head"},
            optionsZh = {"手持位置", "可选值2", "可选值3", "可选值4", "可选值5", "头部"},
            defaultValue = "Hand")
    private static final String SLOT = "slot";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Material",
            labelZh = "材质",
            tooltip = "[material] The type of item to set",
            tooltipZh = "要装备的物品材质名，空格会被转成下划线后按 Material 枚举解析。解析失败节点直接返回 false。",
            options = {"Acacia Door", "Acacia Door Item", "Acacia Fence", "Acacia Fence Gate", "Acacia Stairs", "Activator Rail", "Air", "Anvil", "Apple", "Armor Stand", "Arrow", "Baked Potato", "Banner", "Barrier", "Beacon", "Bed", "Bed Block", "Bedrock", "Beetroot", "Beetroot Block", "Beetroot Seeds", "Beetroot Soup", "Birch Door", "Birch Door Item", "Birch Fence", "Birch Fence Gate", "Birch Wood Stairs", "Black Glazed Terracotta", "Black Shulker Box", "Blaze Powder", "Blaze Rod", "Blue Glazed Terracotta", "Blue Shulker Box", "Boat", "Boat Acacia", "Boat Birch", "Boat Dark Oak", "Boat Jungle", "Boat Spruce", "Bone", "Bone Block", "Book", "Book And Quill", "Bookshelf", "Bow", "Bowl", "Bread", "Brewing Stand", "Brewing Stand Item", "Brick", "Brick Stairs", "Brown Glazed Terracotta", "Brown Mushroom", "Brown Shulker Box", "Bucket", "Burning Furnace", "Cactus", "Cake", "Cake Block", "Carpet", "Carrot", "Carrot Item", "Carrot Stick", "Cauldron", "Cauldron Item", "Chainmail Boots", "Chainmail Chestplate", "Chainmail Helmet", "Chainmail Leggings", "Chest", "Chorus Flower", "Chorus Fruit", "Chorus Fruit Popped", "Chorus Plant", "Clay", "Clay Ball", "Clay Brick", "Coal", "Coal Block", "Coal Ore", "Cobble Wall", "Cobblestone", "Cobblestone Stairs", "Cocoa", "Command", "Command Chain", "Command Minecart", "Command Repeating", "Compass", "Concrete", "Concrete Powder", "Cooked Beef", "Cooked Chicken", "Cooked Fish", "Cooked Mutton", "Cooked Rabbit", "Cookie", "Crops", "Cyan Glazed Terracotta", "Cyan Shulker Box", "Dark Oak Door", "Dark Oak Door Item", "Dark Oak Fence", "Dark Oak Fence Gate", "Dark Oak Stairs", "Daylight Detector", "Daylight Detector Inverted", "Dead Bush", "Detector Rail", "Diamond", "Diamond Axe", "Diamond Barding", "Diamond Block", "Diamond Boots", "Diamond Chestplate", "Diamond Helmet", "Diamond Hoe", "Diamond Leggings", "Diamond Ore", "Diamond Pickaxe", "Diamond Spade", "Diamond Sword", "Diode", "Diode Block Off", "Diode Block On", "Dirt", "Dispenser", "Double Plant", "Double Step", "Double Stone Slab2", "Dragon Egg", "Dragons Breath", "Dropper", "Egg", "Elytra", "Emerald", "Emerald Block", "Emerald Ore", "Empty Map", "Enchanted Book", "Enchantment Table", "End Bricks", "End Crystal", "End Gateway", "End Rod", "Ender Chest", "Ender Pearl", "Ender Portal", "Ender Portal Frame", "Ender Stone", "Exp Bottle", "Explosive Minecart", "Eye Of Ender", "Feather", "Fence", "Fence Gate", "Fermented Spider Eye", "Fire", "Fireball", "Firework", "Firework Charge", "Fishing Rod", "Flint", "Flint And Steel", "Flower Pot", "Flower Pot Item", "Frosted Ice", "Furnace", "Ghast Tear", "Glass", "Glass Bottle", "Glowing Redstone Ore", "Glowstone", "Glowstone Dust", "Gold Axe", "Gold Barding", "Gold Block", "Gold Boots", "Gold Chestplate", "Gold Helmet", "Gold Hoe", "Gold Ingot", "Gold Leggings", "Gold Nugget", "Gold Ore", "Gold Pickaxe", "Gold Plate", "Gold Record", "Gold Spade", "Gold Sword", "Golden Apple", "Golden Carrot", "Grass", "Grass Path", "Gravel", "Gray Glazed Terracotta", "Gray Shulker Box", "Green Glazed Terracotta", "Green Record", "Green Shulker Box", "Grilled Pork", "Hard Clay", "Hay Block", "Hopper", "Hopper Minecart", "Huge Mushroom 1", "Huge Mushroom 2", "Ice", "Ink Sack", "Iron Axe", "Iron Barding", "Iron Block", "Iron Boots", "Iron Chestplate", "Iron Door", "Iron Door Block", "Iron Fence", "Iron Helmet", "Iron Hoe", "Iron Ingot", "Iron Leggings", "Iron Nugget", "Iron Ore", "Iron Pickaxe", "Iron Plate", "Iron Spade", "Iron Sword", "Iron Trapdoor", "Item Frame", "Jack O Lantern", "Jukebox", "Jungle Door", "Jungle Door Item", "Jungle Fence", "Jungle Fence Gate", "Jungle Wood Stairs", "Knowledge Book", "Ladder", "Lapis Block", "Lapis Ore", "Lava", "Lava Bucket", "Leash", "Leather", "Leather Boots", "Leather Chestplate", "Leather Helmet", "Leather Leggings", "Leaves", "Leaves 2", "Lever", "Light Blue Glazed Terracotta", "Light Blue Shulker Box", "Lime Glazed Terracotta", "Lime Shulker Box", "Lingering Potion", "Log", "Log 2", "Long Grass", "Magenta Glazed Terracotta", "Magenta Shulker Box", "Magma", "Magma Cream", "Map", "Melon", "Melon Block", "Melon Seeds", "Melon Stem", "Milk Bucket", "Minecart", "Mob Spawner", "Monster Egg", "Monster Eggs", "Mossy Cobblestone", "Mushroom Soup", "Mutton", "Mycel", "Name Tag", "Nether Brick", "Nether Brick Item", "Nether Brick Stairs", "Nether Fence", "Nether Stalk", "Nether Star", "Nether Wart Block", "Nether Warts", "Netherrack", "Note Block", "Observer", "Obsidian", "Orange Glazed Terracotta", "Orange Shulker Box", "Packed Ice", "Painting", "Paper", "Pink Glazed Terracotta", "Pink Shulker Box", "Piston Base", "Piston Extension", "Piston Moving Piece", "Piston Sticky Base", "Poisonous Potato", "Pork", "Portal", "Potato", "Potato Item", "Potion", "Powered Minecart", "Powered Rail", "Prismarine", "Prismarine Crystals", "Prismarine Shard", "Pumpkin", "Pumpkin Pie", "Pumpkin Seeds", "Pumpkin Stem", "Purple Glazed Terracotta", "Purple Shulker Box", "Purpur Block", "Purpur Double Slab", "Purpur Pillar", "Purpur Slab", "Purpur Stairs", "Quartz", "Quartz Block", "Quartz Ore", "Quartz Stairs", "Rabbit", "Rabbit Foot", "Rabbit Hide", "Rabbit Stew", "Rails", "Raw Beef", "Raw Chicken", "Raw Fish", "Record 10", "Record 11", "Record 12", "Record 3", "Record 4", "Record 5", "Record 6", "Record 7", "Record 8", "Record 9", "Red Glazed Terracotta", "Red Mushroom", "Red Nether Brick", "Red Rose", "Red Sandstone", "Red Sandstone Stairs", "Red Shulker Box", "Redstone", "Redstone Block", "Redstone Comparator", "Redstone Comparator Off", "Redstone Comparator On", "Redstone Lamp Off", "Redstone Lamp On", "Redstone Ore", "Redstone Torch Off", "Redstone Torch On", "Redstone Wire", "Rotten Flesh", "Saddle", "Sand", "Sandstone", "Sandstone Stairs", "Sapling", "Sea Lantern", "Seeds", "Shears", "Shield", "Shulker Shell", "Sign", "Sign Post", "Silver Glazed Terracotta", "Silver Shulker Box", "Skull", "Skull Item", "Slime Ball", "Slime Block", "Smooth Brick", "Smooth Stairs", "Snow", "Snow Ball", "Snow Block", "Soil", "Soul Sand", "Speckled Melon", "Spectral Arrow", "Spider Eye", "Splash Potion", "Sponge", "Spruce Door", "Spruce Door Item", "Spruce Fence", "Spruce Fence Gate", "Spruce Wood Stairs", "Stained Clay", "Stained Glass", "Stained Glass Pane", "Standing Banner", "Stationary Lava", "Stationary Water", "Step", "Stick", "Stone", "Stone Axe", "Stone Button", "Stone Hoe", "Stone Pickaxe", "Stone Plate", "Stone Slab2", "Stone Spade", "Stone Sword", "Storage Minecart", "String", "Structure Block", "Structure Void", "Sugar", "Sugar Cane", "Sugar Cane Block", "Sulphur", "Thin Glass", "Tipped Arrow", "Tnt", "Torch", "Totem", "Trap Door", "Trapped Chest", "Tripwire", "Tripwire Hook", "Vine", "Wall Banner", "Wall Sign", "Watch", "Water", "Water Bucket", "Water Lily", "Web", "Wheat", "White Glazed Terracotta", "White Shulker Box", "Wood", "Wood Axe", "Wood Button", "Wood Door", "Wood Double Step", "Wood Hoe", "Wood Pickaxe", "Wood Plate", "Wood Spade", "Wood Stairs", "Wood Step", "Wood Sword", "Wooden Door", "Wool", "Workbench", "Written Book", "Yellow Flower", "Yellow Glazed Terracotta", "Yellow Shulker Box"},
            optionsZh = {"金合欢木门", "金合欢木门物品", "金合欢木栅栏", "金合欢木栅栏", "金合欢木楼梯", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "箭", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "桦木门", "桦木门物品", "桦木栅栏", "桦木栅栏", "桦木木头楼梯", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "金合欢木", "桦木", "深色橡木", "丛林木", "云杉", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "弓", "其他材质", "其他材质", "其他材质", "物品", "其他材质", "楼梯", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "物品", "其他材质", "其他材质", "物品", "靴子", "胸甲", "头盔", "护腿", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "楼梯", "其他材质", "命令", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "深色橡木门", "深色橡木门物品", "深色橡木栅栏", "深色橡木栅栏", "深色橡木楼梯", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "斧", "其他材质", "其他材质", "靴子", "胸甲", "头盔", "其他材质", "护腿", "其他材质", "镐", "其他材质", "剑", "其他材质", "其他材质", "其他材质", "泥土", "其他材质", "其他材质", "脚步", "石头", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "石头", "其他材质", "其他材质", "其他材质", "其他材质", "栅栏", "栅栏", "其他材质", "火焰", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "物品", "其他材质", "其他材质", "其他材质", "玻璃", "玻璃", "其他材质", "其他材质", "其他材质", "斧", "其他材质", "其他材质", "靴子", "胸甲", "头盔", "其他材质", "其他材质", "护腿", "其他材质", "其他材质", "镐", "其他材质", "其他材质", "其他材质", "剑", "其他材质", "其他材质", "草方块", "草方块", "沙砾", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "斧", "其他材质", "其他材质", "靴子", "胸甲", "门", "门", "栅栏", "头盔", "其他材质", "其他材质", "护腿", "其他材质", "其他材质", "镐", "其他材质", "其他材质", "剑", "其他材质", "物品", "其他材质", "其他材质", "丛林木门", "丛林木门物品", "丛林木栅栏", "丛林木栅栏", "丛林木木头楼梯", "其他材质", "其他材质", "其他材质", "其他材质", "熔岩", "熔岩", "其他材质", "其他材质", "靴子", "胸甲", "头盔", "护腿", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "药水", "其他材质", "其他材质", "草方块", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "物品", "楼梯", "栅栏", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "物品", "药水效果", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "台阶", "其他材质", "台阶", "楼梯", "其他材质", "其他材质", "其他材质", "楼梯", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "楼梯", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "沙子", "其他材质", "楼梯", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "物品", "其他材质", "其他材质", "其他材质", "楼梯", "其他材质", "其他材质", "其他材质", "其他材质", "沙子", "其他材质", "箭", "其他材质", "药水", "其他材质", "云杉门", "云杉门物品", "云杉栅栏", "云杉栅栏", "云杉木头楼梯", "其他材质", "玻璃", "玻璃", "其他材质", "熔岩", "水", "脚步", "其他材质", "石头", "石头斧", "石头", "石头", "石头镐", "石头", "石头", "石头", "石头剑", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "玻璃", "箭", "其他材质", "其他材质", "其他材质", "门", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质", "水", "水", "水", "其他材质", "其他材质", "其他材质", "其他材质", "木头", "木头斧", "木头", "木头门", "木头脚步", "木头", "木头镐", "木头", "木头", "木头楼梯", "木头脚步", "木头剑", "门", "羊毛", "其他材质", "其他材质", "其他材质", "其他材质", "其他材质"},
            defaultValue = "Arrow")
    private static final String MATERIAL = "material";
    @SkillField(
            kind = FieldKind.IntValue,
            label = "Amount",
            labelZh = "数量",
            tooltip = "[amount] The quantity of the item to set",
            tooltipZh = "物品堆叠数量，直接传给 ItemStack 构造。装备槽通常只显示 1 个，改大它一般只在被打掉后捡起时才有意义。",
            defaultValue = "1")
    private static final String AMOUNT   = "amount";
    @SkillField(
            kind = FieldKind.IntValue,
            label = "Durability",
            labelZh = "耐久度",
            tooltip = "[durability] The durability value of the item to set",
            tooltipZh = "物品的损伤值。服务端配置 use-old-durability 时走旧的 setDurability；否则写入 ItemMeta 的 Damageable#setDamage（1.13+ 的新耐久模型）。",
            defaultValue = "0")
    private static final String DURABILITY = "durability";
    @SkillField(
            kind = FieldKind.IntValue,
            label = "Data",
            labelZh = "数据值",
            tooltip = "[data] The data value or the CustomModelData (1.14+ only) to apply to the item",
            tooltipZh = "旧版方块/物品数据值，或 1.14+ 的 CustomModelData。走哪条取决于服务端配置 use-skill-model-data：开启时写 CustomModelData，否则写旧的 MaterialData。",
            defaultValue = "0")
    private static final String DATA     = "data";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Custom",
            labelZh = "自定义",
            tooltip = "[custom] Whether or not to apply a custom name/lore to the item",
            tooltipZh = "是否给物品套用自定义名称和 Lore。为 false 时「名称」和「物品描述」两项完全不生效。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "False")
    private static final String CUSTOM   = "custom";
    @SkillField(
            kind = FieldKind.StringValue,
            label = "Name",
            labelZh = "名称",
            tooltip = "[name] The name of the item",
            tooltipZh = "物品显示名，支持 & 颜色代码。仅在「自定义」为 true 且该项非空时才写入。",
            defaultValue = "Name")
    private static final String NAME     = "name";
    @SkillField(
            kind = FieldKind.StringListValue,
            label = "Lore",
            labelZh = "物品描述",
            tooltip = "[lore] The lore text for the item (the text below the name)",
            tooltipZh = "物品 Lore 文本，一行一条，支持 & 颜色代码。仅在「自定义」为 true 时写入，且会整体替换（不是追加）。",
            defaultValue = "")
    private static final String LORE     = "lore";
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Overwrite",
            labelZh = "覆盖",
            tooltip = "[overwrite] USE WITH CAUTION. Whether or not to overwrite an existing item in the slot. If true, will permanently delete the existing iem",
            tooltipZh = "为 true 时无条件覆盖目标该槽位的原有物品，原物品被永久删除、无法找回。为 false 时只有该槽位是空气才会装备，否则跳过这个目标。",
            options = {"True", "False"},
            optionsZh = {"是", "否"},
            defaultValue = "False")
    private static final String OVERWRITE = "overwrite";

    @Override
    public String getKey() { return "armor"; }

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
        EquipmentSlot slot;
        try {
            slot = EquipmentSlot.valueOf(settings.getString(SLOT, "HAND").toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException exception) { return false; }
        Material material;
        material = com.sucy.skill.api.util.MaterialCompat.resolve(mat, 0, true);
        if (material == null) return false;
        int amount = settings.getInt(AMOUNT, 1);
        int durability = settings.getInt(DURABILITY, 0);
        int data = settings.getInt(DATA, 0);
        boolean overwrite = settings.getBool(OVERWRITE, false);

        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        if (settings.getString(CUSTOM, "false").toLowerCase().equals("true")) {
            String name = TextFormatter.colorString(settings.getString(NAME, ""));
            if (name.length() > 0) {
                meta.setDisplayName(name);
            }
            List<String> lore = TextFormatter.colorStringList(settings.getStringList(LORE));
            meta.setLore(lore);
        }

        if (SkillAPI.getSettings().useSkillModelData()) {
            com.sucy.skill.api.util.MaterialCompat.setCustomModelData(meta, data);
        } else {
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
        boolean success = false;
        for (LivingEntity target : targets) {
            EntityEquipment equipment = target.getEquipment();
            boolean proceed = overwrite;
            if (!overwrite) {
                switch (slot) {
                    case FEET:
                        proceed = equipment.getBoots().getType().equals(Material.AIR);
                        break;
                    case HAND:
                        proceed = equipment.getItemInMainHand().getType().equals(Material.AIR);
                        break;
                    case HEAD:
                        proceed = equipment.getHelmet().getType().equals(Material.AIR);
                        break;
                    case LEGS:
                        proceed = equipment.getLeggings().getType().equals(Material.AIR);
                        break;
                    case CHEST:
                        proceed = equipment.getChestplate().getType().equals(Material.AIR);
                        break;
                    case OFF_HAND:
                        proceed = equipment.getItemInOffHand().getType().equals(Material.AIR);
                        break;
                }
            }
            if (proceed) {
                switch (slot) {
                    case FEET:
                        equipment.setBoots(item);
                        break;
                    case HAND:
                        equipment.setItemInMainHand(item);
                        break;
                    case HEAD:
                        equipment.setHelmet(item);
                        break;
                    case LEGS:
                        equipment.setLeggings(item);
                        break;
                    case CHEST:
                        equipment.setChestplate(item);
                        break;
                    case OFF_HAND:
                        equipment.setItemInOffHand(item);
                        break;
                }
                success = true;
            }
        }
        return success;
    }
}
