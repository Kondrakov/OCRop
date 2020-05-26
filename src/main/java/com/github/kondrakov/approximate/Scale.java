package com.github.kondrakov.approximate;

import com.github.kondrakov.utils.UtilsConv;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Scale {
    // basic simple mode:
    public static final String INTERPOLATE_MODE_NEAREST = "interpolate_mode_nearest";
    // between mode:
    public static final String INTERPOLATE_MODE_BETWEEN = "interpolate_mode_between";

    public static List<int[]> scaleUp(List<int[]> inputRaw, double scaleCoef) {
        List<int[]> input = UtilsConv.cloneMatrixData(inputRaw);
        List<int[]> output = new ArrayList<>();

        //todo smart insertion (interpolation):

        // // // vertical stretching:
        int baseHeight = input.size();
        int[] spreadByHeightPixels = spreadPixels(baseHeight, scaleCoef);
        int targetHeight = spreadByHeightPixels[spreadByHeightPixels.length -1] + 1;
        int insertCounter = 0;
        for (int i = 0; i < targetHeight; i++) {
            //todo simplify contains, maybe use List for spreadHeightPixels
            boolean contains = false;
            int[] currentExistRow = new int[0];
            for (int j = 0; j < spreadByHeightPixels.length; j++) {
                if (spreadByHeightPixels[j] == i) {
                    contains = true;
                    currentExistRow = input.get(insertCounter);
                    insertCounter++;
                }
            }
            if (!contains) {
                int[] approximatedRow = new int[input.get(0).length];
                for (int j = 0; j < approximatedRow.length; j++) {
                    approximatedRow[j] = -1;
                }
                output.add(approximatedRow);
            } else {
                output.add(currentExistRow);
            }
        }

        for (int i = 0; i < output.size(); i++) {
            if (output.get(i)[0] == -1) {
                for (int j = 0; j < output.get(i).length; j++) {
                    if (i == 0) {
                        int[] interpolateRow;
                        int counterNextExistingRow = 0;
                        do {
                            interpolateRow = output.get(counterNextExistingRow);
                        } while (output.get(counterNextExistingRow++)[0] == -1);
                        output.get(i)[j] = interpolateRow[j];
                    } else if (i == output.size() - 1) {
                        output.get(i)[j] = output.get(i - 1)[j];
                    } else {
                        output.get(i)[j] = output.get(i - 1)[j];
                    }
                }
            }
        }

        // // // horizontal stretching:
        int baseWidth = input.get(0).length;
        int[] spreadByWidthPixels = spreadPixels(baseWidth, scaleCoef);
        int targetWidth = spreadByWidthPixels[spreadByWidthPixels.length -1] + 1;
        int[] currentBaseRow;
        int baseWidthPixelCounter = 0;

        for (int i = 0; i < output.size(); i++) {
            currentBaseRow = output.get(i);
            output.remove(i);
            output.add(i, new int[targetWidth]);
            baseWidthPixelCounter = 0;
            for (int j = 0; j < targetWidth; j++) {
                if (spreadByWidthPixels.length > baseWidthPixelCounter &&
                        spreadByWidthPixels[baseWidthPixelCounter] == j) {
                    output.get(i)[j] = currentBaseRow[baseWidthPixelCounter];
                    baseWidthPixelCounter++;
                } else if (spreadByWidthPixels.length > baseWidthPixelCounter) {
                    if (j == 0) {
                        output.get(i)[j] = currentBaseRow[0];
                    } else {
                        output.get(i)[j] = output.get(i)[j - 1];
                    }
                }
            }
        }
        return output;
    }

    public static List<int[]> scaleUp(List<int[]> input, Point resultDims) {
        return input;
    }

    public static int[] spreadPixels(int baseDim, double scaleCoef) {
        int targetDim = (int) ((double) baseDim * scaleCoef);
        int[] spreadPixels = new int[baseDim];
        int currSpreadIndex = 0;
        int insertIndex;
        for (int i = 0; i < baseDim; i++) {
            insertIndex = Math.round((float) scaleCoef * ((float) i));
            if (insertIndex < targetDim) {
                spreadPixels[currSpreadIndex] = insertIndex;
            } else {
                spreadPixels[currSpreadIndex] = targetDim;
            }
            currSpreadIndex++;
        }
        return spreadPixels;
    }
}
