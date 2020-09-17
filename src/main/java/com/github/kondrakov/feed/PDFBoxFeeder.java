package com.github.kondrakov.feed;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PDFBoxFeeder implements IFeeder {
    public PDFBoxFeeder() {
    }

    public PDFBoxFeeder(int pageIndex, float dpi, ImageType imageType) {
        this.pageIndex = pageIndex;
        this.dpi = dpi;
        this.imageType = imageType;
    }

    private int pageIndex = 0;
    private float dpi = 150;
    private ImageType imageType = ImageType.GRAY;

    public File feed(String input, String output) {
        File outputFile = new File(output);
        try {
            PDDocument document = PDDocument.load(new File(input));
            PDFRenderer renderer = new PDFRenderer(document);
            BufferedImage image = renderer.renderImageWithDPI(
                    this.pageIndex,
                    this.dpi,
                    this.imageType);
            ImageIO.write(image, "BMP", outputFile);
            document.close();
        } catch (IOException ex) {
            System.out.println("ex " + ex);
        }
        return outputFile;
    }
}
