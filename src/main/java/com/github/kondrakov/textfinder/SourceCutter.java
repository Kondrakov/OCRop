package com.github.kondrakov.textfinder;


import com.github.kondrakov.feed.CSVProcessorIO;
import com.github.kondrakov.parser.BitmapParser;
import com.github.kondrakov.parser.BitmapUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class SourceCutter {

    public final static String SIMPLE_CUT = "simple_cut";
    public final static String PATH_FIND_CUT = "path_find_cut";

    public SourceCutter() {
        // todo search first pixel?
    }

    public static void loadAndProcessImage() {
        List<int[]> rawBitmap =
                BitmapParser.parse("to_cut.bmp", "to_cut_duplicate.bmp", BitmapUtils.COLOR_16);
        BitmapParser.assembleBitmap(rawBitmap, "to_cut_duplicate.bmp", "generated_to_cut.bmp", BitmapUtils.COLOR_16);
    }

    public static List<int[]> cutCropByDims(String inputPath, Rectangle dims, String colorMode) {
        List<int[]> rawBitmap =
                BitmapParser.parse(inputPath,
                        "data\\strings_to_recognize\\page_to_cut_dupl.bmp", colorMode);
        List<int[]> output = new ArrayList<>();

        int localCounter_i = 0;
        int localCounter_j = 0;
        for (int i = (int) dims.getY(); i < (int) (dims.getHeight() + dims.getY()); i++) {
            output.add(new int[(int)dims.getWidth()]);
            for (int j = (int) dims.getX(); j < (int) (dims.getWidth() + dims.getX()); j++) {
                output.get(localCounter_i)[localCounter_j] = rawBitmap.get(i)[j];
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
    public static List<List<int[]>> simpleCutString(List<int[]> rawString, String cuttingMode, String colorMode) {
        boolean isGapExists = true;
        List<List<int[]>> string = new ArrayList<>();
        List<int[]> currentLetter = new ArrayList<>();
        List<Integer> currentColumn;
        List<List<Integer>> currentLetterBuffer = new ArrayList<>();
        for (int i = 0; i < rawString.get(0).length; i++) {
            currentColumn = new ArrayList<>();
            isGapExists = true;
            for (int j = 0; j < rawString.size(); j++) {
                if (rawString.get(j)[i] != 0) {
                    isGapExists = false;
                }
                currentColumn.add(rawString.get(j)[i]);
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
                        // here inverted already, so switch invertMode to false
                        CSVProcessorIO.writeMatrixToCSVFile(currentLetter,
                                "data\\strings_to_recognize\\l" + string.size() + ".csv",
                                false, colorMode);
                    } catch (Exception ex) {
                        System.out.println(ex);
                    }
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
