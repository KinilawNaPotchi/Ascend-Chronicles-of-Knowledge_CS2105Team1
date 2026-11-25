package Frontend.mainmenu;

import Frontend.mathportal.MathPortalPanel;
import Frontend.playerprofile.ProfileScreenPanel;
import Frontend.titlescreen.TitleScreenPanel;
import Frontend.portalselection.PortalSelectionPanel;
import Frontend.utils.FontManager;

import javax.swing.*;
import java.awt.*;
import java.util.prefs.Preferences;

public class MainMenu extends JFrame {

    private static final Color MENU_BACKGROUND = new Color(19, 24, 46);
    private static final Color TITLE_BACKGROUND = new Color(15, 19, 49);

    private final CardLayout screens = new CardLayout();
    private final JPanel content = new JPanel(screens);

    private final JButton playButton = new JButton("Play");
    private final JButton mathButton = new JButton("Mathematics");
    private final JButton continueButton = new JButton("Continue");

    private final Preferences profile = Preferences.userRoot()
            .node("ascend/chronicles-of-knowledge/player");

    private final ProfileScreenPanel signupPanel;
    private final MathPortalPanel mathPortalPanel;

    public MainMenu() {
        super("Ascend: Chronicles of Knowledge");

        signupPanel = new ProfileScreenPanel(
                profile,
                this::showMainMenu,
                this::openPlayerScreen
        );

        mathPortalPanel = new MathPortalPanel(
                profile,
                this::showPortalSelection,
                this::selectMathBoss
        );

        content.add(new TitleScreenPanel(this::openPlayerScreen), "title");
        content.add(createSavedPlayerScreen(), "saved-player");
        content.add(signupPanel, "signup");
        content.add(createMainMenu(), "menu");
        content.add(createPortalScreen(), "portals");
        content.add(mathPortalPanel, "math");

        setContentPane(content);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(720, 420));
        setSize(960, 540);
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        screens.show(content, "title");
    }

    // Called when the player taps the title screen.
    private void openPlayerScreen() {
        String username = profile.get("username", "").trim();

        if (username.isEmpty()) {
            showSignupScreen();
        } else {
            screens.show(content, "saved-player");
            getRootPane().setDefaultButton(continueButton);
            SwingUtilities.invokeLater(continueButton::requestFocusInWindow);
        }
    }

    private void showSignupScreen() {
        screens.show(content, "signup");
        getRootPane().setDefaultButton(null);
        SwingUtilities.invokeLater(signupPanel::focusUsername);
    }

    private JPanel createSavedPlayerScreen() {
        JPanel screen = new JPanel(new GridBagLayout());

        // SAVED_PLAYER_BACKGROUND: Andeng's saved-player artwork can go here.
        screen.setBackground(TITLE_BACKGROUND);

        JPanel options = new JPanel();
        options.setOpaque(false);
        options.setLayout(new BoxLayout(options, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("ASCEND");
        title.setFont(FontManager.bold(48f));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Chronicles of Knowledge");
        subtitle.setFont(FontManager.regular(20f));
        subtitle.setForeground(Color.WHITE);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        continueButton.addActionListener(event -> showMainMenu());

        JButton newGameButton = new JButton("New Game");
        newGameButton.addActionListener(event -> showSignupScreen());

        JPanel buttons = new JPanel(new GridLayout(0, 1, 0, 12));
        buttons.setOpaque(false);
        buttons.setAlignmentX(Component.CENTER_ALIGNMENT);
        buttons.setPreferredSize(new Dimension(220, 90));
        buttons.setMaximumSize(new Dimension(220, 90));
        buttons.add(continueButton);
        buttons.add(newGameButton);

        options.add(title);
        options.add(subtitle);
        options.add(Box.createVerticalStrut(28));
        options.add(buttons);

        screen.add(options);
        return screen;
    }

    // Loads the design from MainMenuPanel.java.
    // Clicking Play calls showPortalSelection().
    private JPanel createMainMenu() {
        return new MainMenuPanel(
                profile,
                playButton,
                this::showPortalSelection
        );
    }

    private void showMainMenu() {
        screens.show(content, "menu");
        getRootPane().setDefaultButton(playButton);
        SwingUtilities.invokeLater(playButton::requestFocusInWindow);
    }

    private JPanel createPortalScreen() {
        return new PortalSelectionPanel(
                mathButton,
                this::showMathPortal,
                () -> JOptionPane.showMessageDialog(this, "The Science portal is coming soon."),
                () -> JOptionPane.showMessageDialog(this, "The Programming portal is coming soon."),
                this::showMainMenu
        );
    }

    private void showPortalSelection() {
        screens.show(content, "portals");
        getRootPane().setDefaultButton(null);
        SwingUtilities.invokeLater(mathButton::requestFocusInWindow);
    }

    private void showMathPortal() {
        screens.show(content, "math");
        getRootPane().setDefaultButton(null);
        SwingUtilities.invokeLater(mathPortalPanel::focusCurrentBoss);
    }

    private void selectMathBoss(int bossNumber) {
        // MATH_BATTLE: Open the selected boss battle here.
        // Record a victory only after the battle screen reports a win.
        JOptionPane.showMessageDialog(
                this,
                "Selected Math Boss " + bossNumber,
                "Mathematics Portal",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            // Apply Pixelify Sans throughout the game
            FontManager.applyGlobalFont();

            new MainMenu().setVisible(true);
        });
    }
}
