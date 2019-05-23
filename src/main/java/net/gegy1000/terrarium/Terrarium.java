package net.gegy1000.terrarium;

import net.gegy1000.terrarium.server.ServerProxy;
import net.gegy1000.terrarium.server.capability.TerrariumCapabilities;
import net.gegy1000.terrarium.server.message.DataFailWarningMessage;
import net.gegy1000.terrarium.server.message.TerrariumHandshakeMessage;
import net.gegy1000.terrarium.server.world.chunk.tracker.SavedColumnTracker;
import net.gegy1000.terrarium.server.world.chunk.tracker.SavedCubeTracker;
import net.gegy1000.terrarium.server.world.generator.customization.TerrariumPresetRegistry;
import net.gegy1000.terrarium.server.world.pipeline.source.TiledDataSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

@Mod(modid = Terrarium.MODID, name = "Terrarium", version = Terrarium.VERSION, acceptedMinecraftVersions = "[1.12]", dependencies = "after:cubicchunks")
public class Terrarium {
    public static final String MODID = "terrarium";
    public static final String VERSION = "1.1.0";

    public static final String CLIENT_PROXY = "net.gegy1000.terrarium.client.ClientProxy";
    public static final String SERVER_PROXY = "net.gegy1000.terrarium.server.ServerProxy";

    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public static final SimpleNetworkWrapper NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(Terrarium.MODID);

    public static boolean serverHasMod = false;

    @SidedProxy(clientSide = CLIENT_PROXY, serverSide = SERVER_PROXY)
    public static ServerProxy PROXY;

    @Mod.EventHandler
    public static void onPreInit(FMLPreInitializationEvent event) {
        PROXY.onPreInit();

        try {
            Files.createDirectories(TiledDataSource.GLOBAL_CACHE_ROOT);
        } catch (IOException e) {
            Terrarium.LOGGER.warn("Failed to create cache directories", e);
        }

        TerrariumCapabilities.onPreInit();

        NETWORK.registerMessage(TerrariumHandshakeMessage.Handler.class, TerrariumHandshakeMessage.class, 0, Side.SERVER);
        NETWORK.registerMessage(TerrariumHandshakeMessage.Handler.class, TerrariumHandshakeMessage.class, 1, Side.CLIENT);
        NETWORK.registerMessage(DataFailWarningMessage.Handler.class, DataFailWarningMessage.class, 3, Side.CLIENT);

        MinecraftForge.EVENT_BUS.register(SavedColumnTracker.class);

        if (Loader.isModLoaded("cubicchunks")) {
            registerCubicChunksEvents();
        }
    }

    private static void registerCubicChunksEvents() {
        MinecraftForge.EVENT_BUS.register(SavedCubeTracker.class);
    }

    @Mod.EventHandler
    public static void onInit(FMLInitializationEvent event) {
        PROXY.onInit();
        TerrariumPresetRegistry.onInit();
    }

    @Mod.EventHandler
    public static void onPostInit(FMLPostInitializationEvent event) {
        PROXY.onPostInit();
    }

    @NetworkCheckHandler
    public static boolean onCheckNetwork(Map<String, String> mods, Side side) {
        if (side.isServer()) {
            serverHasMod = mods.containsKey(Terrarium.MODID);
        }
        return !mods.containsKey(Terrarium.MODID) || mods.get(Terrarium.MODID).equals(VERSION);
    }
}
