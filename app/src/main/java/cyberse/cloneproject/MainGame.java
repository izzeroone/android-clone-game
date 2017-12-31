package cyberse.cloneproject;

//Our game


import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainGame {
    //timer and its update
    public static final int MAX_TIME = 60000;
    private static final long MOVE_ANIMATION_TIME = GameView.BASE_ANIMATION_TINE;
    private static final long SPAWN_ANIMATION_TIME = GameView.BASE_ANIMATION_TINE;
    private static final long NOTIFICATION_DELAY_TIME = MOVE_ANIMATION_TIME + SPAWN_ANIMATION_TIME;
    private static final long NOTIFICATION_ANIMATION_TIME = GameView.BASE_ANIMATION_TINE * 5;
    private static final String HIGH_SCORE = "high score";
    //Maximum number of mive to make winning state
    private static final int MAX_MOVE = 100;
    public static long timer = 0;
    public final int numCellX = 3;
    public final int numCellY = 3;
    private final Context mContext;
    private final GameView mView;
    public GameState gameState = GameState.NORMAL;
    public GameState lastGameState = GameState.NORMAL;
    public GameState bufferGameState = GameState.NORMAL;
    public Grid winGrid = null; // winning condition of the game
    public Grid grid = null;
    public Grid tempGrid = null;
    public AnimationGrid animationGrid;
    public boolean canUndo;
    public long score = 0;
    public long lastScore = 0;
    public float percent = 0;
    private long bufferScore;
    private long startTime = 0;


    //Sound
    MediaPlayer step;
    public MainGame(Context context, GameView view){
        mContext = context;
        mView = view;
        startTime = System.currentTimeMillis();
        timer = 0;
    }

    public void newGame(){
        Log.d("timer", String.valueOf(timer));
        if(grid == null){
            //create new gird
            grid = new Grid(numCellX, numCellY);
        } else{
            //we already have our grid so save them
            //prepareUndoState();
            //saveUndoState();
            grid.clearGrid();
        }

        //avoid create winGrid multi time
        if(winGrid == null){
            winGrid = new Grid(numCellX, numCellY);
        }

        //create tempGrid to exchange winGrid and grid
        if(tempGrid == null){
            tempGrid = new Grid(numCellX, numCellY);
        }

        //create animation grid
        animationGrid = new AnimationGrid(numCellX, numCellY);
        //set up score
        score = 0;
        //add start title
        addStartTiles();
        //save start title to temp
        copyGridState(tempGrid, grid);
        //make WinGrid save them to winGrid
        makeWinningState();
        copyGridState(winGrid, grid);
        //show the winGrid
        gameState = GameState.READY;
        //reset time
        if (GameActivity.timerRunnable != null)
            GameActivity.timerRunnable.onPause();
        //cancel all animation and add spawn animation
        animationGrid.cancelAnimations();
        spawnGridAnimation();
        mView.refreshLastTime = true;
        mView.resyncTime();
        mView.invalidate();
    }

    public void gameStart(){
        if(gameState == GameState.READY){
            //revert back to start gird
            copyGridState(grid,tempGrid);
            gameState = GameState.NORMAL;
            //clear undo grid a avoid error
            grid.clearUndoGrid();
            canUndo = false;
            //reset score
            score = 0;
            //reset time
            timer = 0;
            //starting counting time
            startTime = System.currentTimeMillis();
            GameActivity.timerRunnable.onResume();
            //add spawn animation to all cell
            animationGrid.cancelAnimations();
            spawnGridAnimation();

            //refresh view
            mView.refreshLastTime = true;
            mView.resyncTime();
            mView.invalidate();
        } else{
            //the game already start, make same notification
        }
    }

    public void update() {
        timer = System.currentTimeMillis() - startTime;
        percent = 1.0f * timer / MAX_TIME;
        if (timer > MAX_TIME) {
            gameState = GameState.LOST;
            endGame();
        }

        //mView.redrawTimePercent();
    }

    private void addStartTiles(){
        for(int xx = 0; xx < numCellX; xx++){
            //make random cell emply
            int ignoreCellY = (int)(Math.random() * numCellY);
            for(int yy = 0; yy < numCellY; yy++){
                if(yy != ignoreCellY){
                    //add tile to cell
                    addTile(xx,yy);
                }
            }
        }
    }

    private void addTile(int x, int y)
    {
        //ratio 0,7 for 1. 0,25 for 2, 0,05 for 3
        //check whether the cell is null
        if(grid.field[x][y] == null){
            int value = Math.random() <= 0.7 ? 1 : Math.random() <= 0.83 ? 2 : 3;
            Tile tile = new Tile(new Cell(x, y), value);
            spawnTile(tile);
        }
    }

    private void spawnTile(Tile tile){
        //insert to grid
        grid.insertTile(tile);
        //add animation
        animationGrid.startAnimation(tile.getX(), tile.getY(), AnimationType.SPAWN,
                SPAWN_ANIMATION_TIME, MOVE_ANIMATION_TIME, null);
    }

    private void spawnGridAnimation(){
        for (int xx = 0; xx < grid.field.length; xx++) {
            for (int yy = 0; yy < grid.field[0].length; yy++) {
                if(grid.field[xx][yy] != null){
                    animationGrid.startAnimation(xx, yy, AnimationType.SPAWN,
                            SPAWN_ANIMATION_TIME, MOVE_ANIMATION_TIME, null); //Direction: -1 = EXPANDING
                }
            }
        }
    }

    private void copyGridState(Grid target, Grid source)
    {
        for (int xx = 0; xx < source.field.length; xx++) {
            for (int yy = 0; yy < source.field[0].length; yy++) {
                if(source.field[xx][yy] == null){
                    target.field[xx][yy] = null;
                } else {
                    target.field[xx][yy] = new Tile(xx, yy, source.field[xx][yy].getValue());
                }
            }
        }
    }

    private void makeWinningState()
    {
        WinStateMaker maker = new WinStateMaker(numCellX );
        grid = maker.makeWinState(grid);
    }

    private void clearMergedFrom(){
        //clear merge from to ready to merge
        for(Tile[] array : grid.field){
            for(Tile tile : array){
                //check whether tile null to avoid exception
                if(grid.isCellOccupied(tile)){
                    tile.setMergedFrom(null);
                }
            }
        }
    }

    private void moveTile(Tile tile, Cell cell){
        //move tile to another cell
        grid.field[tile.getX()][tile.getY()] = null;
        grid.field[cell.getX()][cell.getY()] = tile;
        tile.updatePosition(cell);
    }

    private void prepareUndoState() {
        grid.prepareSaveTiles();
        bufferScore = score;
        bufferGameState = gameState;
    }

    private void saveUndoState() {
        grid.saveTiles();
        canUndo = true;
        lastScore = bufferScore;
        lastGameState = bufferGameState;
    }

    public void revertUndoState(){
        if(!isActive()){
            return;
        }
        if(canUndo){
            canUndo = false;
            animationGrid.cancelAnimations();
            grid.revertTiles();
            score = lastScore;
            gameState = lastGameState;
            mView.refreshLastTime = true;
            mView.invalidate();
        }
    }

    public  boolean isActive() {
        return !(gameState == GameState.WIN || gameState == GameState.LOST || gameState == GameState.READY);
    }

    //moving to direction all cell
    public void move(int direction){
        //cancel all animation
        animationGrid.cancelAnimations();
        if(!isActive()){
            return;
        }
        //save current grid to buffer
        prepareUndoState();
        //make travel loop varible
        Cell vector = getMovingVector(direction);
        List<Integer> travelX = makeTravelCellX(vector);
        List<Integer> travelY = makeTravelCellY(vector);

        boolean moved = false;
        //clear merge from
        clearMergedFrom();
        //loop all the cell in grid
        for(int xx : travelX){
            for(int yy : travelY){
                        if(moveAndCheck(xx, yy, direction) == true){
                            moved = true;
                        }
            }
        }

        if(moved){
            //some cell has moved
            //save Undostate and check for Win Lose
            saveUndoState();
            checkWin();
            checkLose();
        }

        mView.resyncTime();
        mView.invalidate();

    }

    //move spectific cell
    public void move(int xx, int yy, int direction){

        animationGrid.cancelAnimations();
        // 0: up, 1: right, 2: down, 3: left
        if (!isActive()) {
            return;
        }
        prepareUndoState();
        clearMergedFrom();
        boolean moved = moveAndCheck(xx, yy, direction);


        if (moved) {
            saveUndoState();
            //addRandomTile();
            checkWin();
            checkLose();
        }
        mView.resyncTime();
        mView.invalidate();
    }

    private boolean moveAndCheck(int xx, int yy, int direction){
        step = MediaPlayer.create(mContext,R.raw.step);
        step.start();
        boolean moved = false;
        //the the moving vector
        Cell vector = getMovingVector(direction);
        //get the content the current cell
        Cell cell = new Cell(xx, yy);
        Tile tile = grid.getCellContent(cell);
        //check whether the current tile is empty or not
        if(tile != null){
            //find the farthest cell in the direction
            Cell[] positions = findFarthestPosition(cell, vector);
            //get the second cell because the first one is itself
            Tile next = grid.getCellContent(positions[1]);
            //whether they have the same value and not merge with other cell
            if(next != null && next.getValue() == tile.getValue() && next.getMergedFrom() == null){
                //they have the same value
                //increment their value by 1
                Tile merge = new Tile(positions[1], tile.getValue() + 1);
                //set the 2 cells are merged
                Tile[] temp = {tile, next};
                merge.setMergedFrom(temp);
                //remove the first one (or moving cell) and insert the merge cell
                grid.insertTile(merge);
                grid.removeTile(tile);
                tile.updatePosition(positions[1]);
                //add moving and merge animation
                int[] extras = {xx, yy}; // the cell moving to
                animationGrid.startAnimation(merge.getX(), merge.getY(), AnimationType.MOVE,
                        MOVE_ANIMATION_TIME, 0, extras);
                //merge animation after the move animation complete
                animationGrid.startAnimation(merge.getX(), merge.getY(), AnimationType.MERGE,
                        SPAWN_ANIMATION_TIME, MOVE_ANIMATION_TIME, null);
                //update the score
                score += merge.getValue();
            } else {
                //just move the cell
                moveTile(tile, positions[0]);
                int[] extras = {xx, yy, 0};
                animationGrid.startAnimation(positions[0].getX(), positions[0].getY(), AnimationType.MOVE,
                        MOVE_ANIMATION_TIME, 0, extras);
            }

            if(!positionsEqual(cell, tile)){
                //same cell have move
                moved = true;
            }
        }
        return  moved;
    }

    private Cell getMovingVector(int direction){
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

        for (int xx = 0; xx < numCellX; xx++) {
            traversals.add(xx);
        }
        if (vector.getX() == 1) {
            Collections.reverse(traversals);
        }

        return traversals;
    }

    private List<Integer> makeTravelCellY(Cell vector) {
        List<Integer> traversals = new ArrayList<>();

        for (int xx = 0; xx < numCellY; xx++) {
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
        } while (grid.isCellWithinBounds(nextCell) && grid.isCellAvailable(nextCell));

        return new Cell[]{previous, nextCell};
    }


    private boolean positionsEqual(Cell first, Cell second) {
        return first.getX() == second.getX() && first.getY() == second.getY();
    }

    private void checkLose() {
        if(grid.countOccupiedCell() < winGrid.countOccupiedCell() - 1){
            gameState = GameState.LOST;
            endGame();
        }
    }

    private  void checkWin(){
        if(isWin()){
            gameState = GameState.WIN;
            endGame();
        }
    }

    private boolean isWin() {
        for(int xx = 0; xx < grid.field.length; xx++)
        {
            for(int yy = 0; yy < grid.field[0].length; yy++)
            {
                if(grid.field[xx][yy] == null && winGrid.field[xx][yy] == null)
                    continue;
                if(grid.field[xx][yy] == null && winGrid.field[xx][yy] != null)
                    return false;
                else if(grid.field[xx][yy] != null && winGrid.field[xx][yy] == null)
                    return false;
                else if(grid.field[xx][yy].getValue() != winGrid.field[xx][yy].getValue())
                    return false;
            }
        }
        return true;
    }

    public void checkWinState(){
        for(int xx = 0; xx < grid.field.length; xx++)
        {
            for(int yy = 0; yy < grid.field[0].length; yy++)
            {
                if(grid.field[xx][yy] != null  ){
                    if(winGrid.field[xx][yy] != null && grid.field[xx][yy].getValue() == winGrid.field[xx][yy].getValue()){
                        animationGrid.startAnimation(xx, yy, AnimationType.CHECK_RIGHT_CIRCLE,
                                NOTIFICATION_ANIMATION_TIME, 0, null);
                    } else {
                        animationGrid.startAnimation(xx, yy, AnimationType.CHECK_WRONG_CIRCLE,
                                NOTIFICATION_ANIMATION_TIME / 2, 0, null);
                    }
                }
            }
        }
        mView.refreshLastTime = false;
        mView.resyncTime();
        mView.invalidate();
    }

    private void endGame() {
        GameActivity.timerRunnable.onPause();
        animationGrid.startAnimation(-1, -1, AnimationType.FADE_GLOBAL, NOTIFICATION_ANIMATION_TIME, NOTIFICATION_DELAY_TIME, null);
    }


}
