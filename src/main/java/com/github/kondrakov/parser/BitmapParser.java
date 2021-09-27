package com.github.kondrakov.parser;

import com.github.kondrakov.validation.BitmapDataParsingException;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class BitmapParser {

    static int[] headerStartBMP_BM;
    static {
        headerStartBMP_BM = new int[] { 0x42, 0x4d };
    }

    /**
     * Method for parse bmp file and convert file content to two-dimensional matrix with values of
     * pixels from bmp. Header of bmp not to be included in resulting matrix.
     * @param inputFileName path of bmp to parse
     * @param outputFileName path for saving resulting matrix content csv file
     * @param colorMode mandatory bmp color mode of pixels. To be match with color mode from header
     *                  of processed bmp for checking bmp files right color mode
     * @return result of parsing in matrix format
     **/
    public static List<int[]> parse(String inputFileName, String outputFileName, String colorMode) {
        // todo refactor, extend image size field parse, add palette parse
        List<int[]> rawImageMatrix = new LinkedList<>();
        int byteCounterGlobal = 0;
        int startImageMatrixInfoOffset = 10;
        int startImageMatrixOffset = -1; //default, must be reinitialized in InputStream com.github.kondrakov.parser
        int bitPerPixelInfoOffset = 29; //(can be 29-30)
        int widthImageMatrixInfo = 18;
        int imageMatrixInColorPointsWidth = -1; //default, must be reinitialized in InputStream com.github.kondrakov.parser
        int imageMatrixWidthWithTrailingBytes = -1;
        int widthCounter = 0;
        String widthToParse = "";
        byte[] arrBytesWidth = null;

        FileInputStream fileInputString;
        FileOutputStream fileOutputStream = null;
        int[] colorRowBuffer = new int[0];
        try {
            fileInputString = new FileInputStream(inputFileName);
            if (outputFileName != null || !"".equals(outputFileName)) {
                fileOutputStream = new FileOutputStream(outputFileName);
            }
            int nextReadByte;

            while (fileInputString.available() > 0) {
                nextReadByte = fileInputString.read();
                if (byteCounterGlobal == 0 || byteCounterGlobal == 1) {
                    if (nextReadByte != headerStartBMP_BM[byteCounterGlobal]) {
                        throw new IOException("File has not BitMap header, verify file please");
                    }
                }
                if (bitPerPixelInfoOffset - 1 == byteCounterGlobal) {
                    BitmapUtils.assertRightColorMode(colorMode, nextReadByte);
                }
                if (startImageMatrixInfoOffset - 1 == byteCounterGlobal) {
                    //System.out.println(" + " + Integer.toHexString(nextReadByte));
                    if (fileOutputStream != null)
                        fileOutputStream.write(nextReadByte);
                    byteCounterGlobal++;
                    arrBytesWidth = new byte[2];
                    fileInputString.read(arrBytesWidth);
                    startImageMatrixOffset = arrBytesWidth[0] & 0xFF | (arrBytesWidth[1] & 0xFF) << 8;
                    //System.out.println(" ++ " + Integer.toHexString(startImageMatrixOffset));
                }

                if (byteCounterGlobal == widthImageMatrixInfo - 1) {
                    //todo rewrite: remove this if and its contents (maybe rewrite all com.github.kondrakov.parser logic on pre-count byte, and read by bytearray always)
                    if (startImageMatrixOffset == -1 || byteCounterGlobal < startImageMatrixOffset) {
                        if (fileOutputStream != null)
                            fileOutputStream.write(nextReadByte);
                    }
                    byteCounterGlobal++;
                    arrBytesWidth = new byte[2];
                    fileInputString.read(arrBytesWidth);
                    imageMatrixInColorPointsWidth = arrBytesWidth[0] & 0xFF | (arrBytesWidth[1] & 0xFF) << 8;

                    imageMatrixWidthWithTrailingBytes =
                            calculateWidthWithTrailing(imageMatrixInColorPointsWidth, colorMode);
                }
                if (startImageMatrixOffset > -1 && byteCounterGlobal >= startImageMatrixOffset) {
                    if (widthCounter == 0) {
                        colorRowBuffer = new int[imageMatrixWidthWithTrailingBytes];
                    }
                    colorRowBuffer[widthCounter] = nextReadByte;

                    widthCounter ++;
                    if (widthCounter >= imageMatrixWidthWithTrailingBytes) {
                        //reverse rows, defined by bmp com.github.kondrakov.format spec
                        if (BitmapUtils.COLOR_16.equals(colorMode)) {
                            rawImageMatrix.add(0, colorDotsAddRowParse_16(colorRowBuffer,
                                    imageMatrixInColorPointsWidth));
                        } else if (BitmapUtils.COLOR_256.equals(colorMode)) {
                            rawImageMatrix.add(0, colorDotsAddRowParse_256(colorRowBuffer,
                                    imageMatrixInColorPointsWidth));
                        }
                        widthCounter = 0;

                    }
                }

                //System.out.println(Integer.toHexString(nextReadByte));
                //write header
                    /*if (startImageMatrixOffset == -1 || byteCounterGlobal < startImageMatrixOffset)
                        if (fileOutputStream != null)
                            fileOutputStream.write(nextReadByte);*/


                //write header
                if (startImageMatrixOffset == -1 || byteCounterGlobal < startImageMatrixOffset) {
                    if (arrBytesWidth != null) {
                        if (fileOutputStream != null)
                            for (int i = 0; i < arrBytesWidth.length; i++) {
                                fileOutputStream.write(arrBytesWidth[i]);
                            }
                        byteCounterGlobal += arrBytesWidth.length;
                        arrBytesWidth = null;
                    } else {
                        //if (startImageMatrixOffset == -1 || byteCounterGlobal < startImageMatrixOffset)
                        if (fileOutputStream != null)
                            fileOutputStream.write(nextReadByte);
                        byteCounterGlobal++;
                    }
                }
            }

            fileInputString.close();
            if (fileOutputStream != null)
                fileOutputStream.close();
        } catch (BitmapDataParsingException ex) {
            System.out.println(ex.getMessage());
        } catch (IOException ex) {
            System.out.println("Not found BMP file to parse or output path to write bytes is not exists (see error message): " +
                    ex.getMessage());
        }
        return rawImageMatrix;
    }

    /**
     * Method calculates width of image in bytes by width in pixels and color mode including trailing bytes
     * @param realWidth width of image in pixels
     * @param colorMode color mode defining of bytes per pixel ratio
     * @return quantity of bytes (width in bytes) with trailing
     **/
    private static int calculateWidthWithTrailing(int realWidth, String colorMode) {
        int minDiscreteColorLength = (int) (4d / bytesPerColor(colorMode));

        int requiredWidthByTrailingRule = (realWidth / minDiscreteColorLength) * minDiscreteColorLength;
        if (realWidth % minDiscreteColorLength > 0) {
            requiredWidthByTrailingRule += minDiscreteColorLength;
        }
        return (int) ((double) requiredWidthByTrailingRule * bytesPerColor(colorMode));
    }

    /**
     * Method calculates bytes per one pixel for encoding it color
     * @param colorMode color mode defining of bytes per pixel ratio
     * @return quantity of bytes per one pixel
     **/
    private static double bytesPerColor(String colorMode) {
        if (BitmapUtils.COLOR_16.equals(colorMode)) {
            return 0.5;
        }
        if (BitmapUtils.COLOR_256.equals(colorMode)) {
            return 1.0;
        }
        return 1.0;
    }

    /**
     * Method parses bytes of one image row, cuts trailing bytes and converts it to array of pixels for 16 colors mode
     * @param byteRowBuffer one pixel high row of bytes that represents one horizontal line of image with trailing bytes
     * @param rowRealWidth image width in dots of color, not in bytes
     * @return array of pixels, each item of array is a color of one pixel in a 16 color mode
     **/
    private static int[] colorDotsAddRowParse_16(int[] byteRowBuffer, int rowRealWidth) {
        int[] row = new int[rowRealWidth];
        int byteCounter = 0;

        for (int i = 0; i < byteRowBuffer.length; i++) {
            //System.out.println(Integer.toHexString(byteRowBuffer[i]));
            String byteStr = Integer.toHexString(byteRowBuffer[i]);
            if (byteStr.length() == 1) {
                byteStr = "0" + byteStr;
            }
            String firstColor = byteStr.substring(0, 1);
            if (row.length > byteCounter)
                row[byteCounter] = Integer.valueOf(firstColor, 16);
            else
                break;
            byteCounter ++;

            String secondColor = byteStr.substring(1);
            if (row.length > byteCounter)
                row[byteCounter] = Integer.valueOf(secondColor, 16);
            else
                break;
            byteCounter ++;
        }
        return row;
    }

    /**
     * Method parses bytes of one image row, cuts trailing bytes and converts it to array of pixels for 256 colors mode
     * @param byteRowBuffer one pixel high row of bytes that represents one horizontal line of image with trailing bytes
     * @param rowRealWidth image width in dots of color, not in bytes
     * @return array of pixels, each item of array is a color of one pixel in a 256 color mode
     **/
    private static int[] colorDotsAddRowParse_256(int[] byteRowBuffer, int rowRealWidth) {
        int[] row = new int[rowRealWidth];
        for (int i = 0; i < byteRowBuffer.length; i++) {
            String byteStr = Integer.toHexString(byteRowBuffer[i]);
            if (row.length > i)
                row[i] = Integer.valueOf(byteStr, 16);
            else
                break;
        }
        return row;
    }

    /**
     * Method assembles matrix of color pixels into bmp file
     * @param matrixInput matrix of color pixels
     * @param headerPrototypeFileName chunk of bytes that represents a right meta data for assembling bmp
     *                                note: can contains standard bmp header without bmp body inside only
     *                                note: header data for matrixInput can consist of right metadata only
     * @param colorMode color mode that defines colors per pixel
     **/
    public static void assembleBitmap(List<int[]> matrixInput, String headerPrototypeFileName, String outputFileName, String colorMode) {
        List<int[]> matrix = new LinkedList<>();
        // todo refactor, add output color mode, add header generation
        // todo colorMode 256 write mode not implemented here
        /*Stream.iterate(matrixInput.size() - 1, x -> x - 1)
            .limit(matrixInput.size())
            .forEach(x -> matrix.add(matrixInput.get(x)));*/
        matrix = matrixInput;

        //output disabled temporarily
        /*for (int i = 0; i < matrix.size(); i++) {
            for (int j = 0; j < matrix.get(0).length; j++) {
                System.out.print(" " + matrix.get(i)[j]);
            }
            System.out.println();
        }*/

        //todo file size bytes define

        int byteCounterGlobal = 0;
        int widthImageMatrixInfo = 18;
        int heightImageMatrixInfo = 22;

        int coloredBytesRowLength = 0;
        int bytesRowFullLength = 0;
        if (BitmapUtils.COLOR_16.equals(colorMode)) {
            FileInputStream fileInputString;
            FileOutputStream fileOutputStream;
            try {
                fileInputString = new FileInputStream(headerPrototypeFileName);
                fileOutputStream = new FileOutputStream(outputFileName);
                int nextReadByte;

                while (fileInputString.available() > 0) {
                    nextReadByte = fileInputString.read();
                    if (byteCounterGlobal == widthImageMatrixInfo) {
                        int widthWithBasicTrailing = matrix.get(0).length;
                        if (matrix.get(0).length % 2 != 0) {
                            widthWithBasicTrailing += 1;
                        }
                        coloredBytesRowLength = (int) (((double) widthWithBasicTrailing) * bytesPerColor(BitmapUtils.COLOR_16));
                        bytesRowFullLength = ((int) Math.ceil(((double) coloredBytesRowLength) / 4d)) * 4;
                        //todo write bytearray instead of byte:
                        //nextReadByte = matrix.get(0).length;
                    }
                    if (byteCounterGlobal == heightImageMatrixInfo) {
                        //todo write bytearray instead of byte:
                        //nextReadByte = matrix.size();
                    }
                    fileOutputStream.write(nextReadByte);

                    byteCounterGlobal ++;
                }
                for (int i = 0; i < matrix.size(); i++) {
                    String byteToWrite = "";
                    for (int j = 0; j < matrix.get(i).length; j++) {
                        byteToWrite += Integer.toHexString(matrix.get(i)[j]);
                        if ((j + 1) % 2 == 0) {
                            System.out.print(" " + byteToWrite);
                            fileOutputStream.write(Integer.valueOf(byteToWrite, 16));
                            byteToWrite = "";
                        }
                    }
                    if (byteToWrite.length() == 1) {
                        byteToWrite += "0";
                        fileOutputStream.write(Integer.valueOf(byteToWrite, 16));
                    }
                    System.out.println();
                    int trailingLengthBytesCounter = coloredBytesRowLength;
                    while (trailingLengthBytesCounter < bytesRowFullLength) {
                        trailingLengthBytesCounter ++;
                        fileOutputStream.write(Integer.valueOf("00", 16));
                    }
                }
                fileInputString.close();
                fileOutputStream.close();
            } catch (IOException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }
}
