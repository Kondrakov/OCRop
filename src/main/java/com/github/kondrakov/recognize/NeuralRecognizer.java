package com.github.kondrakov.recognize;

import com.github.kondrakov.feed.CSVProcessorIO;
import com.github.kondrakov.model.Alphabet;

import java.util.List;
import java.util.Map;

public class NeuralRecognizer {

    private static int inputNodes;
    private static int hiddenNodes;
    private static int outputNodes;
    private static double learnMode = 0.5d;

    private static String[] targetSymbols;

    private static double[][] inputToHiddenWeights;
    private static double[][] hiddenToOutputWeights;

    public static double[][] getInputToHiddenWeights() {
        return NeuralRecognizer.inputToHiddenWeights;
    }

    public static double[][] getHiddenToOutputWeights() {
        return NeuralRecognizer.hiddenToOutputWeights;
    }

    //w 25 h 25
    public static void initNet(int inputNodesWidth, int inputNodesHeight, int hiddenNodes, int outputNodes) {
        NeuralRecognizer.inputNodes = inputNodesWidth * inputNodesHeight;
        //NeuralRecognizer.hiddenNodes = 300;
        NeuralRecognizer.hiddenNodes = hiddenNodes;
        NeuralRecognizer.outputNodes = outputNodes;

        NeuralRecognizer.targetSymbols = new String[outputNodes];
        for (int i = 0; i < NeuralRecognizer.targetSymbols.length; i++) {
            NeuralRecognizer.targetSymbols[i] = Alphabet.getCurrentAlphabet().get(i);
        }
    }

    public static void createNet() {
        NeuralRecognizer.inputToHiddenWeights =
                new double[NeuralRecognizer.hiddenNodes][NeuralRecognizer.inputNodes];
        for (int i = 0; i < NeuralRecognizer.inputToHiddenWeights.length; i++) {
            for (int j = 0; j < NeuralRecognizer.inputToHiddenWeights[i].length; j++) {
                NeuralRecognizer.inputToHiddenWeights[i][j] = Math.random() - 0.5;
            }
        }

        NeuralRecognizer.hiddenToOutputWeights =
                new double[NeuralRecognizer.outputNodes][NeuralRecognizer.hiddenNodes];
        for (int i = 0; i < NeuralRecognizer.hiddenToOutputWeights.length; i++) {
            for (int j = 0; j < NeuralRecognizer.hiddenToOutputWeights[i].length; j++) {
                NeuralRecognizer.hiddenToOutputWeights[i][j] = Math.random() - 0.5;
            }
        }
    }

    public static double sigmoid(double inputWeight) {
        return (1 / (1 + Math.pow(Math.E, (-1 * inputWeight))));
    }

    public static double[] sigmoidize(double[] inputFor) {
        double[] output = new double[inputFor.length];
        for (int i = 0; i < inputFor.length; i++) {
            output[i] = sigmoid(inputFor[i]);
        }
        return output;
    }

    public static void testSet(List<Map<String, List<int[]>>> trainSet) {
        for (int i = 0; i < trainSet.size(); i++) {
            for (Map.Entry<String, List<int[]>> matrixEntry : trainSet.get(i).entrySet()) {
                NeuralRecognizer.train(
                        fillTargets(matrixEntry.getKey()),
                        fillInputs(matrixEntry.getValue())
                );
            }
        }
    }

    /**
        If there aren't enough train sets to train net the "Overtrain" needed.
        Approximately 40 full train alphabet sets is recommended minimum for net.
        For example we have one train set with 26 EN letters.
        So for effective recognize we need to train resulting model 26 x 40 times.
        In this case overTrainIterations = 40.
        Augmentation of data may be applied instead of "Overtrain".
     */
    public static void testSet(List<Map<String, List<int[]>>> trainSet, int overTrainIterations) {
        for (int i = 0; i < overTrainIterations; i++) {
            testSet(trainSet);
        }
    }

    public static void train(double[] targets, double[][] inputs) {
        double[] hiddenInputsMulti = multiplyMatricesInpToHidden(
                NeuralRecognizer.inputToHiddenWeights,
                inputs
        );
        double[] hiddenOutSigmoid = sigmoidize(hiddenInputsMulti);
        double[] hiddenToOutMulti = multiplyMatricesHiddenToOut(
                NeuralRecognizer.hiddenToOutputWeights,
                hiddenOutSigmoid
        );
        double[] hiddenToOutMultiSigmoidize = sigmoidize(hiddenToOutMulti);
        double[] outErrors = outErrors(targets, hiddenToOutMultiSigmoidize);
        double[] hiddenErrors = hiddenErrors(
                NeuralRecognizer.hiddenToOutputWeights,
                outErrors
        );
        hiddenToOutUpdW(
                NeuralRecognizer.hiddenToOutputWeights,
                outErrors,
                hiddenToOutMultiSigmoidize,
                hiddenOutSigmoid
        );

        inputToHiddenUpdW(
                NeuralRecognizer.inputToHiddenWeights,
                hiddenErrors,
                hiddenOutSigmoid,
                inputs
        );
    }

    public static double[] fillTargets(String value) {
        double[] targets = new double[NeuralRecognizer.targetSymbols.length];
        for (int i = 0; i < NeuralRecognizer.targetSymbols.length; i++) {
            if (value.equals(NeuralRecognizer.targetSymbols[i])) {
                targets[i] = 0.99d;
            } else {
                targets[i] = 0.01d;
            }
        }
        return targets;
    }

    public static double[][] fillInputs(List<int[]> trainMatrixEntry) {
        double[][] inputs = new double[trainMatrixEntry.size()][trainMatrixEntry.get(0).length];
        for (int i = 0; i < trainMatrixEntry.size(); i++) {
            for (int j = 0; j < trainMatrixEntry.get(i).length; j++) {
                inputs[i][j] = ((double) trainMatrixEntry.get(i)[j]) / 255.0d * 0.99d + 0.01d;
            }
        }
        return inputs;
    }

    public static double[] flattenInputs(double[][] inputsMatrix) {
        double[] flatInput = new double[inputsMatrix.length * inputsMatrix[0].length];
        for (int i = 0; i < inputsMatrix.length; i++) {
            for (int j = 0; j < inputsMatrix[i].length; j++) {
                flatInput[i * inputsMatrix[i].length + j] = inputsMatrix[i][j];
            }
        }
        return flatInput;
    }

    public static double[] multiplyMatricesInpToHidden(double[][] weightMatrix, double[][] inputsMatrix) {
        double[] flatInput = flattenInputs(inputsMatrix);
        double[] multipliedMatrices = new double[weightMatrix.length];
        for (int i = 0; i < weightMatrix.length; i++) {
            multipliedMatrices[i] = 0.0d;
            for (int j = 0; j < flatInput.length; j++) {
                multipliedMatrices[i] += flatInput[j] * weightMatrix[i][j];
            }
        }
        return multipliedMatrices;
    }

    public static double[] multiplyMatricesHiddenToOut(double[][] weightMatrix, double[] hiddenOutMatrix) {
        double[] hiddenToOutWeights = new double[weightMatrix.length];
        for (int i = 0; i < weightMatrix.length; i++) {
            hiddenToOutWeights[i] = 0d;
            for (int j = 0; j < weightMatrix[i].length; j++) {
                hiddenToOutWeights[i] += hiddenOutMatrix[j] * weightMatrix[i][j];
            }
        }
        return hiddenToOutWeights;
    }

    public static double[] outErrors(double[] targets, double[] out) {
        double[] errors = new double[targets.length];
        for (int i = 0; i < targets.length; i++) {
            errors[i] = targets[i] - out[i];
        }
        return errors;
    }

    public static double[] hiddenErrors(double[][] weightMatrix, double[] outErrors) {
        //transpose
        double[][] weightMatrixTrans = new double[weightMatrix[0].length][weightMatrix.length];
        for (int i = 0; i < weightMatrixTrans.length; i++) {
            for (int j = 0; j < weightMatrixTrans[i].length; j++) {
                weightMatrixTrans[i][j] = weightMatrix[j][i];
            }
        }

        //multiply matrices
        double[] hiddenErrors = new double[weightMatrix[0].length];
        for (int i = 0; i < weightMatrixTrans.length; i++) {
            hiddenErrors[i] = 0d;
            for (int j = 0; j < weightMatrixTrans[i].length; j++) {
                hiddenErrors[i] += outErrors[j] * weightMatrixTrans[i][j];
            }
        }
        return hiddenErrors;
    }

    public static void hiddenToOutUpdW(
                                            double[][] weightMatrix,
                                            double[] outErrors,
                                            double[] hiddenToOutMultiSigmoidize,
                                            double[] hiddenOutSigmoid) {
        double[] out = new double[outErrors.length];
        for (int i = 0; i < outErrors.length; i++) {
            out[i] = outErrors[i] * hiddenToOutMultiSigmoidize[i] *
                    (1 - hiddenToOutMultiSigmoidize[i]);
        }
        for (int i = 0; i < weightMatrix.length; i++) {
            for (int j = 0; j < weightMatrix[i].length; j++) {
                weightMatrix[i][j] += learnMode * out[i] * hiddenOutSigmoid[j];
            }
        }
    }

    public static void inputToHiddenUpdW(
                                            double[][] weightMatrix,
                                            double[] hiddenErrors,
                                            double[] hiddenOutSigmoid,
                                            double[][] inputsMatrix
            ) {
        double[] hidden = new double[hiddenErrors.length];
        for (int i = 0; i < hiddenErrors.length; i++) {
            hidden[i] = hiddenErrors[i] * hiddenOutSigmoid[i] *
                    (1 - hiddenOutSigmoid[i]);
        }
        double[] flatInput = flattenInputs(inputsMatrix);
        for (int i = 0; i < weightMatrix.length; i++) {
            for (int j = 0; j < weightMatrix[i].length; j++) {
                weightMatrix[i][j] += learnMode * hidden[i] * flatInput[j];
            }
        }
    }

    public static String recognize(List<int[]> guessedLetter) {
        double[][] inputs = fillInputs(guessedLetter);
        double[] hiddenInputsMulti = multiplyMatricesInpToHidden(
                NeuralRecognizer.inputToHiddenWeights,
                inputs
        );
        double[] hiddenOutSigmoid = sigmoidize(hiddenInputsMulti);
        double[] hiddenToOutMulti = multiplyMatricesHiddenToOut(
                NeuralRecognizer.hiddenToOutputWeights,
                hiddenOutSigmoid
        );
        double[] hiddenToOutMultiSigmoidize = sigmoidize(hiddenToOutMulti);

        int maxTriggerFlag = 0;
        double max = hiddenToOutMultiSigmoidize[0];
        for (int i = 1; i < hiddenToOutMultiSigmoidize.length; i++) {
            if (hiddenToOutMultiSigmoidize[i] > max) {
                max = hiddenToOutMultiSigmoidize[i];
                maxTriggerFlag = i;
            }
        }
        return Alphabet.getCurrentAlphabet().get(maxTriggerFlag);
    }

    public static void loadTrained(String inputToHiddenSource,
                                   String hiddenToOutputSource) {

        NeuralRecognizer.inputToHiddenWeights =
                CSVProcessorIO.loadMatrixDoubleFromCSVFile(
                        inputToHiddenSource
                );
        NeuralRecognizer.hiddenToOutputWeights =
                CSVProcessorIO.loadMatrixDoubleFromCSVFile(
                        hiddenToOutputSource
                );
    }
}
