package Frontend.titlescreen;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class TitleScreenPanel extends JPanel {

    private BufferedImage backgroundImage;

    public TitleScreenPanel(Runnable onAdvance) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        loadBackgroundImage();

        JLabel title = new JLabel(
                "<html><div style='text-align:center'>"
                        + "<span style='font-size:44pt'>ASCEND</span><br>"
                        + "<span style='font-size:20pt'>Chronicles of Knowledge</span>"
                        + "</div></html>",
                SwingConstants.CENTER
        );

        title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 32));
        title.setForeground(Color.WHITE);
        add(title, BorderLayout.CENTER);

        JLabel prompt = new JLabel(
                "Tap anywhere",
                SwingConstants.RIGHT
        );

        prompt.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        prompt.setForeground(new Color(204, 210, 255));
        add(prompt, BorderLayout.SOUTH);

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

    private void loadBackgroundImage() {
        try {
            File imageFile = new File(
                    "resources/assets/backgrounds/openingBg.png"
            );

            backgroundImage = ImageIO.read(imageFile);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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