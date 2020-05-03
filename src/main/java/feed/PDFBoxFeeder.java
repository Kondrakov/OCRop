package feed;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PDFBoxFeeder implements IFeeder {
    public File feed(String input, String output) {
        File outputFile = new File(output);
        try {
            PDDocument document = PDDocument.load(new File(input));
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage image = renderer.renderImageWithDPI(0, 150, ImageType.GRAY);
            ImageIO.write(image, "BMP", outputFile);
            document.close();
        } catch (IOException ex) {
            System.out.println("ex " + ex);
        }
        return outputFile;
    }
}
