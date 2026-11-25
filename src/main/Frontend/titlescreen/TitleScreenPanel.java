package Frontend.titlescreen;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TitleScreenPanel extends JPanel {

    public TitleScreenPanel(Runnable onAdvance) {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        // TODO: TITLE SCREEN BACKGROUND PLACEHOLDER
        // Use the navy background until Endrea's artwork is ready.
        setBackground(new Color(15, 19, 49));

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

        JLabel prompt = new JLabel("Tap anywhere", SwingConstants.RIGHT);
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
}
