package view;

import feed.CSVProcessorIO;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

public class VisualDebugForm extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextPane textPaneOutput;
    private JTextField inputPath;
    private JTextField inputOffset;
    private JLabel labelPath;
    private JLabel labelOffset;
    private JButton geomSolve;

    private Canvas canvas;

    public VisualDebugForm() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        geomSolve.addActionListener(e -> {
            List<int[]> matrix = CSVProcessorIO.loadMatrixFromCSVFile(inputPath.getText());
            if (canvas != null) {
                textPaneOutput.remove(canvas);
            }
            canvas = new MatricesOutputCanvas();
            ((MatricesOutputCanvas)canvas).setMatrixData(matrix);
            textPaneOutput.add(canvas);
        });
    }

    private void onOK() {
        dispose();
    }

    private void onCancel() {
        dispose();
    }

    public static void main(String[] args) {
        VisualDebugForm dialog = new VisualDebugForm();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
