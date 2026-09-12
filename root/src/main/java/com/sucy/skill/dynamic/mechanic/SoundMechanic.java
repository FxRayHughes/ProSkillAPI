/**
 * SkillAPI
 * com.sucy.skill.dynamic.mechanic.SoundMechanic
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

import com.sucy.skill.compat.bukkit.EnumCompat;
import org.bukkit.Registry;
import com.sucy.skill.log.Logger;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;

import java.util.List;
import com.sucy.skill.dynamic.meta.SkillNode;
import com.sucy.skill.dynamic.meta.SkillField;
import com.sucy.skill.dynamic.meta.FieldKind;

/**
 * Plays a particle effect
 */
@SkillNode(
        key = "sound",
        name = "Sound",
        nameZh = "音效",
        description = "Plays a sound at the target's location.",
        descriptionZh = "在每个目标所在位置播放一个原版音效。音效名会被转成大写并把空格换成下划线后匹配原版 Sound 枚举，匹配失败会在控制台输出无效音效警告并返回 false。若未配置 sound 键，会回退读取旧键 newsound。音量下限被夹到 0，音调被夹在 0.5 到 2 之间。目标列表为空时返回 false。")
public class SoundMechanic extends MechanicComponent
{
    @SkillField(
            kind = FieldKind.ListValue,
            label = "Sound",
            labelZh = "音效",
            tooltip = "[sound] The sound clip to play",
            tooltipZh = "要播放的音效名，需能匹配当前服务端版本的原版 Sound 枚举（大写下划线形式）。跨版本迁移时旧音效名可能失效。",
            options = {"Ambient Cave", "Block Anvil Break", "Block Anvil Destroy", "Block Anvil Fall", "Block Anvil Hit", "Block Anvil Land", "Block Anvil Place", "Block Anvil Step", "Block Anvil Use", "Block Brewing Stand Brew", "Block Chest Close", "Block Chest Locked", "Block Chest Open", "Block Chorus Flower Death", "Block Chorus Flower Grow", "Block Cloth Break", "Block Cloth Fall", "Block Cloth Hit", "Block Cloth Place", "Block Cloth Step", "Block Comparator Click", "Block Dispenser Dispense", "Block Dispenser Fail", "Block Dispenser Launch", "Block Enchantment Table Use", "Block End Gateway Spawn", "Block End Portal Frame Fill", "Block End Portal Spawn", "Block Enderchest Close", "Block Enderchest Open", "Block Fence Gate Close", "Block Fence Gate Open", "Block Fire Ambient", "Block Fire Extinguish", "Block Furnace Fire Crackle", "Block Glass Break", "Block Glass Fall", "Block Glass Hit", "Block Glass Place", "Block Glass Step", "Block Grass Break", "Block Grass Fall", "Block Grass Hit", "Block Grass Place", "Block Grass Step", "Block Gravel Break", "Block Gravel Fall", "Block Gravel Hit", "Block Gravel Place", "Block Gravel Step", "Block Iron Door Close", "Block Iron Door Open", "Block Iron Trapdoor Close", "Block Iron Trapdoor Open", "Block Ladder Break", "Block Ladder Fall", "Block Ladder Hit", "Block Ladder Place", "Block Ladder Step", "Block Lava Ambient", "Block Lava Extinguish", "Block Lava Pop", "Block Lever Click", "Block Metal Break", "Block Metal Fall", "Block Metal Hit", "Block Metal Place", "Block Metal Pressureplate Click Off", "Block Metal Pressureplate Click On", "Block Metal Step", "Block Note Basedrum", "Block Note Bass", "Block Note Bell", "Block Note Chime", "Block Note Flute", "Block Note Guitar", "Block Note Harp", "Block Note Hat", "Block Note Pling", "Block Note Snare", "Block Note Xylophone", "Block Piston Contract", "Block Piston Extend", "Block Portal Ambient", "Block Portal Travel", "Block Portal Trigger", "Block Redstone Torch Burnout", "Block Sand Break", "Block Sand Fall", "Block Sand Hit", "Block Sand Place", "Block Sand Step", "Block Shulker Box Close", "Block Shulker Box Open", "Block Slime Break", "Block Slime Fall", "Block Slime Hit", "Block Slime Place", "Block Slime Step", "Block Snow Break", "Block Snow Fall", "Block Snow Hit", "Block Snow Place", "Block Snow Step", "Block Stone Break", "Block Stone Button Click Off", "Block Stone Button Click On", "Block Stone Fall", "Block Stone Hit", "Block Stone Place", "Block Stone Pressureplate Click Off", "Block Stone Pressureplate Click On", "Block Stone Step", "Block Tripwire Attach", "Block Tripwire Click Off", "Block Tripwire Click On", "Block Tripwire Detach", "Block Water Ambient", "Block Waterlily Place", "Block Wood Break", "Block Wood Button Click Off", "Block Wood Button Click On", "Block Wood Fall", "Block Wood Hit", "Block Wood Place", "Block Wood Pressureplate Click Off", "Block Wood Pressureplate Click On", "Block Wood Step", "Block Wooden Door Close", "Block Wooden Door Open", "Block Wooden Trapdoor Close", "Block Wooden Trapdoor Open", "Enchant Thorns Hit", "Entity Armorstand Break", "Entity Armorstand Fall", "Entity Armorstand Hit", "Entity Armorstand Place", "Entity Arrow Hit", "Entity Arrow Hit Player", "Entity Arrow Shoot", "Entity Bat Ambient", "Entity Bat Death", "Entity Bat Hurt", "Entity Bat Loop", "Entity Bat Takeoff", "Entity Blaze Ambient", "Entity Blaze Burn", "Entity Blaze Death", "Entity Blaze Hurt", "Entity Blaze Shoot", "Entity Boat Paddle Land", "Entity Boat Paddle Water", "Entity Bobber Retrieve", "Entity Bobber Splash", "Entity Bobber Throw", "Entity Cat Ambient", "Entity Cat Death", "Entity Cat Hiss", "Entity Cat Hurt", "Entity Cat Purr", "Entity Cat Purreow", "Entity Chicken Ambient", "Entity Chicken Death", "Entity Chicken Egg", "Entity Chicken Hurt", "Entity Chicken Step", "Entity Cow Ambient", "Entity Cow Death", "Entity Cow Hurt", "Entity Cow Milk", "Entity Cow Step", "Entity Creeper Death", "Entity Creeper Hurt", "Entity Creeper Primed", "Entity Donkey Ambient", "Entity Donkey Angry", "Entity Donkey Chest", "Entity Donkey Death", "Entity Donkey Hurt", "Entity Egg Throw", "Entity Elder Guardian Ambient", "Entity Elder Guardian Ambient Land", "Entity Elder Guardian Curse", "Entity Elder Guardian Death", "Entity Elder Guardian Death Land", "Entity Elder Guardian Flop", "Entity Elder Guardian Hurt", "Entity Elder Guardian Hurt Land", "Entity Enderdragon Ambient", "Entity Enderdragon Death", "Entity Enderdragon Fireball Explode", "Entity Enderdragon Flap", "Entity Enderdragon Growl", "Entity Enderdragon Hurt", "Entity Enderdragon Shoot", "Entity Endereye Death", "Entity Endereye Launch", "Entity Endermen Ambient", "Entity Endermen Death", "Entity Endermen Hurt", "Entity Endermen Scream", "Entity Endermen Stare", "Entity Endermen Teleport", "Entity Endermite Ambient", "Entity Endermite Death", "Entity Endermite Hurt", "Entity Endermite Step", "Entity Enderpearl Throw", "Entity Evocation Fangs Attack", "Entity Evocation Illager Ambient", "Entity Evocation Illager Cast Spell", "Entity Evocation Illager Death", "Entity Evocation Illager Hurt", "Entity Evocation Illager Prepare Attack", "Entity Evocation Illager Prepare Summon", "Entity Evocation Illager Prepare Wololo", "Entity Experience Bottle Throw", "Entity Experience Orb Pickup", "Entity Firework Blast", "Entity Firework Blast Far", "Entity Firework Large Blast", "Entity Firework Large Blast Far", "Entity Firework Launch", "Entity Firework Shoot", "Entity Firework Twinkle", "Entity Firework Twinkle Far", "Entity Generic Big Fall", "Entity Generic Burn", "Entity Generic Death", "Entity Generic Drink", "Entity Generic Eat", "Entity Generic Explode", "Entity Generic Extinguish Fire", "Entity Generic Hurt", "Entity Generic Small Fall", "Entity Generic Splash", "Entity Generic Swim", "Entity Ghast Ambient", "Entity Ghast Death", "Entity Ghast Hurt", "Entity Ghast Scream", "Entity Ghast Shoot", "Entity Ghast Warn", "Entity Guardian Ambient", "Entity Guardian Ambient Land", "Entity Guardian Attack", "Entity Guardian Death", "Entity Guardian Death Land", "Entity Guardian Flop", "Entity Guardian Hurt", "Entity Guardian Hurt Land", "Entity Horse Ambient", "Entity Horse Angry", "Entity Horse Armor", "Entity Horse Breathe", "Entity Horse Death", "Entity Horse Eat", "Entity Horse Gallop", "Entity Horse Hurt", "Entity Horse Jump", "Entity Horse Land", "Entity Horse Saddle", "Entity Horse Step", "Entity Horse Step Wood", "Entity Hostile Big Fall", "Entity Hostile Death", "Entity Hostile Hurt", "Entity Hostile Small Fall", "Entity Hostile Splash", "Entity Hostile Swim", "Entity Husk Ambient", "Entity Husk Death", "Entity Husk Hurt", "Entity Husk Step", "Entity Illusion Illager Ambient", "Entity Illusion Illager Cast Spell", "Entity Illusion Illager Death", "Entity Illusion Illager Hurt", "Entity Illusion Illager Mirror Move", "Entity Illusion Illager Prepare Blindness", "Entity Illusion Illager Prepare Mirror", "Entity Irongolem Attack", "Entity Irongolem Death", "Entity Irongolem Hurt", "Entity Irongolem Step", "Entity Item Break", "Entity Item Pickup", "Entity Itemframe Add Item", "Entity Itemframe Break", "Entity Itemframe Place", "Entity Itemframe Remove Item", "Entity Itemframe Rotate Item", "Entity Leashknot Break", "Entity Leashknot Place", "Entity Lightning Impact", "Entity Lightning Thunder", "Entity Lingeringpotion Throw", "Entity Llama Ambient", "Entity Llama Angry", "Entity Llama Chest", "Entity Llama Death", "Entity Llama Eat", "Entity Llama Hurt", "Entity Llama Spit", "Entity Llama Step", "Entity Llama Swag", "Entity Magmacube Death", "Entity Magmacube Hurt", "Entity Magmacube Jump", "Entity Magmacube Squish", "Entity Minecart Inside", "Entity Minecart Riding", "Entity Mooshroom Shear", "Entity Mule Ambient", "Entity Mule Chest", "Entity Mule Death", "Entity Mule Hurt", "Entity Painting Break", "Entity Painting Place", "Entity Parrot Ambient", "Entity Parrot Death", "Entity Parrot Eat", "Entity Parrot Fly", "Entity Parrot Hurt", "Entity Parrot Imitate Blaze", "Entity Parrot Imitate Creeper", "Entity Parrot Imitate Elder Guardian", "Entity Parrot Imitate Enderdragon", "Entity Parrot Imitate Enderman", "Entity Parrot Imitate Endermite", "Entity Parrot Imitate Evocation Illager", "Entity Parrot Imitate Ghast", "Entity Parrot Imitate Husk", "Entity Parrot Imitate Illusion Illager", "Entity Parrot Imitate Magmacube", "Entity Parrot Imitate Polar Bear", "Entity Parrot Imitate Shulker", "Entity Parrot Imitate Silverfish", "Entity Parrot Imitate Skeleton", "Entity Parrot Imitate Slime", "Entity Parrot Imitate Spider", "Entity Parrot Imitate Stray", "Entity Parrot Imitate Vex", "Entity Parrot Imitate Vindication Illager", "Entity Parrot Imitate Witch", "Entity Parrot Imitate Wither", "Entity Parrot Imitate Wither Skeleton", "Entity Parrot Imitate Wolf", "Entity Parrot Imitate Zombie", "Entity Parrot Imitate Zombie Pigman", "Entity Parrot Imitate Zombie Villager", "Entity Parrot Step", "Entity Pig Ambient", "Entity Pig Death", "Entity Pig Hurt", "Entity Pig Saddle", "Entity Pig Step", "Entity Player Attack Crit", "Entity Player Attack Knockback", "Entity Player Attack Nodamage", "Entity Player Attack Strong", "Entity Player Attack Sweep", "Entity Player Attack Weak", "Entity Player Big Fall", "Entity Player Breath", "Entity Player Burp", "Entity Player Death", "Entity Player Hurt", "Entity Player Hurt Drown", "Entity Player Hurt On Fire", "Entity Player Levelup", "Entity Player Small Fall", "Entity Player Splash", "Entity Player Swim", "Entity Polar Bear Ambient", "Entity Polar Bear Baby Ambient", "Entity Polar Bear Death", "Entity Polar Bear Hurt", "Entity Polar Bear Step", "Entity Polar Bear Warning", "Entity Rabbit Ambient", "Entity Rabbit Attack", "Entity Rabbit Death", "Entity Rabbit Hurt", "Entity Rabbit Jump", "Entity Sheep Ambient", "Entity Sheep Death", "Entity Sheep Hurt", "Entity Sheep Shear", "Entity Sheep Step", "Entity Shulker Ambient", "Entity Shulker Bullet Hit", "Entity Shulker Bullet Hurt", "Entity Shulker Close", "Entity Shulker Death", "Entity Shulker Hurt", "Entity Shulker Hurt Closed", "Entity Shulker Open", "Entity Shulker Shoot", "Entity Shulker Teleport", "Entity Silverfish Ambient", "Entity Silverfish Death", "Entity Silverfish Hurt", "Entity Silverfish Step", "Entity Skeleton Ambient", "Entity Skeleton Death", "Entity Skeleton Horse Ambient", "Entity Skeleton Horse Death", "Entity Skeleton Horse Hurt", "Entity Skeleton Hurt", "Entity Skeleton Shoot", "Entity Skeleton Step", "Entity Slime Attack", "Entity Slime Death", "Entity Slime Hurt", "Entity Slime Jump", "Entity Slime Squish", "Entity Small Magmacube Death", "Entity Small Magmacube Hurt", "Entity Small Magmacube Squish", "Entity Small Slime Death", "Entity Small Slime Hurt", "Entity Small Slime Jump", "Entity Small Slime Squish", "Entity Snowball Throw", "Entity Snowman Ambient", "Entity Snowman Death", "Entity Snowman Hurt", "Entity Snowman Shoot", "Entity Spider Ambient", "Entity Spider Death", "Entity Spider Hurt", "Entity Spider Step", "Entity Splash Potion Break", "Entity Splash Potion Throw", "Entity Squid Ambient", "Entity Squid Death", "Entity Squid Hurt", "Entity Stray Ambient", "Entity Stray Death", "Entity Stray Hurt", "Entity Stray Step", "Entity Tnt Primed", "Entity Vex Ambient", "Entity Vex Charge", "Entity Vex Death", "Entity Vex Hurt", "Entity Villager Ambient", "Entity Villager Death", "Entity Villager Hurt", "Entity Villager No", "Entity Villager Trading", "Entity Villager Yes", "Entity Vindication Illager Ambient", "Entity Vindication Illager Death", "Entity Vindication Illager Hurt", "Entity Witch Ambient", "Entity Witch Death", "Entity Witch Drink", "Entity Witch Hurt", "Entity Witch Throw", "Entity Wither Ambient", "Entity Wither Break Block", "Entity Wither Death", "Entity Wither Hurt", "Entity Wither Shoot", "Entity Wither Skeleton Ambient", "Entity Wither Skeleton Death", "Entity Wither Skeleton Hurt", "Entity Wither Skeleton Step", "Entity Wither Spawn", "Entity Wolf Ambient", "Entity Wolf Death", "Entity Wolf Growl", "Entity Wolf Howl", "Entity Wolf Hurt", "Entity Wolf Pant", "Entity Wolf Shake", "Entity Wolf Step", "Entity Wolf Whine", "Entity Zombie Ambient", "Entity Zombie Attack Door Wood", "Entity Zombie Attack Iron Door", "Entity Zombie Break Door Wood", "Entity Zombie Death", "Entity Zombie Horse Ambient", "Entity Zombie Horse Death", "Entity Zombie Horse Hurt", "Entity Zombie Hurt", "Entity Zombie Infect", "Entity Zombie Pig Ambient", "Entity Zombie Pig Angry", "Entity Zombie Pig Death", "Entity Zombie Pig Hurt", "Entity Zombie Step", "Entity Zombie Villager Ambient", "Entity Zombie Villager Converted", "Entity Zombie Villager Cure", "Entity Zombie Villager Death", "Entity Zombie Villager Hurt", "Entity Zombie Villager Step", "Item Armor Equip Chain", "Item Armor Equip Diamond", "Item Armor Equip Elytra", "Item Armor Equip Generic", "Item Armor Equip Gold", "Item Armor Equip Iron", "Item Armor Equip Leather", "Item Bottle Empty", "Item Bottle Fill", "Item Bottle Fill Dragonbreath", "Item Bucket Empty", "Item Bucket Empty Lava", "Item Bucket Fill", "Item Bucket Fill Lava", "Item Chorus Fruit Teleport", "Item Elytra Flying", "Item Firecharge Use", "Item Flintandsteel Use", "Item Hoe Till", "Item Shield Block", "Item Shield Break", "Item Shovel Flatten", "Item Totem Use", "Music Creative", "Music Credits", "Music Dragon", "Music End", "Music Game", "Music Menu", "Music Nether", "Record 11", "Record 13", "Record Blocks", "Record Cat", "Record Chirp", "Record Far", "Record Mall", "Record Mellohi", "Record Stal", "Record Strad", "Record Wait", "Record Ward", "Ui Button Click", "Ui Toast Challenge Complete", "Ui Toast In", "Ui Toast Out", "Weather Rain", "Weather Rain Above"},
            optionsZh = {"环境", "破坏", "其他音效", "其他音效", "命中", "其他音效", "放置", "脚步", "使用", "其他音效", "关闭", "其他音效", "打开", "死亡", "其他音效", "破坏", "其他音效", "命中", "放置", "脚步", "其他音效", "其他音效", "其他音效", "其他音效", "使用", "其他音效", "其他音效", "其他音效", "关闭", "打开", "栅栏关闭", "栅栏打开", "火焰环境", "火焰", "火焰", "玻璃破坏", "玻璃", "玻璃命中", "玻璃放置", "玻璃脚步", "草方块破坏", "草方块", "草方块命中", "草方块放置", "草方块脚步", "沙砾破坏", "沙砾", "沙砾命中", "沙砾放置", "沙砾脚步", "门关闭", "门打开", "关闭", "打开", "破坏", "其他音效", "命中", "放置", "脚步", "熔岩环境", "熔岩", "熔岩", "其他音效", "破坏", "其他音效", "命中", "放置", "其他音效", "其他音效", "脚步", "其他音效", "其他音效", "其他音效", "其他音效", "其他音效", "其他音效", "其他音效", "其他音效", "其他音效", "其他音效", "其他音效", "其他音效", "其他音效", "环境", "其他音效", "其他音效", "其他音效", "沙子破坏", "沙子", "沙子命中", "沙子放置", "沙子脚步", "关闭", "打开", "破坏", "其他音效", "命中", "放置", "脚步", "破坏", "其他音效", "命中", "放置", "脚步", "石头破坏", "石头", "石头", "石头", "石头命中", "石头放置", "石头", "石头", "石头脚步", "其他音效", "其他音效", "其他音效", "其他音效", "水环境", "放置", "木头破坏", "木头", "木头", "木头", "木头命中", "木头放置", "木头", "木头", "木头脚步", "门关闭", "门打开", "关闭", "打开", "命中", "实体破坏", "实体", "实体命中", "实体放置", "实体箭命中", "实体箭命中玩家", "实体箭射击", "实体环境", "实体死亡", "实体受伤", "实体", "实体", "实体环境", "实体", "实体死亡", "实体受伤", "实体射击", "实体", "实体水", "实体", "实体", "实体投掷", "实体环境", "实体死亡", "实体", "实体受伤", "实体", "实体", "实体环境", "实体死亡", "实体", "实体受伤", "实体脚步", "实体环境", "实体死亡", "实体受伤", "实体", "实体脚步", "实体苦力怕死亡", "实体苦力怕受伤", "实体苦力怕", "实体环境", "实体", "实体", "实体死亡", "实体受伤", "实体投掷", "实体环境", "实体环境", "实体", "实体死亡", "实体死亡", "实体", "实体受伤", "实体受伤", "实体环境", "实体死亡", "实体", "实体", "实体", "实体受伤", "实体射击", "实体死亡", "实体", "实体环境", "实体死亡", "实体受伤", "实体", "实体", "实体", "实体环境", "实体死亡", "实体受伤", "实体脚步", "实体投掷", "实体", "实体环境", "实体", "实体死亡", "实体受伤", "实体", "实体", "实体", "实体投掷", "实体", "实体", "实体", "实体", "实体", "实体", "实体射击", "实体", "实体", "实体", "实体", "实体死亡", "实体", "实体", "实体", "实体火焰", "实体受伤", "实体", "实体", "实体", "实体环境", "实体死亡", "实体受伤", "实体", "实体射击", "实体", "实体环境", "实体环境", "实体", "实体死亡", "实体死亡", "实体", "实体受伤", "实体受伤", "实体环境", "实体", "实体", "实体", "实体死亡", "实体", "实体", "实体受伤", "实体", "实体", "实体", "实体脚步", "实体脚步木头", "实体", "实体死亡", "实体受伤", "实体", "实体", "实体", "实体环境", "实体死亡", "实体受伤", "实体脚步", "实体环境", "实体", "实体死亡", "实体受伤", "实体", "实体", "实体", "实体", "实体死亡", "实体受伤", "实体脚步", "实体物品破坏", "实体物品", "实体物品", "实体破坏", "实体放置", "实体物品", "实体物品", "实体破坏", "实体放置", "实体", "实体", "实体投掷", "实体环境", "实体", "实体", "实体死亡", "实体", "实体受伤", "实体", "实体脚步", "实体", "实体死亡", "实体受伤", "实体", "实体", "实体", "实体", "实体", "实体环境", "实体", "实体死亡", "实体受伤", "实体破坏", "实体放置", "实体环境", "实体死亡", "实体", "实体", "实体受伤", "实体", "实体苦力怕", "实体", "实体", "实体", "实体", "实体", "实体", "实体", "实体", "实体", "实体", "实体", "实体", "实体骷髅", "实体", "实体", "实体", "实体", "实体", "实体", "实体", "实体骷髅", "实体", "实体僵尸", "实体僵尸", "实体僵尸", "实体脚步", "实体环境", "实体死亡", "实体受伤", "实体", "实体脚步", "实体玩家", "实体玩家", "实体玩家", "实体玩家", "实体玩家", "实体玩家", "实体玩家", "实体玩家", "实体玩家", "实体玩家死亡", "实体玩家受伤", "实体玩家受伤", "实体玩家受伤火焰", "实体玩家", "实体玩家", "实体玩家", "实体玩家", "实体环境", "实体环境", "实体死亡", "实体受伤", "实体脚步", "实体", "实体环境", "实体", "实体死亡", "实体受伤", "实体", "实体环境", "实体死亡", "实体受伤", "实体", "实体脚步", "实体环境", "实体命中", "实体受伤", "实体关闭", "实体死亡", "实体受伤", "实体受伤", "实体打开", "实体射击", "实体", "实体环境", "实体死亡", "实体受伤", "实体脚步", "实体骷髅环境", "实体骷髅死亡", "实体骷髅环境", "实体骷髅死亡", "实体骷髅受伤", "实体骷髅受伤", "实体骷髅射击", "实体骷髅脚步", "实体", "实体死亡", "实体受伤", "实体", "实体", "实体死亡", "实体受伤", "实体", "实体死亡", "实体受伤", "实体", "实体", "实体投掷", "实体环境", "实体死亡", "实体受伤", "实体射击", "实体环境", "实体死亡", "实体受伤", "实体脚步", "实体药水破坏", "实体药水投掷", "实体环境", "实体死亡", "实体受伤", "实体环境", "实体死亡", "实体受伤", "实体脚步", "实体", "实体环境", "实体", "实体死亡", "实体受伤", "实体环境", "实体死亡", "实体受伤", "实体", "实体", "实体", "实体环境", "实体死亡", "实体受伤", "实体环境", "实体死亡", "实体", "实体受伤", "实体投掷", "实体环境", "实体破坏", "实体死亡", "实体受伤", "实体射击", "实体骷髅环境", "实体骷髅死亡", "实体骷髅受伤", "实体骷髅脚步", "实体", "实体环境", "实体死亡", "实体", "实体", "实体受伤", "实体", "实体", "实体脚步", "实体", "实体僵尸环境", "实体僵尸门木头", "实体僵尸门", "实体僵尸破坏门木头", "实体僵尸死亡", "实体僵尸环境", "实体僵尸死亡", "实体僵尸受伤", "实体僵尸受伤", "实体僵尸", "实体僵尸环境", "实体僵尸", "实体僵尸死亡", "实体僵尸受伤", "实体僵尸脚步", "实体僵尸环境", "实体僵尸", "实体僵尸", "实体僵尸死亡", "实体僵尸受伤", "实体僵尸脚步", "物品", "物品", "物品", "物品", "物品", "物品", "物品", "物品", "物品", "物品", "物品", "物品熔岩", "物品", "物品熔岩", "物品", "物品", "物品使用", "物品使用", "物品", "物品", "物品破坏", "物品", "物品使用", "音乐", "音乐", "音乐", "音乐", "音乐", "音乐", "音乐", "其他音效", "其他音效", "其他音效", "其他音效", "其他音效", "其他音效", "其他音效", "其他音效", "其他音效", "其他音效", "其他音效", "其他音效", "其他音效", "其他音效", "其他音效", "其他音效", "其他音效", "其他音效"},
            defaultValue = "Ambience Cave")
    private static final String SOUND  = "sound";
    private static final String SOUND2 = "newsound";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Volume",
            labelZh = "音量",
            tooltip = "[volume] The volume of the sound as a percentage. Numbers above 100 will not get any louder, but will be heard from a farther distance",
            tooltipZh = "音量百分比，随技能等级缩放，默认 100（即 1.0）；内部除以 100 并夹到不小于 0。超过 100 不会更响，但可听见的距离会变远。")
    private static final String VOLUME = "volume";
    @SkillField(
            kind = FieldKind.AttributeValue,
            label = "Pitch",
            labelZh = "音调",
            tooltip = "[pitch] The pitch of the sound as a numeric speed multiplier between 0.5 and 2.",
            tooltipZh = "音调倍率，随技能等级缩放；代码读取的默认值是 0，会被夹到下限 0.5，有效范围 0.5 到 2。")
    private static final String PITCH  = "pitch";

    @Override
    public String getKey() {
        return "sound";
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
        if (targets.size() == 0)
        {
            return false;
        }

        String type = settings.getString(SOUND, settings.getString(SOUND2, "")).toUpperCase().replace(" ", "_");
        try
        {
            // 走反射解析：Sound 从 1.21.3 起是 interface，直接 valueOf 会在
            // 方法链接期抛 IncompatibleClassChangeError，此处的 catch 拦不住。
            Sound sound = EnumCompat.valueOf(Sound.class, type, Registry.SOUNDS);
            if (sound == null) {
                return false;
            }
            float volume = (float) parseValues(caster, VOLUME, level, 100.0) / 100;
            float pitch = (float) parseValues(caster, PITCH, level, 0.0);

            volume = Math.max(0, volume);
            pitch = Math.min(2, Math.max(0.5f, pitch));

            for (LivingEntity target : targets)
            {
                target.getWorld().playSound(target.getLocation(), sound, volume, pitch);
            }
            return targets.size() > 0;
        }
        catch (Exception ex)
        {
            Logger.invalid("Invalid sound type: " + type);
            return false;
        }
    }
}
