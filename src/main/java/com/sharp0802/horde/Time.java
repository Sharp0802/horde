package com.sharp0802.horde;

import net.minecraftforge.server.ServerLifecycleHooks;

public class Time {
    public static long getTick() {
        var server = ServerLifecycleHooks.getCurrentServer();
        var overworld = server.overworld();
        var ticks = overworld.getGameTime();

        // realign for `/time set`
        ticks = (ticks / 24000) * 24000 + overworld.getDayTime();

        return ticks;
    }
}
