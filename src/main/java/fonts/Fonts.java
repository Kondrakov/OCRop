package fonts;

import java.util.ArrayList;
import java.util.List;

public class Fonts {
    private static List<String> fonts;

    static {
        fonts = new ArrayList<String>();
        fonts.add("Arial");
        fonts.add("Helvetica");
        fonts.add("Tahoma");
    }

    public static List<String> getFonts() {
        return fonts;
    }
}
