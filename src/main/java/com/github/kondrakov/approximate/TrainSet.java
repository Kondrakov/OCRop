package com.github.kondrakov.approximate;

import com.github.kondrakov.utils.UtilsConv;

import java.util.*;

public class TrainSet {

    public static List<int[]> mergeOverlappingWeights(List<int[]> baseInput, List<int[]> addMergeInput) {
        List<int[]> baseInputModif;
        baseInputModif = trimLeftBlancSpace(baseInput);
        baseInputModif = alignInputByY(baseInputModif, addMergeInput);
        baseInputModif = trimRightCanvasToFit(baseInputModif, addMergeInput);

        for (int i = 0; i < baseInputModif.size(); i++) {
            for (int j = 0; j < baseInputModif.get(i).length; j++) {
                if (baseInputModif.get(i)[j] == 0 && addMergeInput.get(i)[j] == 0) {
                    // 0 remains
                }
                if (baseInputModif.get(i)[j] > 0 ^ addMergeInput.get(i)[j] > 0) {
                    baseInputModif.get(i)[j] = 1;
                }
                if (baseInputModif.get(i)[j] > 0 && addMergeInput.get(i)[j] > 0) {
                    baseInputModif.get(i)[j] = 2;
                }
            }
        }
        return baseInputModif;
    }

    public static List<int[]> alignInputByY(List<int[]> baseInput, List<int[]> castInput) {
        int blancRowsBeforeStartLetterBase = rowsCountToLetterStart(baseInput);
        int blancRowsBeforeStartLetterCast = rowsCountToLetterStart(castInput);
        List<int[]> baseInputModif = UtilsConv.cloneMatrixData(baseInput);
        if (blancRowsBeforeStartLetterBase == blancRowsBeforeStartLetterCast) {
        } else if (blancRowsBeforeStartLetterBase < blancRowsBeforeStartLetterCast) {
            int addRows = blancRowsBeforeStartLetterCast - blancRowsBeforeStartLetterBase;
            for (int i = 0; i < addRows; i++) {
                baseInputModif.add(0, new int[baseInput.get(0).length]);
            }
        } else {
            int removeRows = blancRowsBeforeStartLetterBase - blancRowsBeforeStartLetterCast;
            for (int i = 0; i < removeRows; i++) {
                baseInputModif.remove(0);
            }
        }

        int heightBase = baseInputModif.size();
        int heightCast = castInput.size();
        if (heightBase == heightCast) {
        } else if (heightBase < heightCast) {
            int addRows = heightCast - heightBase;
            for (int i = 0; i < addRows; i++) {
                baseInputModif.add(new int[baseInput.get(0).length]);
            }
        } else {
            int removeRows = heightBase - heightCast;
            for (int i = 0; i < removeRows; i++) {
                baseInputModif.remove(baseInputModif.size() - 1);
            }
        }
        return baseInputModif;
    }

    public static List<int[]> trimRightCanvasToFit(List<int[]> baseInput, List<int[]> castInput) {
        int widthCanvasBase = baseInput.get(0).length;
        int widthCanvasCast = castInput.get(0).length;
        List<int[]> baseInputModif = UtilsConv.cloneMatrixData(baseInput);
        if (widthCanvasBase == widthCanvasCast) {
        } else if (widthCanvasBase < widthCanvasCast) {
            int[] arrExtended;
            for (int i = 0; i < baseInputModif.size(); i++) {
                arrExtended = new int[widthCanvasCast];
                for (int j = 0; j < widthCanvasCast; j++) {
                    if (baseInputModif.get(i).length > j) {
                        arrExtended[j] = baseInputModif.get(i)[j];
                    } else {
                        arrExtended[j] = 0;
                    }
                }
                baseInputModif.remove(i);
                baseInputModif.add(i, arrExtended);
            }
        } else {
            int[] trimmedArr;
            for (int i = 0; i < baseInputModif.size(); i++) {
                trimmedArr = Arrays.copyOfRange(baseInputModif.get(i), 0, widthCanvasCast);
                baseInputModif.remove(i);
                baseInputModif.add(i, trimmedArr);
            }
        }
        return baseInputModif;
    }

    public static int rowsCountToLetterStart(List<int[]> input) {
        for (int i = 0; i < input.size(); i++) {
            for (int j = 0; j < input.get(i).length; j++) {
                if (input.get(i)[j] > 0) {
                    return i + 1;
                }
            }
        }
        return input.size();
    }

    public static List<int[]> trimLeftBlancSpace(List<int[]> input) {
        List<int[]> output = new ArrayList<>();
        int searchMinOffset = input.get(0).length - 1;
        int searchMinOffsetCurrent;
        for (int i = 0; i < input.size(); i++) {
            searchMinOffsetCurrent = input.get(i).length - 1;
            for (int j = 0; j < input.get(i).length; j++) {
                if (input.get(i)[j] > 0) {
                    searchMinOffsetCurrent = j;
                    break;
                }
            }
            searchMinOffset = Integer.min(searchMinOffset, searchMinOffsetCurrent);
        }
        for (int i = 0; i < input.size(); i++) {
            output.add(
                Arrays.copyOfRange(
                    input.get(i),
                    searchMinOffset,
                    input.get(i).length - 1
                )
            );
        }
        return output;
    }
}
