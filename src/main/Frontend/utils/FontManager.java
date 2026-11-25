
package Frontend.utils;

import java.awt.Font;
import java.io.InputStream;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

public class FontManager {

    // Load Pixelify Sans from the resources folder
    public static Font loadFont(String fileName, float size) {

        try (InputStream stream = FontManager.class
                .getResourceAsStream("/fonts/" + fileName)) {

            if (stream == null) {
                throw new Exception("Font file not found");
            }

            return Font.createFont(Font.TRUETYPE_FONT, stream)
                    .deriveFont(size);

        } catch (Exception e) {
            System.out.println("Failed to load font: " + fileName);
            return new Font("Arial", Font.PLAIN, (int) size);
        }
    }

    // Regular Pixelify Sans
    public static Font regular(float size) {
        return loadFont("PixelifySans-Regular.ttf", size);
    }

    // Bold Pixelify Sans
    public static Font bold(float size) {
        return loadFont("PixelifySans-Bold.ttf", size);
    }

    // Set Pixelify Sans as the default Swing font
    public static void applyGlobalFont() {

        FontUIResource font = new FontUIResource(regular(20f));

        for (Object key : UIManager.getDefaults().keySet()) {

            if (UIManager.get(key) instanceof FontUIResource) {
                UIManager.put(key, font);
            }
        }
    }
}
