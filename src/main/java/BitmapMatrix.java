public class BitmapMatrix {
    public BitmapMatrix() {
        int[][] bits = bitsTest();
    }

    private int[][] bitsTest() {
        int[][] bits = new int[10][10];
        Double representedBit;
        for (int i = 0; i < bits.length; i++) {
            for (int j = 0; j < bits[i].length; j++) {
                bits[i][j] = (int)(Math.random() * 16);
                representedBit = bits[i][j] / 15d;
                System.out.print(bits[i][j] + " : " + representedBit + " ");
            }
            System.out.println();
        }
        return bits;
    }

    private int[][] bitmapSearch() {
        int[][] bits = new int[100][100];
        for (int i = 0; i < bits.length; i++) {
            for (int j = 0; j < bits[i].length; j++) {
                bits[i][j] = -1;
            }
        }
        return bits;
    }
}
