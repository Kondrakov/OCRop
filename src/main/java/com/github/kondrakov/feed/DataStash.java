package com.github.kondrakov.feed;

import com.github.kondrakov.parser.BitmapParser;
import com.github.kondrakov.utils.UtilsEncode;

import java.util.*;

public class DataStash {

    public static String FROM_CSV_MODE = "from_csv_mode";
    public static String FROM_BMP_MODE = "from_bmp_mode";

    public static String NEW = "new";
    public static String ADD = "add";

    //todo rewrite colorMode and outputPath must not required in csv
    public static void prepareReferenceModels(String inputPath, String outputPath, String createMode,
                                              List<String> alphabet, String sourceMode, String colorMode) {
        if (DataStash.NEW.equals(createMode)) {
            letterMatricesCollection = prepareReferenceModel(inputPath, outputPath, alphabet, sourceMode, colorMode);
        } else if (DataStash.ADD.equals(createMode)) {
            if (letterMatricesCollections == null) {
                letterMatricesCollections = new ArrayList<>();
            }
            letterMatricesCollections.add(
                    prepareReferenceModel(inputPath, outputPath, alphabet, sourceMode, colorMode)
            );
        }
    }

    public static void prepareReferenceModels(String inputPath1, String outputPath1,
                                              String inputPath2, String outputPath2,
                                              List<String> alphabet, String sourceMode, String colorMode) {
        letterMatricesCollections = new ArrayList<>();
        letterMatricesCollections.add(
                prepareReferenceModel(inputPath1, outputPath1, alphabet, sourceMode, colorMode)
        );
        letterMatricesCollections.add(
                prepareReferenceModel(inputPath2, outputPath2, alphabet, sourceMode, colorMode)
        );
    }

    public static HashMap<String, List<int[]>> prepareReferenceModel(String inputPath, String outputPath,
                                                     List<String> alphabet, String sourceMode, String colorMode) {
        HashMap<String, List<int[]>> referenceModelMatrices = new HashMap<>();
        try {
            for (int i = 0; i < alphabet.size(); i++) {
                if (FROM_BMP_MODE.equals(sourceMode)) {
                    stashBMPAsCSV(String.format(inputPath, UtilsEncode.toRuntimeCharset(alphabet.get(i))),
                            String.format(outputPath, UtilsEncode.toRuntimeCharset(alphabet.get(i))),
                            true,
                            true,
                            colorMode);
                    referenceModelMatrices.put(alphabet.get(i),
                            CSVProcessorIO.loadMatrixFromCSVFile(
                                    String.format(outputPath, UtilsEncode.toRuntimeCharset(alphabet.get(i)))
                            )
                    );
                }

                if (FROM_CSV_MODE.equals(sourceMode)) {
                    referenceModelMatrices.put(alphabet.get(i),
                            CSVProcessorIO.loadMatrixFromCSVFile(
                                    String.format(inputPath, UtilsEncode.toRuntimeCharset(alphabet.get(i)))
                            )
                    );
                }
            }
        } catch (Exception ex) {
            System.out.println("exeption: " + ex);
        }
        return referenceModelMatrices;
    }

    private static List<Map<String, List<int[]>>> letterMatricesCollections;
    public static List<Map<String, List<int[]>>> getLetterMatricesCollections() {
        return letterMatricesCollections;
    }

    private static Map<String, List<int[]>> letterMatricesCollection;
    public static Map<String, List<int[]>> getLetterMatricesCollection() {
        return letterMatricesCollection;
    }

    public static void stashBMPAsCSV(String inputPath, String outputPath,
                                     boolean noHeaderMode, boolean invertMode, String colorMode) throws Exception {
        List<int[]> matrixRows = BitmapParser.parse(inputPath, outputPath, colorMode);
        if (noHeaderMode) {
            CSVProcessorIO.writeMatrixToCSVFile(matrixRows, outputPath, invertMode, colorMode);
        } else {
            throw new Exception("Mode not supported");
        }
    }
}