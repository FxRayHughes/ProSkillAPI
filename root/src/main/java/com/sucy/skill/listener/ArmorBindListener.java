package com.sucy.skill.listener;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.player.PlayerData;
import com.sucy.skill.api.player.PlayerSkill;
import com.sucy.skill.api.player.PlayerSkillBar;
import com.sucy.skill.api.skills.PassiveSkill;
import com.sucy.skill.api.skills.Skill;
import com.sucy.skill.data.Settings;
import com.sucy.skill.api.util.ItemDataReader;
import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 防具自动装配技能栏监听器
 * 检测玩家防具 lore 中的技能名，自动绑定到 SkillBar 对应槽位
 */
public class ArmorBindListener extends SkillAPIListener {

   // 记录每个玩家由 armor-bind 管理的 (槽位 → 技能名) 映射
   // 用于脱下装备时精准识别"这个槽位是不是装备注入的技能"，避免误删玩家职业技能
   private static final Map<java.util.UUID, java.util.Map<Integer, String>> managedSlots = new HashMap<>();
   // 记录由 armor-bind 注入过的技能名（用于脱下装备后从 PlayerData 中移除临时 PlayerSkill）
   // key: 玩家UUID, value: 技能名集合（全部由装备提供，不是玩家通过职业正常学习的）
   private static final Map<java.util.UUID, java.util.Set<String>> injectedSkills = new HashMap<>();
   // 缓存玩家上次的装备指纹（避免无变化时重复刷新）
   private static final Map<java.util.UUID, String> armorFingerprint = new HashMap<>();
   // 记录上一轮给玩家累计的"套装属性加成"（属性名 → 加成数值）
   // 每次刷新时：先用负数撤销上一轮加成 → 再计算新一轮加成 → 加回去
   private static final Map<java.util.UUID, java.util.Map<String, Integer>> setBonusAttrApplied = new HashMap<>();
   // 记录当前由本 listener 主动 initialize 的被动技能名（小写）
   // key: 玩家UUID, value: 已激活的被动技能名集合
   // 用于装备变化时 diff：新集合-旧集合=需要 initialize，旧集合-新集合=需要 stopEffects
   private static final Map<java.util.UUID, java.util.Set<String>> activePassives = new HashMap<>();
   // Lore 可在技能名后追加 @等级；该表保存本轮扫描结果，避免破坏旧的技能名匹配协议。
   private static final Map<String, Integer> loreSkillLevels = new HashMap<>();
   private static final Map<java.util.UUID, BukkitTask> pendingRefreshes = new HashMap<>();

   /** 合并同一玩家短时间内的多个事件，只保留最后一次刷新任务。 */
   private static void scheduleRefresh(Player player, int delay) {
      scheduleRefresh(player, delay, true);
   }

   /** 指纹模式可避免普通快捷栏切换重建技能栏，保留冷却显示。 */
   private static void scheduleRefresh(Player player, int delay, boolean force) {
      if (player == null) return;
      BukkitTask old = pendingRefreshes.remove(player.getUniqueId());
      if (old != null) old.cancel();
      pendingRefreshes.put(player.getUniqueId(), SkillAPI.schedule(() -> {
         pendingRefreshes.remove(player.getUniqueId());
         updateArmorBind(player, force);
      }, delay));
   }

   @Override
   public void init() {
      MainListener.registerJoin(this::onJoin);
   }

   public void onJoin(Player player) {
      if (!SkillAPI.getSettings().isArmorAutoBindEnabled()) return;
      if (!SkillAPI.getSettings().isSkillBarEnabled()) return;
      // 延迟1tick确保数据加载完成
      scheduleRefresh(player, 1);
   }

   @EventHandler(priority = EventPriority.HIGHEST)
   public void onInventoryClick(InventoryClickEvent event) {
      if (!SkillAPI.getSettings().isArmorAutoBindEnabled()) return;
      if (!SkillAPI.getSettings().isSkillBarEnabled()) return;
      if (!(event.getWhoClicked() instanceof Player)) return;

      // 关键思路：只要"光标上的物品"或"点击槽位的物品"涉及防具或含技能物品 → 触发刷新
      // 不依赖具体动作类型（PLACE/MOVE/SWAP/PICKUP 等），覆盖所有可能的操作路径
      ItemStack cursor = event.getCursor();
      ItemStack current = event.getCurrentItem();
      // 数字键 1-9 交换（HOTBAR_SWAP / HOTBAR_MOVE_AND_READD）：
      // 目标热键槽的物品会被换到点击槽，反之亦然
      // event.getHotbarButton() 在这些动作下返回 0-8 的热键槽索引，其他情况返回 -1
      ItemStack hotbarItem = null;
      int hotbarBtn = event.getHotbarButton();
      if (hotbarBtn >= 0) {
         hotbarItem = ((Player) event.getWhoClicked()).getInventory().getItem(hotbarBtn);
      }
      boolean touchedArmor = isArmor(cursor) || isArmor(current) || isArmor(hotbarItem);
      boolean touchedSkillItem = hasBindableSkillText(cursor) || hasBindableSkillText(current)
            || hasBindableSkillText(hotbarItem);
      boolean clickedEquipSlot = event.getSlotType() == InventoryType.SlotType.ARMOR;

      if (touchedArmor || touchedSkillItem || clickedEquipSlot) {
         Player player = (Player) event.getWhoClicked();
         // 延迟 2 tick 等操作完成，用 force=true 确保刷新必执行（不依赖指纹缓存）
         scheduleRefresh(player, 2);
      }
   }

   @EventHandler(priority = EventPriority.MONITOR)
   public void onInteract(PlayerInteractEvent event) {
      if (!SkillAPI.getSettings().isArmorAutoBindEnabled()) return;
      if (!SkillAPI.getSettings().isSkillBarEnabled()) return;
      Player player = event.getPlayer();
      // 只有"手持防具右键穿戴"才触发
      // 空手 / 手持其他物品（如副手道具书、食物等）一律不触发
      Action act = event.getAction();
      if (act != Action.RIGHT_CLICK_AIR && act != Action.RIGHT_CLICK_BLOCK) return;

      // 从 event.getItem() 读取（右键穿戴防具时，cursor 上的防具）
      ItemStack item = event.getItem();
      if (item == null || item.getType() == Material.AIR) return;
      if (!isArmor(item)) return;

      // force=false 依赖指纹检测：只有真正的装备变化才会刷新
      // 避免每次右键都刷新技能栏（影响冷却显示）
      scheduleRefresh(player, 2);
   }

   @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
   public void onItemBreak(PlayerItemBreakEvent event) {
      if (!SkillAPI.getSettings().isArmorAutoBindEnabled()) return;
      if (!SkillAPI.getSettings().isSkillBarEnabled()) return;
      // 防具损坏后重新检测（force 确保跳过指纹缓存）
      if (isArmor(event.getBrokenItem())) {
         scheduleRefresh(event.getPlayer(), 1);
      }
   }

   @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
   public void onSwapHand(PlayerSwapHandItemsEvent event) {
      if (!SkillAPI.getSettings().isArmorAutoBindEnabled()) return;
      if (!SkillAPI.getSettings().isSkillBarEnabled()) return;
      if (!hasBindableSkillText(event.getMainHandItem()) && !hasBindableSkillText(event.getOffHandItem())) return;
      // 主副手交换后强制刷新（force=true 确保跳过指纹缓存）
      scheduleRefresh(event.getPlayer(), 2);
   }

   /**
    * 滚轮/数字键切换热键槽 → 主手物品变了 → 重新扫描
    * PlayerItemHeldEvent 在选中槽真正变化时才触发，不会因误触频繁刷新
    */
   @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
   public void onItemHeld(PlayerItemHeldEvent event) {
      if (!SkillAPI.getSettings().isArmorAutoBindEnabled()) return;
      if (!SkillAPI.getSettings().isSkillBarEnabled()) return;
      if (SkillAPI.isCasting(event.getPlayer())) return;
      // 普通物品之间切换不会改变 Lore 技能绑定，直接跳过扫描以避免无意义的主线程任务。
      ItemStack previous = event.getPlayer().getInventory().getItem(event.getPreviousSlot());
      ItemStack current = event.getPlayer().getInventory().getItem(event.getNewSlot());
      if (!hasBindableSkillText(previous) && !hasBindableSkillText(current)) return;
      scheduleRefresh(event.getPlayer(), 1, false);
   }

   /**
    * Q 键丢主手物品 → 主手变空 → 需要 stopEffects 掉该物品的被动
    */
   @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
   public void onDropItem(PlayerDropItemEvent event) {
      if (!SkillAPI.getSettings().isArmorAutoBindEnabled()) return;
      if (!SkillAPI.getSettings().isSkillBarEnabled()) return;
      scheduleRefresh(event.getPlayer(), 1);
   }

   /**
    * 玩家退出 → 清理静态映射，避免长期跑服的内存堆积
    */
   @EventHandler(priority = EventPriority.MONITOR)
   public void onQuit(PlayerQuitEvent event) {
      java.util.UUID uuid = event.getPlayer().getUniqueId();
      managedSlots.remove(uuid);
      injectedSkills.remove(uuid);
      armorFingerprint.remove(uuid);
      setBonusAttrApplied.remove(uuid);
      activePassives.remove(uuid);
      BukkitTask task = pendingRefreshes.remove(uuid);
      if (task != null) task.cancel();
   }

   /**
    * 死亡刷新：兼容 CustomDurability 的死亡碎裂/掉落
    * CustomDurability 在 PlayerDeathEvent（NORMAL）中遍历背包，
    * 通过 player.getInventory().setItem(slot, null) 删除碎裂的装备。
    * 我们在 MONITOR（晚于 NORMAL）阶段 force=true 刷新，确保装备删除后技能同步消失。
    */
   @EventHandler(priority = EventPriority.MONITOR)
   public void onPlayerDeath(PlayerDeathEvent event) {
      if (!SkillAPI.getSettings().isArmorAutoBindEnabled()) return;
      if (!SkillAPI.getSettings().isSkillBarEnabled()) return;
      // 延迟到下一 tick，确保 CustomDurability 的碎裂/掉落逻辑完成
      // force=true 强制刷新，忽略指纹缓存
      scheduleRefresh(event.getEntity(), 1);
   }

   /**
    * 重生刷新：死亡后装备可能已经掉落/碎裂，
    * 重生时玩家的装备格是空的（或 keepInventory 保留的），需要重新扫描
    */
   @EventHandler(priority = EventPriority.MONITOR)
   public void onPlayerRespawn(PlayerRespawnEvent event) {
      if (!SkillAPI.getSettings().isArmorAutoBindEnabled()) return;
      if (!SkillAPI.getSettings().isSkillBarEnabled()) return;
      // 延迟 2 tick，确保 Bukkit 已完成重生流程和 inventory 初始化
      // force=true 强制刷新
      scheduleRefresh(event.getPlayer(), 2);
   }

   /**
    * 副手白名单：拦截不含指定 lore 的物品放入副手
    * 因为 Bukkit 副手放置有多种 Action，最稳的方式是 1 tick 后检查副手实际内容
    */
   @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
   public void onOffhandPlace(InventoryClickEvent event) {
      if (!SkillAPI.getSettings().isArmorAutoBindEnabled()) return;
      if (!(event.getWhoClicked() instanceof Player)) return;
      String whitelist = SkillAPI.getSettings().getOffhandWhitelistLore();
      if (whitelist == null || whitelist.isEmpty()) return;

      Player player = (Player) event.getWhoClicked();
      // 1 tick 后强制校验副手
      SkillAPI.schedule(() -> validateOffhand(player, whitelist), 1);
   }

   @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
   public void onSwapHandWhitelist(PlayerSwapHandItemsEvent event) {
      if (!SkillAPI.getSettings().isArmorAutoBindEnabled()) return;
      String whitelist = SkillAPI.getSettings().getOffhandWhitelistLore();
      if (whitelist == null || whitelist.isEmpty()) return;

      ItemStack offhandItem = event.getOffHandItem();
      if (offhandItem != null && offhandItem.getType() != Material.AIR
            && !hasLoreText(offhandItem, whitelist)) {
         event.setCancelled(true);
         event.getPlayer().sendMessage(
               ChatColor.RED + "此物品无法放入副手（缺少 \"" + whitelist + "\" 标识）");
      }
   }

   /**
    * 校验副手内容：不合规就强制取出
    */
   private static void validateOffhand(Player player, String whitelist) {
      if (player == null || !player.isOnline()) return;
      ItemStack off = player.getInventory().getItemInOffHand();
      if (off == null || off.getType() == Material.AIR) return;
      if (hasLoreText(off, whitelist)) return;

      // 不合规：清空副手并尝试放回背包
      player.getInventory().setItemInOffHand(null);
      java.util.HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(off);
      // 背包满则掉地上
      for (ItemStack drop : overflow.values()) {
         player.getWorld().dropItemNaturally(player.getLocation(), drop);
      }
      player.sendMessage(
            ChatColor.RED + "此物品无法放入副手（缺少 \"" + whitelist + "\" 标识）");
      // 踢出后重新刷新技能栏（清除残留的技能装配）
      updateArmorBind(player);
   }

   /**
    * 检查物品 lore 是否包含指定文本（去色后匹配）
    */
   private static boolean hasLoreText(ItemStack item, String text) {
      if (item == null || !item.hasItemMeta()) return false;
      ItemMeta meta = item.getItemMeta();
      if (meta == null || !meta.hasLore()) return false;
      String plainText = ChatColor.stripColor(text);
      for (String line : meta.getLore()) {
         if (ChatColor.stripColor(line).contains(plainText)) return true;
      }
      return false;
   }

   /**
    * 检查物品是否含有"附带技能"lore（不限制物品类型，支持副手道具、饰品等任意物品）
    */
   private static boolean hasBindableSkillText(ItemStack item) {
      if (item == null || !item.hasItemMeta()) return false;
      Settings settings = SkillAPI.getSettings();
      String pre = settings.getArmorDetectPre();
      if (pre == null || pre.isEmpty()) return false;
      String post = settings.getArmorDetectPost();
      return extractSkillFromItem(item, pre, post) != null;
   }

   /**
    * 判断物品是否为防具类型
    */
   private static boolean isArmor(ItemStack item) {
      if (item == null || item.getType() == Material.AIR) return false;
      String name = item.getType().name();
      return name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE")
            || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS")
            || name.equals("ELYTRA");
   }

   /**
    * 扫描四个防具槽位，提取 lore 中的技能名并装配到 SkillBar
    * - 先清空所有技能槽（避免脱光后残留旧技能）
    * - 然后按防具 lore 重新装配（包括注入未学习的技能）
    * @param force 为 true 时跳过指纹检测，强制刷新（用于右键穿戴等明确装备变化的场景）
    */
   public static void updateArmorBind(Player player) {
      updateArmorBind(player, false);
   }

   public static void updateArmorBind(Player player, boolean force) {
      if (player == null || !player.isOnline()) return;
      PlayerData data = SkillAPI.getPlayerData(player);
      if (data == null || !data.hasClass()) return;

      PlayerSkillBar bar = data.getSkillBar();
      if (bar == null) return;

      // 确保 bar 已 setup（玩家在世界中且有职业）
      if (!bar.isSetup()) {
         bar.setup(player);
      }

      Settings settings = SkillAPI.getSettings();
      String pre = settings.getArmorDetectPre();
      String post = settings.getArmorDetectPost();
      if (pre == null) return;

      // ============== 第一步：副手白名单双重验证 ==============
      // 无论通过哪个事件触发，都强制验证副手上的物品
      // 如果不合规，立刻踢出（放回背包/掉地上），确保它不会触发后续"附带技能"逻辑
      String whitelist = settings.getOffhandWhitelistLore();
      if (whitelist != null && !whitelist.isEmpty()) {
         ItemStack off = player.getInventory().getItemInOffHand();
         if (off != null && off.getType() != Material.AIR && !hasLoreText(off, whitelist)) {
            // 踢出：清空副手，尝试放回背包，满则掉落
            player.getInventory().setItemInOffHand(null);
            java.util.HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(off);
            for (ItemStack drop : overflow.values()) {
               player.getWorld().dropItemNaturally(player.getLocation(), drop);
            }
            player.sendMessage(ChatColor.RED + "此物品无法放入副手（缺少 \"" + whitelist + "\" 标识）");
         }
      }

      ItemStack[] armor = player.getInventory().getArmorContents();
      ItemStack offhandPeek = player.getInventory().getItemInOffHand();
      ItemStack mainhandPeek = player.getInventory().getItemInMainHand();
      // 装备指纹：4防具+副手+主手 → 同样的指纹则跳过刷新（避免覆盖冷却显示）
      String fp = computeFingerprint(armor, offhandPeek, mainhandPeek);
      String prevFp = armorFingerprint.get(player.getUniqueId());
      if (fp.equals(prevFp)) {
         return;
      }
      armorFingerprint.put(player.getUniqueId(), fp);
      // armor[0]=靴子 armor[1]=护腿 armor[2]=胸甲 armor[3]=头盔
      String[] types = {"boots", "leggings", "chestplate", "helmet"};

      // ============== 第二步：扫描所有装备/手持物品，构建 (槽位, 可施放技能) + 全量需要集 ==============
      // 一件物品可有多行 `附带技能: X`，每行一个技能。
      // 可施放技能（sk.canCast()==true）占 SkillBar 槽位：第一个入基础槽，后续用 findFreeSlot 找空槽。
      // 纯被动技能（canCast()==false）不占槽位，稍后通过 activePassives diff 处理 initialize。
      // 手持槽（副手 / 主手）若物品是防具则整体跳过，实现"手持防具不生效"。
      Map<Integer, String> targetSlots = new HashMap<>();
      loreSkillLevels.clear();
      java.util.Set<Integer> pendingUsedSlots = new java.util.HashSet<>();
      java.util.Set<String> neededSkillNames = new java.util.HashSet<>();

      // 收集物品来源：baseSlot / item / 是否手持
      java.util.List<int[]> sourceMeta = new java.util.ArrayList<>();
      java.util.List<ItemStack> sourceItems = new java.util.ArrayList<>();
      for (int i = 0; i < 4; i++) {
         sourceMeta.add(new int[]{settings.getArmorSlot(types[i]), 0});
         sourceItems.add(armor[i]);
      }
      sourceMeta.add(new int[]{settings.getArmorSlot("offhand"), 1});
      sourceItems.add(offhandPeek);
      sourceMeta.add(new int[]{settings.getArmorSlot("mainhand"), 1});
      sourceItems.add(mainhandPeek);

      for (int srcIdx = 0; srcIdx < sourceItems.size(); srcIdx++) {
         int baseSlot = sourceMeta.get(srcIdx)[0];
         boolean isHandHeld = sourceMeta.get(srcIdx)[1] == 1;
         ItemStack it = sourceItems.get(srcIdx);
         if (baseSlot < 1 || baseSlot > 9) continue;
         if (it == null || it.getType() == Material.AIR) continue;
         // 手持防具直接跳过（穿戴上依旧生效）
         if (isHandHeld && isArmor(it)) continue;
         if (!meetsRequirement(player, it)) continue;
         List<String> skills = extractAllSkillsFromItem(it, pre, post);
         if (skills.isEmpty()) continue;

         boolean baseUsed = false;
         for (String skillName : skills) {
            Skill sk = SkillAPI.getSkill(skillName);
            if (sk == null) continue;
            neededSkillNames.add(skillName.toLowerCase());
            if (!sk.canCast()) continue; // 纯被动交给 activePassives diff 处理

            int slot;
            if (!baseUsed && !pendingUsedSlots.contains(baseSlot)) {
               slot = baseSlot;
               baseUsed = true;
            } else {
               slot = findFreeSlot(bar, pendingUsedSlots);
               if (slot < 0) continue; // 无空闲槽，放弃当前技能
            }
            targetSlots.put(slot, skillName);
            pendingUsedSlots.add(slot);
         }
      }

      // 第二步：清空"由 armor-bind 管理且值未被玩家修改"的槽位
      // 关键修复：managedSlots 记录的是 (槽位 → 技能名) 映射
      // 清空条件：当前槽位的值 == 上轮记录的技能名（即槽位确实放着装备注入的技能）
      // 如果玩家手动改了槽位内容（比如把装备技能拖走换成英勇打击），不动那个槽
      // 额外优化：如果当前值 == 本轮 targetSlots 的目标值 → 也跳过（避免清空后立即重新assign导致冷却重置）
      java.util.Map<Integer, String> prevManaged = managedSlots.getOrDefault(player.getUniqueId(), java.util.Collections.emptyMap());
      for (java.util.Map.Entry<Integer, String> entry : prevManaged.entrySet()) {
         int slot = entry.getKey();
         String prevSkillName = entry.getValue();
         if (slot >= 1 && slot <= 9 && !bar.isWeaponSlot(slot - 1)) {
            String current = bar.getData().get(slot);
            // 目标槽的新目标值（如果一致就跳过清空，避免重设导致冷却刷新）
            String targetVal = targetSlots.get(slot);
            boolean targetMatches = targetVal != null && targetVal.equalsIgnoreCase(prevSkillName);
            // 只清空：当前槽值为空/为"e"/为上轮记录的技能名，且目标值与当前值不一致
            if ((current == null || "e".equals(current) || prevSkillName.equalsIgnoreCase(current))
                  && !targetMatches) {
               bar.getData().put(slot, "e");
            }
         }
      }

      // 新一轮管理的 (槽位 → 技能名) 映射
      java.util.Map<Integer, String> newManaged = new java.util.HashMap<>();
      // neededSkillNames 已在第二步初始化并含所有已扫描到的技能名（含纯被动）

      // 第三步：按目标装配（支持装配未学习的技能 → giveSkill 注入临时实例）
      java.util.Map<Integer, String> slotMap = bar.getData();
      java.util.Set<Integer> usedByArmor = new java.util.HashSet<>();
      for (Map.Entry<Integer, String> entry : targetSlots.entrySet()) {
         int slot = entry.getKey();
         String skillName = entry.getValue();
         neededSkillNames.add(skillName.toLowerCase());

         // 快捷路径：如果目标槽当前值已经等于本轮目标技能名 → 无需任何操作
         String existing = slotMap.get(slot);
         if (existing != null && existing.equalsIgnoreCase(skillName)) {
            newManaged.put(slot, skillName);
            usedByArmor.add(slot);
            continue;
         }

         // 如果目标槽位被玩家手动放了技能（非空、非 armor-bind 管理），则换到空闲槽
         boolean slotHeldByArmor = prevManaged.containsKey(slot)
               && prevManaged.get(slot).equalsIgnoreCase(existing == null ? "e" : existing);
         if (existing != null && !"e".equals(existing) && !slotHeldByArmor) {
            // 目标槽被玩家手动放了技能 → 找空闲槽
            java.util.Set<Integer> tempUsed = new java.util.HashSet<>(usedByArmor);
            tempUsed.addAll(targetSlots.keySet());
            slot = findFreeSlot(bar, tempUsed);
            if (slot < 0) continue; // 无空闲
         }

         PlayerSkill ps = data.getSkill(skillName);
         if (ps == null) {
            Skill skill = SkillAPI.getSkill(skillName);
            if (skill == null) continue;
            data.giveSkill(skill, null);
            ps = data.getSkill(skillName);
            if (ps != null && ps.getLevel() < 1) {
               ps.setLevel(Math.max(1, loreSkillLevels.getOrDefault(skillName.toLowerCase(), 1)));
            }
         } else {
            // 已存在的 PlayerSkill：
            // - 若是玩家通过职业学会的（playerClass != null）→ 正常处理
            // - 若是装备注入且之前被禁用的（level == 0）→ 恢复 level=1，保留冷却
            if (ps.getPlayerClass() == null && ps.getLevel() < 1) {
               ps.setLevel(Math.max(1, loreSkillLevels.getOrDefault(skillName.toLowerCase(), 1)));
            }
            // 如果已经在 bar 的某个位置（非空），不要重复 assign
            boolean alreadyInBar = false;
            for (Map.Entry<Integer, String> barEntry : slotMap.entrySet()) {
               if (skillName.equalsIgnoreCase(barEntry.getValue())) {
                  alreadyInBar = true;
                  break;
               }
            }
            if (alreadyInBar) continue;
         }
         if (ps != null) {
            bar.assign(ps, slot - 1);
            newManaged.put(slot, skillName);
            usedByArmor.add(slot);
         }
      }

      // 第四步：套装效果 — 统计套装件数 → 追加额外技能到空闲槽
      java.util.Map<String, Settings.SetBonusEntry> setBonuses = settings.getSetBonuses();
      if (!setBonuses.isEmpty()) {
         // 收集所有参与检测的物品（4防具 + 副手）
         java.util.List<ItemStack> allItems = new java.util.ArrayList<>();
         for (ItemStack a : armor) { if (a != null && a.getType() != Material.AIR) allItems.add(a); }
         ItemStack offhandItem = player.getInventory().getItemInOffHand();
         if (offhandItem != null && offhandItem.getType() != Material.AIR) allItems.add(offhandItem);

         // 找出已占用的槽位
         java.util.Set<Integer> usedSlots = new java.util.HashSet<>(targetSlots.keySet());

         for (Settings.SetBonusEntry setEntry : setBonuses.values()) {
            // 统计该套装在身上的件数
            int count = 0;
            for (ItemStack it : allItems) {
               if (hasLoreText(it, setEntry.detectLore)) count++;
            }
            // 收集所有达成件数的奖励技能（向下包含，如4件套同时享有2件奖励）
            java.util.List<String> bonusSkills = new java.util.ArrayList<>();
            for (java.util.Map.Entry<Integer, java.util.List<String>> be : setEntry.bonuses.entrySet()) {
               if (count >= be.getKey()) {
                  bonusSkills.addAll(be.getValue());
               }
            }
            // 把奖励技能填入空闲槽
            for (String skillName : bonusSkills) {
               neededSkillNames.add(skillName.toLowerCase());
               // 已在 bar 中的技能不重复装配
               boolean alreadyInBar = false;
               for (Map.Entry<Integer, String> barEntry : bar.getData().entrySet()) {
                  if (skillName.equalsIgnoreCase(barEntry.getValue())) {
                     alreadyInBar = true;
                     break;
                  }
               }
               if (alreadyInBar) continue;

               int freeSlot = findFreeSlot(bar, usedSlots);
               if (freeSlot < 0) break; // 没空闲槽了
               PlayerSkill ps = data.getSkill(skillName);
               if (ps == null) {
                  Skill skill = SkillAPI.getSkill(skillName);
                  if (skill == null) continue;
                  data.giveSkill(skill, null);
                  ps = data.getSkill(skillName);
                  if (ps != null && ps.getLevel() < 1) ps.setLevel(ps.getData().getMaxLevel());
               } else {
                  // 已存在的 PlayerSkill：若是装备注入且被禁用（level==0），恢复等级并保留冷却
                  if (ps.getPlayerClass() == null && ps.getLevel() < 1) {
                     ps.setLevel(ps.getData().getMaxLevel());
                  }
               }
               if (ps != null) {
                  bar.assign(ps, freeSlot - 1);
                  usedSlots.add(freeSlot);
                  newManaged.put(freeSlot, skillName);
               }
            }
         }
      }

      // 第四步-b：套装属性加成 — 根据套装件数给玩家临时增加属性
      // 策略：累计所有"件数 <= 当前件数"的档位加成（向下包含）
      // 例如：穿4件 → count-2 + count-4 的属性全部累加
      java.util.Map<String, Integer> newAttrBonuses = new java.util.HashMap<>();
      java.util.Map<String, Settings.SetBonusEntry> setBonuses2 = settings.getSetBonuses();
      if (!setBonuses2.isEmpty()) {
         // 再次遍历套装列表（复用上面的 allItems 已经收集了，这里重新收集避免变量作用域问题）
         java.util.List<ItemStack> allItems2 = new java.util.ArrayList<>();
         for (ItemStack a : armor) { if (a != null && a.getType() != Material.AIR) allItems2.add(a); }
         ItemStack offhand2 = player.getInventory().getItemInOffHand();
         if (offhand2 != null && offhand2.getType() != Material.AIR) allItems2.add(offhand2);

         for (Settings.SetBonusEntry setEntry : setBonuses2.values()) {
            if (setEntry.attributeBonuses.isEmpty()) continue;
            // 统计件数
            int count2 = 0;
            for (ItemStack it : allItems2) {
               if (hasLoreText(it, setEntry.detectLore)) count2++;
            }
            // 累计所有 count <= 当前件数的属性加成
            for (java.util.Map.Entry<Integer, java.util.Map<String, Integer>> attrBe : setEntry.attributeBonuses.entrySet()) {
               if (count2 >= attrBe.getKey()) {
                  for (java.util.Map.Entry<String, Integer> e : attrBe.getValue().entrySet()) {
                     String attrKey = e.getKey();
                     int val = e.getValue();
                     newAttrBonuses.put(attrKey, newAttrBonuses.getOrDefault(attrKey, 0) + val);
                  }
               }
            }
         }
      }

      // 先撤销上一轮的属性加成（用负数）
      java.util.Map<String, Integer> prevAttr = setBonusAttrApplied.get(player.getUniqueId());
      if (prevAttr != null && !prevAttr.isEmpty()) {
         for (java.util.Map.Entry<String, Integer> e : prevAttr.entrySet()) {
            try {
               data.addBonusAttributes(e.getKey(), -e.getValue());
            } catch (Exception ex) {
               // 属性 key 不存在时忽略（配置可能改了）
            }
         }
      }
      // 应用新一轮的属性加成
      if (!newAttrBonuses.isEmpty()) {
         for (java.util.Map.Entry<String, Integer> e : newAttrBonuses.entrySet()) {
            try {
               data.addBonusAttributes(e.getKey(), e.getValue());
            } catch (Exception ex) {
               // 属性 key 不存在时忽略
            }
         }
      }
      // 发布完整快照，SX-Attribute 用固定来源替换，保证穿脱装备不会产生残留叠加。
      Bukkit.getPluginManager().callEvent(new com.sucy.skill.api.event.SetBonusAttributeEvent(player, newAttrBonuses));
      // 记录本轮给玩家的加成（下次刷新时用做撤销依据）
      if (!newAttrBonuses.isEmpty()) {
         setBonusAttrApplied.put(player.getUniqueId(), newAttrBonuses);
      } else {
         setBonusAttrApplied.remove(player.getUniqueId());
      }

      // ============== 第四步-c：被动技能 initialize / stopEffects diff ==============
      // 目标：让含 Physical Damage / Death 等 trigger 的 Dynamic 技能，在装备后真正响应事件
      // 只处理 armor-bind 注入的被动（playerClass == null）；玩家职业已学的被动由 startPassives 管
      java.util.Set<String> currentPassives = activePassives.getOrDefault(
            player.getUniqueId(), java.util.Collections.emptySet());
      java.util.Set<String> newActivePassives = new java.util.HashSet<>();
      for (String name : neededSkillNames) {
         Skill sk = SkillAPI.getSkill(name);
         if (!(sk instanceof PassiveSkill)) continue;
         PlayerSkill existingPs = data.getSkill(name);
         // 只跟踪不属于职业的（职业已学 → startPassives 会处理，我们别重复 init）
         if (existingPs == null || existingPs.getPlayerClass() == null) {
            newActivePassives.add(name);
         }
      }

      // 上一轮激活但本轮不再需要 → stopEffects
      for (String name : currentPassives) {
         if (newActivePassives.contains(name)) continue;
         PlayerSkill ps = data.getSkill(name);
         if (ps == null || !(ps.getData() instanceof PassiveSkill)) continue;
         try {
            int lvl = Math.max(1, ps.getLevel());
            ((PassiveSkill) ps.getData()).stopEffects(player, lvl);
         } catch (Exception ignored) {
            // 极端情况下 skill 实例已损坏，忽略以免影响主流程
         }
      }

      // 本轮新增激活 → giveSkill（若还没）+ setLevel(1) + initialize
      for (String name : newActivePassives) {
         if (currentPassives.contains(name)) continue;
         PlayerSkill ps = data.getSkill(name);
         if (ps == null) {
            Skill sk = SkillAPI.getSkill(name);
            if (sk == null) continue;
            data.giveSkill(sk, null);
            ps = data.getSkill(name);
         }
         if (ps == null) continue;
         // 只对 armor-bind 注入的被动手动 init（职业学的 startPassives 已管）
         if (ps.getPlayerClass() != null) continue;
         if (ps.getLevel() < 1) ps.setLevel(1);
         // 防止 double-init：如果 startPassives 已经在 join/respawn/world-change 时初始化过
         // （对 DynamicSkill 可通过 isActive 检测），直接跳过 initialize 调用，只登记跟踪
         boolean alreadyActive = false;
         if (ps.getData() instanceof com.sucy.skill.dynamic.DynamicSkill) {
            alreadyActive = ((com.sucy.skill.dynamic.DynamicSkill) ps.getData()).isActive(player);
         }
         if (!alreadyActive) {
            try {
               ((PassiveSkill) ps.getData()).initialize(player, ps.getLevel());
            } catch (Exception ignored) {
               // 单个技能 init 失败不影响其他技能
            }
         }
      }

      activePassives.put(player.getUniqueId(), newActivePassives);

      // 第五步：清理 PlayerData 中由 armor-bind 注入但本轮不再需要的临时 PlayerSkill
      // 关键修复：不删除 PlayerSkill 实例（删除会导致冷却重置），而是把 level 设为 0
      // level=0 时 PlayerData.check() 会返回 NOT_UNLOCKED，玩家无法施放
      // 再次带上装备时，第三步会检测到 level<1 并恢复为 1，冷却值自然保留
      java.util.Set<String> prevInjected = injectedSkills.getOrDefault(player.getUniqueId(), java.util.Collections.emptySet());
      if (!prevInjected.isEmpty()) {
         java.util.Set<String> keepInjected = new java.util.HashSet<>();
         for (String skillNameLower : prevInjected) {
            if (neededSkillNames.contains(skillNameLower)) {
               // 本轮仍需要 → 保留
               keepInjected.add(skillNameLower);
               continue;
            }
            // 本轮不需要了 → 把 level 设为 0（保留冷却，玩家无法施放）
            PlayerSkill ps = data.getSkill(skillNameLower);
            if (ps != null && ps.getPlayerClass() == null) {
               // 确认不是玩家职业学会的技能
               boolean learnedByClass = false;
               for (com.sucy.skill.api.player.PlayerClass pc : data.getClasses()) {
                  if (pc.getData().getSkills() != null) {
                     for (Skill s : pc.getData().getSkills()) {
                        if (s.getName().toLowerCase().equals(skillNameLower)) {
                           learnedByClass = true;
                           break;
                        }
                     }
                  }
               }
               if (!learnedByClass) {
                  ps.setLevel(0); // 禁用技能，保留冷却
               } else {
                  keepInjected.add(skillNameLower);
               }
            }
         }
         injectedSkills.put(player.getUniqueId(), keepInjected);
      }
      // 记录本轮新注入的技能名（合并到 injectedSkills）
      if (!neededSkillNames.isEmpty()) {
         java.util.Set<String> current = injectedSkills.get(player.getUniqueId());
         if (current == null) {
            current = new java.util.HashSet<>();
            injectedSkills.put(player.getUniqueId(), current);
         }
         current.addAll(neededSkillNames);
      }

      // 第六步：刷新背包显示
      managedSlots.put(player.getUniqueId(), newManaged);
      bar.update(player);
   }

   /**
    * 查找第一个空闲的技能槽（1-9），跳过已占用、武器槽，以及玩家已有技能的槽
    */
   private static int findFreeSlot(PlayerSkillBar bar, java.util.Set<Integer> usedSlots) {
      java.util.Map<Integer, String> slotMap = bar.getData();
      for (int i = 1; i <= 9; i++) {
         if (usedSlots.contains(i)) continue;
         if (bar.isWeaponSlot(i - 1)) continue;
         if (!bar.isSetup()) continue;
         // 该槽已有玩家手动放置的技能时跳过（"e" 表示空槽）
         String existing = slotMap.get(i);
         if (existing != null && !"e".equals(existing)) continue;
         return i;
      }
      return -1;
   }

   /**
    * 计算装备指纹（材质+lore hash），用于判断装备是否真的变化了
    * 包含 4 件防具 + 副手 + 主手
    */
   private static String computeFingerprint(ItemStack[] armor, ItemStack offhand, ItemStack mainhand) {
      StringBuilder sb = new StringBuilder();
      for (ItemStack item : armor) {
         if (item == null || item.getType() == Material.AIR) { sb.append("_"); continue; }
         sb.append(item.getType().name());
         if (item.hasItemMeta() && item.getItemMeta().hasLore()) sb.append(item.getItemMeta().getLore().hashCode());
         sb.append("|");
      }
      sb.append("O|");
      if (offhand != null && offhand.getType() != Material.AIR) {
         // 手持物品只有 Lore 会影响技能绑定；材质变化但 Lore 均无绑定标记时无需重建技能栏。
         if (offhand.hasItemMeta() && offhand.getItemMeta().hasLore()) sb.append(offhand.getItemMeta().getLore().hashCode());
      }
      sb.append("|M|");
      if (mainhand != null && mainhand.getType() != Material.AIR) {
         // 不把普通物品材质写入指纹，避免切换普通快捷栏物品时重置冷却显示。
         if (mainhand.hasItemMeta() && mainhand.getItemMeta().hasLore()) sb.append(mainhand.getItemMeta().getLore().hashCode());
      }
      return sb.toString();
   }

   /**
    * 检查物品是否满足玩家的等级/职业需求（复用 SkillAPI lore 检测逻辑）
    */
   private static boolean meetsRequirement(Player player, ItemStack item) {
      if (item == null || item.getType() == Material.AIR) return false;
      if (!item.hasItemMeta() || !item.getItemMeta().hasLore()) return true;
      Settings settings = SkillAPI.getSettings();
      String levelText = settings.getLoreLevelText();
      String classText = settings.getLoreClassText();
      String excludeText = settings.getLoreExcludeText();
      PlayerData data = SkillAPI.getPlayerData(player);
      com.sucy.skill.api.player.PlayerClass pc = data.getMainClass();

      for (String line : item.getItemMeta().getLore()) {
         String stripped = ChatColor.stripColor(line);
         // 等级需求
         if (levelText != null && !levelText.isEmpty() && stripped.startsWith(levelText)) {
            int req = parseIntSafe(stripped.substring(levelText.length()).trim());
            if (req > 0 && (pc == null || pc.getLevel() < req)) return false;
         }
         // 职业需求
         if (classText != null && !classText.isEmpty() && stripped.startsWith(classText)) {
            String reqClass = stripped.substring(classText.length()).trim().toLowerCase();
            if (pc == null || !pc.getData().getName().toLowerCase().equals(reqClass)) return false;
         }
         // 职业排除
         if (excludeText != null && !excludeText.isEmpty() && stripped.startsWith(excludeText)) {
            String excClass = stripped.substring(excludeText.length()).trim().toLowerCase();
            if (pc != null && pc.getData().getName().toLowerCase().equals(excClass)) return false;
         }
      }
      return true;
   }

   private static int parseIntSafe(String s) {
      try { return Integer.parseInt(s); }
      catch (NumberFormatException e) { return 0; }
   }

   /**
    * 从物品 lore 中提取所有附带技能名（按 lore 顺序）
    * 同一物品可有多行 `附带技能: X`，每行一个技能
    */
   private static List<String> extractAllSkillsFromItem(ItemStack item, String pre, String post) {
      List<String> result = new java.util.ArrayList<>();
      // NBT 技能列表与 Lore 并行读取；每项格式为“技能名”或“技能名@等级”。
      for (String encoded : ItemDataReader.getStringList(item, "SkillAPI", "skills")) {
         if (encoded == null) continue;
         String name = encoded.trim();
         int marker = name.lastIndexOf('@');
         int level = 1;
         if (marker > 0) {
            try { level = Math.max(1, Integer.parseInt(name.substring(marker + 1).trim())); name = name.substring(0, marker).trim(); }
            catch (NumberFormatException ignored) { }
         }
         if (!name.isEmpty() && SkillAPI.getSkill(name) != null) {
            loreSkillLevels.put(name.toLowerCase(), level);
            result.add(name);
         }
      }
      if (item == null || !item.hasItemMeta()) return result;
      ItemMeta meta = item.getItemMeta();
      if (meta == null || !meta.hasLore()) return result;

      String prePlain = ChatColor.stripColor(pre);
      String postPlain = ChatColor.stripColor(post);
      for (String line : meta.getLore()) {
         String plain = ChatColor.stripColor(line);
         if (plain.startsWith(prePlain) && plain.endsWith(postPlain)) {
            int start = prePlain.length();
            int end = plain.length() - postPlain.length();
            if (start <= end) {
               String name = plain.substring(start, end).trim();
               int requestedLevel = 1;
               int marker = name.lastIndexOf('@');
               if (marker > 0) {
                  try { requestedLevel = Math.max(1, Integer.parseInt(name.substring(marker + 1).trim())); name = name.substring(0, marker).trim(); }
                  catch (NumberFormatException ignored) { /* @ 可作为普通技能名字符 */ }
               }
               if (!name.isEmpty() && SkillAPI.getSkill(name) != null) {
                  loreSkillLevels.put(name.toLowerCase(), requestedLevel);
                  result.add(name);
               }
            }
         }
      }
      return result;
   }

   /**
    * 从物品 lore 中提取技能名（返回第一个匹配，保留兼容旧调用）
    */
   private static String extractSkillFromItem(ItemStack item, String pre, String post) {
      if (item == null || !item.hasItemMeta()) return null;
      ItemMeta meta = item.getItemMeta();
      if (meta == null || !meta.hasLore()) return null;

      List<String> lore = meta.getLore();
      // 去色后匹配
      String prePlain = ChatColor.stripColor(pre);
      String postPlain = ChatColor.stripColor(post);

      for (String line : lore) {
         String plain = ChatColor.stripColor(line);
         if (plain.startsWith(prePlain) && plain.endsWith(postPlain)) {
            int start = prePlain.length();
            int end = plain.length() - postPlain.length();
            if (start <= end) {
               String name = plain.substring(start, end).trim();
               if (!name.isEmpty() && SkillAPI.getSkill(name) != null) {
                  return name;
               }
            }
         }
      }
      return null;
   }
}

