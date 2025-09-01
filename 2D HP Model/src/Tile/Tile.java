package Tile;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import utilities.utilities.Direction;

public class Tile implements Cloneable {
    private List<Boolean> types;
    private List<Integer> amino_acids;
    private List<Direction> nextDirections;

    public Tile(boolean hydrophobic, int amino_acid_number, Direction direction){
        this.amino_acids = new ArrayList<>();
        this.nextDirections = new ArrayList<>();
        this.types = new ArrayList<>();

        this.types.add(hydrophobic);
        this.amino_acids.add(amino_acid_number);
        this.nextDirections.add(direction);
    }

    public boolean hasAcid(int index){
        return amino_acids.contains(index);
    }

    public boolean getType(int acid_number){
        return types.get(this.getAcidIndex(acid_number));
    }

    private int getAcidIndex(int acid_number){
        for(int i = 0; i < this.amino_acids.size(); i++)
            if(acid_number == this.amino_acids.get(i))
                return i;
        return -1;
    }


    public Color getColor(){
        if(this.types.getFirst()){
            return Color.BLACK;
        } else {
            return Color.WHITE;
        }
    }

    public boolean removeAminoAcid(int acid_number){
        if(this.amino_acids.size() == 1) return true;

        for(int i= 0; i < this.amino_acids.size(); i++)
            if(acid_number == this.amino_acids.get(i)) {
                this.amino_acids.remove(i);
                this.nextDirections.remove(i);
                return this.amino_acids.isEmpty();
            }

        return false;
    }

    public String getElements(){
        StringBuilder result = new StringBuilder(String.valueOf(amino_acids.getFirst()));
        for (int i = 1; i < amino_acids.size(); i++){
            result.append(",").append(amino_acids.get(i));
        }
        return result.toString();
    }

    public void setDirection(Direction dir, int acid_number){
        for(int i = 0; i < this.amino_acids.size(); i++){
            if(acid_number == this.amino_acids.get(i))
                this.nextDirections.set(i, dir);
        }
    }

    public void addAminoAcid(int amino_acid_number, Direction dir, boolean type){
        this.amino_acids.add(amino_acid_number);
        this.nextDirections.add(dir);
        this.types.add(type);
    }

    public int getOverlaps(){
        int n = amino_acids.size();
        return (n * (n - 1)) / 2;
    }

    public List<Direction> getNextDirections(){
        return new ArrayList<>(this.nextDirections);
    }

    public boolean is_last_element(int acid_number){
        return acid_number == this.amino_acids.get(amino_acids.size() - 1);
    }

    public Direction getDirection(int acid_number){
        for(int i = 0; i < this.amino_acids.size(); i++)
            if(acid_number == this.amino_acids.get(i))
                return this.nextDirections.get(i);
        return Direction.NONE;
    }

    public int get_bounds(Tile t){
        int counter = 0;

        if (t == null) return counter;

        for (int m_amino : this.amino_acids){
            for(int t_amino : t.amino_acids){
                if(m_amino + 1 != t_amino && m_amino - 1 != t_amino){
                    if(this.getType(m_amino) && t.getType(t_amino)){
                        counter++;
                    }
                }
            }

        }

        return counter;
    }

    @Override
    public Tile clone() {
        try {
            Tile cloned = (Tile) super.clone();
            // Deep copy of the lists
            cloned.types = new ArrayList<>(this.types);
            cloned.amino_acids = new ArrayList<>(this.amino_acids);
            cloned.nextDirections = new ArrayList<>(this.nextDirections);
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Cloning not supported", e);
        }
    }
}
