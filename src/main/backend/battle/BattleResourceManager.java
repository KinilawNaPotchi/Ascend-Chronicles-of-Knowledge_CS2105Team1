package backend.battle;

import backend.model.Category;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class BattleResourceManager {
    private final Map<Path, Map<Integer, JsonObject>> questionBanks = new ConcurrentHashMap<>();

    public int getQuestionID(Category category, int bossNumber, List<Integer> shownQuestionIds) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int difficulty = switch (bossNumber) {
            case 1 -> 1;
            case 2 -> random.nextInt(100) < 70 ? 2 : 1;
            case 3 -> random.nextInt(100) < 70 ? 3 : 2;
            default -> throw new IllegalArgumentException("Invalid boss number: " + bossNumber);
        };

        List<Integer> allIds = new ArrayList<>(getQuestionBank(category, difficulty).keySet());
        List<Integer> unusedIds = new ArrayList<>(allIds);
        unusedIds.removeAll(shownQuestionIds);

        List<Integer> candidates = unusedIds.isEmpty() ? allIds : unusedIds;
        return candidates.get(random.nextInt(candidates.size()));
    }

    public String getQuestion(int id) {
        return findQuestion(id).get("question").getAsString();
    }

    public String getAnswer(int id) {
        return findQuestion(id).get("answer").getAsString();
    }

    List<String> getChoices(int id) {
        JsonArray choices = findQuestion(id).getAsJsonArray("choices");
        if (choices == null) {
            return List.of();
        }

        List<String> options = new ArrayList<>();
        for (JsonElement choice : choices) {
            options.add(choice.getAsString());
        }
        return List.copyOf(options);
    }

    private JsonObject findQuestion(int id) {
        Category category = switch (id / 1000) {
            case 1 -> Category.MATHEMATICS;
            case 2 -> Category.SCIENCE;
            case 3 -> Category.PROGRAMMING;
            default -> throw new IllegalArgumentException("Invalid question ID: " + id);
        };
        int difficulty = (id / 100) % 10;
        JsonObject question = getQuestionBank(category, difficulty).get(id);
        if (question == null) {
            throw new IllegalArgumentException("No question with ID " + id);
        }
        return question;
    }

    private Map<Integer, JsonObject> getQuestionBank(Category category, int difficulty) {
        String folder = getCategoryName(category).toLowerCase(Locale.ROOT);
        String difficultyName = switch (difficulty) {
            case 1 -> "Easy";
            case 2 -> "Normal";
            case 3 -> "Difficult";
            default -> throw new IllegalArgumentException("Invalid question difficulty: " + difficulty);
        };
        Path file = Path.of("resources", "question-bank", folder, folder + difficultyName + ".json");
        return questionBanks.computeIfAbsent(file, BattleResourceManager::readQuestionBank);
    }

    private static Map<Integer, JsonObject> readQuestionBank(Path file) {
        Map<Integer, JsonObject> questions = new HashMap<>();
        try (Reader reader = Files.newBufferedReader(file)) {
            for (JsonElement entry : JsonParser.parseReader(reader).getAsJsonArray()) {
                JsonObject question = entry.getAsJsonObject();
                questions.put(question.get("id").getAsInt(), question);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + file, e);
        }
        if (questions.isEmpty()) {
            throw new IllegalStateException("No questions in " + file);
        }
        return questions;
    }

    public String getBossBeforeDialog(Category category, int bossNumber) {
        Path file = Path.of("resources", "text", "before-battle-boss-dialogue.json");
        String dialogueCategory = getCategoryName(category);
        try (Reader reader = Files.newBufferedReader(file)) {
            for (JsonElement entry : JsonParser.parseReader(reader).getAsJsonArray()) {
                JsonObject dialogue = entry.getAsJsonObject();
                if (dialogueCategory.equals(dialogue.get("category").getAsString())
                        && bossNumber == dialogue.get("bossNumber").getAsInt()) {
                    return dialogue.get("name").getAsString() + ": " + dialogue.get("text").getAsString();
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + file, e);
        }
        throw new IllegalStateException(
                "No dialogue for " + dialogueCategory + " boss " + bossNumber + " in " + file);
    }

    public String getMcBeforeDialog(Category category, int bossNumber) {
        return getMcBeforeDialog(category, bossNumber, getCharacterName());
    }

    String getCharacterName() {
        String name = new SaveManager().currentPlayer().characterName();
        return name.isBlank() ? "Main Character" : name;
    }

    String getMcBeforeDialog(Category category, int bossNumber, String characterName) {
        Path file = Path.of("resources", "text", "before-battle-mc-dialogue.json");
        String dialogueCategory = getCategoryName(category);
        try (Reader dialogueReader = Files.newBufferedReader(file)) {
            for (JsonElement entry : JsonParser.parseReader(dialogueReader).getAsJsonArray()) {
                JsonObject dialogue = entry.getAsJsonObject();
                if (dialogueCategory.equals(dialogue.get("enemyCategory").getAsString())
                        && bossNumber == dialogue.get("enemyNumber").getAsInt()) {
                    return characterName + ": " + dialogue.get("text").getAsString();
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + file, e);
        }
        throw new IllegalStateException(
                "No dialogue for " + dialogueCategory + " boss " + bossNumber + " in " + file);
    }

    List<String> getEndDialogue(Category category, String bossName, String characterName, boolean playerWon) {
        String bossFile = playerWon ? "after-battle-enemy-defeat-dialogue.json" : "boss-victory-dialogue.json";
        String characterFile = playerWon ? "after-battle-mc-dialogue.json" : "mc-defeat-dialogue.json";
        String dialogueCategory = getCategoryName(category);

        return List.of(
                bossName + ": " + findEndDialogue(bossFile, dialogueCategory, bossName),
                characterName + ": " + findEndDialogue(characterFile, dialogueCategory, bossName));
    }

    private String findEndDialogue(String fileName, String dialogueCategory, String bossName) {
        Path file = Path.of("resources", "text", fileName);
        try (Reader reader = Files.newBufferedReader(file)) {
            for (JsonElement entry : JsonParser.parseReader(reader).getAsJsonArray()) {
                JsonObject dialogue = entry.getAsJsonObject();
                if (dialogueCategory.equals(dialogue.get("enemyCategory").getAsString())
                        && bossName.equals(dialogue.get("enemyName").getAsString())) {
                    return dialogue.get("text").getAsString();
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + file, e);
        }
        throw new IllegalStateException("No dialogue for " + dialogueCategory + " boss " + bossName + " in " + file);
    }

    private static String getCategoryName(Category category) {
        return switch (category) {
            case MATHEMATICS -> "Math";
            case SCIENCE -> "Science";
            case PROGRAMMING -> "Programming";
        };
    }
}
