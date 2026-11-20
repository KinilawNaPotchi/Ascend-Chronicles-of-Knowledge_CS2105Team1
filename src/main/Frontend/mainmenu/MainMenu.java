package Frontend.mainmenu;

import Frontend.titlescreen.TitleScreenPanel;
import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class MainMenu extends JFrame {
    private static final Color BACKGROUND = new Color(19, 24, 46);
    private static final String ART_PATH = "/assets/ui/main-menu/";

    private final CardLayout screens = new CardLayout();
    private final JPanel content = new JPanel(screens);
    private final JButton playButton = new JButton("Play");
    private final JButton backButton = new JButton("Back");
    private final JButton mathButton = new JButton("Mathematics");

    public MainMenu() {
        super("Ascend: Chronicles of Knowledge");

        content.add(createMainMenu(), "menu");
        content.add(new TitleScreenPanel(this::showMainMenu), "title");
        content.add(createPortalScreen(), "portals");
        setContentPane(content);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(720, 420));
        setSize(960, 540);
        setLocationRelativeTo(null);
        screens.show(content, "title");
    }

    private JPanel createMainMenu() {
        JPanel menu = new MenuBackground();
        menu.setLayout(new BorderLayout(24, 24));
        menu.setBorder(BorderFactory.createEmptyBorder(28, 32, 32, 32));

        JLabel title = new JLabel("Ascend: Chronicles of Knowledge", SwingConstants.CENTER);
        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 32));
        title.setForeground(Color.WHITE);
        menu.add(title, BorderLayout.NORTH);

        JPanel characterArea = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        characterArea.setOpaque(false);
        ImageIcon character = loadImage("menu-character.png");
        JLabel characterLabel = new JLabel("Character artwork goes here");
        characterLabel.setForeground(Color.WHITE);

        if (character != null) {
            characterLabel.setText("");
            characterLabel.setIcon(new ImageIcon(
                    character.getImage().getScaledInstance(-1, 180, Image.SCALE_FAST)
            ));
        }

        characterArea.add(characterLabel);
        menu.add(characterArea, BorderLayout.SOUTH);

        JPanel rightSide = new JPanel(new BorderLayout());
        rightSide.setOpaque(false);
        rightSide.setPreferredSize(new Dimension(220, 0));

        JPanel progressArea = new JPanel(new BorderLayout(0, 8));
        progressArea.setOpaque(false);

        JLabel progressLabel = new JLabel("Game Progress");
        progressLabel.setForeground(Color.WHITE);
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
        progressLabel.setLabelFor(progressBar);

        progressArea.add(progressLabel, BorderLayout.NORTH);
        progressArea.add(progressBar, BorderLayout.CENTER);

        JPanel progressCenter = new JPanel(new GridBagLayout());
        progressCenter.setOpaque(false);
        GridBagConstraints progressPosition = new GridBagConstraints();
        progressPosition.weightx = 1;
        progressPosition.fill = GridBagConstraints.HORIZONTAL;
        progressCenter.add(progressArea, progressPosition);
        rightSide.add(progressCenter, BorderLayout.CENTER);

        playButton.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        playButton.setPreferredSize(new Dimension(220, 48));
        playButton.addActionListener(event -> {
            screens.show(content, "portals");
            getRootPane().setDefaultButton(null);
            mathButton.requestFocusInWindow();
        });

        // Keep Play on the right while the character stays on the left.
        characterArea.setLayout(new BorderLayout(24, 0));
        characterArea.add(playButton, BorderLayout.EAST);
        menu.add(rightSide, BorderLayout.EAST);

        return menu;
    }

    private JPanel createPortalScreen() {
        JPanel screen = new JPanel(new GridBagLayout());
        screen.setBackground(BACKGROUND);

        JPanel choices = new JPanel(new GridLayout(0, 1, 0, 12));
        choices.setOpaque(false);

        JLabel heading = new JLabel("Choose Your Portal", SwingConstants.CENTER);
        heading.setForeground(Color.WHITE);
        heading.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
        choices.add(heading);

        choices.add(mathButton);
        choices.add(new JButton("Science"));
        choices.add(new JButton("Programming"));

        for (int index = 1; index <= 3; index++) {
            JButton portalButton = (JButton) choices.getComponent(index);
            portalButton.addActionListener(event -> JOptionPane.showMessageDialog(
                    this, portalButton.getText() + " levels will go here."
            ));
        }

        backButton.addActionListener(event -> showMainMenu());
        choices.add(backButton);
        screen.add(choices);
        return screen;
    }

    private void showMainMenu() {
        screens.show(content, "menu");
        getRootPane().setDefaultButton(playButton);
        SwingUtilities.invokeLater(() -> playButton.requestFocusInWindow());
    }

    private static ImageIcon loadImage(String filename) {
        URL resource = MainMenu.class.getResource(ART_PATH + filename);
        return resource == null ? null : new ImageIcon(resource);
    }

    private static class MenuBackground extends JPanel {
        private final ImageIcon artwork = loadImage("menu-background.png");

        MenuBackground() {
            setBackground(BACKGROUND);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);

            if (artwork != null) {
                graphics.drawImage(artwork.getImage(), 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainMenu().setVisible(true));
    }
}