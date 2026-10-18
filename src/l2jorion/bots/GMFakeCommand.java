package l2jorion.bots;

/**
 * GM entry points for controlling fake players. Integrate these methods with
 * your server's GM/command framework to provide runtime control.
 *
 * Example (pseudo):
 *   if (command.equals("spawnFake")) { GMFakeCommand.spawn(argsCount); }
 */
public class GMFakeCommand {
    public static void spawn(int count) {
        FakePlayerManager.init();
        FakePlayerManager.spawnFake(count);
    }

    public static void spawnDefault() {
        FakePlayerManager.init();
        FakePlayerManager.spawnFake(100);
    }

    public static void despawnAll() {
        FakePlayerManager.despawnAll();
    }

    public static void despawn(int count) {
        FakePlayerManager.despawnCount(count);
    }

    public static void list() {
        for (FakePlayer p : FakePlayerManager.list()) {
            System.out.println("FakePlayer: " + p.getName() + " state=" + p.getState());
        }
    }
}
