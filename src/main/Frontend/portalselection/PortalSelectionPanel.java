package Frontend.portalselection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.net.URL;
import Frontend.utils.FontManager;

public final class PortalSelectionPanel extends JPanel {

    private static final Color BACKGROUND = new Color(15, 19, 49);
    private static final Color PATH_COLOR = new Color(78, 84, 120);
    private static final Color PLATFORM_TOP = new Color(130, 128, 180);
    private static final Color PLATFORM_EDGE = new Color(191, 197, 245);
    private static final Color PLATFORM_ROCK = new Color(61, 55, 96);
    private static final Color FOCUS_COLOR = new Color(255, 209, 86);

    private static final String ART = "/assets/ui/portal-selection/";

    // ANDENG_MAIN_BACKGROUND:
    // Add the main background artwork using this filename.
    private final Image mainBackground =
            loadImage("portal-background.png");

    // ANDENG_TITLE_BACKGROUND:
    // Add the transparent title background using this filename.
    private final Image titleBackground =
            loadImage("title-background.png");

    private final JButton[] portals;
    private final Timer floatTimer;
    private double phase;

    public PortalSelectionPanel(
            JButton mathButton,
            Runnable onMath,
            Runnable onScience,
            Runnable onProgramming,
            Runnable onBack
    ) {

        setLayout(new BorderLayout());
        setBackground(BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // ==========================================
        // PORTAL TITLE
        // ==========================================

        JLabel heading = new JLabel(
                "Choose Your Portal",
                SwingConstants.CENTER
        );

        heading.setForeground(Color.WHITE);
        heading.setFont(FontManager.bold(36f));
        heading.setOpaque(false);

        // ANDENG_TITLE_BACKGROUND:
        // Reserved space for the title artwork.
        JPanel titlePanel = new JPanel(new GridBagLayout()) {

            @Override
            protected void paintComponent(Graphics graphics) {

                super.paintComponent(graphics);

                if (titleBackground != null) {

                    Graphics2D g =
                            (Graphics2D) graphics.create();

                    g.setRenderingHint(
                            RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
                    );

                    int artWidth = Math.min(600, getWidth());
                    int artHeight = 100;

                    int x = (getWidth() - artWidth) / 2;
                    int y = (getHeight() - artHeight) / 2;

                    g.drawImage(
                            titleBackground,
                            x, y,
                            artWidth, artHeight,
                            this
                    );

                    g.dispose();
                }
            }
        };

        titlePanel.setOpaque(false);

        // Reserved title area.
        titlePanel.setPreferredSize(new Dimension(0, 150));

        titlePanel.add(heading);

        add(titlePanel, BorderLayout.NORTH);

        // ==========================================
        // PORTAL BUTTONS
        // ==========================================

        JButton scienceButton = new JButton("Science");
        JButton programmingButton = new JButton("Programming");

        portals = new JButton[]{
                mathButton,
                scienceButton,
                programmingButton
        };

        // ANDENG_PORTAL_ART:
        // Add transparent PNGs using these filenames.

        configurePortal(
                mathButton,
                "+",
                new Color(115, 213, 242),
                "mathematics.png",
                onMath
        );

        configurePortal(
                scienceButton,
                "S",
                new Color(139, 221, 167),
                "science.png",
                onScience
        );

        configurePortal(
                programmingButton,
                "<>",
                new Color(200, 167, 246),
                "programming.png",
                onProgramming
        );

        IslandMap map = new IslandMap();
        add(map, BorderLayout.CENTER);

        // ==========================================
        // BACK BUTTON
        // ==========================================

        JButton back = new JButton("Back");

        back.setFont(FontManager.regular(20f));
        back.addActionListener(event -> onBack.run());

        JPanel footer = new JPanel(
                new FlowLayout(FlowLayout.LEFT, 0, 0)
        );

        footer.setOpaque(false);
        footer.add(back);

        add(footer, BorderLayout.SOUTH);

        // ==========================================
        // FLOATING ANIMATION
        // ==========================================

        floatTimer = new Timer(40, event -> {

            phase += 0.065;

            map.doLayout();
            map.repaint();
        });

        // Animate only while this screen is visible.

        addHierarchyListener(event -> {

            if ((event.getChangeFlags()
                    & HierarchyEvent.SHOWING_CHANGED) != 0) {

                if (isShowing()) {
                    floatTimer.start();
                } else {
                    floatTimer.stop();
                }
            }
        });
    }

    // ==========================================
    // IMAGE LOADER
    // ==========================================

    private static Image loadImage(String filename) {

        URL url = PortalSelectionPanel.class
                .getResource(ART + filename);

        return url == null
                ? null
                : new ImageIcon(url).getImage();
    }

    // ==========================================
    // MAIN BACKGROUND
    // ==========================================

    @Override
    protected void paintComponent(Graphics graphics) {

        super.paintComponent(graphics);

        // ANDENG_MAIN_BACKGROUND:
        // The artwork will appear automatically
        // once portal-background.png is added.

        if (mainBackground != null) {

            Graphics2D g = (Graphics2D) graphics.create();

            g.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
            );

            g.drawImage(
                    mainBackground,
                    0, 0,
                    getWidth(),
                    getHeight(),
                    this
            );

            g.dispose();
        }
    }

    // ==========================================
    // CONFIGURE PORTALS
    // ==========================================

    private void configurePortal(
            JButton button,
            String symbol,
            Color color,
            String filename,
            Runnable action
    ) {

        button.setFont(FontManager.bold(20f));
        button.setForeground(Color.WHITE);

        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setRolloverEnabled(true);

        button.setBorder(
                BorderFactory.createEmptyBorder(4, 4, 4, 4)
        );

        button.setHorizontalTextPosition(
                SwingConstants.CENTER
        );

        button.setVerticalTextPosition(
                SwingConstants.BOTTOM
        );

        button.setIconTextGap(3);

        button.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );

        Image image = loadImage(filename);

        button.setIcon(
                new IslandIcon(symbol, color, image)
        );

        button.addActionListener(event -> action.run());
    }

    // ==========================================
    // FLOATING ISLAND MAP
    // ==========================================

    private final class IslandMap extends JPanel {

        IslandMap() {

            setLayout(null);
            setOpaque(false);

            for (JButton portal : portals) {
                add(portal);
            }
        }

        @Override
        public void doLayout() {

            int width = getWidth();
            int height = getHeight();

            int tileWidth = Math.min(190, width / 3);
            int tileHeight = Math.min(148, height);

            for (int i = 0; i < portals.length; i++) {

                // Left and right islands sit lower
                // than the middle island.

                int centerX =
                        (int) (width * (0.18 + i * 0.32));

                double row = i == 1 ? 0.32 : 0.76;

                int bob = (int) Math.round(
                        4 * Math.sin(phase + i * 1.3)
                );

                int y =
                        (int) ((height - tileHeight) * row)
                                + bob;

                portals[i].setBounds(
                        centerX - tileWidth / 2,
                        Math.max(
                                0,
                                Math.min(y, height - tileHeight)
                        ),
                        tileWidth,
                        tileHeight
                );
            }
        }

        @Override
        protected void paintComponent(Graphics graphics) {

            super.paintComponent(graphics);

            Graphics2D g =
                    (Graphics2D) graphics.create();

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            // Draw paths behind the floating platforms.

            g.setColor(PATH_COLOR);
            g.setStroke(new BasicStroke(3));

            for (int i = 1; i < portals.length; i++) {

                Rectangle left =
                        portals[i - 1].getBounds();

                Rectangle right =
                        portals[i].getBounds();

                g.drawLine(
                        left.x + left.width / 2,
                        left.y + left.height / 2 + 8,
                        right.x + right.width / 2,
                        right.y + right.height / 2 + 8
                );
            }

            // Gold frame for keyboard focus or mouse hover.

            g.setColor(FOCUS_COLOR);
            g.setStroke(new BasicStroke(2));

            for (JButton portal : portals) {

                if (portal.isFocusOwner()
                        || portal.getModel().isRollover()) {

                    Rectangle r = portal.getBounds();

                    g.drawRoundRect(
                            r.x + 2,
                            r.y + 2,
                            r.width - 5,
                            r.height - 5,
                            12, 12
                    );
                }
            }

            g.dispose();
        }
    }

    // ==========================================
    // ISLAND ICON DESIGN
    // ==========================================

    private static final class IslandIcon implements Icon {

        private final String symbol;
        private final Color color;
        private final Image artwork;

        IslandIcon(
                String symbol,
                Color color,
                Image artwork
        ) {

            this.symbol = symbol;
            this.color = color;
            this.artwork = artwork;
        }

        @Override
        public int getIconWidth() {
            return 120;
        }

        @Override
        public int getIconHeight() {
            return 94;
        }

        @Override
        public void paintIcon(
                Component component,
                Graphics graphics,
                int x,
                int y
        ) {

            Graphics2D g =
                    (Graphics2D) graphics.create();

            g.translate(x, y);

            g.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            // Floating rock and platform.

            g.setColor(PLATFORM_ROCK);

            g.fillPolygon(
                    new int[]{5, 25, 43, 60, 78, 94, 115},
                    new int[]{64, 82, 78, 92, 78, 83, 64},
                    7
            );

            g.setColor(PLATFORM_TOP);
            g.fillOval(4, 53, 112, 25);

            g.setColor(PLATFORM_EDGE);
            g.drawOval(4, 53, 112, 25);

            if (artwork != null) {

                g.setRenderingHint(
                        RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
                );

                g.drawImage(
                        artwork,
                        32, 4,
                        56, 56,
                        component
                );

            } else {

                // Temporary subject crystal until
                // Andeng adds the portal artwork.

                Polygon crystal = new Polygon(
                        new int[]{60, 81, 72, 48, 39},
                        new int[]{4, 31, 59, 59, 31},
                        5
                );

                g.setColor(color);
                g.fillPolygon(crystal);

                g.setColor(Color.WHITE);
                g.drawPolygon(crystal);

                // Pixelify Sans for the crystal symbols.

                g.setFont(FontManager.bold(18f));
                g.setColor(BACKGROUND);

                int textWidth =
                        g.getFontMetrics().stringWidth(symbol);

                g.drawString(
                        symbol,
                        60 - textWidth / 2,
                        42
                );
            }

            g.dispose();
        }
    }
}
