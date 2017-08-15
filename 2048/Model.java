package com.javarush.task.task35.task3513;

import java.util.*;

/**
 * Created by oem on 23.05.17.
 */
public class Model {

   protected int score;
   protected int maxTile;
    boolean  isSaveNeeded = true;

   private Stack<Tile[][]> previousStates = new Stack<>();
   private Stack<Integer> previousScores = new Stack<>();

    private static final int FIELD_WIDTH = 4;

    private Tile[][] gameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];

    public Model() {
        this.score = 0;
        this.maxTile = 2;
        resetGameTiles();
    }

    public void autoMove() {
        PriorityQueue<MoveEfficiency> queue = new PriorityQueue<>(4, Collections.reverseOrder());
        queue.offer(getMoveEfficiency(this::up));
        queue.offer(getMoveEfficiency(this::down));
        queue.offer(getMoveEfficiency(this::left));
        queue.offer(getMoveEfficiency(this::right));
        queue.peek().getMove().move();
    }

    private void saveState(Tile[][] tiles) {
        Tile[][] previosTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for(int i = 0; i < FIELD_WIDTH; i++) {
            for(int j = 0; j < FIELD_WIDTH; j++) {
                previosTiles[i][j] = new  Tile(tiles[i][j].value);
            }
        }
        previousStates.push(previosTiles);
        previousScores.push(score);
        isSaveNeeded = false;
    }

    public boolean hasBoardChanged() {
        boolean isChanged = false;
        for(int i = 0; i < gameTiles.length; i++) {
            for(int j = 0; j < gameTiles.length; j++) {
                if(gameTiles[i][j].value != previousStates.peek()[i][j].value) {
                    isChanged = true;
                }
            }
        }
        return isChanged;
    }

    public MoveEfficiency getMoveEfficiency(Move move) {
        MoveEfficiency moveEfficiency;
        move.move();
        if(hasBoardChanged()) {
            moveEfficiency = new MoveEfficiency(getEmptyTiles().size(), score, move);
        }
        else moveEfficiency = new MoveEfficiency(-1, 0, move);
        rollback();
        return moveEfficiency;
    }

    public void rollback() {
        if(!previousScores.empty() && !previousStates.empty()) {
            gameTiles = previousStates.pop();
            score =  previousScores.pop();
        }
    }

    public boolean canMove() {
        if(!getEmptyTiles().isEmpty())
            return true;
        for(int i = 0; i < gameTiles.length; i++) {
            for(int j = 1; j < gameTiles.length; j++) {
                if(gameTiles[i][j].value == gameTiles[i][j-1].value)
                    return true;
            }
        }
        for(int j = 0; j < gameTiles.length; j++) {
            for(int i = 1; i < gameTiles.length; i++) {
                if(gameTiles[i][j].value == gameTiles[i-1][j].value)
                    return true;
            }
        }
        return false;
    }

    public void rotate(Tile[][] ar) {
        Tile x = ar[0][0];
        ar[0][0] = ar[3][0];
        ar[3][0] = ar[3][3];
        ar[3][3] = ar[0][3];
        ar[0][3] = x;

        Tile z = ar[0][2];
        ar[0][2] = ar[1][0];
        ar[1][0] = ar[3][1];
        ar[3][1] = ar[2][3];
        ar[2][3] = z;

        Tile d = ar[0][1];
        ar[0][1] = ar[2][0];
        ar[2][0] = ar[3][2];
        ar[3][2] = ar[1][3];
        ar[1][3] = d;

        Tile y = ar[1][1];
        ar[1][1] = ar[2][1];
        ar[2][1] = ar[2][2];
        ar[2][2] = ar[1][2];
        ar[1][2] = y;
    }

    public void randomMove() {
        int n = ((int) (Math.random()*100))%4;
        switch (n) {
            case 0 : right();
            break;
            case 1 : left();
            break;
            case 2 : up();
            break;
            case 3 : down();
            break;

        }
    }

    public void right() {
        saveState(gameTiles);
        rotate(gameTiles);
        rotate(gameTiles);
        left();
        rotate(gameTiles);
        rotate(gameTiles);
    }

    public void up() {
        saveState(gameTiles);
        rotate(gameTiles);
        rotate(gameTiles);
        rotate(gameTiles);
        left();
        rotate(gameTiles);
    }

    public void down() {
        saveState(gameTiles);
        rotate(gameTiles);
        left();
        rotate(gameTiles);
        rotate(gameTiles);
        rotate(gameTiles);
    }

    public void left() {
        if (isSaveNeeded) {
            saveState(gameTiles);
        }
        boolean isChanged = false;
        for(Tile[] t : gameTiles) {
           if(compressTiles(t) || mergeTiles(t)) {
               isChanged = true;
           }
        }
        if (isChanged) {
            addTile();
        }
        isSaveNeeded = true;

    }

     private boolean compressTiles(Tile[] tiles) {
        boolean isChanged = false;
            for(int i = 0; i<tiles.length; i++) {
                if (tiles[i].value == 0 && i < tiles.length-1 && tiles[i+1].value != 0) {
                    isChanged = true;
                    int tmp = tiles[i].value;
                    tiles[i].value = tiles[i+1].value;
                    tiles[i+1].value = tmp;
                    i = -1;
                }

            }
        return isChanged;
    }

     private boolean mergeTiles(Tile[] tiles) {
        Tile[] orifinalTiles = new Tile[tiles.length];
        boolean isChanged = false;
        for(int i = 0; i < tiles.length; i++) {
            orifinalTiles[i] = new Tile(tiles[i].value);
        }
        for(int i = 0; i < tiles.length-1; i++) {
            if(tiles[i].value == tiles[i+1].value) {
                tiles[i].value *= 2;
                tiles[i+1].value = 0;
                score += tiles[i].value;
                if(maxTile < tiles[i].value) {
                    maxTile = tiles[i].value;
                }
                compressTiles(tiles);
            }
        }

        for(int i = 0; i < tiles.length; i++) {
            if(tiles[i].value != orifinalTiles[i].value) {
                isChanged = true;
            }
        }
        return isChanged;
    }

    public void resetGameTiles() {
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++ ) {
                gameTiles[i][j] = new Tile();
            }
        }
        addTile();
        addTile();
    }

    public void addTile() {
        int randomValue = Math.random() < 0.9 ? 2 : 4;
        List<Tile> emptyTiles = getEmptyTiles();
        emptyTiles.get((int) (emptyTiles.size() * Math.random())).value = randomValue;

    }

    private List<Tile> getEmptyTiles() {
        List<Tile> emptyTiles = new ArrayList<>();
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                if (gameTiles[i][j].isEmpty()) {
                        emptyTiles.add(gameTiles[i][j]);
                }
            }
        }
        return emptyTiles;
    }


    public Tile[][] getGameTiles() {
        return gameTiles;
    }
}
