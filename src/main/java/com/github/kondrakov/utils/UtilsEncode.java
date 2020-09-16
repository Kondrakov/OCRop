package com.github.kondrakov.utils;


import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class UtilsEncode {

    private static String innerCharset = "UTF-8";

    public static void setInnerCharset(String charsetName) {
        innerCharset = charsetName;
    }

    public static String getInnerCharset() {
        return innerCharset;
    }

    public static String toRuntimeCharset(String encodingString) {
        try {
            encodingString = new String(encodingString.getBytes(getInnerCharset()),
                    Charset.defaultCharset().name());
        } catch (UnsupportedEncodingException ex) {
            System.out.println("ex " + ex);
        }
        return encodingString;
    }

    private static String outerCharset = "UTF-8";

    public static void setOuterCharset(String charsetName) {
        outerCharset = charsetName;
    }

    public static String getOuterCharset() {
        return outerCharset;
    }

    public static String toOuterCharset(String encodingString) {
        try {
            encodingString = new String(encodingString.getBytes(getInnerCharset()),
                    getOuterCharset());
        } catch (UnsupportedEncodingException ex) {
            System.out.println("ex " + ex);
        }
        return encodingString;
    }
}
