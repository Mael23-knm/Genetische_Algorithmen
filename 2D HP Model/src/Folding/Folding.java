package Folding;

import java.util.HashMap;
import utilities.utilities.*;
import Tile.Tile;
import utilities.utilities;

import static utilities.utilities.getDirectionCoordinates;

public class Folding implements Cloneable{
    Tile[][] folding_elements;
    int sequence_length;

    public void print(){

        for(int i = 0; i < folding_elements.length; i++) {
            for (int j = 0; j < folding_elements[i].length; j++) {
                if (folding_elements[i][j] != null)
                    System.out.print(i + ":" + j + "=" + folding_elements[i][j].getElements() + "(" + folding_elements[i][j].getNextDirections() + ")" + " ");
            }
            System.out.print("\n");
        }
    }

    public Tile[][] getTiles(){
        return this.folding_elements;
    }

    public Folding(String sequence){

        int foldingLength =  (2 * sequence.length()) -1;

        this.folding_elements = new Tile[foldingLength][foldingLength];
        this.sequence_length = sequence.length();

        int x_pos = sequence.length() - 1;
        int y_pos = sequence.length() - 1;
        Direction nextAcidDirection = Direction.NONE;

        for(int i=0; i < sequence.length(); i++){
            boolean acid_type = (sequence.charAt(i) == '0');

            //nextAcidDirection = (Direction) utilities.pick_random_object(utilities.DIRECTIONS, utilities.get_oposite_direction(nextAcidDirection));
            nextAcidDirection = (Direction) utilities.pick_random_object(utilities.DIRECTIONS, Direction.NONE);

            if (i == sequence.length() - 1)
                nextAcidDirection = Direction.NONE;

            this.addTile(x_pos, y_pos, acid_type, i, nextAcidDirection);

            int[] normalised_direction = getDirectionCoordinates(nextAcidDirection);
            y_pos += normalised_direction[0];
            x_pos += normalised_direction[1];
        }
    }

    private void addTile(int x, int y, boolean acid_type, int acid_number, Direction direction){
        if(this.folding_elements[y][x] == null){
            Tile tile = new Tile(acid_type, acid_number, direction);
            this.folding_elements[y][x] = tile;
        }else{
            this.folding_elements[y][x].addAminoAcid(acid_number, direction, acid_type);
        }
    }

    public double fitness() {
        int number_of_bounds = getNumberBonds();
        if (number_of_bounds == 0)
            return 0.000001 / Math.pow(getNumberOverlaps() + 1, 2);
        return (double)number_of_bounds / Math.pow(getNumberOverlaps() + 1, 2);
    }

    public int getNumberOverlaps(){
        int number = 0;
        for (Tile[] foldingElement : this.folding_elements)
            for (Tile tile : foldingElement)
                if (tile != null)
                    number += tile.getOverlaps();
        return number;
    }

    public int getNumberBonds(){
        int bonds = 0;
        for (int i = 0; i < this.folding_elements.length; i++)
            for (int j = 0; j < this.folding_elements[i].length; j++)
                if(this.folding_elements[i][j] != null){
                    if(i < this.folding_elements.length - 1){
                        Tile unter_tile = this.folding_elements[i+1][j];
                        bonds += this.folding_elements[i][j].get_bounds(unter_tile);
                    }

                    if(j < this.folding_elements[i].length - 1){
                        Tile rechter_tile = this.folding_elements[i][j+1];
                        bonds += this.folding_elements[i][j].get_bounds(rechter_tile);
                    }
                }
        return bonds;
    }

    public void compute_crossover(Tile[] foreign_gens){
        Tile[]  remaining_gens = this.cute_elements(foreign_gens.length, this.sequence_length);
        this.place_elements(utilities.combine_arrays(foreign_gens, remaining_gens));
    }


    public Tile[] cute_elements(int start, int number_of_elements) {
        if (number_of_elements == 0 || number_of_elements > this.sequence_length || start >= number_of_elements)
            return new Tile[0];

        Tile[] result = new Tile[number_of_elements - start];
        int x_pos, y_pos;
        if (start == 0) {
            x_pos = y_pos = this.sequence_length - 1;
        } else {
            int[] coordinates = findTileByNumber(start);
            y_pos = coordinates[0];
            x_pos = coordinates[1];
        }

        for (int acid_number = start, index = 0; acid_number < number_of_elements; acid_number++, index++) {
            result[index] = this.folding_elements[y_pos][x_pos];

            if(folding_elements[y_pos][x_pos].is_last_element(acid_number))
                this.folding_elements[y_pos][x_pos] = null;

            if (result[index] != null) {
                int[] coordinates = getDirectionCoordinates(result[index].getDirection(acid_number));
                x_pos += coordinates[1];
                y_pos += coordinates[0];
            }
        }
        return result;
    }

    @Override
    public Object clone() {
        try {
            Folding cloned = (Folding) super.clone();

            cloned.folding_elements = new Tile[this.folding_elements.length][];
            for (int i = 0; i < this.folding_elements.length; i++) {
                cloned.folding_elements[i] = new Tile[this.folding_elements[i].length];
                for (int j = 0; j < this.folding_elements[i].length; j++) {
                    if (this.folding_elements[i][j] != null) {
                        cloned.folding_elements[i][j] = (Tile) this.folding_elements[i][j].clone(); // Annahme: Tile implementiert ebenfalls clone()
                    }
                }
            }

            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }


    public HashMap<String, Integer> get_folding_information(){

        int first_row_index = 100000;
        int first_column_index = 100000;
        int last_row_index = -1;
        int last_column_index = -1;

        for(int i = 0; i < this.folding_elements.length; i++){
            for(int j = 0; j < this.folding_elements[i].length; j++){
                if(this.folding_elements[i][j]!= null){

                    first_column_index = Math.min(first_column_index, j);
                    first_row_index = Math.min(first_row_index, i);

                    last_row_index = Math.max(last_row_index, i);
                    last_column_index = Math.max(last_column_index, j);
                }
            }
        }
        HashMap<String, Integer> folding_info = new HashMap<>();
        folding_info.put("first_row", first_row_index);
        folding_info.put("first_column", first_column_index);
        folding_info.put("last_row", last_row_index);
        folding_info.put("last_column", last_column_index);
        return folding_info;
    }

    public void place_elements(Tile[] tiles) {
        int x_pos = this.sequence_length - 1;
        int y_pos = this.sequence_length - 1;

        for(int i = 0; i < tiles.length; i++) {
            Tile current = tiles[i];
            this.addTile(x_pos, y_pos, current.getType(i), i, current.getDirection(i));

            int[] coordinates = utilities.getDirectionCoordinates(current.getDirection(i));
            y_pos += coordinates[0];
            x_pos += coordinates[1];
        }
    }

    public void mutate(int acid_number){
        if(acid_number >= this.sequence_length || acid_number < 0)
            return;

        int[] coordinates = findTileByNumber(acid_number);
        Tile t = this.folding_elements[coordinates[0]][coordinates[1]];

        Direction mutatedDirection = (Direction) utilities.pick_random_object(utilities.DIRECTIONS, t.getDirection(acid_number));
        t.setDirection(mutatedDirection, acid_number);

        Tile[] first = this.cute_elements(0, acid_number + 1);
        Tile[] second = this.cute_elements(acid_number + 1, this.sequence_length);

        this.place_elements(utilities.combine_arrays(first, second));
    }

    private int[] findTileByNumber(int acid_number) {
        for (int i = 0; i < this.folding_elements.length; i++)
            for (int j = 0; j < this.folding_elements[i].length; j++)
                if (this.folding_elements[i][j] != null && this.folding_elements[i][j].hasAcid(acid_number))
                    return new int[]{i, j};
        return null;
    }
}