package com.github.kondrakov.fonts;

import com.github.kondrakov.model.Alphabet;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class FontStampCreator {

    static{

        String fontsDirectoryPath = "data/com.github.kondrakov.fonts/";

        File directory = new File(fontsDirectoryPath);

       if (directory.isDirectory()) {
           ArrayList<String> names = new ArrayList<String>(Arrays.asList(directory.list()));
            if (names.size() > 0) {
                System.out.println("The directory " + directory.getPath() + " is not empty");
            } else {
                System.out.println("The directory " + directory.getPath() + " is empty");
                try {
                    createImagesPackageOfFonts(fontsDirectoryPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void stringToImage(String s, String fontName, String path) throws IOException {

        //First, we have to calculate the string's width and height
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics g = img.getGraphics();

        //Set the font to be used when drawing the string
        Font f = new Font(fontName, Font.PLAIN, 48);
        g.setFont(f);

        //Get the string visual bounds
        FontRenderContext frc = g.getFontMetrics().getFontRenderContext();
        Rectangle2D rect = f.getStringBounds(s, frc);
        //Release resources
        g.dispose();

        //Then, we have to draw the string on the final image

        //Create a new image where to print the character
        int width = (int) Math.ceil(rect.getWidth());
        int height = (int) Math.ceil(rect.getHeight());
        img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        g = img.getGraphics();
        g.setColor(Color.WHITE); //Background color
        g.fillRect(0, 0, width, height);
        g.setColor(Color.BLACK); //Set text color
        g.setFont(f);

        //Calculate x and y for that string
        FontMetrics fm = g.getFontMetrics();
        int x = 0;
        int y = fm.getAscent(); //getAscent() = baseline
        g.drawString(s, x, y);

        //Release resources
        g.dispose();

        //Save image
        File imageFile = new File(path + "/" + s + ".bmp");
        ImageIO.write(img, "BMP", imageFile);
    }

    public static void createFontAlphabetInImages(String fontName, String path) throws IOException {
        for (String s : Alphabet.getAlphabetRU()) {
            stringToImage(s, fontName, path);
        }
    }

    public static void createImagesPackageOfFonts(String directoryPath) throws IOException {
        for (String s : Fonts.getFonts()) {
            String fontFolderPath = directoryPath + s;
            createDirectory(fontFolderPath);
            createFontAlphabetInImages(s, fontFolderPath);
        }

    }

    public static void createDirectory(String directoryName) {
        File file = new File(directoryName);
        //Creating the directory
        boolean bool = file.mkdirs();
        if (bool) {
            System.out.println("Directory "+"'"+directoryName+"'"+" created successfully");
        } else {
            System.out.println("Sorry couldnt create specified directory" +"'"+directoryName+"'");
        }

    }

}
