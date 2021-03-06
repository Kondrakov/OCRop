package com.github.kondrakov.scenario;

import com.github.kondrakov.approximate.TrainSet;
import com.github.kondrakov.feed.*;
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

    private Map<String, List<int[]>> letterMatricesCollectionForTraining;
    private List<Map<String, List<int[]>>> letterMatricesCollectionsForTraining;

    public BundleScenarios loadAlphabetModel(String basePathFrom, String basePathTo,
                                             String createMode,
                                             List<String> alphabetRange, String sourceMode,
                                             String colorMode) {
        DataStash.prepareReferenceModels(basePathFrom, basePathTo, createMode, alphabetRange, sourceMode, colorMode);
        letterMatricesCollectionForTraining = DataStash.getLetterMatricesCollection(); //used in single collection only 'NEW' mode
        letterMatricesCollectionsForTraining = DataStash.getLetterMatricesCollections(); //used in multi collections only 'ADD' mode
        return this;
    }

    public BundleScenarios loadAndMergeAlphabetModel(String basePathFrom1, String basePathTo1,
                                                     String basePathFrom2, String basePathTo2,
                                                     String pathOutputMerged,
                                             List<String> alphabetRange, String sourceMode,
                                             String colorMode) {
        DataStash.prepareReferenceModels(
                basePathFrom1, basePathTo1,
                basePathFrom2, basePathTo2,
                alphabetRange, sourceMode, colorMode);

        List<Map<String, List<int[]>>> letterMatricesCollections = DataStash.getLetterMatricesCollections();
        letterMatricesCollectionsForTraining = letterMatricesCollections;
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
        referenceMatrices = mergedMap;
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
        //todo check cut logic, to cut string down to up maybe? No, it's not work for Ё Й similar symbols, tolerance logic maybe?
        List<List<int[]>> stringsFromBlock =
                SourceCutter.simpleCutBlockIntoStrings(blockMatrixInverted);

        toRecognizeMatrices = new ArrayList<>();
        for (int i = 0; i < stringsFromBlock.size(); i++) {
            if (SourceCutter.NO_GAP_SEARCH_CUT.equals(cutMode)) {
                toRecognizeMatrices.addAll(
                        SourceCutter.simpleCutString(stringsFromBlock.get(i), cutMode, colorMode, spaceSymbolTolerance, averageWidth, outputDebugPath)
                );
            } else if (SourceCutter.NO_CHECK_GAP_CUT.equals(cutMode)) {
                //todo check cut into strings compare with simpleCutString (it is compatible with block cutting into strings?)
                toRecognizeMatrices.addAll(
                        SourceCutter.simpleCutStringNoGapCheck(stringsFromBlock.get(i), cutMode, colorMode, spaceSymbolTolerance, averageWidth)
                );
            }

        }
        return this;
    }

    public BundleScenarios recognize(String pathToSaveReferenceAlphabetMatrices) {
        if (referenceMatrices == null) {
            referenceMatrices = DataStash.getLetterMatricesCollection();
        }
        StringBuilder answer = new StringBuilder();
        for (int i = 0; i < toRecognizeMatrices.size(); i++) {
            if (toRecognizeMatrices.get(i).size() > 0) {
                Map<String, List<int[]>> formattedMatrices = new HashMap<>();
                for (Map.Entry<String, List<int[]>> matrixEntry : referenceMatrices.entrySet()) {
                    formattedMatrices.put(matrixEntry.getKey(),
                            Format.frameToPattern(
                                    matrixEntry.getValue(), toRecognizeMatrices.get(i)
                            ));
                    try {
                        CSVProcessorIO.writeMatrixToCSVFile(
                                formattedMatrices.get(matrixEntry.getKey()),
                                String.format(
                                        pathToSaveReferenceAlphabetMatrices,
                                        UtilsEncode.toRuntimeCharset(matrixEntry.getKey())
                                ), false, BitmapUtils.COLOR_256
                        );
                    } catch (Exception ex) {
                        System.out.println(ex);
                    }
                }
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

    public BundleScenarios recognizeSimple(String pathToSaveReferenceAlphabetMatrices) {
        if (referenceMatrices == null) {
            referenceMatrices = DataStash.getLetterMatricesCollection();
        }
        StringBuilder answer = new StringBuilder();
        for (int i = 0; i < toRecognizeMatrices.size(); i++) {
            if (toRecognizeMatrices.get(i).size() > 0) {
                Map<String, List<int[]>> formattedMatrices = new HashMap<>();
                for (Map.Entry<String, List<int[]>> matrixEntry : referenceMatrices.entrySet()) {
                    formattedMatrices.put(matrixEntry.getKey(),
                            Format.frameToPattern(
                                    matrixEntry.getValue(), toRecognizeMatrices.get(i)
                            ));
                    try {
                        CSVProcessorIO.writeMatrixToCSVFile(
                                formattedMatrices.get(matrixEntry.getKey()),
                                String.format(
                                        pathToSaveReferenceAlphabetMatrices,
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
    private Map<String, List<int[]>> referenceMatrices;

    private String recognized;
    public String getRecognized() {
        return this.recognized;
    }

    public static List<int[]> spaceCleanAndFix(List<int[]> input, double percentTolerance,
                                               int width, int height) {
        double filledCounter = 0.0d;
        for (int i = 0; i < input.size(); i++) {
            for (int j = 0; j < input.get(i).length; j++) {
                if (input.get(i)[j] > 0) {
                    filledCounter ++;
                }
            }
        }
        double base = (double) (width * height);
        double percent = filledCounter * 100 / base;
        if (percent < percentTolerance) {
            return TrainSet.canvasUniformFill(0, width, height);
        } else {
            //bypass
            return input;
        }
    }

    List<List<int[]>> toRecognizeMatricesModif;

    private int width;
    private int height;

    public BundleScenarios cornerizeModelsForTraining(int width, int height) {
        this.width = width;
        this.height = height;
        Map<String, List<int[]>> currMap;
        List<Map<String, List<int[]>>> currList = new ArrayList<>();

        for (int i = 0; i < letterMatricesCollectionsForTraining.size(); i++) {
            currMap = new HashMap<>();
            for (Map.Entry<String, List<int[]>> matrixEntry : letterMatricesCollectionsForTraining.get(i).entrySet()) {
                currMap.put(matrixEntry.getKey(), TrainSet.cornerizeTrimModel(
                        matrixEntry.getValue(), width, height
                ));
            }
            currList.add(currMap);
        }
        letterMatricesCollectionsForTraining = currList;
        return this;
    }

    public BundleScenarios cornerizeModelsToRecognize(int width, int height,
                                                      double spaceFixTolerance) {
        toRecognizeMatricesModif = new ArrayList<>();
        for (int i = 0; i < toRecognizeMatrices.size(); i++) {
            toRecognizeMatrices.set(i,
                    spaceCleanAndFix(
                            toRecognizeMatrices.get(i),
                            spaceFixTolerance, width, height
                    )
            );

            if (toRecognizeMatrices.get(i).size() > 0) {
                toRecognizeMatricesModif.add(TrainSet.cornerizeTrimModel(toRecognizeMatrices.get(i),
                        width, height));
            }
        }
        toRecognizeMatrices = toRecognizeMatricesModif;
        return this;
    }

    public BundleScenarios centerizeModelsForTraining(int width, int height) {
        this.width = width;
        this.height = height;
        Map<String, List<int[]>> currMap;
        List<Map<String, List<int[]>>> currList = new ArrayList<>();

        for (int i = 0; i < letterMatricesCollectionsForTraining.size(); i++) {
            currMap = new HashMap<>();
            for (Map.Entry<String, List<int[]>> matrixEntry : letterMatricesCollectionsForTraining.get(i).entrySet()) {
                currMap.put(matrixEntry.getKey(), TrainSet.centerizeTrimModel(
                        matrixEntry.getValue(), width, height
                ));
            }
            currList.add(currMap);
        }
        letterMatricesCollectionsForTraining = currList;
        return this;
    }

    public BundleScenarios centerizeModelsToRecognize(int width, int height,
                                                      double spaceFixTolerance) {
        toRecognizeMatricesModif = new ArrayList<>();
        for (int i = 0; i < toRecognizeMatrices.size(); i++) {
            if (toRecognizeMatrices.get(i).size() > 0) {

                //use filter instead?:
                toRecognizeMatrices.set(i,
                    spaceCleanAndFix(
                            toRecognizeMatrices.get(i),
                            spaceFixTolerance, width, height
                    )
                );

                toRecognizeMatricesModif.add(TrainSet.centerizeTrimModel(toRecognizeMatrices.get(i),
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

        NeuralRecognizer.initNet(width, height, hiddenNodes, outputNodes);
        NeuralRecognizer.createNet();
        NeuralRecognizer.testSet(letterMatricesCollectionsForTraining, overTrainIterations);

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
