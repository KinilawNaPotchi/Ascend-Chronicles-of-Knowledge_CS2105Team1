package Frontend.mainmenu;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.prefs.Preferences;

public final class MainMenuPanel extends JPanel {
    private static final String ART = "/assets/ui/main-menu/";

    // ANDENG_PLAYER_PORTRAIT
    // Add Andeng's portrait to resources/assets/ui/main-menu/player-portrait.png
    private final BufferedImage portrait = loadImage(ART + "player-portrait.png");

    // ANDENG_MENU_BACKGROUND
    // Add Andeng's background to resources/assets/ui/main-menu/menu-background.png
    private final BufferedImage background = loadImage(ART + "menu-background.png");

    private final BufferedImage character = loadImage(ART + "menu-character.png");

    // KAREN_ABOUT_US: Replace this text with Karen's finished introduction.
    private static final String ABOUT_US =
            "Ascend: Chronicles of Knowledge is an educational adventure game.";

    // TEAM_POLICIES: Replace this text with your team's policies.
    private static final String POLICIES =
            "The team will add the game policies here.";

    private final Preferences settings = Preferences.userRoot()
            .node("ascend/chronicles-of-knowledge/settings");

    private final JLabel username = new JLabel();
    private Clip music;
    private Clip click;
    private boolean audioLoaded;

    public MainMenuPanel(Preferences profile, JButton playButton, Runnable onPlay) {
        setLayout(new BorderLayout(16, 16));
        setBackground(new Color(23, 29, 45));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Upper left: black portrait placeholder and read-only username.
        JLabel portraitSlot = new JLabel();
        portraitSlot.setOpaque(true);
        portraitSlot.setBackground(Color.BLACK);
        portraitSlot.setPreferredSize(new Dimension(48, 48));

        if (portrait != null) {
            Image scaled = portrait.getScaledInstance(48, 48, Image.SCALE_REPLICATE);
            portraitSlot.setIcon(new ImageIcon(scaled));
        }

        username.setForeground(Color.WHITE);
        username.setFont(new Font(Font.MONOSPACED, Font.BOLD, 17));
        username.putClientProperty("html.disable", true);

        JPanel player = row(FlowLayout.LEFT);
        player.add(portraitSlot);
        player.add(username);

        // Upper right: settings.
        JButton settingsButton = new JButton("Settings");
        settingsButton.addActionListener(event -> {
            playClick();
            showSettings();
        });

        JPanel actions = row(FlowLayout.RIGHT);
        actions.add(settingsButton);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(player, BorderLayout.CENTER);
        top.add(actions, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        JLabel title = new JLabel(
                "Ascend: Chronicles of Knowledge", SwingConstants.CENTER
        );
        title.setFont(new Font(Font.MONOSPACED, Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        add(title, BorderLayout.CENTER);

        // Bottom left: About Us. Bottom right: Play.
        JButton aboutButton = new JButton("About Us");
        aboutButton.addActionListener(event -> {
            playClick();
            showText(this, "About Us", ABOUT_US);
        });

        playButton.addActionListener(event -> {
            playClick();
            onPlay.run();
        });

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.add(aboutButton, BorderLayout.WEST);
        footer.add(playButton, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);

        // Read the saved username each time the menu opens, including after sign-up.
        addHierarchyListener(event -> {
            if ((event.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (isShowing()) {
                    username.setText(profile.get("username", ""));
                    loadAudio();
                }
                updateMusic();
            }
        });
    }

    private static JPanel row(int alignment) {
        JPanel panel = new JPanel(new FlowLayout(alignment, 8, 0));
        panel.setOpaque(false);
        return panel;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g = (Graphics2D) graphics.create();

        g.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        );

        if (background != null) {
            g.drawImage(background, 0, 0, getWidth(), getHeight(), null);
        }

        if (character != null) {
            int height = Math.min(150, getHeight() / 4);
            int width = character.getWidth() * height / character.getHeight();
            g.drawImage(character, 24, getHeight() - height - 64, width, height, null);
        }

        g.dispose();
    }

    private void showSettings() {
        JPanel options = new JPanel(new GridLayout(0, 1, 0, 10));
        options.add(audioSwitch("Music", "musicEnabled"));
        options.add(audioSwitch("Sound effects", "soundEffectsEnabled"));

        JButton policiesButton = new JButton("Policies");
        policiesButton.addActionListener(event -> {
            playClick();
            showText(policiesButton, "Policies", POLICIES);
        });
        options.add(policiesButton);

        JOptionPane.showMessageDialog(
                this, options, "Settings", JOptionPane.PLAIN_MESSAGE
        );
    }

    private JCheckBox audioSwitch(String label, String key) {
        boolean enabled = settings.getBoolean(key, true);
        JCheckBox toggle = new JCheckBox(label + (enabled ? " on" : " off"), enabled);

        toggle.addActionListener(event -> {
            boolean on = toggle.isSelected();
            settings.putBoolean(key, on);
            toggle.setText(label + (on ? " on" : " off"));

            if ("musicEnabled".equals(key)) {
                updateMusic();
            } else if (!on && click != null) {
                click.stop();
            }

            playClick();
        });

        return toggle;
    }

    private static void showText(Component owner, String heading, String message) {
        JTextArea text = new JTextArea(message, 6, 30);
        text.setEditable(false);
        text.setLineWrap(true);
        text.setWrapStyleWord(true);
        text.setCaretPosition(0);

        JOptionPane.showMessageDialog(
                owner, new JScrollPane(text), heading, JOptionPane.PLAIN_MESSAGE
        );
    }

    private static BufferedImage loadImage(String path) {
        URL resource = MainMenuPanel.class.getResource(path);
        if (resource == null) return null;

        try {
            return ImageIO.read(resource);
        } catch (IOException error) {
            System.err.println("Could not load " + path + ": " + error.getMessage());
            return null;
        }
    }

    private void loadAudio() {
        if (audioLoaded) return;
        audioLoaded = true;

        // Add the team's WAV files to resources/assets/audio/.
        music = loadClip("/assets/audio/menu-music.wav");
        click = loadClip("/assets/audio/menu-click.wav");
    }

    private static Clip loadClip(String path) {
        URL resource = MainMenuPanel.class.getResource(path);
        if (resource == null) return null;

        Clip clip = null;
        try (AudioInputStream stream = AudioSystem.getAudioInputStream(resource)) {
            clip = AudioSystem.getClip();
            clip.open(stream);
            return clip;
        } catch (UnsupportedAudioFileException | IOException |
                 LineUnavailableException | IllegalArgumentException error) {
            if (clip != null) clip.close();
            System.err.println("Could not load " + path + ": " + error.getMessage());
            return null;
        }
    }

    private void updateMusic() {
        if (music == null) return;

        if (isShowing() && settings.getBoolean("musicEnabled", true)) {
            if (!music.isRunning()) {
                music.loop(Clip.LOOP_CONTINUOUSLY);
            }
        } else {
            music.stop();
        }
    }

    private void playClick() {
        if (click != null && settings.getBoolean("soundEffectsEnabled", true)) {
            click.stop();
            click.setFramePosition(0);
            click.start();
        }
    }

    @Override
    public void removeNotify() {
        if (music != null) music.close();
        if (click != null) click.close();
        music = null;
        click = null;
        audioLoaded = false;
        super.removeNotify();
    }
}