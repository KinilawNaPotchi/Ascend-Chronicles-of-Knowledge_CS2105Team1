
package Frontend.titlescreen;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.logging.Level;
import java.util.logging.Logger;
import Frontend.utils.FontManager;

public class TitleScreenPanel extends JPanel {

    private static final Logger LOGGER =
            Logger.getLogger(TitleScreenPanel.class.getName());

    private BufferedImage backgroundImage;
    private BufferedImage titleImage;

    public TitleScreenPanel(Runnable onAdvance) {

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        // LOAD IMAGES
        loadBackgroundImage();
        loadTitleImage();

        // GAME TITLE IMAGE
        JLabel title = new JLabel();
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setVerticalAlignment(SwingConstants.CENTER);

        if (titleImage != null) {

            Image scaledTitle = titleImage.getScaledInstance(
                    1000,
                    -1,
                    Image.SCALE_SMOOTH
            );

            title.setIcon(new ImageIcon(scaledTitle));

        } else {
            LOGGER.warning("Title image is not available.");
        }

        add(title, BorderLayout.CENTER);

    // TAP ANYWHERE PROMPT
        JLabel prompt = new JLabel("TAP ANYWHERE...", SwingConstants.RIGHT);

        prompt.setFont(FontManager.bold(20f));
        prompt.setForeground(Color.BLACK);

        add(prompt, BorderLayout.SOUTH);

        // MOUSE CLICK EVENT
        MouseAdapter advanceOnClick = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (SwingUtilities.isLeftMouseButton(event)) {
                    onAdvance.run();
                }
            }
        };

        addMouseListener(advanceOnClick);
        title.addMouseListener(advanceOnClick);
        prompt.addMouseListener(advanceOnClick);

        // KEYBOARD EVENTS
        InputMap keys = getInputMap(WHEN_IN_FOCUSED_WINDOW);

        keys.put(KeyStroke.getKeyStroke("ENTER"), "advance");
        keys.put(KeyStroke.getKeyStroke("SPACE"), "advance");

        getActionMap().put("advance", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                onAdvance.run();
            }
        });
    }

    // LOAD BACKGROUND IMAGE
    private void loadBackgroundImage() {
        try {

            File imageFile = new File(
                    "resources/assets/backgrounds/openingBg.png"
            );

            if (imageFile.exists()) {
                backgroundImage = ImageIO.read(imageFile);
            } else {
                LOGGER.warning("Background image not found: "
                        + imageFile.getAbsolutePath());
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load background image.", e);
        }
    }

    // LOAD GAME TITLE IMAGE
    private void loadTitleImage() {
        try {

            File imageFile = new File(
                    "resources/assets/placeholders/GameTitle.png"
            );

            if (imageFile.exists()) {
                titleImage = ImageIO.read(imageFile);

                if (titleImage != null) {
                    LOGGER.info("Title image loaded successfully.");
                    LOGGER.info("Original width: " + titleImage.getWidth());
                    LOGGER.info("Original height: " + titleImage.getHeight());
                } else {
                    LOGGER.warning("Invalid title image format.");
                }

            } else {
                LOGGER.warning("Title image not found: "
                        + imageFile.getAbsolutePath());
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load title image.", e);
        }
    }

    // DRAW BACKGROUND IMAGE
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (backgroundImage == null) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();

        int panelWidth = getWidth();
        int panelHeight = getHeight();

        double scaleX =
                (double) panelWidth / backgroundImage.getWidth();

        double scaleY =
                (double) panelHeight / backgroundImage.getHeight();

        // COVER THE ENTIRE PANEL
        double scale = Math.max(scaleX, scaleY);

        int scaledWidth =
                (int) (backgroundImage.getWidth() * scale);

        int scaledHeight =
                (int) (backgroundImage.getHeight() * scale);

        int x = (panelWidth - scaledWidth) / 2;
        int y = (panelHeight - scaledHeight) / 2;

        g2.drawImage(
                backgroundImage,
                x,
                y,
                scaledWidth,
                scaledHeight,
                null
        );

        g2.dispose();
    }
}
