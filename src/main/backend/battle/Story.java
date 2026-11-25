package backend.battle;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Story {
    public enum Scene { INTRODUCTION, ENDING, NONE }
    public record Line(String text, boolean asksForName) {}

    public static List<Line> lines(Scene scene) {
        List<Line> lines = new ArrayList<>();
        if (scene == Scene.NONE) return lines;
        Path file = Path.of("resources", "text", "game-dialogue.json");
        try (Reader reader = Files.newBufferedReader(file)) {
            var entries = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonArray("dialogues");
            List<String> types = scene == Scene.INTRODUCTION ? List.of("Opening", "Introduction") : List.of("Ending");
            for (String type : types) {
                for (JsonElement entry : entries) {
                    JsonObject line = entry.getAsJsonObject();
                    if (type.equals(line.get("dialogueType").getAsString())) {
                        boolean asks = type.equals("Introduction") && line.get("dialogueId").getAsString().equals("D009");
                        lines.add(new Line(line.get("text").getAsString(), asks));
                    }
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not load " + file, e);
        }
        if (lines.isEmpty()) throw new IllegalStateException("No dialogue for " + scene);
        return List.copyOf(lines);
    }
}
