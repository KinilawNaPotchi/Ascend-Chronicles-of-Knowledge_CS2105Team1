
package Frontend.utils;

import java.awt.Font;
import java.io.InputStream;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

public class FontManager {

    // LOAD PIXELIFY SANS FROM RESOURCES
    public static Font loadFont(String fileName, float size) {

        try (InputStream stream = FontManager.class
                .getResourceAsStream("/assets/fonts/" + fileName)) {

            if (stream == null) {
                throw new Exception("Font file not found: " + fileName);
            }

            return Font.createFont(Font.TRUETYPE_FONT, stream)
                    .deriveFont(size);

        } catch (Exception e) {
            System.out.println("Failed to load font: " + fileName);
            return new Font("Arial", Font.PLAIN, (int) size);
        }
    }

    // REGULAR PIXELIFY SANS
    public static Font regular(float size) {
        return loadFont("PixelifySans-Regular.ttf", size);
    }

    // MEDIUM PIXELIFY SANS
    public static Font medium(float size) {
        return loadFont("PixelifySans-Medium.ttf", size);
    }

    // BOLD PIXELIFY SANS
    public static Font bold(float size) {
        return loadFont("PixelifySans-Bold.ttf", size);
    }

    // SET PIXELIFY SANS AS DEFAULT SWING FONT
    public static void applyGlobalFont() {

        FontUIResource font = new FontUIResource(regular(20f));

        for (Object key : UIManager.getDefaults().keySet()) {

            if (UIManager.get(key) instanceof FontUIResource) {
                UIManager.put(key, font);
            }
        }
    }
}
