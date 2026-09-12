package com.sucy.skill.combat.threat;

import com.sucy.skill.thread.IThreadTask;

/**
 * 仇恨系统定时任务
 * 每秒（20 tick）执行一次 ThreatManager.tick()
 */
public class ThreatTask implements IThreadTask {

    private int counter = 0;

    @Override
    public boolean tick() {
        counter++;
        // MainThread 每 50ms 一次 tick，20 次 = 1 秒
        if (counter >= 20) {
            counter = 0;
            ThreatManager.tick();
        }
        return false; // 永不移除
    }

    @Override
    public void run() {
        // 插件关闭时清理
        ThreatManager.clearAll();
    }
}
