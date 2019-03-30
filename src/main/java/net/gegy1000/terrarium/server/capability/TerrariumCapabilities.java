package net.gegy1000.terrarium.server.capability;

import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.world.chunk.tracker.ChunkTrackerHooks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import java.util.concurrent.Callable;

public class TerrariumCapabilities {
    public static final ResourceLocation WORLD_DATA_ID = new ResourceLocation(Terrarium.MODID, "world_data");
    public static final ResourceLocation EXTERNAL_DATA_ID = new ResourceLocation(Terrarium.MODID, "external_data");
    public static final ResourceLocation TRACKER_HOOKS_ID = new ResourceLocation(Terrarium.MODID, "tracker_hooks");

    @CapabilityInject(TerrariumWorldData.class)
    public static Capability<TerrariumWorldData> worldDataCapability;

    @CapabilityInject(TerrariumExternalCapProvider.class)
    public static Capability<TerrariumExternalCapProvider> externalProviderCapability;

    @CapabilityInject(ChunkTrackerHooks.class)
    public static Capability<ChunkTrackerHooks> chunkHooksCapability;

    public static void onPreInit() {
        CapabilityManager.INSTANCE.register(TerrariumWorldData.class, new VoidStorage<>(), unsupported());
        CapabilityManager.INSTANCE.register(TerrariumExternalCapProvider.class, new VoidStorage<>(), TerrariumExternalCapProvider.Implementation::new);
        CapabilityManager.INSTANCE.register(ChunkTrackerHooks.class, new VoidStorage<>(), unsupported());
    }

    private static <T> Callable<T> unsupported() {
        return () -> {
            throw new UnsupportedOperationException();
        };
    }
}
