package com.github.kondrakov.parser;

import com.github.kondrakov.validation.BitmapDataParsingException;

public class BitmapUtils {
    public final static String COLOR_16 = "color_16";
    public final static String COLOR_256 = "color_256";
    public final static String COLOR_24_BIT = "color_24_bit";

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
        if (COLOR_24_BIT.equals(colorMode)) {
            return 16777215;
        }
        return 255;
    }

    public static boolean assertRightColorMode(String colorModeAsserting, int colorModeFromHeader) throws BitmapDataParsingException {
        if (!colorModeAsserting.equals(recognizeColorMode(colorModeFromHeader))) {
            throw new BitmapDataParsingException("WARNING!! Recognized color mode in bmp not match with asserted, please check file input color format\n" +
                    "Asserted: " + colorModeAsserting + ", from bmp header: " + recognizeColorMode(colorModeFromHeader));
        }
        return true;
    }

    public static String recognizeColorMode(int colorModeFromHeader) {
        int colorsCount = (int) Math.pow(2, colorModeFromHeader);
        if (colorsCount == 16) {
            return BitmapUtils.COLOR_16;
        } else if (colorsCount == 256) {
            return BitmapUtils.COLOR_256;
        } else if (colorsCount == 16777216) {
            return BitmapUtils.COLOR_24_BIT;
        } else {
            return "Unrecognized color mode";
        }
    }
}
