package com.github.kondrakov.textfinder;


import com.github.kondrakov.Experimental;
import com.github.kondrakov.approximate.TrainSet;
import com.github.kondrakov.feed.CSVProcessorIO;
import com.github.kondrakov.parser.BitmapParser;
import com.github.kondrakov.parser.BitmapUtils;
import com.github.kondrakov.utils.UtilsConv;

import java.awt.*;
import java.util.*;
import java.util.List;

public class SourceCutter {

    public final static String SIMPLE_CUT = "simple_cut";
    public final static String PATH_FIND_CUT = "path_find_cut";
    public final static String NO_GAP_SEARCH_CUT = "no_gap_search_cut";
    public final static String NO_CHECK_GAP_CUT = "no_check_gap_cut";

    public SourceCutter() {
        // todo search first pixel?
    }

    public static void loadAndProcessImage() {
        List<int[]> rawBitmap =
                BitmapParser.parse("to_cut.bmp", "to_cut_duplicate.bmp", BitmapUtils.COLOR_16);
        BitmapParser.assembleBitmap(rawBitmap, "to_cut_duplicate.bmp", "generated_to_cut.bmp", BitmapUtils.COLOR_16);
    }

    public static List<int[]> cutCropByDims(String inputPath, Rectangle dims, String colorMode) {
        List<int[]> rawBitmapPixels =
                BitmapParser.parse(inputPath,
                        null, colorMode);
        List<int[]> output = new ArrayList<>();

        int localCounter_i = 0;
        int localCounter_j = 0;
        for (int i = (int) dims.getY(); i < (int) (dims.getHeight() + dims.getY()); i++) {
            output.add(new int[(int)dims.getWidth()]);
            for (int j = (int) dims.getX(); j < (int) (dims.getWidth() + dims.getX()); j++) {
                output.get(localCounter_i)[localCounter_j] = rawBitmapPixels.get(i)[j];
                localCounter_j++;
            }
            localCounter_j = 0;
            localCounter_i++;
        }
        return output;
    }

    public void searchBlock() {
        // todo search block
    }

    public void searchString() {
        // todo search string
    }

    //todo feature recognizing the font via neural net to choose mode for cutting letters
    public static List<List<int[]>> simpleCutString(List<int[]> symbolCharRow, String cuttingMode, String colorMode, int spaceSymbolTolerance, int averageWidth, String outputDebugPath) {
        boolean isGapExists = true;
        int gaps = 0;
        List<List<int[]>> string = new ArrayList<>();
        List<int[]> currentLetter = new ArrayList<>();
        List<Integer> currentColumn;
        List<List<Integer>> currentLetterBuffer = new ArrayList<>();

        for (int i = 0; i < symbolCharRow.get(0).length; i++) {
            currentColumn = new ArrayList<>();
            isGapExists = true;
            for (int j = 0; j < symbolCharRow.size(); j++) {
                if (symbolCharRow.get(j)[i] != 0) {
                    isGapExists = false;
                }
                currentColumn.add(symbolCharRow.get(j)[i]);
            }
            if (!isGapExists) {
                if (spaceSymbolTolerance < gaps) {
                    string.add(new ArrayList<>());
                }
                gaps = 0;
            } else {
                gaps++;
            }
            if (isGapExists) {
                if (currentLetterBuffer.size() > 0) {
                    for (int j = 0; j < currentLetterBuffer.get(0).size(); j++) {
                        currentLetter.add(new int[currentLetterBuffer.size()]);
                        for (int k = 0; k < currentLetterBuffer.size(); k++) {
                            currentLetter.get(j)[k] = currentLetterBuffer.get(k).get(j);
                        }
                    }
                    try {
                        // here inverted already, so set invertMode to false
                        CSVProcessorIO.writeMatrixToCSVFile(currentLetter,
                                String.format(outputDebugPath, gaps),
                                false, colorMode);
                    } catch (Exception ex) {
                        System.out.println(ex);
                    }
                    /*if (currentLetter.size() > 0) {
                        System.out.println("length " + currentLetter.get(0).length);
                    }*/

                    if (NO_GAP_SEARCH_CUT.equals(cuttingMode) && averageWidth > 0) {
                        //we check welded letters here and cut by average letter width (better working with mono-width fonts)
                        long addSymbolsCount = Math.round((double)currentLetter.get(0).length / (double) averageWidth);

                        if (addSymbolsCount > 1) {
                            long width = Math.round((double)currentLetter.get(0).length / (double) addSymbolsCount);
                            List<List<int[]>> extractedLetters = new ArrayList<>();
                            for (int j = 0; j < addSymbolsCount; j++) {
                                extractedLetters.add(new ArrayList<>());
                            }
                            for (int j = 0; j < currentLetter.size(); j++) {
                                for (int k = 0; k < addSymbolsCount; k++) {
                                    extractedLetters.get(k).add(
                                            Arrays.copyOfRange(currentLetter.get(j),
                                                    (int)(k * width), (int)((k + 1) * width))
                                    );
                                }
                            }
                            string.addAll(extractedLetters);
                        }


                    }
                    //todo exclude if subdivided when no_gap_search_cut mode used
                    string.add(currentLetter);
                    currentLetter = new ArrayList<>();
                }
                currentLetterBuffer = new ArrayList<>();
            } else {
                currentLetterBuffer.add(currentColumn);
            }
        }
        return string;
    }

    @Experimental
    public static List<List<int[]>> simpleCutStringNoGapCheck(List<int[]> symbolCharRow, String cuttingMode, String colorMode, int spaceSymbolTolerance, int averageWidth) {
        List<int[]> trimmedSymbolRow = UtilsConv.cloneMatrixData(symbolCharRow);
        if (SourceCutter.NO_CHECK_GAP_CUT.equals(cuttingMode)) {
            trimmedSymbolRow = TrainSet.trimLeftBlankSpace(trimmedSymbolRow);
            trimmedSymbolRow = TrainSet.trimRightBlankSpace(trimmedSymbolRow);

            List<List<int[]>> extractedLetters = new ArrayList<>();
            int discreteWidthCounter;
            int discreteIndexCounter;
            for (int j = 0; j < trimmedSymbolRow.size(); j++) {
                discreteWidthCounter = 0;
                discreteIndexCounter = 0;
                while (discreteWidthCounter <= trimmedSymbolRow.get(j).length) {
                    if (j == 0) {
                        extractedLetters.add(new ArrayList<>());
                    }
                    extractedLetters.get(discreteIndexCounter).add(
                            Arrays.copyOfRange(trimmedSymbolRow.get(j),
                                    (int)(discreteWidthCounter),
                                    (int)(discreteWidthCounter + averageWidth)
                            )
                    );
                    discreteWidthCounter += averageWidth;
                    discreteIndexCounter++;
                }
            }
            return extractedLetters;
        }
        return new ArrayList<>();
    }

    public static List<List<int[]>> simpleCutBlockIntoStrings(List<int[]> rawBlock) {
        List<List<int[]>> extractedStrings = new ArrayList<>();
        boolean gapFound;
        int currStringFound = 0;
        extractedStrings.add(currStringFound, new ArrayList<>());
        for (int j = 0; j < rawBlock.size(); j++) {
            gapFound = true;
            for (int i = 0; i < rawBlock.get(j).length; i++) {
                if (rawBlock.get(j)[i] != 0) {
                    gapFound = false;
                    break;
                }
            }

            if (!gapFound) {
                extractedStrings.get(currStringFound).add(rawBlock.get(j));
            } else {
                if (extractedStrings.get(currStringFound).size() != 0) {
                    currStringFound ++;
                    extractedStrings.add(currStringFound, new ArrayList<>());
                }
            }
        }
        // remove trailing empty row:
        if (extractedStrings.get(currStringFound).isEmpty()) {
            extractedStrings.remove(currStringFound);
        }
        return extractedStrings;
    }
}
