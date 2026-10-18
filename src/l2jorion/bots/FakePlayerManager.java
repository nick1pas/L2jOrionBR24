package l2jorion.bots;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Manager that spawns and controls FakePlayers.
 *
 * Usage:
 *  - Call FakePlayerManager.init() early during server startup.
 *  - Use spawnFake(count) / despawnAll() or GMFakeCommand entry points to control bots.
 *
 * This manager uses a ScheduledExecutorService to tick all bots at a fixed interval (1s).
 */
public class FakePlayerManager {
    private static final Map<String, FakePlayer> ALL = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService SCHED = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "FakePlayerManager");
        t.setDaemon(true);
        return t;
    });
    private static ScheduledFuture<?> TICK_TASK;
    private static final AtomicInteger NAME_COUNTER = new AtomicInteger(1);

    private static Config config;

    public static class Preset {
        String namePattern = "Fake%03d";
        int level = 1;
        String behavior = "default";
    }

    public static class Config {
        int defaultCount = 10;
        int maxCount = 100;
        int tickIntervalMs = 1000;
        List<String> chatMessages = Collections.emptyList();
        Preset preset = new Preset();
    }

    public static synchronized void init() {
        if (config != null) return;
        loadConfig();
        startTickLoop();
        log("FakePlayerManager initialized (defaultCount=" + config.defaultCount + ", max=" + config.maxCount + ")");
    }

    private static void loadConfig() {
        try (Reader r = new InputStreamReader(FakePlayerManager.class.getResourceAsStream("/config/fake_players.json"), StandardCharsets.UTF_8)) {
            Gson g = new Gson();
            config = g.fromJson(r, new TypeToken<Config>() {}.getType());
            if (config == null) config = new Config();
        } catch (Exception e) {
            log("Could not load config/fake_players.json - using defaults. Error: " + e.getMessage());
            config = new Config();
        }
    }

    private static void startTickLoop() {
        if (TICK_TASK != null) return;
        TICK_TASK = SCHED.scheduleAtFixedRate(() -> {
            try {
                tickAll();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }, 0, Math.max(100, config.tickIntervalMs), TimeUnit.MILLISECONDS);
    }

    private static void tickAll() {
        for (FakePlayer p : new ArrayList<>(ALL.values())) {
            p.tick();
        }
    }

    public static List<FakePlayer> list() {
        return new ArrayList<>(ALL.values());
    }

    public static synchronized List<FakePlayer> spawnFake(int count) {
        if (config == null) init();
        int toSpawn = Math.min(count, config.maxCount - ALL.size());
        List<FakePlayer> created = new ArrayList<>(toSpawn);
        for (int i = 0; i < toSpawn; i++) {
            String name = String.format(config.preset.namePattern, NAME_COUNTER.getAndIncrement());
            FakePlayer fp = new FakePlayer(name, config.preset.level, config.preset.behavior, randomCoord(), randomCoord(), 0);
            fp.spawn();
            ALL.put(fp.getId(), fp);
            created.add(fp);
        }
        log("Spawned " + created.size() + " fake players (requested=" + count + ")");
        return created;
    }

    public static synchronized void despawnAll() {
        for (FakePlayer p : new ArrayList<>(ALL.values())) {
            p.despawn();
            ALL.remove(p.getId());
        }
        log("All fake players despawned");
    }

    public static synchronized int despawnCount(int count) {
        int removed = 0;
        List<FakePlayer> copy = new ArrayList<>(ALL.values());
        for (FakePlayer p : copy) {
            if (removed >= count) break;
            p.despawn();
            ALL.remove(p.getId());
            removed++;
        }
        log("Despawned " + removed + " fake players (requested=" + count + ")");
        return removed;
    }

    private static int randomCoord() {
        return ThreadLocalRandom.current().nextInt(0, 10000);
    }

    public static void shutdown() {
        if (TICK_TASK != null) TICK_TASK.cancel(false);
        SCHED.shutdownNow();
    }

    private static void log(String s) {
        System.out.println("[FakePlayerManager] " + s);
    }
}
