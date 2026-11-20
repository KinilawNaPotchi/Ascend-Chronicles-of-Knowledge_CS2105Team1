package Frontend.playerprofile;

import javax.swing.*;
import java.awt.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class ProfileScreenPanel extends JPanel {

    public ProfileScreenPanel(Preferences profile, Runnable onStart) {
        setLayout(new GridBagLayout());

        // TODO: SIGN-UP BACKGROUND PLACEHOLDER
        setBackground(new Color(15, 19, 49));

        JTextField usernameField = new JTextField(18);
        JComboBox<String> genderField = new JComboBox<>(
                new String[]{"Female", "Male"}
        );
        JTextField ageField = new JTextField(18);

        JPanel form = new JPanel(new GridLayout(0, 1, 0, 8));
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JLabel title = new JLabel("ASCEND", SwingConstants.CENTER);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 38));
        title.setForeground(Color.WHITE);
        form.add(title);

        JLabel subtitle = new JLabel("Chronicles of Knowledge", SwingConstants.CENTER);
        subtitle.setForeground(Color.WHITE);
        form.add(subtitle);

        JButton startButton = new JButton("Start Game");
        startButton.addActionListener(event -> {
            String username = usernameField.getText().trim();

            if (username.isEmpty() || username.length() > 24) {
                JOptionPane.showMessageDialog(this, "Enter a username with 1 to 24 characters.");
                usernameField.requestFocusInWindow();
                return;
            }

            int age;
            try {
                age = Integer.parseInt(ageField.getText().trim());
            } catch (NumberFormatException exception) {
                age = 0;
            }

            if (age < 1 || age > 120) {
                JOptionPane.showMessageDialog(this, "Enter a whole-number age from 1 to 120.");
                ageField.requestFocusInWindow();
                return;
            }

            if (!profile.get("username", "").isEmpty()) {
                int choice = JOptionPane.showConfirmDialog(
                        this,
                        "Replace your current player profile?",
                        "New Game",
                        JOptionPane.YES_NO_OPTION
                );

                if (choice != JOptionPane.YES_OPTION) {
                    return;
                }
            }

            try {
                profile.put("username", username);
                profile.put("gender", (String) genderField.getSelectedItem());
                profile.putInt("age", age);
                profile.flush();
            } catch (BackingStoreException | SecurityException exception) {
                JOptionPane.showMessageDialog(this, "The game couldn't save your profile.");
                return;
            }

            usernameField.setText("");
            genderField.setSelectedIndex(0);
            ageField.setText("");
            onStart.run();
        });

        form.add(startButton);
        add(form);
    }
}
