package com.github.kondrakov.model;

import com.github.kondrakov.feed.CSVProcessorIO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Alphabet {

    public static final String RU = "ru";
    public static final String EN = "en";

    private static List<String> alphabetRU;

    static {
        alphabetRU = new ArrayList<>();
        alphabetRU.add("а");
        alphabetRU.add("б");
        alphabetRU.add("в");
        alphabetRU.add("г");
        alphabetRU.add("д");
        alphabetRU.add("е");
        alphabetRU.add("ё");
        alphabetRU.add("ж");
        alphabetRU.add("з");
        alphabetRU.add("и");
        alphabetRU.add("й");
        alphabetRU.add("к");
        alphabetRU.add("л");
        alphabetRU.add("м");
        alphabetRU.add("н");
        alphabetRU.add("о");
        alphabetRU.add("п");
        alphabetRU.add("р");
        alphabetRU.add("с");
        alphabetRU.add("т");
        alphabetRU.add("у");
        alphabetRU.add("ф");
        alphabetRU.add("х");
        alphabetRU.add("ц");
        alphabetRU.add("ч");
        alphabetRU.add("щ");
        alphabetRU.add("ш");
        alphabetRU.add("ъ");
        alphabetRU.add("ы");
        alphabetRU.add("ь");
        alphabetRU.add("э");
        alphabetRU.add("ю");
        alphabetRU.add("я");
    }

    public static List<String> getAlphabetRU() {
        return alphabetRU;
    }

    private static Map<String, String> mappingRU;

    static {
        mappingRU = new HashMap<>();
        mappingRU.put("а", "а");
        mappingRU.put("б", "б");
        mappingRU.put("в", "в");
        mappingRU.put("г", "г");
        mappingRU.put("д", "д");
        mappingRU.put("е", "е");
        mappingRU.put("ё", "ё");
        mappingRU.put("ж", "ж");
        mappingRU.put("з", "з");
        mappingRU.put("и", "и");
        mappingRU.put("й", "й");
        mappingRU.put("к", "к");
        mappingRU.put("л", "л");
        mappingRU.put("м", "м");
        mappingRU.put("н", "н");
        mappingRU.put("о", "о");
        mappingRU.put("п", "п");
        mappingRU.put("р", "р");
        mappingRU.put("с", "с");
        mappingRU.put("т", "т");
        mappingRU.put("у", "у");
        mappingRU.put("ф", "ф");
        mappingRU.put("х", "х");
        mappingRU.put("ц", "ц");
        mappingRU.put("ч", "ч");
        mappingRU.put("щ", "щ");
        mappingRU.put("ш", "ш");
        mappingRU.put("ъ", "ъ");
        mappingRU.put("ы", "ы");
        mappingRU.put("ь", "ь");
        mappingRU.put("э", "э");
        mappingRU.put("ю", "ю");
        mappingRU.put("я", "я");
    }

    public static Map<String, String> getMappingRU() {
        return mappingRU;
    }


    private static List<String> alphabetEN;

    static {
        alphabetEN = new ArrayList<>();
        alphabetEN.add("a");
        alphabetEN.add("b");
        alphabetEN.add("c");
        alphabetEN.add("d");
        alphabetEN.add("e");
        alphabetEN.add("f");
        alphabetEN.add("g");
        alphabetEN.add("h");
        alphabetEN.add("i");
        alphabetEN.add("j");
        alphabetEN.add("k");
        alphabetEN.add("l");
        alphabetEN.add("m");
        alphabetEN.add("n");
        alphabetEN.add("o");
        alphabetEN.add("p");
        alphabetEN.add("q");
        alphabetEN.add("r");
        alphabetEN.add("s");
        alphabetEN.add("t");
        alphabetEN.add("u");
        alphabetEN.add("v");
        alphabetEN.add("w");
        alphabetEN.add("x");
        alphabetEN.add("y");
        alphabetEN.add("z");
        alphabetEN.add("colon");
    }

    public static List<String> getAlphabetEN() {
        return alphabetEN;
    }

    private static Map<String, String> mappingEN;

    static {
        mappingEN = new HashMap<>();
        mappingEN.put("a", "a");
        mappingEN.put("b", "b");
        mappingEN.put("c", "c");
        mappingEN.put("d", "d");
        mappingEN.put("e", "e");
        mappingEN.put("f", "f");
        mappingEN.put("g", "g");
        mappingEN.put("h", "h");
        mappingEN.put("i", "i");
        mappingEN.put("j", "j");
        mappingEN.put("k", "k");
        mappingEN.put("l", "l");
        mappingEN.put("m", "m");
        mappingEN.put("n", "n");
        mappingEN.put("o", "o");
        mappingEN.put("p", "p");
        mappingEN.put("q", "q");
        mappingEN.put("r", "r");
        mappingEN.put("s", "s");
        mappingEN.put("t", "t");
        mappingEN.put("u", "u");
        mappingEN.put("v", "v");
        mappingEN.put("w", "w");
        mappingEN.put("x", "x");
        mappingEN.put("y", "y");
        mappingEN.put("z", "z");
        mappingEN.put("colon", ":");
    }

    public static Map<String, String> getMappingEN() {
        return mappingEN;
    }

    public static void setExternalAlphabet(String alphabetPath, String mappingPath, String set) {
        if (RU.equals(set)) {
            alphabetRU = CSVProcessorIO.loadStringListFromCSVFile(alphabetPath);
            mappingRU = CSVProcessorIO.loadKeyValueStringsFromCSVFile(mappingPath);
        } else if (EN.equals(set)) {
            alphabetEN = CSVProcessorIO.loadStringListFromCSVFile(alphabetPath);
            mappingEN = CSVProcessorIO.loadKeyValueStringsFromCSVFile(mappingPath);
        } else {
            System.out.println(
                    String.format("Warning, alphabet not loaded, mode %s not supported.", set)
            );
        }
    }
}