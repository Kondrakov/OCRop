package com.github.kondrakov.recognize;

import com.github.kondrakov.model.Alphabet;
import com.github.kondrakov.parser.BitmapUtils;
import com.github.kondrakov.utils.UtilsConv;

import java.util.List;
import java.util.Map;

public class Recognizer {

    public static String recognize(Map<String, List<int[]>> alphabet, List<int[]> guessedLetter) {
        int matchResult;
        int maxMatchResult = Integer.MIN_VALUE;
        String result = "";
        for (Map.Entry<String, List<int[]>> letter : alphabet.entrySet()) {
            matchResult = 0;

            alphabet.put(letter.getKey(), UtilsConv.fitMatrixCanvas(letter.getValue(),
                    guessedLetter.get(0).length,
                    guessedLetter.size()
            ));

            for (int y = 0; y < guessedLetter.size(); y++) {
                for (int x = 0; x < guessedLetter.get(0).length; x++) {
                    if (guessedLetter.get(y)[x] != letter.getValue().get(y)[x]) {
                        matchResult -= 2;
                    } else {
                        matchResult ++;
                    }
                }
            }
            System.out.println("letter " + letter.getKey() + " matchResult " + matchResult);
            if (matchResult > maxMatchResult) {
                result = Alphabet.getMappingEN().get(letter.getKey());
            }
            maxMatchResult = Integer.max(maxMatchResult, matchResult);
        }
        return result;
    }


    public static String recognizeByPercent(Map<String, List<int[]>> alphabet,
                                            List<int[]> guessedLetter, String colorMode) {
        double percentValue = ((double) BitmapUtils.maxColorByMode(colorMode) + 1.0d) / 100;
        double matchResult;
        double maxMatchResult = -Double.MAX_VALUE;
        String result = "";
        for (Map.Entry<String, List<int[]>> letter : alphabet.entrySet()) {
            matchResult = 0;

            alphabet.put(letter.getKey(), UtilsConv.fitMatrixCanvas(letter.getValue(),
                    guessedLetter.get(0).length,
                    guessedLetter.size()
            ));

            for (int y = 0; y < guessedLetter.size(); y++) {
                for (int x = 0; x < guessedLetter.get(0).length; x++) {
                    if (guessedLetter.get(y)[x] != letter.getValue().get(y)[x]) {
                        matchResult -=
                                Math.abs((double)guessedLetter.get(y)[x] -
                                        (double)letter.getValue().get(y)[x]) * percentValue;
                    } else {

                    }
                }
            }
            System.out.println("letter " + letter.getKey() + " matchResult " + matchResult);
            if (matchResult > maxMatchResult) {
                result = Alphabet.getMappingEN().get(letter.getKey());
            }
            maxMatchResult = Double.max(maxMatchResult, matchResult);
        }
        return result;
    }
}