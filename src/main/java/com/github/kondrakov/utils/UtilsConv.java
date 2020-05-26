package com.github.kondrakov.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class UtilsConv {
    public static int[] arrStrToArrInt(String[] arrStr) {
        int[] arrInt = new int[arrStr.length];
        IntStream.range(0, arrInt.length).forEach(i -> arrInt[i] = Integer.parseInt(arrStr[i]));
        return arrInt;
    }

    public static String[] arrIntToArrStr(int[] arrInt) {
        String[] arrStr = new String[arrInt.length];
        IntStream.range(0, arrInt.length).forEach(i -> arrStr[i] = String.valueOf(arrInt[i]));
        return arrStr;
    }

    public static List<int[]> fitMatrixCanvas(List<int[]> inputMatrix, int width, int heigth) {
        int[] rowCash = null;
        for (int i = 0; i < heigth; i++) {
            if (inputMatrix.size() == i) {
                inputMatrix.add(new int[width]);
            }
            if (inputMatrix.get(i).length != width) {
                rowCash = inputMatrix.get(i);
                inputMatrix.remove(i);
                inputMatrix.add(i, new int[width]);
            }
            for (int j = 0; j < width; j++) {
                if (rowCash != null) {
                    if (j < rowCash.length) {
                        inputMatrix.get(i)[j] = rowCash[j];
                    } else  {
                        inputMatrix.get(i)[j] = 0;
                    }
                }
            }
            rowCash = null;
        }
        return inputMatrix;
    }

    public static List<int[]> cloneMatrixData(List<int[]> inputMatrix) {
        List<int[]> outputMatrix = new ArrayList<>();
        for (int i = 0; i < inputMatrix.size(); i++) {
            outputMatrix.add(i, inputMatrix.get(i).clone());
        }
        return outputMatrix;
    }
}