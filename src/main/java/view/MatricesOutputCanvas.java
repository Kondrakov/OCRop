package view;

import java.awt.*;

public class MatricesOutputCanvas extends Canvas {
    public MatricesOutputCanvas() {
        setBackground(Color.GRAY);
        setSize(300, 150);
    }

    private java.util.List<int[]> matrixData;
    public void setMatrixData(java.util.List<int[]> matrix) {
        this.matrixData = matrix;
    }

    private Graphics g;

    public void paint(Graphics g) {
        this.g = (Graphics2D) g;

        for (int i = 0; i < this.matrixData.size(); i++) {
            for (int j = 0; j < this.matrixData.get(i).length; j++) {
                if (this.matrixData.get(i)[j] > 0) {
                    this.g.setColor(Color.BLACK);
                    this.g.drawRect(j * 2, i * 2, 1, 1);
                } else {
                    this.g.setColor(Color.WHITE);
                    this.g.drawRect(j * 2, i * 2, 1, 1);
                }

            }
        }
    }
}