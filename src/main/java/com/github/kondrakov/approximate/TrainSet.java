package com.github.kondrakov.approximate;

import com.github.kondrakov.utils.UtilsConv;

import java.util.*;

public class TrainSet {

    public static List<int[]> mergeOverlappingWeights(List<int[]> baseInput, List<int[]> addMergeInput) {
        List<int[]> baseInputModif;
        baseInputModif = trimLeftBlankSpace(baseInput);
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
        int widthCanvasCast = castInput.get(0).length;
        return trimRightCanvasToFit(baseInput, widthCanvasCast);
    }

    public static List<int[]> trimRightCanvasToFit(List<int[]> baseInput, int toWidth) {
        int widthCanvasBase = baseInput.get(0).length;
        int widthCanvasCast = toWidth;
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

    public static List<int[]> trimLeftBlankSpace(List<int[]> input) {
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
            //todo add optimized right break logic here
            /*if (searchMinOffsetCurrent > -1) {
                searchMinOffset = Integer.min(searchMinOffset, searchMinOffsetCurrent);
                break;
            }*/
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

    public static List<int[]> trimRightBlankSpace(List<int[]> input) {
        List<int[]> output = new ArrayList<>();
        int searchMaxOffset = 0;
        int searchMaxOffsetCurrent;
        for (int i = 0; i < input.size(); i++) {
            searchMaxOffsetCurrent = -1;
            for (int j = input.get(i).length - 1; j >= 0; j--) {
                if (input.get(i)[j] > 0) {
                    searchMaxOffsetCurrent = j;
                    break;
                }
            }
            if (searchMaxOffsetCurrent > -1) {
                searchMaxOffset = Integer.max(searchMaxOffset, searchMaxOffsetCurrent);
                break;
            }
        }
        for (int i = 0; i < input.size(); i++) {
            output.add(
                    Arrays.copyOfRange(
                            input.get(i),
                            0,
                            searchMaxOffset
                    )
            );
        }
        return output;
    }

    public static List<int[]> trimUpperBlankSpace(List<int[]> input) {
        int startCutInd = -1;
        for (int i = 0; i < input.size(); i++) {
            for (int j = 0; j < input.get(i).length; j++) {
                if (input.get(i)[j] > 0) {
                    if (startCutInd == -1)
                        startCutInd = i;
                    break;
                }
            }
        }
        if (startCutInd == -1)
            startCutInd = 0;
        return input.subList(startCutInd, input.size() - 1);
    }

    public static List<int[]> trimLowerBlankSpace(List<int[]> input) {
        List<int[]> output = new ArrayList<>();
        boolean flagTrim = false;
        for (int i = input.size() - 1; i >= 0; i--) {
            flagTrim = true;
            for (int j = 0; j < input.get(i).length; j++) {
                if (input.get(i)[j] > 0) {
                    flagTrim = false;
                    break;
                }
            }
            if (!flagTrim) {
                output.add(0, input.get(i));
            }
        }
        return output;
    }

    ////////////////////////////////

    public static List<int[]> cornerizeTrimModel(List<int[]> input, int width, int height) {
        List<int[]> output = trimLeftBlankSpace(input);
        output = trimRightCanvasToFit(output, width);
        output = trimUpperBlankSpace(output);
        if (output.size() > height) {
            output = output.subList(0, height - 1);
        }
        int[] stub = new int[output.get(0).length];
        while (output.size() < height) {
            output.add(stub);
        }
        return output;
    }

    public static List<int[]> centerizeTrimModel(List<int[]> input, int width, int height) {
        List<int[]> outputTrimmed = trimLeftBlankSpace(input);
        outputTrimmed = trimRightBlankSpace(outputTrimmed);
        outputTrimmed = trimUpperBlankSpace(outputTrimmed);
        outputTrimmed = trimLowerBlankSpace(outputTrimmed);

        //todo some problem may occur when 24 bit bmp input image processed as 256 colors, check it:
        if (outputTrimmed.size() == 0) {
            System.out.println("problem with zero size List " + outputTrimmed.size());
        }

        int horizontalOffset = (width - outputTrimmed.get(0).length) / 2;
        int verticalOffset = (height - outputTrimmed.size()) / 2;
        List<int[]> output = new ArrayList<>();

        for (int i = 0; i < height; i++) {
            output.add(new int[width]);
        }

        for (int i = 0; i < outputTrimmed.size(); i++) {
            for (int j = 0; j < outputTrimmed.get(i).length; j++) {
                if (horizontalOffset + j >= 0 && horizontalOffset + j < width) {
                    if (verticalOffset + i >= 0 && verticalOffset + i < height) {
                        output.get(verticalOffset + i)[horizontalOffset + j] = outputTrimmed.get(i)[j];
                    }
                }
            }
        }
        return output;
    }
}
