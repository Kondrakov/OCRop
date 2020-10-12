package com.github.kondrakov;

import com.github.kondrakov.approximate.Scale;
import com.github.kondrakov.feed.CSVProcessorIO;
import com.github.kondrakov.feed.DataStash;
import com.github.kondrakov.feed.IFeeder;
import com.github.kondrakov.feed.PDFBoxFeeder;
import com.github.kondrakov.format.Format;
import com.github.kondrakov.model.Alphabet;
import com.github.kondrakov.parser.BitmapParser;
import com.github.kondrakov.parser.BitmapUtils;
import com.github.kondrakov.recognize.Recognizer;
import com.github.kondrakov.scenario.BundleScenarios;
import com.github.kondrakov.textfinder.SourceCutter;
import com.github.kondrakov.utils.UtilsConv;
import com.github.kondrakov.utils.UtilsEncode;
import com.github.kondrakov.view.VisualDebugForm;
import org.apache.pdfbox.rendering.ImageType;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class APIUsecaseExamples {

    //todo research what corrupt csv letters data?:
    public void startAllExamples() {
        this.exampleVisualDebugForm();
        this.exampleConveyorFromBmpSymbolToRecognize();
        this.exampleFullConveyorFromPdfPageToRecognize();
        this.exampleEnLetterToRecognize("e");
        this.exampleEnLetterToRecognize("d");
        this.exampleSimpleCutLettersFromString();
        this.exampleCropCutByDimensions();
        this.exampleSimpleConvertPdfToBmp();
        this.exampleScaleUpLetter();
        this.exampleParseRaw();
        this.exampleBitmapAssembling();
        this.exampleBitmapLoadingAndAssembling();
    }

    //UI form for csv visualisation:
    public void exampleVisualDebugForm() {
        VisualDebugForm visualDebugForm = new VisualDebugForm();
        visualDebugForm.setSize(800, 300);
        visualDebugForm.setVisible(true);
    }

    //working example symbol bmp --> symbol recognized:
    public void exampleConveyorFromBmpSymbolToRecognize() {
        //alphabet data preparing: *.bmp => *.csv; *.csv => List<int[]> :
        DataStash.prepareReferenceModels("data\\bmp_source_models\\%s.bmp",
                "data\\bmp_source_models\\%s.csv",
                DataStash.NEW,
                Alphabet.getAlphabetRU(),
                DataStash.FROM_BMP_MODE, BitmapUtils.COLOR_16);
        //alphabet data preparing: alphabet *.csv => List<int[]> :
        DataStash.prepareReferenceModels("data\\csv_source_models\\%s.csv", "",
                DataStash.NEW,
                Alphabet.getAlphabetRU(),
                DataStash.FROM_CSV_MODE, BitmapUtils.COLOR_16);
        //get alphabet matrices from generated csv :
        Map<String, List<int[]>> matrices = DataStash.getLetterMatricesCollection();

        //com.github.kondrakov.recognize letter:
        String answer = Recognizer.recognize(matrices, CSVProcessorIO.loadMatrixFromCSVFile("data\\csv_source_models\\е.csv"));
        System.out.println("recognized letter is: " + answer);
    }

    //working example bmp letter --> csv letter --> letter recognized:
    public void exampleEnLetterToRecognize(String letter) {
        DataStash.prepareReferenceModels("data\\bmp_source_models\\en\\%s.bmp",
                "data\\csv_source_models\\en\\%s.csv",
                DataStash.NEW,
                Alphabet.getAlphabetEN(),
                DataStash.FROM_BMP_MODE, BitmapUtils.COLOR_256);
        //alphabet data preparing: alphabet *.csv => List<int[]> :
        DataStash.prepareReferenceModels("data\\csv_source_models\\en\\%s.csv", "",
                DataStash.NEW,
                Alphabet.getAlphabetEN(),
                DataStash.FROM_CSV_MODE, BitmapUtils.COLOR_256);

        try {
            DataStash.stashBMPAsCSV(String.format(
                    "data\\symbols_to_recognize\\en\\%s.bmp",
                    UtilsEncode.toRuntimeCharset(letter)
                    ),
                    String.format(
                            "data\\symbols_to_recognize_csv\\en\\%s.csv",
                            UtilsEncode.toRuntimeCharset(letter)
                    ),
                    true, true, BitmapUtils.COLOR_256);
        } catch (Exception ex) {
            System.out.println(ex);
        }

        List<int[]> toRecognize = CSVProcessorIO.
                loadMatrixFromCSVFile(
                        String.format("data\\symbols_to_recognize_csv\\en\\%s.csv",
                                UtilsEncode.toRuntimeCharset(letter)));

        List<int[]> toRecognizeFormatted = Format.frameExtendPattern(toRecognize);

        Map<String, List<int[]>> matrices = DataStash.getLetterMatricesCollection();
        StringBuilder answer = new StringBuilder();
        Map<String, List<int[]>> formattedMatrices = new HashMap<>();
        for (Map.Entry<String, List<int[]>> matrixEntry : matrices.entrySet()) {
            formattedMatrices.put(matrixEntry.getKey(),
                    Format.frameToPattern(
                            matrixEntry.getValue(), toRecognizeFormatted
                    ));
            try {
                CSVProcessorIO.writeMatrixToCSVFile(
                        formattedMatrices.get(matrixEntry.getKey()),
                        String.format(
                                "data\\symbols_formatted_csv\\en\\%s.csv",
                                UtilsEncode.toRuntimeCharset(matrixEntry.getKey())
                        ), false, BitmapUtils.COLOR_256
                );
            } catch (Exception ex) {
                System.out.println(ex);
            }
        }
        answer.append(Recognizer.recognize(formattedMatrices,
                toRecognizeFormatted));

        System.out.println("answer " + answer.toString());
    }

    //working example pdf --> page bmp --> string csv --> string to com.github.kondrakov.recognize (List<List<int[]>>) --> String recognized:
    public void exampleFullConveyorFromPdfPageToRecognize() {
        DataStash.prepareReferenceModels("data\\bmp_source_models\\en\\%s.bmp",
                "data\\csv_source_models\\en\\%s.csv",
                DataStash.NEW,
                Alphabet.getAlphabetEN(),
                DataStash.FROM_BMP_MODE, BitmapUtils.COLOR_256);
        //alphabet data preparing: alphabet *.csv => List<int[]> :
        DataStash.prepareReferenceModels("data\\csv_source_models\\en\\%s.csv", "",
                DataStash.NEW,
                Alphabet.getAlphabetEN(),
                DataStash.FROM_CSV_MODE, BitmapUtils.COLOR_256);

        // 1. Extract bmp from pdf:
        IFeeder feeder = new PDFBoxFeeder();
        feeder.feed("data\\pages_to_recognize\\input_pdf.pdf",
                "data\\pages_to_recognize\\pdf_to_bmp.bmp");

        // 2. Cut stringMatrix from page matrix:
        List<int[]> stringMatrix = SourceCutter.cutCropByDims(
                "data\\pages_to_recognize\\pdf_to_bmp.bmp",
                new Rectangle(180, 155, 280, 45),
                BitmapUtils.COLOR_256
        );

        // 3. Invert colors to further processing string
        List<int[]> stringMatrixInverted = UtilsConv.cloneMatrixData(stringMatrix);
        for (int i = 0; i < stringMatrix.size(); i++) {
            for (int j = 0; j < stringMatrix.get(i).length; j++) {
                stringMatrixInverted.get(i)[j] = BitmapUtils.invert(stringMatrixInverted.get(i)[j], BitmapUtils.COLOR_256);
            }
        }

        try {
            CSVProcessorIO.writeMatrixToCSVFile(stringMatrixInverted,
                    "data\\teststring.csv", false, BitmapUtils.COLOR_256);
        } catch (Exception ex) {
            System.out.println(ex);
        }

        // 4. Cut stringLetterMatrices from stringMatrix:
        List<List<int[]>> stringLetterMatrices =
                SourceCutter.simpleCutString(stringMatrixInverted, SourceCutter.SIMPLE_CUT, BitmapUtils.COLOR_256, 5, 0, "data\\strings_to_recognize\\l%s.csv");
        try {
            for (int i = 0; i < stringLetterMatrices.size(); i++) {
                CSVProcessorIO.writeMatrixToCSVFile(stringLetterMatrices.get(i),
                        "data\\teststring_" + i + ".csv", false, BitmapUtils.COLOR_256);
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }

        // 5. Load reference learned matrices:
        Map<String, List<int[]>> matrices = DataStash.getLetterMatricesCollection();

        // 6. Load external alphabet and mapping:
        Alphabet.setExternalAlphabet(
                "data\\alphabets\\en_alphabet.csv",
                "data\\alphabets\\en_mapping.csv",
                Alphabet.EN
        );

        // 7. Recognize String from stringLetterMatrices:
        StringBuilder answer = new StringBuilder();
        for (int i = 0; i < stringLetterMatrices.size(); i++) {
            if (stringLetterMatrices.get(i).size() > 0) {
                Map<String, List<int[]>> formattedMatrices = new HashMap<>();
                for (Map.Entry<String, List<int[]>> matrixEntry : matrices.entrySet()) {
                    formattedMatrices.put(matrixEntry.getKey(),
                            Format.frameToPattern(
                                    matrixEntry.getValue(), stringLetterMatrices.get(i)
                            ));
                    try {
                        CSVProcessorIO.writeMatrixToCSVFile(
                                formattedMatrices.get(matrixEntry.getKey()),
                                String.format(
                                        "data\\symbols_formatted_csv\\en\\%s.csv",
                                        UtilsEncode.toRuntimeCharset(matrixEntry.getKey())
                                ), false, BitmapUtils.COLOR_256
                        );
                    } catch (Exception ex) {
                        System.out.println(ex);
                    }
                }
                answer.append(Recognizer.recognize(formattedMatrices,
                        Format.frameExtendPattern(stringLetterMatrices.get(i))
                ));
            } else {
                answer.append(" ");
            }
            System.out.println("answer " + answer);
            System.out.println("EOF" + " letter");
        }

        System.out.println("answer " + answer.toString());

        System.out.println("EOF" + " example");
    }

    public void exampleSimpleCutLettersFromString() {
        try {
            DataStash.stashBMPAsCSV("data\\strings_to_recognize\\string_to_cut.bmp",
                    "data\\strings_to_recognize\\string_to_cut.csv",
                    true, true, BitmapUtils.COLOR_256);
        } catch (Exception ex) {
            System.out.println(ex);
        }
        List<int[]> stringInput = CSVProcessorIO.loadMatrixFromCSVFile("data\\strings_to_recognize\\string_to_cut.csv");
        SourceCutter.simpleCutString(stringInput, SourceCutter.SIMPLE_CUT, BitmapUtils.COLOR_256, 5, 0, "data\\strings_to_recognize\\l%s.csv");
    }

    public void exampleCropCutByDimensions() {
        List<int[]> inputString = SourceCutter.cutCropByDims(
                "data\\pages_to_recognize\\page_to_cut.bmp",
                new Rectangle(390, 184, 120, 18),
                BitmapUtils.COLOR_16
        );
    }

    public void exampleSimpleConvertPdfToBmp() {
        IFeeder feeder = new PDFBoxFeeder();
        feeder.feed("pdf.pdf", "pdf.bmp");
    }

    public void exampleScaleUpLetter() {
        // test scale up feature:
        Scale.scaleUp(CSVProcessorIO.loadMatrixFromCSVFile("data\\csv_source_models\\е.csv"), 1.6);
    }

    @Deprecated
    public void exampleParseRaw() {
        // todo refactor not in use
        new BitmapProviderText().parseRaw();
    }

    public void exampleBitmapAssembling() {
        //example bitmap assembling:
        List<int[]> list = new HelperA1().loadLetterMatrix();
        BitmapParser.assembleBitmap(list, "header_16_colors_bw",
                "generated.bmp", BitmapUtils.COLOR_16);
    }

    public void exampleMultilineInputSourceTextBlock() {
        DataStash.prepareReferenceModels("data\\bmp_source_models\\en\\%s.bmp",
                "data\\csv_source_models\\en\\%s.csv",
                DataStash.NEW,
                Alphabet.getAlphabetEN(),
                DataStash.FROM_BMP_MODE, BitmapUtils.COLOR_256);
        //alphabet data preparing: alphabet *.csv => List<int[]> :
        DataStash.prepareReferenceModels("data\\csv_source_models\\en\\%s.csv", "",
                DataStash.NEW,
                Alphabet.getAlphabetEN(),
                DataStash.FROM_CSV_MODE, BitmapUtils.COLOR_256);

        // Extract bmp from pdf:
        IFeeder feeder = new PDFBoxFeeder();
        feeder.feed("data\\pages_to_recognize\\text_multiline.pdf",
                "data\\pages_to_recognize\\text_multiline.bmp");

        // Cut matrix block with strings from page matrix:
        List<int[]> blockMatrix = SourceCutter.cutCropByDims(
                "data\\pages_to_recognize\\text_multiline.bmp",
                new Rectangle(182, 150, 390, 190),
                BitmapUtils.COLOR_256
        );

        // Invert colors to further processing string
        List<int[]> blockMatrixInverted = UtilsConv.cloneMatrixData(blockMatrix);
        for (int i = 0; i < blockMatrix.size(); i++) {
            for (int j = 0; j < blockMatrix.get(i).length; j++) {
                blockMatrixInverted.get(i)[j] = BitmapUtils.invert(blockMatrixInverted.get(i)[j], BitmapUtils.COLOR_256);
            }
        }

        // Cut strings block into strings
        List<List<int[]>> stringsFromBlock =
                SourceCutter.simpleCutBlockIntoStrings(blockMatrixInverted);

        try {
            CSVProcessorIO.writeMatrixToCSVFile(blockMatrixInverted,
                    "data\\text_block_multiline.csv", false, BitmapUtils.COLOR_256);
        } catch (Exception ex) {
            System.out.println(ex);
        }

        // Cut stringLetterMatrices from list of stringMatrices:
        List<List<int[]>> stringLetterMatrices = new ArrayList<>();
        for (int i = 0; i < stringsFromBlock.size(); i++) {
            stringLetterMatrices.addAll(
                    SourceCutter.simpleCutString(stringsFromBlock.get(i), SourceCutter.SIMPLE_CUT, BitmapUtils.COLOR_256, 5, 0, "data\\strings_to_recognize\\l%s.csv")
            );
        }

        try {
            for (int i = 0; i < stringLetterMatrices.size(); i++) {
                CSVProcessorIO.writeMatrixToCSVFile(stringLetterMatrices.get(i),
                        "data\\text_block_multiline_" + i + ".csv", false, BitmapUtils.COLOR_256);
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }

        // Load reference learned matrices:
        Map<String, List<int[]>> matrices = DataStash.getLetterMatricesCollection();

        // Load external alphabet and mapping:
        Alphabet.setExternalAlphabet(
                "data\\alphabets\\en_alphabet.csv",
                "data\\alphabets\\en_mapping.csv",
                Alphabet.EN
        );

        // 7. Recognize String from stringLetterMatrices:
        StringBuilder answer = new StringBuilder();
        for (int i = 0; i < stringLetterMatrices.size(); i++) {
            if (stringLetterMatrices.get(i).size() > 0) {
                Map<String, List<int[]>> formattedMatrices = new HashMap<>();
                for (Map.Entry<String, List<int[]>> matrixEntry : matrices.entrySet()) {
                    formattedMatrices.put(matrixEntry.getKey(),
                            Format.frameToPattern(
                                    matrixEntry.getValue(), stringLetterMatrices.get(i)
                            ));
                    try {
                        CSVProcessorIO.writeMatrixToCSVFile(
                                formattedMatrices.get(matrixEntry.getKey()),
                                String.format(
                                        "data\\symbols_formatted_csv\\en\\%s.csv",
                                        UtilsEncode.toRuntimeCharset(matrixEntry.getKey())
                                ), false, BitmapUtils.COLOR_256
                        );
                    } catch (Exception ex) {
                        System.out.println(ex);
                    }
                }
                answer.append(Recognizer.recognizeByPercent(formattedMatrices,
                        Format.frameExtendPattern(stringLetterMatrices.get(i)),
                        BitmapUtils.COLOR_256
                ));
            } else {
                answer.append(" ");
            }
            System.out.println(String.format("answer ->>%s<<-", answer));
            System.out.println("EOF" + " letter");
        }
    }

    public void bundleScenarioExample() {
        String recognizeString = (new BundleScenarios()).
                loadSymbolsRange("data\\alphabets\\ru_alphabet_capitals.csv",
                        "data\\alphabets\\ru_mapping_capitals.csv",
                        Alphabet.RU).
                loadAlphabetModel("data\\bmp_source_models\\ru_courier_new_bold\\%s.bmp",
                        "data\\csv_source_models\\ru_courier_new_bold\\%s.csv",
                        DataStash.NEW,
                        Alphabet.getAlphabetRU(),
                        DataStash.FROM_BMP_MODE, BitmapUtils.COLOR_256).
                getBmpFromPdfFormat("data\\pages_to_recognize\\ru_bold_courier_new_sign.pdf",
                        "data\\pages_to_recognize\\ru_bold_courier_new_sign.bmp",
                        0, 150, ImageType.GRAY
                        ).
                extractStringsToRecognize(
                        "data\\pages_to_recognize\\ru_bold_courier_new_sign.bmp",
                        new Rectangle(182, 150, 390, 190),
                        BitmapUtils.COLOR_256,
                        SourceCutter.NO_GAP_SEARCH_CUT,
                        4, 19,
                        "data\\strings_to_recognize\\l%s.csv"
                ).
                recognize("data\\symbols_formatted_csv\\en\\%s.csv").
                getRecognized();
        System.out.println(recognizeString);
        System.out.println("answer ==>" + recognizeString);
    }

    public void bundleScenarioExampleWithMerge() {
        String recognizeString = (new BundleScenarios()).
                loadSymbolsRange("data\\alphabets\\ru_alphabet_capitals.csv",
                        "data\\alphabets\\ru_mapping_capitals.csv",
                        Alphabet.RU).
                loadAndMergeAlphabetModel(
                        "data\\bmp_source_models\\ru_courier_new_bold\\%s.bmp",
                        "data\\csv_source_models\\ru_courier_new_bold\\%s.csv",
                        "data\\bmp_source_models\\ru_courier_new_bold_left_cut\\%s.bmp",
                        "data\\csv_source_models\\ru_courier_new_bold_left_cut\\%s.csv",
                        "data\\csv_source_models\\ru_courier_new_bold_merged\\%s.csv",
                        Alphabet.getAlphabetRU(),
                        DataStash.FROM_BMP_MODE, BitmapUtils.COLOR_256).
                getBmpFromPdfFormat("data\\pages_to_recognize\\ru_bold_courier_new_sign_long.pdf",
                        "data\\pages_to_recognize\\ru_bold_courier_new_sign_long.bmp",
                        0, 150, ImageType.GRAY).
                extractStringsToRecognize(
                        "data\\pages_to_recognize\\ru_bold_courier_new_sign_long.bmp",
                        new Rectangle(182, 150, 325, 190),
                        BitmapUtils.COLOR_256,
                        SourceCutter.NO_GAP_SEARCH_CUT,
                        4, 19,
                        "data\\strings_to_recognize\\l%s.csv"
                ).
                recognizeSimple("data\\symbols_formatted_csv\\en\\%s.csv").
                getRecognized();
        System.out.println(recognizeString);
        System.out.println("answer ==>" + recognizeString);
    }

    public void bundleScenarioExampleNeural() {
        String recognizeString = (new BundleScenarios()).
                loadSymbolsRange("data\\alphabets\\ru_alphabet_capitals.csv",
                        "data\\alphabets\\ru_mapping_capitals.csv",
                        Alphabet.RU).
                loadAndMergeAlphabetModel(
                        "data\\bmp_source_models\\ru_courier_new_bold\\%s.bmp",
                        "data\\csv_source_models\\ru_courier_new_bold\\%s.csv",
                        "data\\bmp_source_models\\ru_courier_new_bold_left_cut\\%s.bmp",
                        "data\\csv_source_models\\ru_courier_new_bold_left_cut\\%s.csv",
                        "data\\csv_source_models\\ru_courier_new_bold_merged\\%s.csv",
                        Alphabet.getAlphabetRU(),
                        DataStash.FROM_BMP_MODE, BitmapUtils.COLOR_256).
                getBmpFromPdfFormat("data\\pages_to_recognize\\ru_bold_courier_new_sign_long.pdf",
                        "data\\pages_to_recognize\\ru_bold_courier_new_sign_long.bmp",
                        0, 150, ImageType.GRAY).
                extractStringsToRecognize(
                        "data\\pages_to_recognize\\ru_bold_courier_new_sign_long.bmp",
                        new Rectangle(182, 150, 325, 190),
                        BitmapUtils.COLOR_256,
                        SourceCutter.NO_GAP_SEARCH_CUT,
                        4, 19,
                        "data\\strings_to_recognize\\l%s.csv"
                ).
                cornerizeModelsForTraining(25, 25).
                cornerizeModelsToRecognize(25, 25).
                neuralRecognize().
                getRecognized();
    }

    @Deprecated
    public void exampleBitmapLoadingAndAssembling() {
        //example bitmap loading and assembling:
        SourceCutter.loadAndProcessImage();
        //deprecated logic for com.github.kondrakov.recognize:
        new OCRRecognize().recognize(new HelperA1().traceClean());
    }
}
