package utilities;
import Folding.Folding;
import Tile.Tile;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import utilities.utilities.Direction;

import javax.imageio.ImageIO;


public class ImageCreator {
    private final int width;
    private final int height;
    private final int rectangle_length;
    private final int margin;
    private final int footer_height;
    
    public ImageCreator(int width, int height, int rectangle_length, int margin, int footer_height){
        this.width = width;
        this.height = height;
        this.rectangle_length = rectangle_length;
        this.margin = margin;
        this.footer_height = footer_height;
    }
    
    public void createFoldingImage(Folding folding, String image_name){
        BufferedImage image = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB);
         Graphics2D g2d = image.createGraphics();

        //Hintergrundfarbe setzen
        g2d.setColor(new Color(205,205,205));
        g2d.fillRect(0, 0, width, height);

        g2d.setStroke(new BasicStroke(7));

        //Informationen über die Faltung holen
        HashMap<String, Integer> informations = folding.get_folding_information();

        int first_row = informations.get("first_row");
        int first_column = informations.get("first_column");
        int last_row = informations.get("last_row");
        int last_column = informations.get("last_column");

        int row_spacing = (height - footer_height - (2 * margin) - (rectangle_length / 2));
        int column_spacing = (width - (2 * margin) - (rectangle_length / 2));

        //Abstände zwischen den Amino Acids
        if(last_row - first_row != 0)
            row_spacing /= (last_row - first_row);

        if(last_column -first_column != 0)
            column_spacing /= (last_column - first_column);

        Tile[][] folding_elements = folding.getTiles();

        for(int i = 0; i < folding_elements.length; i++){
            for(int j = 0; j < folding_elements[i].length; j++){
                if(folding.getTiles()[i][j]!= null){
                    int x = (margin / 2) + (j - first_column) * column_spacing;
                    int y = (margin / 2) + (i - first_row) * row_spacing;

                    g2d.setColor(folding.getTiles()[i][j].getColor());
                    g2d.fillRect(x, y, rectangle_length, rectangle_length);
                    if(folding_elements[i][j].getElements().contains(",")){
                        g2d.fillRect(x + 50 ,y - 25, rectangle_length, rectangle_length);
                    }

                    List<Direction> nextDirections = folding_elements[i][j].getNextDirections();
                    g2d.setColor(Color.BLACK);

                    for (Direction nextDirection : nextDirections) {
                        if (nextDirection == Direction.UP)
                            g2d.drawLine(x + (rectangle_length / 2), y, x + (rectangle_length / 2), y - row_spacing + rectangle_length);
                        else if (nextDirection == Direction.DOWN)
                            g2d.drawLine(x + (rectangle_length / 2), y + rectangle_length, x + (rectangle_length /2), y + row_spacing);
                        else if (nextDirection == Direction.LEFT)
                            g2d.drawLine(x , y + rectangle_length / 2, x - column_spacing + rectangle_length, y + rectangle_length / 2);
                        else if (nextDirection == Direction.RIGHT)
                            g2d.drawLine(x + rectangle_length , y + rectangle_length / 2, x + column_spacing, y + rectangle_length / 2);
                    }

                    g2d.setFont(new Font("Arial", Font.PLAIN, 60));
                    g2d.setColor(toogle_color(folding_elements[i][j].getColor()));
                    g2d.drawString(folding_elements[i][j].getElements(), x - 70 + (rectangle_length / 2), y + 20 + (rectangle_length / 2));
                }
            }
        }


        g2d.setColor(Color.BLACK);

        g2d.drawString("Fitness = " + String.format(Locale.GERMANY,"%.4f", folding.fitness()), 500, height - (footer_height / 2));
        g2d.drawString("Overlaps = " + folding.getNumberOverlaps(), 1200, height - (footer_height / 2));
        g2d.drawString("Bonds = " + folding.getNumberBonds(), 1900, height - (footer_height / 2));

        g2d.dispose();

        try {
            File outputfile = new File("images/" + image_name + ".png");
            ImageIO.write(image, "png", outputfile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Color toogle_color(Color c){
        if(c == Color.BLACK) return Color.WHITE;
        return Color.BLACK;
    }
}
