package l2jorion.bots;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Lightweight server-side fake player simulation.
 *
 * NOTE: This implementation is intentionally self-contained and uses simple simulation
 * primitives (position, state machine, scheduled ticks). To integrate with the existing
 * game world (so the fake player appears in the online list, interacts with mobs, uses
 * skills, etc.) you should wire the TODO hooks marked below to the project's Player/World APIs.
 *
 * Integration points (placeholders marked with TODO):
 *  - spawnIntoWorld(): register the fake player with the server world / visible lists
 *  - despawnFromWorld(): unregister the fake player
 *  - findNearbyMobs(): query server world for attackable mobs
 *  - pathTo(x,y,z): ask server pathfinder to move actor
 *  - useSkillOn(target): invoke skill usage on a target
 *
 * This class focuses on behavior (idle -> patrol -> engage -> attack -> chat) and a
 * safe, configurable scheduler. It was written so you can extend it to call the
 * project's real movement/combat APIs.
 */
public class FakePlayer {
    public enum State { IDLE, PATROL, ENGAGE, ATTACK, CHAT }

    private final String id; // unique id for manager
    private final String name;
    private final int level;
    private final String behaviorPreset;

    // Simple position representation; integrate with real world coordinates as needed
    private volatile int x;
    private volatile int y;
    private volatile int z;

    private volatile State state = State.IDLE;
    private volatile long lastChatAt = 0L;

    public FakePlayer(String name, int level, String behaviorPreset, int startX, int startY, int startZ) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.level = level;
        this.behaviorPreset = behaviorPreset;
        this.x = startX;
        this.y = startY;
        this.z = startZ;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getLevel() { return level; }
    public State getState() { return state; }

    // Called once when spawning
    public void spawn() {
        // TODO: register this fake player into the server "World" so it's visible to other players
        // Example: World.getInstance().addFakePlayer(this);
        spawnIntoWorld();
        state = State.IDLE;
    }

    // Called once when despawning
    public void despawn() {
        // TODO: unregister from server world
        despawnFromWorld();
        state = State.IDLE;
    }

    // Main tick called by manager (e.g. every 1000 ms)
    public void tick() {
        // 1. Random chat
        long now = System.currentTimeMillis();
        if (shouldChat(now)) {
            state = State.CHAT;
            doChat();
            lastChatAt = now;
            return;
        }

        // 2. See if there are nearby mobs to engage (placeholder)
        Object mob = findNearbyMobs();
        if (mob != null) {
            state = State.ENGAGE;
            // Move toward mob
            pathToMob(mob);
            // When in range, switch to ATTACK and use skill
            if (isInAttackRange(mob)) {
                state = State.ATTACK;
                useSkillOn(mob);
            }
            return;
        }

        // 3. Patrol or idle depending on preset
        if (shouldPatrol()) {
            state = State.PATROL;
            doPatrolStep();
        } else {
            state = State.IDLE;
            // small random walk
            randomWalkStep();
        }
    }

    private boolean shouldChat(long now) {
        // default chat frequency 60-180s, randomized per bot
        long elapsed = now - lastChatAt;
        long min = 60_000L; // 60s
        long max = 180_000L; // 180s
        long threshold = ThreadLocalRandom.current().nextLong(min, max + 1);
        return elapsed > threshold && ThreadLocalRandom.current().nextDouble() < 0.5; // 50% chance to actually chat
    }

    private void doChat() {
        String msg = pickChatMessage();
        // TODO: send chat into server chat system, e.g. ChatManager.broadcast(name, msg)
        sendChatMessage(msg);
    }

    private String pickChatMessage() {
        String[] sample = new String[] {
            "Hello everyone!",
            "Anyone wants to party?",
            "Selling rare items!",
            "Where is the boss?",
            "Looking for a team.",
            "/dance"
        };
        int i = ThreadLocalRandom.current().nextInt(sample.length);
        return sample[i];
    }

    private Object findNearbyMobs() {
        // TODO: query the world for nearby NPCs/mobs within a configured radius
        // Placeholder: return null for now
        return null;
    }

    private void pathToMob(Object mob) {
        // TODO: use real pathfinder to move this bot towards the mob
    }

    private boolean isInAttackRange(Object mob) {
        // TODO: check distance/line-of-sight using the server API
        return ThreadLocalRandom.current().nextDouble() < 0.3; // simulated 30% chance to be in range
    }

    private void useSkillOn(Object mob) {
        // TODO: call skill usage APIs: SkillsManager.useSkill(this, skillId, mob)
        // For now we simulate by printing/logging
        log(String.format("%s attacks a mob (simulated)", name));
    }

    private boolean shouldPatrol() {
        // preset-driven; for now random 60% patrol
        return ThreadLocalRandom.current().nextDouble() < 0.6;
    }

    private void doPatrolStep() {
        // Basic patrol: move to a random nearby point within 300 units
        int dx = ThreadLocalRandom.current().nextInt(-300, 301);
        int dy = ThreadLocalRandom.current().nextInt(-300, 301);
        int newX = x + dx;
        int newY = y + dy;
        pathTo(newX, newY, z);
    }

    private void randomWalkStep() {
        int dx = ThreadLocalRandom.current().nextInt(-20, 21);
        int dy = ThreadLocalRandom.current().nextInt(-20, 21);
        x += dx;
        y += dy;
        // TODO: notify world about new position if integrated
        updatePositionInWorld();
    }

    private void pathTo(int nx, int ny, int nz) {
        // TODO: call server pathfinder; here we just teleport for simplicity
        this.x = nx;
        this.y = ny;
        this.z = nz;
        updatePositionInWorld();
    }

    // Integration placeholder methods (log & comments)
    private void spawnIntoWorld() {
        log(String.format("Spawning fake player %s (lvl %d) at %d,%d,%d", name, level, x, y, z));
        // TODO: integrate with the game's world/player registry
    }

    private void despawnFromWorld() {
        log(String.format("Despawning fake player %s", name));
        // TODO: integrate with the game's world/player registry
    }

    private void sendChatMessage(String msg) {
        log(String.format("[%s] %s", name, msg));
        // TODO: use server chat APIs to broadcast message
    }

    private void updatePositionInWorld() {
        // TODO: update the player's position in the real world so clients see movement
    }

    private void log(String s) {
        System.out.println("[FakePlayer] " + s);
    }
}
