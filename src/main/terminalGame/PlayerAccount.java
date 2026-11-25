package terminalGame;

import backend.battle.SaveManager;
import java.io.IOException;
import java.io.UncheckedIOException;

/** Existing account API, backed by the same saves used by the campaign. */
public class PlayerAccount {
    public static boolean accountExists() {
        return new SaveManager().accountExists();
    }

    public static void createAccount(String username, int age, String characterName) throws IOException {
        createAccount(username, age, characterName, false);
    }

    static void createAccount(String username, int age, String characterName, boolean replace) throws IOException {
        try {
            new SaveManager().newGame(username, age, characterName, replace);
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }
}
