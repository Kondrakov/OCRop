package feed;

import parser.BitmapParser;

import java.util.*;

public class DataStash {

    public static String FROM_CSV_MODE = "from_csv_mode";
    public static String FROM_BMP_MODE = "from_bmp_mode";

    //todo rewrite colorMode and outputPath must not required in csv
    public static void prepareEtalonModels(String inputPath, String outputPath,
                                           List<String> alphabet, String sourceMode, String colorMode) {
        try {
            letterMatricesCollection = new HashMap<>();
            for (int i = 0; i < alphabet.size(); i++) {
                if (FROM_BMP_MODE.equals(sourceMode)) {
                    stashBMPAsCSV(String.format(inputPath, alphabet.get(i)),
                            String.format(outputPath, alphabet.get(i)),
                            true,
                            true,
                            colorMode);
                    letterMatricesCollection.put(alphabet.get(i),
                            CSVProcessorIO.loadMatrixFromCSVFile(
                                    String.format(outputPath, alphabet.get(i))
                            )
                    );
                }

                if (FROM_CSV_MODE.equals(sourceMode)) {
                    letterMatricesCollection.put(alphabet.get(i),
                            CSVProcessorIO.loadMatrixFromCSVFile(
                                    String.format(inputPath, alphabet.get(i))
                            )
                    );
                }
            }
        } catch (Exception ex) {
            System.out.println("exeption: " + ex);
        }
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