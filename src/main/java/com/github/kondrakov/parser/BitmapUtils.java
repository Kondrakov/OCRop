package com.github.kondrakov.parser;

public class BitmapUtils {
    public final static String COLOR_16 = "color_16";
    public final static String COLOR_256 = "color_256";

    public void convertColorMode(String inutColorMode, String outputColorMode) {
        //todo implement convert from 16 colors to grayscale
    }

    public static int invert(int color) {
        return invert(color, COLOR_16);
    }

    /**
        Bitmap color inversion by color mode, 256 colors mode (1 pixel per byte) by default
     */
    public static int invert(int color, String colorMode) {
        return maxColorByMode(colorMode) - color;
    }

    public static int maxColorByMode(String colorMode) {
        if (COLOR_16.equals(colorMode)) {
            return 15;
        }
        if (COLOR_256.equals(colorMode)) {
            return 255;
        }
        return 255;
    }
}
