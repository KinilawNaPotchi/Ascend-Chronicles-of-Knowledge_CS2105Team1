package terminalGame;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

public class PlayerAccount {
    private static final Path SAVE_FILE = Path.of("saves", "player.json");

    public static boolean accountExists() {
        return Files.isRegularFile(SAVE_FILE);
    }

    public static void createAccount(String username, int age, String characterName) throws IOException {
        createAccount(username, age, characterName, false);
    }

    static void createAccount(String username, int age, String characterName, boolean replaceExistingSave)
            throws IOException {
        String json = "{\n"
                + "  \"username\": \"" + escapeJson(username) + "\",\n"
                + "  \"age\": " + age + ",\n"
                + "  \"characterName\": \"" + escapeJson(characterName) + "\"\n"
                + "}\n";

        Files.createDirectories(SAVE_FILE.getParent());
        if (!replaceExistingSave) {
            Files.writeString(SAVE_FILE, json, StandardOpenOption.CREATE_NEW);
            return;
        }

        Path pendingSave = Files.createTempFile(SAVE_FILE.getParent(), "player-", ".json");
        try {
            Files.writeString(pendingSave, json);
            Files.move(pendingSave, SAVE_FILE, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(pendingSave);
        }
    }

    private static String escapeJson(String value) {
        StringBuilder escaped = new StringBuilder();

        for (char character : value.toCharArray()) {
            if (character == '"' || character == '\\') {
                escaped.append('\\').append(character);
            } else if (character < ' ') {
                escaped.append(String.format("\\u%04x", (int) character));
            } else {
                escaped.append(character);
            }
        }

        return escaped.toString();
    }
}
