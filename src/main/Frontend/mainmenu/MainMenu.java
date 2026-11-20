package Frontend.mainmenu;

import Frontend.playerprofile.ProfileScreenPanel;
import Frontend.titlescreen.TitleScreenPanel;
import Frontend.mathportal.MathPortalPanel;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.prefs.Preferences;

public class MainMenu extends JFrame {
    private static final Color MENU_BACKGROUND = new Color(19, 24, 46);
    private static final Color TITLE_BACKGROUND = new Color(15, 19, 49);
    private static final String ART_PATH = "/assets/ui/main-menu/";

    private final CardLayout screens = new CardLayout();
    private final JPanel content = new JPanel(screens);
    private final JButton playButton = new JButton("Play");
    private final JButton mathButton = new JButton("Mathematics");
    private final JButton continueButton = new JButton("Continue");

    private final Preferences profile =
            Preferences.userRoot().node("ascend/chronicles-of-knowledge/player");

    private final ProfileScreenPanel signupPanel;
    private final MathPortalPanel mathPortalPanel;

    public MainMenu() {
        super("Ascend: Chronicles of Knowledge");

        signupPanel = new ProfileScreenPanel(
                profile, this::showMainMenu, this::openPlayerScreen
        );

        mathPortalPanel = new MathPortalPanel(
                profile,
                () -> {
                    screens.show(content, "portals");
                    SwingUtilities.invokeLater(mathButton::requestFocusInWindow);
                },
                bossNumber -> {
                    // TODO: Open the battle screen for the selected Math boss here.
                    JOptionPane.showMessageDialog(this, "Selected Math Boss " + bossNumber);
                }
        );
        content.add(new TitleScreenPanel(this::openPlayerScreen), "title");
        content.add(mathPortalPanel, "math");
        content.add(createSavedPlayerScreen(), "saved-player");
        content.add(signupPanel, "signup");
        content.add(createMainMenu(), "menu");
        content.add(createPortalScreen(), "portals");
        setContentPane(content);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(720, 420));
        setSize(960, 540);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        screens.show(content, "title");
    }

    private void openPlayerScreen() {
        if (profile.get("username", "").isEmpty()) {
            showSignupScreen();
        } else {
            screens.show(content, "saved-player");
            getRootPane().setDefaultButton(continueButton);
            continueButton.requestFocusInWindow();
        }
    }

    private void showSignupScreen() {
        screens.show(content, "signup");
        getRootPane().setDefaultButton(null);
        signupPanel.focusUsername();
    }

    private JPanel createSavedPlayerScreen() {
        JPanel screen = new JPanel(new GridBagLayout());

        // SAVED PLAYER SCREEN BACKGROUND PLACEHOLDER
        screen.setBackground(TITLE_BACKGROUND);

        JPanel options = new JPanel();
        options.setOpaque(false);
        options.setLayout(new BoxLayout(options, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("ASCEND");
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 48));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Chronicles of Knowledge");
        subtitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 20));
        subtitle.setForeground(Color.WHITE);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        continueButton.addActionListener(event -> showMainMenu());
        JButton newGameButton = new JButton("New Game");
        newGameButton.addActionListener(event -> showSignupScreen());

        JPanel buttons = new JPanel(new GridLayout(0, 1, 0, 12));
        buttons.setOpaque(false);
        buttons.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttons.setMaximumSize(new Dimension(220, 90));
        buttons.setPreferredSize(new Dimension(220, 90));
        buttons.add(continueButton);
        buttons.add(newGameButton);

        options.add(title);
        options.add(subtitle);
        options.add(Box.createVerticalStrut(28));
        options.add(buttons);
        screen.add(options);

        return screen;
    }

    private JPanel createMainMenu() {
        JPanel menu = new MenuBackground();
        menu.setLayout(new BorderLayout(24, 24));
        menu.setBorder(BorderFactory.createEmptyBorder(28, 32, 32, 32));

        JLabel title = new JLabel(
                "Ascend: Chronicles of Knowledge", SwingConstants.CENTER
        );
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 32));
        title.setForeground(Color.WHITE);
        menu.add(title, BorderLayout.NORTH);

        ImageIcon character = loadImage("menu-character.png");
        JLabel characterLabel = new JLabel("Character artwork goes here");
        characterLabel.setForeground(Color.WHITE);

        if (character != null) {
            characterLabel.setText("");
            characterLabel.setIcon(new ImageIcon(
                    character.getImage().getScaledInstance(-1, 180, Image.SCALE_FAST)
            ));
        }

        JPanel bottom = new JPanel(new BorderLayout(24, 0));
        bottom.setOpaque(false);
        bottom.add(characterLabel, BorderLayout.WEST);

        playButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        playButton.setPreferredSize(new Dimension(220, 48));
        playButton.addActionListener(event -> {
            getRootPane().setDefaultButton(null);
            screens.show(content, "portals");
            mathButton.requestFocusInWindow();
        });

        JPanel playArea = new JPanel(new BorderLayout());
        playArea.setOpaque(false);
        playArea.add(playButton, BorderLayout.SOUTH);
        bottom.add(playArea, BorderLayout.EAST);
        menu.add(bottom, BorderLayout.SOUTH);


        return menu;
    }

    private JPanel createPortalScreen() {
        JPanel screen = new JPanel(new GridBagLayout());
        screen.setBackground(MENU_BACKGROUND);

        JPanel choices = new JPanel(new GridLayout(0, 1, 0, 12));
        choices.setOpaque(false);

        JLabel heading = new JLabel("Choose Your Portal", SwingConstants.CENTER);
        heading.setForeground(Color.WHITE);
        heading.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        choices.add(heading);

        mathButton.addActionListener(event -> {
            screens.show(content, "math");
            mathPortalPanel.focusCurrentBoss();
        });
        choices.add(mathButton);

        for (String subject : new String[]{"Science", "Programming"}){
            JButton portalButton = new JButton(subject);
            portalButton.addActionListener(event -> JOptionPane.showMessageDialog(
                    this, "The" + subject + "portal is still in development."
            ));
            choices.add(portalButton);
        }

        JButton backButton = new JButton("Back");
        backButton.addActionListener(event -> showMainMenu());
        choices.add(backButton);

        screen.add(choices);
        return screen;
    }
    private void showPortalSelection() {
        getRootPane().setDefaultButton(null);
        screens.show(content, "portals");
        SwingUtilities.invokeLater(mathButton::requestFocusInWindow);
    }

    private void selectMathBoss(int bossNumber) {
        // TODO: MATH BATTLE CONNECTION
        // Open the battle for this boss here. When the player wins,
        // the battle's victory handler should call:
        // mathPortalPanel.recordBossVictory(bossNumber);
        JOptionPane.showMessageDialog(this, "Selected Math Boss " + bossNumber);
    }

    private void showMainMenu() {
        screens.show(content, "menu");
        getRootPane().setDefaultButton(playButton);
        SwingUtilities.invokeLater(playButton::requestFocusInWindow);
    }

    private static ImageIcon loadImage(String filename) {
        URL resource = MainMenu.class.getResource(ART_PATH + filename);
        return resource == null ? null : new ImageIcon(resource);
    }

    private static class MenuBackground extends JPanel {
        private final ImageIcon artwork = loadImage("menu-background.png");

        MenuBackground() {
            setBackground(MENU_BACKGROUND);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);

            if (artwork != null) {
                graphics.drawImage(
                        artwork.getImage(), 0, 0, getWidth(), getHeight(), this
                );
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainMenu().setVisible(true));
    }
}