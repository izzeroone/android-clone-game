package cyberse.cloneproject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;



public class Grid {
    //field save the current state
    public final Tile[][] field;
    //undoField state before
    public final Tile[][] undoField;
    //to save current stage then assign to undoField to avoid mess thing up
    private final Tile[][] bufferField;
    public int score = 0;

    public Grid(int sizeX, int sizeY) {
        field = new Tile[sizeX][sizeY];
        undoField = new Tile[sizeX][sizeY];
        bufferField = new Tile[sizeX][sizeY];
        score = 0;
        clearGrid();
        clearUndoGrid();
    }

    public void addTile(int x, int y) {
        //ratio 0,5 for 1. 0,3 for 2, 0,2 for 3
        //check whether the cell is null
        if (field[x][y] == null) {
            int value = Math.random() <= 0.5 ? 1 : Math.random() <= 0.6 ? 2 : 3;
            Tile tile = new Tile(new Cell(x, y), value);
            spawnTile(tile);
        }
    }

    public void spawnTile(Tile tile) {
        //insert to grid
        insertTile(tile);
    }

    public void clearMergedFrom() {
        //clear merge from to ready to merge
        for (Tile[] array : field) {
            for (Tile tile : array) {
                //check whether tile null to avoid exception
                if (isCellOccupied(tile)) {
                    tile.setMergedFrom(null);
                }
            }
        }
    }

    public void moveTile(Tile tile, Cell cell) {
        //move tile to another cell
        field[tile.getX()][tile.getY()] = null;
        field[cell.getX()][cell.getY()] = tile;
        tile.updatePosition(cell);
    }

    private Cell getMovingVector(int direction) {
        Cell[] map = {
                new Cell(0, -1), // up
                new Cell(1, 0),  // right
                new Cell(0, 1),  // down
                new Cell(-1, 0)  // left
        };
        return map[direction];
    }

    private List<Integer> makeTravelCellX(Cell vector) {
        List<Integer> traversals = new ArrayList<>();

        for (int xx = 0; xx < field.length; xx++) {
            traversals.add(xx);
        }
        if (vector.getX() == 1) {
            Collections.reverse(traversals);
        }

        return traversals;
    }

    private List<Integer> makeTravelCellY(Cell vector) {
        List<Integer> traversals = new ArrayList<>();

        for (int xx = 0; xx < field[0].length; xx++) {
            traversals.add(xx);
        }
        if (vector.getY() == 1) {
            Collections.reverse(traversals);
        }

        return traversals;
    }

    private Cell[] findFarthestPosition(Cell cell, Cell vector) {
        Cell previous;
        Cell nextCell = new Cell(cell.getX(), cell.getY());
        do {
            previous = nextCell;
            nextCell = new Cell(previous.getX() + vector.getX(),
                    previous.getY() + vector.getY());
        } while (isCellWithinBounds(nextCell) && isCellAvailable(nextCell));

        return new Cell[]{previous, nextCell};
    }

    public boolean move(int direction) {

        //make travel loop varible
        Cell vector = getMovingVector(direction);
        List<Integer> travelX = makeTravelCellX(vector);
        List<Integer> travelY = makeTravelCellY(vector);

        boolean moved = false;
        //clear merge from
        clearMergedFrom();
        //loop all the cell in grid
        for (int xx : travelX) {
            for (int yy : travelY) {
                if (moveAndCheck(xx, yy, direction) == true) {
                    moved = true;
                }
            }
        }
        return moved;
    }

    private boolean moveAndCheck(int xx, int yy, int direction) {
        boolean moved = false;
        //the the moving vector
        Cell vector = getMovingVector(direction);
        //get the content the current cell
        Cell cell = new Cell(xx, yy);
        Tile tile = getCellContent(cell);
        //check whether the current tile is empty or not
        if (tile != null) {
            //find the farthest cell in the direction
            Cell[] positions = findFarthestPosition(cell, vector);
            //get the second cell because the first one is itself
            Tile next = getCellContent(positions[1]);
            //whether they have the same value and not merge with other cell
            if (next != null && next.getValue() == tile.getValue() && next.getMergedFrom() == null) {
                //they have the same value
                //increment their value by 1
                Tile merge = new Tile(positions[1], tile.getValue() + 1);
                //set the 2 cells are merged
                Tile[] temp = {tile, next};
                merge.setMergedFrom(temp);
                //remove the first one (or moving cell) and insert the merge cell
                insertTile(merge);
                removeTile(tile);
                tile.updatePosition(positions[1]);
                score += tile.getValue() + 1;

            } else {
                //just move the cell
                moveTile(tile, positions[0]);
            }

            if (!positionsEqual(cell, tile)) {
                //same cell have move
                moved = true;
            }
        }
        return moved;
    }


    private boolean positionsEqual(Cell first, Cell second) {
        return first.getX() == second.getX() && first.getY() == second.getY();
    }

    public Cell randomAvailableCell() {
        ArrayList<Cell> availableCells = getAvailableCells();
        if (availableCells.size() >= 1) {
            return availableCells.get((int) Math.floor(Math.random() * availableCells.size()));
        }
        return null;
    }

    public ArrayList<Cell> getAvailableCells() {
        ArrayList<Cell> availableCells = new ArrayList<>();
        for (int xx = 0; xx < field.length; xx++) {
            for (int yy = 0; yy < field[0].length; yy++) {
                if (field[xx][yy] == null) {
                    availableCells.add(new Cell(xx, yy));
                }
            }
        }
        return availableCells;
    }

    public int getNumberOccupiedCells() {
        int occupiedCells = 0;
        for (int xx = 0; xx < field.length; xx++) {
            for (int yy = 0; yy < field[0].length; yy++) {
                if (field[xx][yy] != null) {
                    occupiedCells++;
                }
            }
        }
        return occupiedCells;
    }

    public boolean isCellsAvailable() {
        return (getAvailableCells().size() >= 1);
    }

    public boolean isCellAvailable(Cell cell) {
        return !isCellOccupied(cell);
    }

    public boolean isCellOccupied(Cell cell) {
        return (getCellContent(cell) != null);
    }

    public Tile getCellContent(Cell cell) {
        if (cell != null && isCellWithinBounds(cell)) {
            return field[cell.getX()][cell.getY()];
        } else {
            return null;
        }
    }

    public Tile getCellContent(int x, int y) {
        if (isCellWithinBounds(x, y)) {
            return field[x][y];
        } else {
            return null;
        }
    }

    public boolean isCellWithinBounds(Cell cell) {
        return 0 <= cell.getX() && cell.getX() < field.length
                && 0 <= cell.getY() && cell.getY() < field[0].length;
    }

    private boolean isCellWithinBounds(int x, int y) {
        return 0 <= x && x < field.length
                && 0 <= y && y < field[0].length;
    }

    public void insertTile(Tile tile) {
        field[tile.getX()][tile.getY()] = tile;
    }

    public void removeTile(Tile tile) {
        field[tile.getX()][tile.getY()] = null;
    }

    public void saveTiles() {
        for (int xx = 0; xx < bufferField.length; xx++) {
            for (int yy = 0; yy < bufferField[0].length; yy++) {
                if (bufferField[xx][yy] == null) {
                    undoField[xx][yy] = null;
                } else {
                    undoField[xx][yy] = new Tile(xx, yy, bufferField[xx][yy].getValue());
                }
            }
        }
    }

    public void prepareSaveTiles() {
        for (int xx = 0; xx < field.length; xx++) {
            for (int yy = 0; yy < field[0].length; yy++) {
                if (field[xx][yy] == null) {
                    bufferField[xx][yy] = null;
                } else {
                    bufferField[xx][yy] = new Tile(xx, yy, field[xx][yy].getValue());
                }
            }
        }
    }

    public void revertTiles() {
        for (int xx = 0; xx < undoField.length; xx++) {
            for (int yy = 0; yy < undoField[0].length; yy++) {
                if (undoField[xx][yy] == null) {
                    field[xx][yy] = null;
                } else {
                    field[xx][yy] = new Tile(xx, yy, undoField[xx][yy].getValue());
                }
            }
        }
    }

    public void clearGrid() {
        for (int xx = 0; xx < field.length; xx++) {
            for (int yy = 0; yy < field[0].length; yy++) {
                field[xx][yy] = null;
            }
        }
    }

    public void clearUndoGrid() {
        for (int xx = 0; xx < field.length; xx++) {
            for (int yy = 0; yy < field[0].length; yy++) {
                undoField[xx][yy] = null;
            }
        }
    }

    public int countOccupiedCell(){
        int count = 0;
        for (int xx = 0; xx < field.length; xx++) {
            for (int yy = 0; yy < field[0].length; yy++) {
                if(field[xx][yy] != null){
                    count++;
                }
            }
        }
        return  count;
    }

    public int countUndoOccupiedCell(){
        int count = 0;
        for (int xx = 0; xx < undoField.length; xx++) {
            for (int yy = 0; yy < undoField[0].length; yy++) {
                if(undoField[xx][yy] != null){
                    count++;
                }
            }
        }
        return  count;
    }

    public Grid clone() {
        Grid newGrid = new Grid(field.length, field[0].length);
        for (int xx = 0; xx < field.length; xx++) {
            for (int yy = 0; yy < field[0].length; yy++) {
                if (field[xx][yy] != null) {
                    Tile tile = new Tile(xx, yy, field[xx][yy].getValue());
                    newGrid.insertTile(tile);
                }
            }
        }
        newGrid.score = score;
        return newGrid;
    }






}
