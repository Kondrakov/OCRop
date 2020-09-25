package com.github.kondrakov.scenario;

import com.github.kondrakov.approximate.TrainSet;
import com.github.kondrakov.feed.CSVProcessorIO;
import com.github.kondrakov.feed.DataStash;
import com.github.kondrakov.feed.IFeeder;
import com.github.kondrakov.feed.PDFBoxFeeder;
import com.github.kondrakov.format.Format;
import com.github.kondrakov.model.Alphabet;
import com.github.kondrakov.parser.BitmapUtils;
import com.github.kondrakov.recognize.NeuralRecognizer;
import com.github.kondrakov.recognize.Recognizer;
import com.github.kondrakov.textfinder.SourceCutter;
import com.github.kondrakov.utils.UtilsConv;
import com.github.kondrakov.utils.UtilsEncode;
import org.apache.pdfbox.rendering.ImageType;

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

    private List<Map<String, List<int[]>>> letterMatricesCollectionsThis;

    public BundleScenarios loadAlphabetModel(String basePathFrom, String basePathTo,
                                             List<String> alphabetRange, String sourceMode,
                                             String colorMode) {
        DataStash.prepareEtalonModels(basePathFrom, basePathTo, alphabetRange, sourceMode, colorMode);
        return this;
    }

    public BundleScenarios loadAndMergeAlphabetModel(String basePathFrom1, String basePathTo1,
                                                     String basePathFrom2, String basePathTo2,
                                                     String pathOutputMerged,
                                             List<String> alphabetRange, String sourceMode,
                                             String colorMode) {
        DataStash.prepareEtalonModelsForMerge(
                basePathFrom1, basePathTo1,
                basePathFrom2, basePathTo2,
                alphabetRange, sourceMode, colorMode);

        List<Map<String, List<int[]>>> letterMatricesCollections =  DataStash.getLetterMatricesCollections();
        letterMatricesCollectionsThis = letterMatricesCollections;
        Map<String, List<int[]>> mergedMap = new HashMap<>();
        for (Map.Entry<String, List<int[]>> matrixEntry : letterMatricesCollections.get(0).entrySet()) {
            mergedMap.put(
                    matrixEntry.getKey(),
                    TrainSet.mergeOverlappingWeights(
                            matrixEntry.getValue(),
                            letterMatricesCollections.get(1).get(matrixEntry.getKey())
                    )
            );

            try {
                CSVProcessorIO.writeMatrixToCSVFile(
                        mergedMap.get(matrixEntry.getKey()),
                        String.format(
                                pathOutputMerged,
                                UtilsEncode.toRuntimeCharset(matrixEntry.getKey())
                        ),
                        false,
                        colorMode);
            } catch (Exception ex) {
                System.out.println("Can't write " + ex);
            }

        }
        etalonMatrices = mergedMap;
        return this;
    }

    public BundleScenarios getBmpFromPdfFormat(String pathPdf, String pathBmp,
                                               int pageIndex, float dpi, ImageType imageType) {
        IFeeder feeder = new PDFBoxFeeder(pageIndex, dpi, imageType);
        feeder.feed(pathPdf, pathBmp);
        return this;
    }

    public BundleScenarios extractStringsToRecognize(String pathBmp, Rectangle bounds,
                                                     String colorMode, String cutMode,
                                                     int spaceSymbolTolerance, int averageWidth,
                                                     String outputDebugPath) {

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
                    SourceCutter.simpleCutString(stringsFromBlock.get(i), cutMode, colorMode, spaceSymbolTolerance, averageWidth, outputDebugPath)
            );
        }
        return this;
    }

    public BundleScenarios recognize(String pathToSaveEtalonAlphabetMatrices) {
        if (etalonMatrices == null) {
            etalonMatrices = DataStash.getLetterMatricesCollection();
        }
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
                                        pathToSaveEtalonAlphabetMatrices,
                                        UtilsEncode.toRuntimeCharset(matrixEntry.getKey())
                                ), false, BitmapUtils.COLOR_256
                        );
                    } catch (Exception ex) {
                        System.out.println(ex);
                    }
                }
                /*answer.append(Recognizer.recognizeByPercent(formattedMatrices,
                        Format.frameExtendPattern(toRecognizeMatrices.get(i)),
                        BitmapUtils.COLOR_256
                ));
*/
                answer.append(Recognizer.recognize(formattedMatrices,
                        Format.frameExtendPattern(toRecognizeMatrices.get(i))
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

    public BundleScenarios recognizeSimple(String pathToSaveEtalonAlphabetMatrices) {
        if (etalonMatrices == null) {
            etalonMatrices = DataStash.getLetterMatricesCollection();
        }
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
                                        pathToSaveEtalonAlphabetMatrices,
                                        UtilsEncode.toRuntimeCharset(matrixEntry.getKey())
                                ), false, BitmapUtils.COLOR_256
                        );
                    } catch (Exception ex) {
                        System.out.println(ex);
                    }
                }
                answer.append(Recognizer.recognizeSimple(formattedMatrices,
                        Format.frameExtendPattern(toRecognizeMatrices.get(i))
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

    List<List<int[]>> toRecognizeMatricesModif;

    private int w;
    private int h;

    public BundleScenarios cornerizeModels(int width, int height) {
        w = width;
        h = height;
        Map<String, List<int[]>> currMap;
        List<Map<String, List<int[]>>> currList = new ArrayList<>();

        for (int i = 0; i < letterMatricesCollectionsThis.size(); i++) {
            currMap = new HashMap<>();
            for (Map.Entry<String, List<int[]>> matrixEntry : letterMatricesCollectionsThis.get(i).entrySet()) {
                currMap.put(matrixEntry.getKey(), TrainSet.cornerizeTrimModel(
                        matrixEntry.getValue(), width, height
                ));
            }
            currList.add(currMap);
        }
        letterMatricesCollectionsThis = currList;

        toRecognizeMatricesModif = new ArrayList<>();
        for (int i = 0; i < toRecognizeMatrices.size(); i++) {
            if (toRecognizeMatrices.get(i).size() > 0) {
                toRecognizeMatricesModif.add(TrainSet.cornerizeTrimModel(toRecognizeMatrices.get(i),
                        width, height));
            }
        }
        toRecognizeMatrices = toRecognizeMatricesModif;
        return this;
    }

    public BundleScenarios neuralTrain(
            String inputToHiddenWeightsPathSave,
            String hiddenToOutputPathSave,
            int hiddenNodes,
            int outputNodes,
            int overTrainIterations
    ) {
        this.inputToHiddenSourcePath = inputToHiddenWeightsPathSave;
        this.hiddenToOutputSourcePath = hiddenToOutputPathSave;

        NeuralRecognizer.initNet(w, h, hiddenNodes, outputNodes);
        NeuralRecognizer.createNet();
        NeuralRecognizer.testSet(letterMatricesCollectionsThis, overTrainIterations);

        try {
            CSVProcessorIO.writeMatrixToCSVFile(
                    NeuralRecognizer.getInputToHiddenWeights(),
                    inputToHiddenWeightsPathSave
            );
            CSVProcessorIO.writeMatrixToCSVFile(
                    NeuralRecognizer.getHiddenToOutputWeights(),
                    hiddenToOutputPathSave
            );
        } catch (Exception ex) {
            System.out.println("Can't write weight model files");
        }
        return this;
    }

    private String inputToHiddenSourcePath;
    private String hiddenToOutputSourcePath;

    public BundleScenarios neuralRecognize() {
        StringBuilder answer = new StringBuilder();
        for (int i = 0; i < toRecognizeMatricesModif.size(); i++) {
            answer.append(
                    NeuralRecognizer.recognize(toRecognizeMatricesModif.get(i))
            );
        }
        recognized = answer.toString();
        return this;
    }

    public BundleScenarios neuralRecognize(String inputToHiddenSource,
                                           String hiddenToOutputSource) {
        StringBuilder answer = new StringBuilder();
        NeuralRecognizer.loadTrained(
                inputToHiddenSource,
                hiddenToOutputSource
        );
        for (int i = 0; i < toRecognizeMatricesModif.size(); i++) {
            answer.append(
                    NeuralRecognizer.recognize(
                            toRecognizeMatricesModif.get(i)
                    )
            );
        }
        this.neuralRecognize();
        return this;
    }
}
