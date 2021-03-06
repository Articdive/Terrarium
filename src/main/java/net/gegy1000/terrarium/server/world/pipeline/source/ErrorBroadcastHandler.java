package net.gegy1000.terrarium.server.world.pipeline.source;

import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.TerrariumHandshakeTracker;
import net.gegy1000.terrarium.server.message.DataFailWarningMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber(modid = TerrariumEarth.MODID)
public class ErrorBroadcastHandler {
    private static final Object LOCK = new Object();

    private static final long FAIL_NOTIFICATION_INTERVAL = 8000;
    private static final int FAIL_NOTIFICATION_THRESHOLD = 5;

    private static int failCount;
    private static long lastFailNotificationTime;

    @SubscribeEvent
    public static void onWorldLoad(WorldEvent.Load event) {
        lastFailNotificationTime = System.currentTimeMillis();
    }

    @SubscribeEvent
    public static void onTick(TickEvent event) {
        long time = System.currentTimeMillis();

        if (time - lastFailNotificationTime > FAIL_NOTIFICATION_INTERVAL) {
            if (failCount > FAIL_NOTIFICATION_THRESHOLD) {
                broadcastFailNotification(failCount);
                failCount = 0;
            }
            lastFailNotificationTime = time;
        }
    }

    private static void broadcastFailNotification(int failCount) {
        DataFailWarningMessage message = new DataFailWarningMessage(failCount);
        for (EntityPlayer player : TerrariumHandshakeTracker.getFriends()) {
            Terrarium.NETWORK.sendTo(message, (EntityPlayerMP) player);
        }
    }

    public static void recordFailure() {
        synchronized (LOCK) {
            failCount++;
        }
    }
}
