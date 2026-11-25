package backend.battle;

import backend.model.Category;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.*;
import java.util.EnumSet;
import java.util.UUID;

public class SaveManager {
    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private final Path folder;
    private final Path playerFile;
    private UUID activeId;

    public SaveManager() { this(Path.of("saves")); }

    public SaveManager(Path folder) {
        this.folder = folder;
        playerFile = folder.resolve("player.json");
    }

    public static class Player {
        private UUID saveId;
        private String username;
        private int age;
        private String characterName = "";
        private int level = 1;
        private boolean introductionComplete;
        private boolean endingShown;
        private EnumSet<Category> defeatedFinalBosses = EnumSet.noneOf(Category.class);

        public UUID saveId() { return saveId; }
        public String characterName() { return characterName; }
        public int level() { return level; }

        public Story.Scene pendingScene() {
            if (!introductionComplete) return Story.Scene.INTRODUCTION;
            if (level == 4 && !endingShown) return Story.Scene.ENDING;
            return Story.Scene.NONE;
        }
    }

    public boolean accountExists() { return Files.isRegularFile(playerFile); }

    public Player newGame(String username, int age, String name, boolean replace) {
        return locked(() -> {
            if (!replace && Files.exists(playerFile)) throw new FileAlreadyExistsException(playerFile.toString());
            Player player = new Player();
            player.username = username;
            player.age = age;
            player.characterName = name == null ? "" : name.strip();
            initialize(player);
            return player;
        });
    }

    private void initialize(Player player) throws IOException {
        player.saveId = UUID.randomUUID();
        JsonObject unlocks = new JsonObject();
        for (Category category : Category.values()) unlocks.addProperty(category.name(), 1);
        // Publish the new player only after their own progress file exists.
        write(progressFile(player), unlocks);
        write(playerFile, player);
        activeId = player.saveId;
    }

    private Path progressFile(Player player) {
        return folder.resolve(player.saveId.toString()).resolve("boss-progress.json");
    }

    public Player currentPlayer() { return locked(this::loadPlayer); }

    private Player loadPlayer() throws IOException {
        Player player = JSON.fromJson(Files.readString(playerFile), Player.class);
        if (player == null) throw new IOException("Empty player save");
        if (activeId != null && !activeId.equals(player.saveId)) throw new IOException("The active save has changed");
        if (player.characterName == null) player.characterName = "";
        if (player.saveId == null) initialize(player); // Original username/age/characterName format.
        int expectedLevel = 1 + player.defeatedFinalBosses.size();
        if (player.level != expectedLevel) {
            player.level = expectedLevel;
            write(playerFile, player);
        }
        activeId = player.saveId;
        return player;
    }

    public void saveName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Enter a character name.");
        locked(() -> {
            Player player = loadPlayer();
            if (player.characterName.isBlank()) {
                player.characterName = name.strip();
                write(playerFile, player);
            }
            return player;
        });
    }

    public void completeScene(Story.Scene scene) {
        locked(() -> {
            Player player = loadPlayer();
            if (scene == Story.Scene.NONE || scene != player.pendingScene()) return player;
            if (scene == Story.Scene.INTRODUCTION) {
                if (player.characterName.isBlank()) throw new IllegalStateException("A character name is required");
                player.introductionComplete = true;
            } else {
                player.endingShown = true;
            }
            write(playerFile, player);
            return player;
        });
    }

    public record BossProgress(int highestUnlocked) {
        public boolean allows(int bossNumber) { return bossNumber >= 1 && bossNumber <= highestUnlocked; }
    }

    public BossProgress progress(Category category) {
        return locked(() -> {
            JsonObject unlocks = readUnlocks(loadPlayer());
            return new BossProgress(unlocks.get(category.name()).getAsInt());
        });
    }

    private JsonObject readUnlocks(Player player) throws IOException {
        return JSON.fromJson(Files.readString(progressFile(player)), JsonObject.class);
    }

    public record Victory(boolean shardAwarded, int level) {}

    public Victory recordVictory(UUID saveId, Category category, int bossNumber) {
        return locked(() -> {
            Player player = loadPlayer();
            if (!player.saveId.equals(saveId)) throw new IOException("This battle belongs to a different save");
            JsonObject unlocks = readUnlocks(player);
            int highest = unlocks.get(category.name()).getAsInt();
            if (!new BossProgress(highest).allows(bossNumber)) throw new IllegalArgumentException("Boss is locked");
            if (bossNumber == 3 && player.defeatedFinalBosses.add(category)) {
                player.level = 1 + player.defeatedFinalBosses.size();
                write(playerFile, player); // The victory and its level are one atomic write.
                return new Victory(true, player.level);
            }
            if (bossNumber == highest && highest < 3) {
                unlocks.addProperty(category.name(), highest + 1);
                write(progressFile(player), unlocks);
            }
            return new Victory(false, player.level);
        });
    }

    private static void write(Path path, Object value) throws IOException {
        Files.createDirectories(path.getParent());
        Path pending = Files.createTempFile(path.getParent(), "save-", ".tmp");
        try {
            Files.writeString(pending, JSON.toJson(value) + System.lineSeparator());
            Files.move(pending, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(pending);
        }
    }

    private interface SaveOperation<T> { T run() throws IOException; }

    private <T> T locked(SaveOperation<T> operation) {
        // Serialize read-modify-write operations across controllers and game processes.
        synchronized (SaveManager.class) {
            try {
                Files.createDirectories(folder);
                try (FileChannel channel = FileChannel.open(folder.resolve("player.lock"),
                        StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                     FileLock lock = channel.lock()) {
                    return operation.run();
                }
            } catch (IOException e) {
                throw new UncheckedIOException("Could not access player save: " + e.getMessage(), e);
            }
        }
    }
}
