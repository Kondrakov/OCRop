import au.com.bytecode.opencsv.CSVReader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class BitmapProviderText {

    public BitmapProviderText() {

    }

    @Deprecated
    public void parseRaw() {
        InputStreamReader reader;
        FileInputStream fileInputStream;
        // todo move method, same method exists
        try {
            fileInputStream = new FileInputStream("inputRaw.csv");
            reader = new InputStreamReader(fileInputStream);
            List<String[]> inputs = new ArrayList<>();
            String[] input;
            CSVReader csvReader = new CSVReader(reader);
            while ((input = csvReader.readNext()) != null) {
                inputs.add(input);
            }
        } catch (IOException ex) {
            System.out.println(ex);
        }
    }
}
