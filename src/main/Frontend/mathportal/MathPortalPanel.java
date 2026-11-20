package Frontend.mathportal;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.function.IntConsumer;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class MathPortalPanel extends JPanel {
    public static final String PROGRESS_KEY = "mathBossesDefeated";

    private static final String ART_PATH = "/assets/ui/math-portal/";
    private static final Color SKY = new Color(15, 19, 49);
    private static final Color SILHOUETTE = new Color(24, 25, 43);

    private final Preferences profile;
    private final BossMap map;
    private final Timer floatTimer;
    private int defeated;
    private double animationTime;

    public MathPortalPanel(Preferences profile, Runnable onBack, IntConsumer onBossSelected) {
        this.profile = profile;

        setLayout(new BorderLayout());
        setBackground(SKY);
        setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        JLabel title = new JLabel("Math Dimension", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 34f));
        add(title, BorderLayout.NORTH);

        map = new BossMap(onBossSelected);
        add(map, BorderLayout.CENTER);

        JButton back = new JButton("Back");
        back.addActionListener(event -> onBack.run());

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        footer.setOpaque(false);
        footer.add(back);
        add(footer, BorderLayout.SOUTH);

        floatTimer = new Timer(32, event -> {
            animationTime += 0.045;
            map.positionPlatforms();
            map.repaint();
        });

        // Animate only while the player is looking at this portal.
        addHierarchyListener(event -> {
            if (isShowing()) {
                refreshProgress();
                floatTimer.start();
            } else {
                floatTimer.stop();
            }
        });

        refreshProgress();
    }

    private void refreshProgress() {
        defeated = Math.max(0, Math.min(3, profile.getInt(PROGRESS_KEY, 0)));
        map.updateBosses();
    }

    public void focusCurrentBoss() {
        refreshProgress();
        int index = Math.min(defeated, 2);
        SwingUtilities.invokeLater(map.bosses[index]::requestFocusInWindow);
    }

    // Call this only after the battle reports a victory.
    // bossNumber is 1, 2, or 3.
    public void recordBossVictory(int bossNumber) {
        refreshProgress();

        if (bossNumber < 1 || bossNumber > 3 || bossNumber != defeated + 1) {
            return;
        }

        profile.putInt(PROGRESS_KEY, bossNumber);
        try {
            profile.flush();
        } catch (BackingStoreException exception) {
            JOptionPane.showMessageDialog(this,
                    "Your victory counts for this session, but the game could not confirm the save.");
        }

        refreshProgress();
    }

    private class BossMap extends JPanel {
        private final BossButton[] bosses = new BossButton[3];

        // MATH BACKGROUND PLACEHOLDER: Endrea can add math-background.png here.
        private final ImageIcon background = loadArt("math-background.png");

        BossMap(IntConsumer onBossSelected) {
            setLayout(null);
            setBackground(SKY);

            for (int index = 0; index < bosses.length; index++) {
                int bossNumber = index + 1;
                BossButton boss = new BossButton(bossNumber);
                boss.addActionListener(event -> onBossSelected.accept(bossNumber));
                bosses[index] = boss;
                add(boss);
            }

            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent event) {
                    positionPlatforms();
                }
            });
        }

        void updateBosses() {
            for (BossButton boss : bosses) {
                boolean revealed = boss.number <= defeated + 1;
                boolean cleared = boss.number <= defeated;

                boss.setEnabled(revealed);
                String label = revealed ? "Boss " + boss.number : "Hidden boss";
                if (cleared) {
                    label += " (cleared)";
                }
                boss.getAccessibleContext().setAccessibleName(label);
                boss.setToolTipText(revealed ? label : "Defeat the previous boss to reveal this one.");
            }
            repaint();
        }

        void positionPlatforms() {
            int width = getWidth();
            int height = getHeight();
            int size = Math.min(190, Math.min(width / 4, height / 2));
            if (size < 1) {
                return;
            }

            for (int index = 0; index < bosses.length; index++) {
                double xFraction = 0.18 + index * 0.32;
                double yFraction = index == 1 ? 0.34 : 0.66;
                int bob = (int) Math.round(5 * Math.sin(animationTime + index * 1.6));

                int x = (int) (width * xFraction) - size / 2;
                int y = (int) (height * yFraction) - size / 2 + bob;
                bosses[index].setBounds(x, y, size, size);
            }
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();

            if (background != null) {
                g.drawImage(background.getImage(), 0, 0, getWidth(), getHeight(), this);
            }

            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g.setStroke(new BasicStroke(3f));

            for (int index = 0; index < bosses.length - 1; index++) {
                Rectangle from = bosses[index].getBounds();
                Rectangle to = bosses[index + 1].getBounds();

                g.setColor(defeated > index
                        ? new Color(157, 183, 255)
                        : new Color(83, 88, 126));

                g.drawLine(from.x + from.width / 2, from.y + from.height * 2 / 3,
                        to.x + to.width / 2, to.y + to.height * 2 / 3);
            }
            g.dispose();
        }
    }

    private class BossButton extends JButton {
        private final int number;
        private final ImageIcon portrait;
        private final BufferedImage shadow;

        BossButton(int number) {
            this.number = number;

            // BOSS ART PLACEHOLDERS: use transparent PNGs so the silhouette
            // follows each boss's outline.
            portrait = loadArt("boss-" + number + ".png");
            shadow = makeSilhouette(portrait);

            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setRolloverEnabled(true);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g.scale(getWidth() / 180.0, getHeight() / 180.0);

            boolean revealed = number <= defeated + 1;
            boolean cleared = number <= defeated;

            // Floating island: pointed rock underneath an oval platform.
            Path2D rock = new Path2D.Double();
            rock.moveTo(20, 111);
            rock.lineTo(44, 137);
            rock.lineTo(68, 132);
            rock.lineTo(88, 150);
            rock.lineTo(111, 134);
            rock.lineTo(132, 138);
            rock.lineTo(160, 111);
            rock.closePath();
            g.setColor(new Color(62, 56, 103));
            g.fill(rock);

            g.setColor(new Color(130, 127, 184));
            g.fillOval(19, 96, 142, 32);
            g.setColor(cleared ? new Color(121, 232, 182) : new Color(189, 199, 255));
            g.drawOval(19, 96, 142, 32);

            if (portrait == null) {
                drawPlaceholderBoss(g, number, revealed);
            } else {
                Image image = revealed ? portrait.getImage() : shadow;
                double scale = Math.min(90.0 / portrait.getIconWidth(),
                        84.0 / portrait.getIconHeight());
                int w = (int) Math.round(portrait.getIconWidth() * scale);
                int h = (int) Math.round(portrait.getIconHeight() * scale);
                g.drawImage(image, (180 - w) / 2, 107 - h, w, h, this);
            }

            String label = revealed ? "Boss " + number : "???";
            if (cleared) {
                label += " • Cleared";
            }
            g.setFont(getFont().deriveFont(Font.BOLD, 14f));
            g.setColor(Color.WHITE);
            int textWidth = g.getFontMetrics().stringWidth(label);
            g.drawString(label, (180 - textWidth) / 2, 169);

            if (isFocusOwner() || (isEnabled() && getModel().isRollover())) {
                g.setColor(new Color(251, 209, 109));
                g.setStroke(new BasicStroke(2f));
                g.drawRoundRect(4, 4, 171, 171, 18, 18);
            }
            g.dispose();
        }
    }

    private static void drawPlaceholderBoss(Graphics2D g, int number, boolean revealed) {
        // Temporary shapes: a crystal, a horned guardian, and a crowned guardian.
        int[][] outlines = {
                {90, 30, 117, 68, 104, 104, 76, 104, 63, 68},
                {55, 36, 78, 55, 102, 55, 125, 36, 116, 83, 105, 105, 75, 105, 64, 83},
                {52, 48, 75, 59, 90, 31, 105, 59, 128, 48, 114, 102, 66, 102}
        };
        Color[] colors = {
                new Color(120, 218, 246),
                new Color(185, 145, 248),
                new Color(248, 190, 98)
        };

        int[] points = outlines[number - 1];
        Polygon body = new Polygon();
        for (int i = 0; i < points.length; i += 2) {
            body.addPoint(points[i], points[i + 1]);
        }

        g.setColor(revealed ? colors[number - 1] : SILHOUETTE);
        g.fillPolygon(body);
        g.setColor(revealed ? Color.WHITE : new Color(102, 104, 136));
        g.drawPolygon(body);

        if (revealed) {
            g.setColor(SKY);
            g.fillRect(77, 75, 7, 5);
            g.fillRect(96, 75, 7, 5);
        }
    }

    private static ImageIcon loadArt(String filename) {
        URL resource = MathPortalPanel.class.getResource(ART_PATH + filename);
        if (resource == null) {
            return null;
        }

        ImageIcon icon = new ImageIcon(resource);
        return icon.getIconWidth() > 0 && icon.getIconHeight() > 0 ? icon : null;
    }

    private static BufferedImage makeSilhouette(ImageIcon portrait) {
        if (portrait == null) {
            return null;
        }

        BufferedImage result = new BufferedImage(
                portrait.getIconWidth(), portrait.getIconHeight(), BufferedImage.TYPE_INT_ARGB
        );
        Graphics2D g = result.createGraphics();
        g.drawImage(portrait.getImage(), 0, 0, null);
        g.setComposite(AlphaComposite.SrcIn);
        g.setColor(SILHOUETTE);
        g.fillRect(0, 0, result.getWidth(), result.getHeight());
        g.dispose();
        return result;
    }
}