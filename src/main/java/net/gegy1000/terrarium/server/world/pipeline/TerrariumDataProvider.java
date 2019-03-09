package net.gegy1000.terrarium.server.world.pipeline;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.world.pipeline.adapter.RegionAdapter;
import net.gegy1000.terrarium.server.world.pipeline.component.AttachedComponent;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponent;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.layer.CachedLayerContext;
import net.gegy1000.terrarium.server.world.pipeline.source.DataSourceHandler;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.TiledDataAccess;
import net.gegy1000.terrarium.server.world.region.GenerationRegion;
import net.gegy1000.terrarium.server.world.region.RegionData;
import net.gegy1000.terrarium.server.world.region.RegionTilePos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TerrariumDataProvider {
    private static final int DATA_SIZE = GenerationRegion.BUFFERED_SIZE;

    private final ImmutableMap<RegionComponentType<?>, AttachedComponent<?>> attachedComponents;
    private final ImmutableList<RegionAdapter> adapters;

    private TerrariumDataProvider(
            ImmutableMap<RegionComponentType<?>, AttachedComponent<?>> attachedComponents,
            ImmutableList<RegionAdapter> adapters
    ) {
        this.attachedComponents = attachedComponents;
        this.adapters = adapters;
    }

    public static Builder builder() {
        return new Builder();
    }

    public void enqueueData(RegionTilePos pos) {
        CachedLayerContext context = new CachedLayerContext(DataSourceHandler.INSTANCE);
        DataView view = new DataView(pos.getMinBufferedX(), pos.getMinBufferedZ(), DATA_SIZE, DATA_SIZE);

        Set<DataTileKey<?>> requiredData = new HashSet<>();
        for (AttachedComponent<?> attachedComponent : this.attachedComponents.values()) {
            requiredData.addAll(attachedComponent.getRequiredData(context, view));
        }

        DataSourceHandler.INSTANCE.enqueueData(requiredData);
    }

    public RegionData populateData(RegionTilePos pos) {
        CachedLayerContext context = new CachedLayerContext(DataSourceHandler.INSTANCE);
        DataView view = new DataView(pos.getMinBufferedX(), pos.getMinBufferedZ(), DATA_SIZE, DATA_SIZE);

        Map<RegionComponentType<?>, RegionComponent<?>> populatedComponents = new HashMap<>();
        for (AttachedComponent<?> attachedComponent : this.attachedComponents.values()) {
            RegionComponent<?> component = attachedComponent.createAndPopulate(context, view);
            populatedComponents.put(attachedComponent.getType(), component);
        }

        RegionData data = new RegionData(populatedComponents);
        this.applyAdapters(data, pos);

        return data;
    }

    @SuppressWarnings("unchecked")
    public <T extends TiledDataAccess> T populatePartialData(RegionComponentType<T> componentType, int x, int z, int width, int height) {
        AttachedComponent<T> attachedComponent = (AttachedComponent<T>) this.attachedComponents.get(componentType);
        if (attachedComponent == null) {
            throw new IllegalArgumentException("Cannot populate partial data tile for component that is not attached!");
        }

        CachedLayerContext context = new CachedLayerContext(DataSourceHandler.INSTANCE);
        DataView view = new DataView(x, z, width, height);

        Set<DataTileKey<?>> requiredData = new HashSet<>(attachedComponent.getRequiredData(context, view));
        DataSourceHandler.INSTANCE.enqueueData(requiredData);

        RegionComponent<T> component = attachedComponent.createAndPopulate(context, view);
        return component.getData();
    }

    public RegionData createDefaultData(int width, int height) {
        Map<RegionComponentType<?>, RegionComponent<?>> defaultComponents = new HashMap<>();
        for (RegionComponentType<?> componentType : this.attachedComponents.keySet()) {
            defaultComponents.put(componentType, componentType.createDefaultComponent(width, height));
        }

        return new RegionData(defaultComponents);
    }

    private void applyAdapters(RegionData data, RegionTilePos pos) {
        for (RegionAdapter adapter : this.adapters) {
            try {
                adapter.adapt(data, pos.getMinBufferedX(), pos.getMinBufferedZ(), DATA_SIZE, DATA_SIZE);
            } catch (Exception e) {
                Terrarium.LOGGER.warn("Failed to run adapter {}", adapter.getClass().getName(), e);
            }
        }
    }

    public ImmutableSet<RegionComponentType<?>> getAttachedComponentTypes() {
        return this.attachedComponents.keySet();
    }

    public static class Builder {
        private final Map<RegionComponentType<?>, AttachedComponent<?>> attachedComponents = new HashMap<>();
        private final List<RegionAdapter> adapters = new ArrayList<>();

        private Builder() {
        }

        public <T extends TiledDataAccess> Builder withComponent(RegionComponentType<T> type, DataLayer<T> producer) {
            this.attachedComponents.put(type, new AttachedComponent<>(type, producer));
            return this;
        }

        public Builder withAdapter(RegionAdapter adapter) {
            this.adapters.add(adapter);
            return this;
        }

        public TerrariumDataProvider build() {
            return new TerrariumDataProvider(ImmutableMap.copyOf(this.attachedComponents), ImmutableList.copyOf(this.adapters));
        }
    }
}
