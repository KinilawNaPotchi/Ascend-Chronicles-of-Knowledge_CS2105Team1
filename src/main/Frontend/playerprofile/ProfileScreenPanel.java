package Frontend.playerprofile;

import javax.swing.*;
import java.awt.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class ProfileScreenPanel extends JPanel {
    private final JTextField usernameField = new JTextField(20);
    private final JComboBox<String> genderField = new JComboBox<>(
            new String[]{"Female", "Male"}
    );
    private final JTextField ageField = new JTextField(20);

    public ProfileScreenPanel(Preferences profile, Runnable onStart, Runnable onBack) {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // TODO: SIGN-UP BACKGROUND PLACEHOLDER
        setBackground(new Color(15, 19, 49));

        JPanel page = new JPanel();
        page.setOpaque(false);
        page.setLayout(new BoxLayout(page, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("ASCEND");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 48));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Chronicles of Knowledge");
        subtitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
        subtitle.setForeground(Color.WHITE);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setAlignmentX(Component.CENTER_ALIGNMENT);

        addField(form, 0, "Username", usernameField);
        addField(form, 1, "Gender", genderField);
        addField(form, 2, "Age", ageField);

        JButton startButton = new JButton("Start Game");
        startButton.addActionListener(event -> saveProfile(profile, onStart));
        ageField.addActionListener(event -> startButton.doClick());

        JButton backButton = new JButton("Back");
        backButton.addActionListener(event -> onBack.run());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttons.setOpaque(false);
        buttons.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Returning players can go back without replacing their saved profile.
        if (!profile.get("username", "").isEmpty()) {
            buttons.add(backButton);
        }
        buttons.add(startButton);

        page.add(title);
        page.add(subtitle);
        page.add(Box.createVerticalStrut(24));
        page.add(form);
        page.add(Box.createVerticalStrut(20));
        page.add(buttons);
        add(page);
    }

    private void addField(JPanel form, int row, String text, JComponent field) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setLabelFor(field);

        GridBagConstraints labelPosition = new GridBagConstraints();
        labelPosition.gridx = 0;
        labelPosition.gridy = row * 2;
        labelPosition.anchor = GridBagConstraints.LINE_START;
        labelPosition.insets = new Insets(8, 0, 4, 0);
        form.add(label, labelPosition);

        GridBagConstraints fieldPosition = new GridBagConstraints();
        fieldPosition.gridx = 0;
        fieldPosition.gridy = row * 2 + 1;
        fieldPosition.weightx = 1;
        fieldPosition.fill = GridBagConstraints.HORIZONTAL;
        form.add(field, fieldPosition);
    }

    public void focusUsername() {
        SwingUtilities.invokeLater(usernameField::requestFocusInWindow);
    }

    private void saveProfile(Preferences profile, Runnable onStart) {
        String username = usernameField.getText().trim();

        if (username.isEmpty() || username.length() > 24) {
            JOptionPane.showMessageDialog(this, "Enter a username with 1 to 24 characters.");
            focusUsername();
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
    }
}