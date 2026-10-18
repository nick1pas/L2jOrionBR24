Fake Players (server-side) - README

This branch adds a basic, self-contained server-side fake player system.

Files added (branch: feature/fake-players)
- src/l2jorion/bots/FakePlayer.java
- src/l2jorion/bots/FakePlayerManager.java
- src/l2jorion/bots/GMFakeCommand.java
- src/l2jorion/bots/config/fake_players.json

What this does
- Spawns configurable fake players that run a simple state machine (idle/patrol/engage/attack/chat).
- Manager uses a ScheduledExecutorService to tick bots every second (configurable).
- Default config spawns up to 100 bots and uses simple sample chat messages.

Important integration notes
- The implementation is deliberately lightweight and contains TODO hooks where you must connect
  the code to the server's real APIs so bots become visible and interactive:
    - spawnIntoWorld() / despawnFromWorld() in FakePlayer
    - findNearbyMobs(), pathTo(), useSkillOn() in FakePlayer
    - updatePositionInWorld() and sendChatMessage() in FakePlayer
- To enable the system automatically at server start, call:
    l2jorion.bots.FakePlayerManager.init();
  from your server bootstrap (e.g. at GS/Server initialization).

Usage examples (manual / GM)
- Spawn 100 bots (default preset):
    l2jorion.bots.GMFakeCommand.spawnDefault();
- Spawn N bots:
    l2jorion.bots.GMFakeCommand.spawn(50);
- Despawn all bots:
    l2jorion.bots.GMFakeCommand.despawnAll();
- List bots in console:
    l2jorion.bots.GMFakeCommand.list();

Testing and safety
- Start with a small number of bots in a test environment and verify integration points before
  enabling at scale. The manager enforces maxCount from the config (default 100).
- The code logs activity to stdout. Replace with your logger if needed.

Next steps I can do for you
- Wire the TODO integration points to the server's World/Player/Chat APIs so bots are fully
  interactive and visible to real players.
- Add persistent presets and an admin command parser that works with your GM command framework.
- Improve AI: skill rotations, aggro tables, target selection, pathfinding integration.

