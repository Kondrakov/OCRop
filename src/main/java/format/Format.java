package format;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Format {
    public static Map<String, List<int[]>> formatToDims(Map<String, List<int[]>> toFormat, List<int[]> pattern) {
        Map<String, List<int[]>> formatted = new HashMap<>();
        for (Map.Entry<String, List<int[]>> sampleToFormat : toFormat.entrySet()) {
            formatted.put(sampleToFormat.getKey(), frameToPatternConstrainsByInput(
                    pattern.get(0).length,
                    pattern.size(),
                    sampleToFormat.getValue(),
                    pattern
                )
            );
        }
        return formatted;
    }

    public static Point corner(List<int[]> matrix) {
        int row = -1;
        int column = -1;
        for (int i = 0; i < matrix.size(); i++) {
            for (int j = 0; j < matrix.get(0).length; j++) {
                if (matrix.get(i)[j] > 0) {
                    if (row == -1) {
                        row = i;
                    }
                    if (column == -1) {
                        column = j;
                    } else {
                        column = Math.min(column, j);
                    }
                }
            }
        }
        return new Point(column, row);
    }

    public static List<int[]> frameToPatternConstrainsByInput(int width, int height, List<int[]> input,
                                                              List<int[]> pattern) {
        Point cornerPointPattern = corner(pattern);
        Point cornerPointSample = corner(input);
        Point offset = new Point(
                (int) (cornerPointSample.getX() - cornerPointPattern.getX()),
                (int) (cornerPointSample.getY() - cornerPointPattern.getY())
        );

        List<int[]> output = new ArrayList<>();
        for (int i = 0; i < height; i++) {
            output.add(new int[width]);
            for (int j = 0; j < output.get(i).length; j++) {
                if (i + (int) offset.getY() > -1 && i + (int) offset.getY() < input.size() &&
                        j + (int) offset.getX() > -1 && j + (int) offset.getX() < input.get(0).length) {
                    output.get(i)[j] = input.get(i + (int) offset.getY())[j + (int) offset.getX()];
                } else {
                    output.get(i)[j] = 0;
                }
            }
        }
        return output;
    }

    public static List<int[]> frameToPattern(List<int[]> input,
                                             List<int[]> pattern) {
        int width = 100;
        int height = 100;
        Point cornerPointPattern = corner(pattern);
        Point cornerPointSample = corner(input);
        Point offset = new Point(
                (int) (cornerPointSample.getX() - cornerPointPattern.getX()),
                (int) (cornerPointSample.getY() - cornerPointPattern.getY())
        );

        List<int[]> output = new ArrayList<>();
        for (int i = 0; i < width; i++) {
            output.add(new int[width]);
            for (int j = 0; j < height; j++) {
                if (i + (int) offset.getY() > -1 && i + (int) offset.getY() < input.size() &&
                        j + (int) offset.getX() > -1 && j + (int) offset.getX() < input.get(0).length) {
                    output.get(i)[j] = input.get(i + (int) offset.getY())[j + (int) offset.getX()];
                } else {
                    output.get(i)[j] = 0;
                }
            }
        }
        return output;
    }

    public static List<int[]> frameExtendPattern(List<int[]> pattern) {
        int width = 100;
        int height = 100;
        List<int[]> patternExtended = new ArrayList<>();
        for (int i = 0; i < width; i++) {
            patternExtended.add(new int[width]);
            for (int j = 0; j < height; j++) {
                if (i < pattern.size() && j < pattern.get(0).length) {
                    patternExtended.get(i)[j] = pattern.get(i)[j];
                }
            }
        }
        return patternExtended;
    }
}
