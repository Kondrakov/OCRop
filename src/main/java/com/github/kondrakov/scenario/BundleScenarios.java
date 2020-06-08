package com.github.kondrakov.scenario;

import com.github.kondrakov.feed.CSVProcessorIO;
import com.github.kondrakov.feed.DataStash;
import com.github.kondrakov.feed.IFeeder;
import com.github.kondrakov.feed.PDFBoxFeeder;
import com.github.kondrakov.format.Format;
import com.github.kondrakov.model.Alphabet;
import com.github.kondrakov.parser.BitmapUtils;
import com.github.kondrakov.recognize.Recognizer;
import com.github.kondrakov.textfinder.SourceCutter;
import com.github.kondrakov.utils.UtilsConv;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BundleScenarios {
    public BundleScenarios() {
    }

    public BundleScenarios loadSymbolsRange(String alphabetPath, String mappingPath, String set) {
        Alphabet.setExternalAlphabet(alphabetPath, mappingPath, set);
        Alphabet.setCurrentSet(set);
        return this;
    }

    public BundleScenarios loadAlphabetModel(String basePathFrom, String basePathTo,
                                             List<String> alphabetRange, String sourceMode,
                                             String colorMode) {
        DataStash.prepareEtalonModels(basePathFrom, basePathTo, alphabetRange, sourceMode, colorMode);
        return this;
    }

    public BundleScenarios extractStringsToRecognize(String pathPdf, String pathBmp,
                                                     Rectangle bounds, String colorMode,
                                                     String cutMode, int cutTolerance) {
        IFeeder feeder = new PDFBoxFeeder();
        feeder.feed(pathPdf, pathBmp);

        List<int[]> blockMatrix = SourceCutter.cutCropByDims(pathBmp, bounds, colorMode);

        List<int[]> blockMatrixInverted = UtilsConv.cloneMatrixData(blockMatrix);
        for (int i = 0; i < blockMatrix.size(); i++) {
            for (int j = 0; j < blockMatrix.get(i).length; j++) {
                blockMatrixInverted.get(i)[j] = BitmapUtils.invert(blockMatrixInverted.get(i)[j], colorMode);
            }
        }
        List<List<int[]>> stringsFromBlock =
                SourceCutter.simpleCutBlockIntoStrings(blockMatrixInverted);

        toRecognizeMatrices = new ArrayList<>();
        for (int i = 0; i < stringsFromBlock.size(); i++) {
            toRecognizeMatrices.addAll(
                    SourceCutter.simpleCutString(stringsFromBlock.get(i), cutMode, colorMode, cutTolerance)
            );
        }
        return this;
    }

    public BundleScenarios recognize() {
        etalonMatrices = DataStash.getLetterMatricesCollection();
        StringBuilder answer = new StringBuilder();
        for (int i = 0; i < toRecognizeMatrices.size(); i++) {
            if (toRecognizeMatrices.get(i).size() > 0) {
                Map<String, List<int[]>> formattedMatrices = new HashMap<>();
                for (Map.Entry<String, List<int[]>> matrixEntry : etalonMatrices.entrySet()) {
                    formattedMatrices.put(matrixEntry.getKey(),
                            Format.frameToPattern(
                                    matrixEntry.getValue(), toRecognizeMatrices.get(i)
                            ));
                    try {
                        CSVProcessorIO.writeMatrixToCSVFile(
                                formattedMatrices.get(matrixEntry.getKey()),
                                String.format(
                                        "data\\symbols_formatted_csv\\en\\%s.csv",
                                        matrixEntry.getKey()
                                ), false, BitmapUtils.COLOR_256
                        );
                    } catch (Exception ex) {
                        System.out.println(ex);
                    }
                }
                answer.append(Recognizer.recognizeByPercent(formattedMatrices,
                        Format.frameExtendPattern(toRecognizeMatrices.get(i)),
                        BitmapUtils.COLOR_256
                ));
            } else {
                answer.append(" ");
            }
            System.out.println(String.format("answer ->>%s<<-", answer));
            System.out.println("EOF" + " letter");
        }
        this.recognized = answer.toString();
        return this;
    }

    private List<List<int[]>> toRecognizeMatrices;
    private Map<String, List<int[]>> etalonMatrices;

    private String recognized;
    public String getRecognized() {
        return this.recognized;
    }
}
