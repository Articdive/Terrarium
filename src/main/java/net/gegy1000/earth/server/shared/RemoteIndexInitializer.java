package net.gegy1000.earth.server.shared;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.gegy1000.earth.TerrariumEarth;
import net.gegy1000.earth.server.util.ProcessTracker;
import net.gegy1000.earth.server.util.ProgressTracker;
import net.gegy1000.earth.server.util.TrackedInputStream;
import net.gegy1000.earth.server.world.data.source.EarthRemoteIndex;
import net.gegy1000.terrarium.server.world.pipeline.source.TiledDataSource;
import net.minecraft.util.text.TextComponentTranslation;
import org.apache.commons.io.IOUtils;
import org.tukaani.xz.SingleXZInputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public final class RemoteIndexInitializer implements SharedDataInitializer {
    private final static String INDEX_URL = "https://terrariumearth.azureedge.net/geo/data_index.json.xz";
    private final static String SHA1_URL = "https://terrariumearth.azureedge.net/geo/data_index.json.xz.sha1";

    private static final Path CACHE_PATH = TiledDataSource.GLOBAL_CACHE_ROOT.resolve("remote_index.json.xz");
    private final static JsonParser JSON_PARSER = new JsonParser();

    @Override
    public void initialize(SharedEarthData data, ProcessTracker processTracker) {
        ProgressTracker master = processTracker.push(new TextComponentTranslation("initializer.terrarium.remote_index"), 1);

        master.use(() -> {
            EarthRemoteIndex index = this.loadIndex(processTracker);
            data.put(SharedEarthData.REMOTE_INDEX, index);
        });
    }

    private EarthRemoteIndex loadIndex(ProcessTracker processTracker) throws IOException {
        if (Files.exists(CACHE_PATH)) {
            byte[] cachedBytes = Files.readAllBytes(CACHE_PATH);
            if (this.isCacheUpToDate(cachedBytes)) {
                return this.parse(cachedBytes);
            }
        }

        URL url = new URL(INDEX_URL);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent", TerrariumEarth.USER_AGENT);

        try (
                InputStream input = new TrackedInputStream(connection.getInputStream())
                        .submitTo(new TextComponentTranslation("initializer.terrarium.remote_index.downloading"), processTracker)
        ) {
            byte[] bytes = IOUtils.toByteArray(input);

            try (OutputStream output = Files.newOutputStream(CACHE_PATH)) {
                output.write(bytes);
            }

            return this.parse(bytes);
        }
    }

    private boolean isCacheUpToDate(byte[] cachedBytes) {
        try {
            byte[] remoteHash = this.loadRemoteHash();

            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            byte[] cachedHash = sha1.digest(cachedBytes);

            return Arrays.equals(cachedHash, remoteHash);
        } catch (IOException | NoSuchAlgorithmException e) {
            TerrariumEarth.LOGGER.warn("Failed to compare cache hash", e);
            return true;
        }
    }

    private EarthRemoteIndex parse(byte[] bytes) throws IOException {
        try (InputStream input = new SingleXZInputStream(new ByteArrayInputStream(bytes))) {
            JsonElement root = JSON_PARSER.parse(new InputStreamReader(input));
            return EarthRemoteIndex.parse(root.getAsJsonObject());
        }
    }

    private byte[] loadRemoteHash() throws IOException {
        URL url = new URL(SHA1_URL);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent", TerrariumEarth.USER_AGENT);

        try (InputStream input = connection.getInputStream()) {
            return IOUtils.toByteArray(input);
        }
    }
}
