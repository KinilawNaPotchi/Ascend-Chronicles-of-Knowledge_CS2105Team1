package backend.battle;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class BattleManager {

    public String bossBeforeDialog() {
        Path file = Path.of("resources", "text", "game-dialogue.json");
        try (Reader reader = Files.newBufferedReader(file)) {
            JsonObject gameDialogue = JsonParser.parseReader(reader).getAsJsonObject();
            for (JsonElement entry : gameDialogue.getAsJsonArray("dialogues")) {
                JsonObject dialogue = entry.getAsJsonObject();
                if ("D011".equals(dialogue.get("dialogueId").getAsString())) {
                    return dialogue.get("text").getAsString();
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + file, e);
        }
        throw new IllegalStateException("Dialogue D011 is missing from " + file);
    }
}
