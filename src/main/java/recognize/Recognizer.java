package recognize;

import model.Alphabet;
import utils.UtilsConv;

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
}