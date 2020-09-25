package com.github.kondrakov;

import java.util.*;
import java.util.Map.Entry;

public class OCRRecognize {

    private List<List<int[]>> inputStringToRecognize;

    private Map<String, List<int[]>> etalonMatrices;

    public OCRRecognize() {
    }

    @Deprecated
    public String recognizeAll(List<List<int[]>> inputStringToRecognize) {
        //deprecated method to be moved, refactored or changed, now see Recognizer instead
        String recognized = "";
        for (int i = 0; i < inputStringToRecognize.size(); i++) {
            recognized += this.recognize(inputStringToRecognize.get(i));
        }
        return recognized;
    }

    @Deprecated
    public String recognize(List<int[]> recognizeInputMatrix) {
        //deprecated method to be moved, refactored or changed, now see Recognizer instead
        //todo must be changed, debug stub:
        this.etalonMatrices = new HashMap<>();
        this.etalonMatrices.put("o", recognizeInputMatrix);

        for (Entry matrixEntry:etalonMatrices.entrySet()) {
            if (match(recognizeInputMatrix, (ArrayList) matrixEntry.getValue())) {
                return matrixEntry.getKey().toString();
            }
        }
        return "";
    }

    @Deprecated
    private boolean match(List<int[]> recognizeInputMatrix, List<int[]> etalonMatrix) {
        //deprecated method to be moved, refactored or changed, now see Recognizer instead
        for (int i = 0; i < etalonMatrix.size(); i++) {
            for (int j = 0; j < etalonMatrix.get(i).length; j++) {
                if (etalonMatrix.get(i)[j] != recognizeInputMatrix.get(i)[j]) {
                    System.out.println("valid"  +  " valid");
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    @Deprecated
    public String recognizeNeural(List<int[]> recognizeInputMatrix) {
        //deprecated method to be moved, refactored or changed, now see Recognizer instead
        return "";
    }
}
