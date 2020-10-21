package com.github.kondrakov.feed;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import com.github.kondrakov.parser.BitmapUtils;
import com.github.kondrakov.utils.UtilsConv;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CSVProcessorIO {

    public static List<int[]> loadMatrixFromCSVFile(String inputPath) {
        List<int[]> matrix = new ArrayList<>();
        String[] currentRow = new String[0];
        try {
            CSVReader reader = new CSVReader(new FileReader(inputPath), ';', '"', 0);
            boolean flagUTFRem = false;
            while (currentRow != null) {
                currentRow = reader.readNext();
                if (currentRow != null) {
                    if (!flagUTFRem) {
                        flagUTFRem = true;
                        currentRow = removeUTFByte(currentRow);
                    }
                    matrix.add(UtilsConv.arrStrToArrInt(currentRow));
                }
            }
        } catch (IOException ex) {
            System.out.println("IOException exception loadMatrixFromCSVFile: " + ex);
        }
        return matrix;
    }

    public static List<String> loadStringListFromCSVFile(String inputPath) {
        List<String> list = new ArrayList<>();
        String[] currentRow = new String[0];
        try {
            CSVReader reader = new CSVReader(new FileReader(inputPath), ';', '"', 0);
            boolean flagUTFRem = false;
            while (currentRow != null) {
                currentRow = reader.readNext();
                if (currentRow != null) {
                    if (!flagUTFRem) {
                        flagUTFRem = true;
                        currentRow = removeUTFByte(currentRow);
                    }
                    list.add(currentRow[0]);
                }
            }
        } catch (IOException ex) {
            System.out.println("IOException exception loadStringListFromCSVFile: " + ex);
        }
        return list;
    }

    public static Map<String, String> loadKeyValueStringsFromCSVFile(String inputPath) {
        Map<String, String> map = new HashMap<>();
        String[] currentRow = new String[0];
        try {
            CSVReader reader = new CSVReader(new FileReader(inputPath), ';', '"', 0);
            while (currentRow != null) {
                currentRow = reader.readNext();
                if (currentRow != null) {
                    map.put(currentRow[0], currentRow[1]);
                }
            }
        } catch (IOException ex) {
            System.out.println("IOException exception loadKeyValueStringsFromCSVFile: " + ex);
        }
        return map;
    }

    public static void writeMatrixToCSVFile(List<int[]> inputMatrix, String outputPath, boolean invertMode, String colorMode) throws Exception {
        CSVWriter writer = createCSVWriter(outputPath);
        for (int i = 0; i < inputMatrix.size(); i++) {
            if (invertMode) {
                for (int j = 0; j < inputMatrix.get(i).length; j++) {
                    inputMatrix.get(i)[j] = BitmapUtils.invert(inputMatrix.get(i)[j], colorMode);
                }
            }
            writer.writeNext(UtilsConv.arrIntToArrStr(inputMatrix.get(i)));
        }
        writer.close();
    }

    /**
         Writing for matrices of trained weights
     */
    public static void writeMatrixToCSVFile(double[][] inputMatrix, String outputPath) throws Exception {
        CSVWriter writer = createCSVWriter(outputPath);
        for (int i = 0; i < inputMatrix.length; i++) {
            writer.writeNext(UtilsConv.arrDoubleToArrStr(inputMatrix[i]));
        }
        writer.close();
    }

    /**
        Reading for matrices of trained weights
     */
    public static double[][] loadMatrixDoubleFromCSVFile(String inputPath) {
        //List<int[]> matrix = new ArrayList<>();
        List<double[]> matrix = new ArrayList<>();
        double[][] matrixOutput = new double[0][0];
        String[] currentRow = new String[0];
        try {
            CSVReader reader = new CSVReader(new FileReader(inputPath), ';', '"', 0);
            boolean flagUTFRem = false;
            while (currentRow != null) {
                currentRow = reader.readNext();
                if (currentRow != null) {
                    if (!flagUTFRem) {
                        flagUTFRem = true;
                        currentRow = removeUTFByte(currentRow);
                    }
                    matrix.add(UtilsConv.arrStrToArrDouble(currentRow));
                }
            }
            matrixOutput = new double[matrix.size()][matrix.get(0).length];
            for (int i = 0; i < matrix.size(); i++) {
                for (int j = 0; j < matrix.get(i).length; j++) {
                    matrixOutput[i][j] = matrix.get(i)[j];
                }
            }
        } catch (IOException ex) {
            System.out.println("IOException exception loadMatrixFromCSVFile: " + ex);
        }
        return matrixOutput;
    }

    public static CSVWriter createCSVWriter(String path) throws IOException {
        //for correct unicode symbols and no encoding problems write \uFEFF:
        FileOutputStream unicodeSymbols = new FileOutputStream(
                new File(path)
        );
        unicodeSymbols.write(0xef);
        unicodeSymbols.write(0xbb);
        unicodeSymbols.write(0xbf);
        return new CSVWriter(
            new OutputStreamWriter(unicodeSymbols),
            ';',
            '"'
        );
    }

    public static String[] removeUTFByte(String[] arrStr) {
        if (arrStr.length > 0) {
            if (
                    arrStr[0].length() > 1 &&
                    arrStr[0].substring(0, 1).charAt(0) == "\uFEFF".charAt(0)
            ) {
                arrStr[0] = arrStr[0].substring(1);
            }
        }
        return arrStr;
    }
}