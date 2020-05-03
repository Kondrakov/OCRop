import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;
import utils.UtilsConv;

import java.awt.Point;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.List;
import java.util.stream.IntStream;

public class HelperA1 {
    public HelperA1() {

    }

    public List<int[]> traceClean() {

        matrixResult = new ArrayList<>();

        loadLetterMatrix();
        loadBlindMatrices();

        setRightAndLeftBlindsDrafts();
        setKeyPoints(matrixToFillWaves);
        setKeyPoints(matrixToFillWavesLeft);
        setKeyPoints(matrixToFillWavesRight);

        setBlindzonesForPathFind(matrixToFillWavesRight, matrixRawBlindClockwise);
        setBlindzonesForPathFind(matrixToFillWavesLeft, matrixRawBlindClockwiseReverse);

        waveStartToEnd(matrixToFillWavesRight);
        waveStartToEnd(matrixToFillWavesLeft);

        this.rightPath = pathByWaves(matrixToFillWavesRight, PATH_MODE_RIGHT_TO_START);
        this.leftPath = pathByWaves(matrixToFillWavesLeft, PATH_MODE_LEFT_TO_START);

        this.cleaningMatrix = UtilsConv.cloneMatrixData(matrixToFillWaves);

        return cleanExternalByPath();
    }

    private List<int[]> cleaningMatrix;

    private List<Point> rightPath;
    private List<Point> leftPath;

    private List<int[]> cleanExternalByPath() {
        visualise(this.cleaningMatrix);
        for (int i = 0; i < this.rightPath.size(); i++) {
            for (int j = (int) this.rightPath.get(i).getX() + 1;
                 j < this.cleaningMatrix.get(0).length;
                 j++) {
                this.cleaningMatrix.get((int) this.rightPath.get(i).getY())[j] = 0;
            }
        }
        for (int i = 0; i < this.leftPath.size(); i++) {
            for (int j = 0;
                 j < (int) this.leftPath.get(i).getX() - 1;
                 j++) {
                this.cleaningMatrix.get((int) this.leftPath.get(i).getY())[j] = 0;
            }
        }
        this.cleaningMatrix.get(0)[0] = 0;
        this.cleaningMatrix.get(this.cleaningMatrix.size() - 1)[this.cleaningMatrix.get(0).length - 1] = 0;


        visualise(this.cleaningMatrix);
        return this.cleaningMatrix;
    }

    public static final String PATH_MODE_RIGHT_TO_START = "pathModeRightToStart";
    public static final String PATH_MODE_LEFT_TO_START = "pathModeLeftToStart";

    private ListIterator pathMode;

    private List<Point> currentRotationSteps;
    private Point currentPathPoint;
    private Point currentBestNextRotationStep;

    private List<Point> pathByWaves(List<int[]> waves, String pathModeType) {
        if (PATH_MODE_RIGHT_TO_START.equals(pathModeType)) {
            this.pathMode = new PathMode(PathMode.COUNTER_CLOCKWISE, new Point(1, 0));
        } else if (PATH_MODE_LEFT_TO_START.equals(pathModeType)) {
            this.pathMode = new PathMode(PathMode.CLOCKWISE, new Point(-1, 0));
        }

        List<Point> path = new ArrayList<>();
        this.currentPathPoint = this.keyEndPointCoords;
        path.add(this.currentPathPoint);
        Point searchPoint;

        int maxWaveValue = -10000;
        int pathSize = -1;

        while (pathSize != path.size()) {
            pathSize = path.size();
            this.currentRotationSteps = new ArrayList<>();
            for (int i = 0; i < PathMode.standardDeltaPoints.size(); i++) {
                searchPoint = directionPointApply(this.currentPathPoint,
                        ((PathMode)this.pathMode).getCurrentDirection());

                if (searchPoint != null) {
                    if (waves.get((int) searchPoint.getY())[(int) searchPoint.getX()] > maxWaveValue) {
                        if (maxWaveValue < waves.get((int) searchPoint.getY())[(int) searchPoint.getX()] &&
                                waves.get((int) searchPoint.getY())[(int) searchPoint.getX()] < 0) {
                            maxWaveValue = waves.get((int) searchPoint.getY())[(int) searchPoint.getX()];
                            path.add(new Point((int) searchPoint.getX(), (int) searchPoint.getY()));
                        }
                    }
                }
                ((PathMode) this.pathMode).rotate();
            }
            if (!path.get(path.size() - 1).equals(this.currentPathPoint)) {
                this.currentPathPoint = path.get(path.size() - 1);
            }
        }
        return path;
    }

    private Point directionPointApply(Point currentPoint, Point directionPoint) {
        if (currentPoint.getX() + directionPoint.getX() >= 0 &&
                currentPoint.getX() + directionPoint.getX() < this.matrixToFillWaves.get(0).length &&
                currentPoint.getY() + directionPoint.getY() >= 0 &&
                currentPoint.getY() + directionPoint.getY() < this.matrixToFillWaves.size()) {
            return new Point((int) (currentPoint.getX() + directionPoint.getX()),
                    (int) (currentPoint.getY() + directionPoint.getY()));
        } else {
            return null;
        }
    }

    private List<int[]> matrixResult;

    private List<int[]> matrixToFillWavesLeft;
    private List<int[]> matrixToFillWavesRight;

    private List<int[]> matrixRawBlindClockwise;
    private List<int[]> matrixRawBlindClockwiseReverse;
    public void loadBlindMatrices() {
        matrixRawBlindClockwise = strToIntMatrix(parseRaw("threeQuartersClockwise.csv"));
        matrixRawBlindClockwiseReverse = strToIntMatrix(parseRaw("threeQuartersClockwiseReverse.csv"));
    }

    public void setRightAndLeftBlindsDrafts() {
        matrixToFillWavesLeft = UtilsConv.cloneMatrixData(matrixToFillWaves);
        matrixToFillWavesRight = UtilsConv.cloneMatrixData(matrixToFillWaves);
    }

    private void setBlindzonesForPathFind(List<int[]> inputMatrix, List<int[]> applyMatrix) {
        for (int i = 0; i < inputMatrix.size(); i++) {
            for (int j = 0; j < inputMatrix.get(i).length; j++) {
                if (applyMatrix.get(i)[j] == 1 && inputMatrix.get(i)[j] == 0) {
                    inputMatrix.get(i)[j] = applyMatrix.get(i)[j];
                }
            }
        }
    }

    private List<int[]> matrixToFillWaves;
    private int keyStartPoint = 10;
    private int keyEndPoint = 20;

    private Point keyStartPointCoords = new Point(0, 0);
    private Point keyEndPointCoords;

    public List<int[]> loadLetterMatrix() {
        //matrixToFillWaves = strToIntMatrix(parseRaw("inputRawCleanZeroShift.csv"));
        matrixToFillWaves = strToIntMatrix(parseRaw("inputRawCleanZeroR.csv"));
        return matrixToFillWaves;
    }

    public void setKeyPoints(List<int[]> inputMatrix) {
        keyEndPointCoords = new Point(inputMatrix.get(0).length - 1, inputMatrix.size() - 1);
        inputMatrix.get(((int) keyStartPointCoords.getY()))[((int) keyStartPointCoords.getX())] = keyStartPoint;
        inputMatrix.get(((int) keyEndPointCoords.getY()))[((int) keyEndPointCoords.getX())] = keyEndPoint;

        System.out.println("--------------- begin " + "setKeyPoints visualise");
        visualise(inputMatrix);
        System.out.println("--------------- end " + "setKeyPoints visualise");
    }

    public List<String[]> parseRaw(String path) {
        InputStreamReader reader;
        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(path);
            reader = new InputStreamReader(fileInputStream);
            List<String[]> inputs = new ArrayList<>();
            String[] input;
            String[] parsedInput;
            CSVReader csvReader = new CSVReader(reader);
            while ((input = csvReader.readNext()) != null) {
                parsedInput = strToArrStr(input[0]);
                inputs.add(parsedInput);
            }
            return inputs;
        } catch (IOException ex) {
            System.out.println(ex);
        }
        return null;
    }

    public static String[] strToArrStr(String str) {
        CSVParser parser = new CSVParser(";".charAt(0));
        String[] arr = null;
        try {
            arr = parser.parseLine(str);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return arr;
    }

    private List<int[]> strToIntMatrix(List<String[]> input) {
        // todo as Stream
        List<int[]> output = new ArrayList<>();
        int[] outputElement;
        for (int i = 0; i < input.size(); i++) {
            outputElement = strToInt(input.get(i));
            output.add(outputElement);
        }
        return output;
    }

    private int[] strToInt(String[] input) {
        int[] output = new int[input.length];
        IntStream.range(0, output.length).forEach(i -> output[i] = Integer.parseInt(input[i]));
        return output;
    }

    private void visualise(List<int[]> matrix) {
        for (int i = 0; i < matrix.size(); i++) {
            int[] line = matrix.get(i);
            IntStream.range(0, line.length).forEach(value -> System.out.print("\t" + line[value]));
            System.out.println();
        }
    }

    private void waveStartToEnd() {
        List<Point> currWaveEdgePoints = new ArrayList<>();
        List<Point> nextWaveEdgePoints = new ArrayList<>();
        int currWaveCounter = -1;

        currWaveEdgePoints.add(keyStartPointCoords);
        while (currWaveEdgePoints.size() > 0) {
            for (int i = 0; i < currWaveEdgePoints.size(); i++) {
                for (int j = 0; j < PathMode.standardDeltaPoints.size(); j++) {
                    Point shiftPointCoords = new Point(
                            (int) (currWaveEdgePoints.get(i).getX() + PathMode.standardDeltaPoints.get(j).getX() ),
                            (int) (currWaveEdgePoints.get(i).getY() + PathMode.standardDeltaPoints.get(j).getY())
                    );
                    if (isShiftInValidField(shiftPointCoords)) {
                        if (this.matrixToFillWaves.get(((int) shiftPointCoords.getY()))[((int) shiftPointCoords.getX())] == 0) {
                            this.matrixToFillWaves.get(((int) shiftPointCoords.getY()))[((int) shiftPointCoords.getX())] = currWaveCounter;
                            nextWaveEdgePoints.add(shiftPointCoords);
                        }
                    }
                }
            }
            currWaveEdgePoints = nextWaveEdgePoints;
            nextWaveEdgePoints = new ArrayList<>();
            currWaveCounter --;
            System.out.println("pre visualise");
            visualise(this.matrixToFillWaves);
            System.out.println("next " + ">>>");
        }
    }

    private void waveStartToEnd(List<int[]> inputMatrix) {
        List<Point> currWaveEdgePoints = new ArrayList<>();
        List<Point> nextWaveEdgePoints = new ArrayList<>();
        int currWaveCounter = -1;

        currWaveEdgePoints.add(keyStartPointCoords);
        while (currWaveEdgePoints.size() > 0) {
            for (int i = 0; i < currWaveEdgePoints.size(); i++) {
                for (int j = 0; j < PathMode.standardDeltaPoints.size(); j++) {
                    Point shiftPointCoords = new Point(
                            (int) (currWaveEdgePoints.get(i).getX() + PathMode.standardDeltaPoints.get(j).getX()),
                            (int) (currWaveEdgePoints.get(i).getY() + PathMode.standardDeltaPoints.get(j).getY())
                    );
                    if (isShiftInValidField(shiftPointCoords)) {
                        if (inputMatrix.get(((int) shiftPointCoords.getY()))[((int) shiftPointCoords.getX())] == 0) {
                            inputMatrix.get(((int) shiftPointCoords.getY()))[((int) shiftPointCoords.getX())] = currWaveCounter;
                            nextWaveEdgePoints.add(shiftPointCoords);
                        }
                    }
                }
            }
            currWaveEdgePoints = nextWaveEdgePoints;
            nextWaveEdgePoints = new ArrayList<>();
            currWaveCounter --;
            System.out.println("pre visualise");
            visualise(inputMatrix);
            System.out.println("next " + ">>>");
        }
    }

    public boolean isShiftInValidField(Point coords) {
        if (coords.getX() >= 0 && coords.getY() >= 0 &&
                matrixToFillWaves.size() > coords.getY() &&
                matrixToFillWaves.get(0).length > coords.getX()
        ) {
            return true;
        }
        return false;
    }
}