package com.github.kondrakov.feed;

import com.github.kondrakov.parser.BitmapParser;
import com.github.kondrakov.utils.UtilsEncode;

import java.util.*;

public class DataStash {

    public static String FROM_CSV_MODE = "from_csv_mode";
    public static String FROM_BMP_MODE = "from_bmp_mode";

    //todo rewrite colorMode and outputPath must not required in csv
    public static void prepareEtalonModels(String inputPath, String outputPath,
                                           List<String> alphabet, String sourceMode, String colorMode) {
        letterMatricesCollection = prepareEtalonModel(inputPath, outputPath, alphabet, sourceMode, colorMode);
    }

    public static void prepareEtalonModelsForMerge(String inputPath1, String outputPath1,
                                                   String inputPath2, String outputPath2,
                                           List<String> alphabet, String sourceMode, String colorMode) {
        letterMatricesCollections = new ArrayList<>();
        letterMatricesCollections.add(
                prepareEtalonModel(inputPath1, outputPath1, alphabet, sourceMode, colorMode)
        );
        letterMatricesCollections.add(
                prepareEtalonModel(inputPath2, outputPath2, alphabet, sourceMode, colorMode)
        );
    }

    public static HashMap<String, List<int[]>> prepareEtalonModel(String inputPath, String outputPath,
                                           List<String> alphabet, String sourceMode, String colorMode) {
        HashMap<String, List<int[]>> etalonModelMatrices = new HashMap<>();
        try {
            for (int i = 0; i < alphabet.size(); i++) {
                if (FROM_BMP_MODE.equals(sourceMode)) {
                    stashBMPAsCSV(String.format(inputPath, UtilsEncode.toRuntimeCharset(alphabet.get(i))),
                            String.format(outputPath, UtilsEncode.toRuntimeCharset(alphabet.get(i))),
                            true,
                            true,
                            colorMode);
                    etalonModelMatrices.put(alphabet.get(i),
                            CSVProcessorIO.loadMatrixFromCSVFile(
                                    String.format(outputPath, UtilsEncode.toRuntimeCharset(alphabet.get(i)))
                            )
                    );
                }

                if (FROM_CSV_MODE.equals(sourceMode)) {
                    etalonModelMatrices.put(alphabet.get(i),
                            CSVProcessorIO.loadMatrixFromCSVFile(
                                    String.format(inputPath, UtilsEncode.toRuntimeCharset(alphabet.get(i)))
                            )
                    );
                }
            }
        } catch (Exception ex) {
            System.out.println("exeption: " + ex);
        }
        return etalonModelMatrices;
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