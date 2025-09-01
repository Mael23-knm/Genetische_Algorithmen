package utilities;

import Tile.Tile;
import java.util.Random;

public class utilities {
    public enum Direction{UP, DOWN, LEFT, RIGHT, NONE};

    public static Direction[] DIRECTIONS= {Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};

    public static Object pick_random_object(Object[] array, Object outsider){
        Random rand = new Random();
        int randomIndex = 0;
        do {
            randomIndex = rand.nextInt(array.length);
        } while (array[randomIndex] == outsider);
        return array[randomIndex];
    }

    public static Direction get_oposite_direction(Direction direction){
        return switch (direction) {
            case UP -> Direction.DOWN;
            case DOWN -> Direction.UP;
            case LEFT -> Direction.RIGHT;
            case RIGHT -> Direction.LEFT;
            case NONE -> Direction.NONE;
        };
    }

    public static Tile[] combine_arrays(Tile[] t1, Tile[] t2){

        if(t1.length == 0) return t2;
        if(t2.length == 0) return t1;

        Tile[] result = new Tile[t1.length + t2.length];
        System.arraycopy(t1, 0, result, 0, t1.length);
        System.arraycopy(t2, 0, result, t1.length, t2.length);
        return result;
    }

    public static int[] getDirectionCoordinates(Direction direction){
        return switch (direction) {
            case UP -> new int[]{-1, 0};
            case DOWN -> new int[]{1, 0};
            case RIGHT -> new int[]{0, 1};
            case LEFT -> new int[]{0, -1};
            default -> new int[]{0, 0};
        };
    }

    public enum Selection{FITNESS, TOURNAMENT};
}
