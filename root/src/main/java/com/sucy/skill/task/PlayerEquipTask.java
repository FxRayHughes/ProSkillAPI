package com.sucy.skill.task;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.attribute.mob.MobAttribute;
import com.sucy.skill.api.player.PlayerData;
import com.sucy.skill.data.PlayerEquipsRead;
import com.sucy.skill.thread.RepeatThreadTask;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.UUID;

public class PlayerEquipTask extends RepeatThreadTask {

    public PlayerEquipTask() {
        super(0, 5);
    }

    @Override
    public void run() {
        try {
            Bukkit.getOnlinePlayers().forEach(player -> {
                PlayerData data = SkillAPI.getPlayerData(player);
                if (data != null) {
                    PlayerEquipsRead.update(data);
                    PlayerEquipsRead.updateHand(data, false);
                }
            });
        } catch (Exception ignored) {

        }
    }
}
