package cyberse.cloneproject;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

//draw game
public class GameView extends View {

    //Animation constant
    static final int BASE_ANIMATION_TINE = 100000000;
    private static final String TAG = MenuView.class.getSimpleName();
    private static final float MERGING_ACCELERATION = -0.5f;
    private static final float INITIAL_VELOCITY = (1 - MERGING_ACCELERATION) / 4;
    //number of diffirent type of celll
    public final  int numCellTypes = 21;
    //out game xD
    public final MainGame game;
    //Internal variables
    //Drawing paint
    private final Paint paint = new Paint();
    public boolean restartButtonEnabled = false;
    //Drawing location
    //Grid location
    public Rectangle gridRect = new Rectangle();
    //icon size for new game and undo button
    public int iconSize;
    //restart and undo button base on the same lin Y
    public int sYIcons;
    //new button
    public int sXNewGame;
    //undo button
    public int sXUndo;
    //check button
    public Rectangle checkRect = new Rectangle();
    //timer
    public Rectangle percentRect = new Rectangle();
    //Misc
    boolean refreshLastTime = true;
    //Bit map of the cell
    private BitmapDrawable[] bitmapCell = new BitmapDrawable[numCellTypes];
    //layout
    private int cellSize;
    private float textSize;
    private float cellTextSize;
    private int gridWidth;
    private int textPaddingSize;
    private int iconPaddingSize;
    //text size;
    private float instructionTextSize;
    private float subInstructionTextSize;
    //Asset
    private Drawable backgroundRectangle;
    private Drawable lightUpRectangle;
    private Drawable iconLeftRectangle;
    private Drawable iconRightRectangle;
    private Drawable iconRectangle;
    private Bitmap background = null;
    //text position
    private int sYInstruction;
    private int sYSubInstruction;
    private int eYAll;
    private int sYScore;
    //string
    private String instruction;
    private String subInstruction;
    //Timing
    private long lastFPSTime = System.nanoTime();

    public GameView(Context context) {
        super(context);

        Resources resources = context.getResources();
        //Loading resources
        game = new MainGame(context, this);
        try {
            //Getting assets
            backgroundRectangle = resources.getDrawable(R.drawable.background_rectangle);
            lightUpRectangle = resources.getDrawable(R.drawable.light_up_rectangle);
            iconLeftRectangle = resources.getDrawable(R.drawable.icon_rectangle_left);
            iconRightRectangle = resources.getDrawable(R.drawable.icon_rectangle_right);
            iconRectangle = resources.getDrawable(R.drawable.icon_rectangle);
            this.setBackgroundColor(resources.getColor(R.color.background));
            Typeface font = Typeface.createFromAsset(resources.getAssets(), "fonts/ClearSans-Bold.ttf");
            paint.setTypeface(font);
            paint.setAntiAlias(true);
        } catch (Exception e) {
            Log.e(TAG, "Error getting assets?", e);
        }
        setOnTouchListener(new GameListener(this));
        //game.newGame();
    }

    @Override
    public void onDraw(Canvas canvas) {
        //Reset the transparency of the screen

        canvas.drawBitmap(background, 0, 0, paint);

        drawScoreText(canvas);
        drawTimePercent(canvas);

        if (!game.isActive() && !game.animationGrid.isAnimationActive()) {
            drawNewGameButton(canvas, true);
        }


        drawCells(canvas);

        drawInstructionState(canvas);

        //Refresh the screen if there is still an animation running
        if (game.animationGrid.isAnimationActive()) {
            invalidate(gridRect.left, gridRect.top, gridRect.right, gridRect.bottom);
            update();
            //Refresh one last time on game end.
        } else if (!game.isActive() && refreshLastTime) {
            invalidate();
            refreshLastTime = false;
        }
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldW, int oldH) {
        super.onSizeChanged(width, height, oldW, oldH);
        makeLayout(width, height);
        createBitmapCells();
        createBackgroundBitmap(width, height);
    }

    private void drawDrawable(Canvas canvas, Drawable draw, int left, int top, int right, int bottom) {
        //Draw to canvas with bound
        draw.setBounds(left, top, right, bottom);
        draw.draw(canvas);
    }

    private void drawDrawable(Canvas canvas, Drawable draw, Rectangle rect) {
        //Draw to canvas with bound
        draw.setBounds(rect.left, rect.top, rect.right, rect.bottom);
        draw.draw(canvas);
    }

    private void drawCellText(Canvas canvas, int value) {
        //Draw the text number inside the cell
        int textShiftY = centerText();
        paint.setColor(getResources().getColor(R.color.text_white));
        canvas.drawText("" + value, cellSize / 2, cellSize / 2 - textShiftY, paint);
    }
    private void drawScoreText(Canvas canvas) {
        //Drawing the score text: Ver 2
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(getResources().getColor(R.color.text_black));
        paint.setTextSize(subInstructionTextSize);
        canvas.drawText(String.valueOf(game.score), (gridRect.left + gridRect.right) / 2, sYScore + paint.getTextSize(), paint);
    }

    private void drawTimePercent(Canvas canvas) {
        int percentRight = (int) (percentRect.left + game.percent * percentRect.getWidth());

        //Outputting scores box
        drawDrawable(canvas,
                backgroundRectangle,
                percentRect);
        drawDrawable(canvas,
                lightUpRectangle,
                percentRect.left,
                percentRect.top,
                percentRight,
                percentRect.bottom);
    }

    public void redrawTimePercent() {
        invalidate(percentRect.left, percentRect.top, percentRect.right, percentRect.bottom);
    }

    private void drawNewGameButton(Canvas canvas, boolean lightUp) {

        if (lightUp) {
            drawDrawable(canvas,
                    lightUpRectangle,
                    sXNewGame,
                    sYIcons,
                    sXNewGame + iconSize,
                    sYIcons + iconSize
            );
        } else {
            drawDrawable(canvas,
                    iconRectangle,
                    sXNewGame,
                    sYIcons, sXNewGame + iconSize,
                    sYIcons + iconSize
            );
        }

        drawDrawable(canvas,
                getResources().getDrawable(R.drawable.ic_action_refresh),
                sXNewGame + iconPaddingSize,
                sYIcons + iconPaddingSize,
                sXNewGame + iconSize - iconPaddingSize,
                sYIcons + iconSize - iconPaddingSize
        );
    }

    private void drawUndoButton(Canvas canvas) {

        drawDrawable(canvas,
                iconLeftRectangle,
                sXUndo,
                sYIcons, sXUndo + (int) (iconSize * 1.2),
                sYIcons + iconSize
        );

        drawDrawable(canvas,
                getResources().getDrawable(R.drawable.ic_action_undo),
                sXUndo + iconPaddingSize,
                sYIcons + iconPaddingSize,
                sXUndo + iconSize - iconPaddingSize,
                sYIcons + iconSize - iconPaddingSize
        );
    }

    private void drawCheckButton(Canvas canvas) {

        drawDrawable(canvas,
                iconRightRectangle,
                checkRect.right - (int) (iconSize * 1.2),
                checkRect.top, checkRect.right,
                checkRect.bottom
        );

        drawDrawable(canvas,
                getResources().getDrawable(R.drawable.ic_action_check),
                checkRect.right - iconSize + iconPaddingSize,
                sYIcons + iconPaddingSize,
                checkRect.right - iconPaddingSize,
                sYIcons + iconSize - iconPaddingSize
        );


    }




    private void drawCells(Canvas canvas) {
        paint.setTextSize(textSize);
        paint.setTextAlign(Paint.Align.CENTER);
        // Outputting the individual cells
        for (int xx = 0; xx < game.numCellX; xx++) {
            for (int yy = 0; yy < game.numCellY; yy++) {
                int sX = gridRect.left + gridWidth + (cellSize + gridWidth) * xx;
                int eX = sX + cellSize;
                int sY = gridRect.top + gridWidth + (cellSize + gridWidth) * yy;
                int eY = sY + cellSize;

                Tile currentTile = game.grid.getCellContent(xx, yy);
                if (currentTile != null) {
                    //Get and represent the value of the tile
                    int value = currentTile.getValue();
                    int index = value;

                    //Check for any active animations
                    ArrayList<AnimationCell> aArray = game.animationGrid.getAnimationCell(xx, yy);
                    boolean animated = false;
                    for (int i = aArray.size() - 1; i >= 0; i--) {
                        AnimationCell aCell = aArray.get(i);
                        //If this animation is not active, skip it
                        if (aCell.getAnimationType() == AnimationType.SPAWN) {
                            animated = true;
                        }
                        if (!aCell.isActive()) {
                            continue;
                        }
                        double percentDone;
                        float textScaleSize;
                        float cellScaleSize;
                        int sweepAngle;
                        int startAngle;
                        switch (aCell.getAnimationType()){
                            case MOVE:
                              percentDone = aCell.getPercentageDone();
                                int tempIndex = index;
                                if (aArray.size() >= 2) {
                                    tempIndex = tempIndex - 1;
                                }
                                int previousX = aCell.extras[0];
                                int previousY = aCell.extras[1];
                                int currentX = currentTile.getX();
                                int currentY = currentTile.getY();
                                int dX = (int) ((currentX - previousX) * (cellSize + gridWidth) * (percentDone - 1) * 1.0);
                                int dY = (int) ((currentY - previousY) * (cellSize + gridWidth) * (percentDone - 1) * 1.0);
                                bitmapCell[tempIndex].setBounds(sX + dX, sY + dY, eX + dX, eY + dY);
                                bitmapCell[tempIndex].draw(canvas);
                                break;
                            case CHECK:
                                percentDone = aCell.getPercentageDone();
                                if(percentDone < 0.5) {// make it smaller
                                    textScaleSize = (float) (0.5 - percentDone) * 2;
                                    paint.setTextSize(textSize * textScaleSize);

                                    cellScaleSize = - cellSize / 16 * (1 - textScaleSize);
                                    bitmapCell[index].setBounds((int) (sX + cellScaleSize), (int) (sY + cellScaleSize), (int) (eX - cellScaleSize), (int) (eY - cellScaleSize));
                                    bitmapCell[index].draw(canvas);
                                } else { // make it bigger

                                    textScaleSize = (float) (percentDone - 0.5) * 2;
                                    paint.setTextSize(textSize * textScaleSize);

                                    cellScaleSize = - cellSize / 16 * (1 - textScaleSize);
                                    bitmapCell[index].setBounds((int) (sX + cellScaleSize), (int) (sY + cellScaleSize), (int) (eX - cellScaleSize), (int) (eY - cellScaleSize));
                                    bitmapCell[index].draw(canvas);
                                }
                                break;
                            case SPAWN:
                                percentDone = aCell.getPercentageDone();
                                textScaleSize = (float) (percentDone);
                                paint.setTextSize(textSize * textScaleSize);

                                cellScaleSize = cellSize / 2 * (1 - textScaleSize);
                                bitmapCell[index].setBounds((int) (sX + cellScaleSize), (int) (sY + cellScaleSize), (int) (eX - cellScaleSize), (int) (eY - cellScaleSize));
                                bitmapCell[index].draw(canvas);
                                break;
                            case MERGE:
                                percentDone = aCell.getPercentageDone();
                                textScaleSize = (float) (1 + INITIAL_VELOCITY * percentDone
                                        + MERGING_ACCELERATION * percentDone * percentDone / 2);
                                paint.setTextSize(textSize * textScaleSize);

                                cellScaleSize = cellSize / 2 * (1 - textScaleSize);
                                bitmapCell[index].setBounds((int) (sX + cellScaleSize), (int) (sY + cellScaleSize), (int) (eX - cellScaleSize), (int) (eY - cellScaleSize));
                                bitmapCell[index].draw(canvas);
                                break;
                            case CHECK_RIGHT_CIRCLE:
                                bitmapCell[index].setBounds(sX, sY, eX, eY);
                                bitmapCell[index].draw(canvas);
                                paint.setStrokeWidth(cellSize / 10);
                                paint.setStyle(Paint.Style.STROKE);
                                paint.setColor(getResources().getColor(R.color.check_right));
                                startAngle = 90;
                                sweepAngle = 90 + (int) (360 * aCell.getPercentageDone());
                                canvas.drawArc(new RectF(sX - cellSize / 10, sY - cellSize / 10, eX + cellSize / 10, eY + cellSize / 10), startAngle, sweepAngle, false, paint);
                                paint.setStrokeWidth(1);
                                paint.setStyle(Paint.Style.FILL);
                                break;
//                            case CHECK_WRONG_CIRCLE:
//                                bitmapCell[index].setBounds(sX, sY, eX, eY);
//                                bitmapCell[index].draw(canvas);
//                                paint.setStrokeWidth(cellSize / 15);
//                                paint.setStyle(Paint.Style.STROKE);
//                                paint.setColor(getResources().getColor(R.color.check_wrong));
//                                sweepAngle = (int) (360 * (aCell.getPercentageDone()));
//                                startAngle = 270 - sweepAngle / 2;
//                                canvas.drawArc(new RectF(sX - cellSize / 15, sY - cellSize / 15, eX + cellSize / 15, eY + cellSize / 15), startAngle, sweepAngle, false, paint);
//                                break;

                            case FADE_GLOBAL:
                                break;
                        }
                        animated = true;
                    }

                    //No active animations? Just draw the cell
                    if (!animated) {
                        bitmapCell[index].setBounds(sX, sY, eX, eY);
                        bitmapCell[index].draw(canvas);
                    }
                }
            }
        }
    }

    private void drawInstructionState(Canvas canvas) {
        restartButtonEnabled = false;

        switch (game.gameState) {
            case WIN:
                instruction = getResources().getString(R.string.you_win);
                subInstruction = getResources().getString(R.string.go_on);
                paint.setColor(getResources().getColor(R.color.text_downy));
                drawInstructions(canvas);
                drawSubInstructions(canvas);
                restartButtonEnabled = true;
                break;
            case LOST:
                instruction = getResources().getString(R.string.game_over);
                subInstruction = getResources().getString(R.string.go_on);
                paint.setColor(getResources().getColor(R.color.text_softred));
                drawInstructions(canvas);
                drawSubInstructions(canvas);
                break;
            case READY:
                instruction = getResources().getString(R.string.show);
                subInstruction = getResources().getString(R.string.go_on);
                paint.setColor(getResources().getColor(R.color.text_blue));
                drawInstructions(canvas);
                drawSubInstructions(canvas);
                restartButtonEnabled = true;
                break;
            default:
                instruction = getResources().getString(R.string.brain);
                subInstruction = getResources().getString(R.string.use_your);
                paint.setColor(getResources().getColor(R.color.text_cyan));
                drawInstructions(canvas);
                drawSubInstructions(canvas);
                restartButtonEnabled = true;
                break;

        }
    }
    private void drawInstructions(Canvas canvas) {
        paint.setTextSize(instructionTextSize);
        paint.setTextAlign(Paint.Align.CENTER);
        int textShiftY = centerText() * 2;
        canvas.drawText(instruction,
                (gridRect.left + gridRect.right) / 2, sYInstruction - textShiftY + textPaddingSize, paint);
    }

    private void drawSubInstructions(Canvas canvas) {
        paint.setTextSize(subInstructionTextSize);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(getResources().getColor(R.color.text_black));
        int textShiftY = centerText() * 2;
        canvas.drawText(subInstruction,
                (gridRect.left + gridRect.right) / 2, sYSubInstruction - textShiftY + textPaddingSize, paint);
    }



    private void createBackgroundBitmap(int width, int height) {
        background = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(background);
        drawNewGameButton(canvas, false);
        drawUndoButton(canvas);
        drawCheckButton(canvas);

    }

    private void createBitmapCells() {
        Resources resources = getResources();
        int[] cellRectangleIds = getCellRectangleIds();
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(getResources().getColor(R.color.text_white));
        for (int xx = 1; xx < bitmapCell.length; xx++) {
            //int value = (int) Math.pow(2, xx);
            int value = xx;
            paint.setTextSize(cellTextSize);
            float tempTextSize = cellTextSize * cellSize * 0.9f / Math.max(cellSize * 0.9f, paint.measureText(String.valueOf(value)));
            paint.setTextSize(tempTextSize);
            Bitmap bitmap = Bitmap.createBitmap(cellSize, cellSize, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawDrawable(canvas, resources.getDrawable(cellRectangleIds[xx]), 0, 0, cellSize, cellSize);
            drawCellText(canvas, value);
            bitmapCell[xx] = new BitmapDrawable(resources, bitmap);
        }
    }

    private int[] getCellRectangleIds() {
        int[] cellRectangleIds = new int[numCellTypes];
        cellRectangleIds[0] = R.drawable.cell_rectangle;
        cellRectangleIds[1] = R.drawable.cell_rectangle_2;
        cellRectangleIds[2] = R.drawable.cell_rectangle_4;
        cellRectangleIds[3] = R.drawable.cell_rectangle_8;
        cellRectangleIds[4] = R.drawable.cell_rectangle_16;
        cellRectangleIds[5] = R.drawable.cell_rectangle_32;
        cellRectangleIds[6] = R.drawable.cell_rectangle_64;
        cellRectangleIds[7] = R.drawable.cell_rectangle_128;
        cellRectangleIds[8] = R.drawable.cell_rectangle_256;
        cellRectangleIds[9] = R.drawable.cell_rectangle_512;
        cellRectangleIds[10] = R.drawable.cell_rectangle_1024;
        cellRectangleIds[11] = R.drawable.cell_rectangle_2048;
        for (int xx = 12; xx < cellRectangleIds.length; xx++) {
            cellRectangleIds[xx] = R.drawable.cell_rectangle_4096;
        }
        return cellRectangleIds;
    }


    private void update() {
        long currentTime = System.nanoTime();
        game.animationGrid.updateAll(currentTime - lastFPSTime);
        lastFPSTime = currentTime;
    }

    private void makeLayout(int width, int height){
        int staticCellSize = (5 * width / 9) / 4;
        cellSize = (5 * width / 9) / game.numCellY;
        //padding width
        gridWidth = cellSize * 2 / 3;
        int screenMidX = width / 2;
        int screenMidY = height / 2;
        int boardMidY = screenMidY + cellSize/2;
        iconSize = (int) ((5 * width / 9) / 5 * 1.5);

        //Grid dimension
        double halfNumSquaresX = game.numCellX / 2d;
        double halfNumSquaresY = game.numCellY / 2d;

        gridRect.left = (int) (screenMidX - (cellSize + gridWidth) * halfNumSquaresX - gridWidth / 2);
        gridRect.right = (int) (screenMidX + (cellSize + gridWidth) * halfNumSquaresX + gridWidth / 2);

        gridRect.top = (int) (boardMidY - (cellSize + gridWidth) * halfNumSquaresY - gridWidth / 2);
        gridRect.bottom = (int) (boardMidY + (cellSize + gridWidth) * halfNumSquaresY + gridWidth / 2);

        float widthWithPadding = gridRect.right - gridRect.left;

        // Text Dimensions
        paint.setTextSize(cellSize);
        textSize = cellSize * cellSize / Math.max(cellSize, paint.measureText("0000"));

        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(1000);

        instructionTextSize = width / 7.f;
        Log.d("TextSize", String.valueOf(instructionTextSize));
        Log.d("Width", String.valueOf(width));
        subInstructionTextSize = instructionTextSize / 2.f;

        paint.setTextSize(cellSize);
        cellTextSize = textSize * 1.3f;
        textPaddingSize = (int) (subInstructionTextSize / 3);
        iconPaddingSize = iconSize / 4;


        //static variables
        sYInstruction = (int) (gridRect.top - staticCellSize * 1.5);
        sYSubInstruction = (int) (gridRect.top - staticCellSize * 0.5);
        sYScore = 0;

        //sYIcons = gridRect.bottom + cellSize / 2;
        sYIcons = height - iconSize - 10;
        sXNewGame = screenMidX - iconSize / 2;
        sXUndo = 0;

        percentRect.left = gridRect.left;
        percentRect.right = gridRect.right;
        percentRect.top = gridRect.bottom + textPaddingSize;
        percentRect.bottom = percentRect.top + textPaddingSize;

        checkRect.right = width;
        checkRect.left = width - iconSize;
        checkRect.top = sYIcons;
        checkRect.bottom = checkRect.top + iconSize;
        resyncTime();
    }

    private int centerText() {
        return (int) ((paint.descent() + paint.ascent()) / 2);
    }

    public void resyncTime() {
        lastFPSTime = System.nanoTime();
    }

    public int[] clickedCell(int x, int y) {
        // Outputting the game grid
        int[] position = new int[2];
        for (int xx = 0; xx < game.numCellX; xx++) {
            for (int yy = 0; yy < game.numCellY; yy++) {
                int sX = gridRect.left + gridWidth + (cellSize + gridWidth) * xx;
                int eX = sX + cellSize;
                int sY = gridRect.top + gridWidth + (cellSize + gridWidth) * yy;
                int eY = sY + cellSize;
                if (x >= sX && x <= eX && y >= sY && y <= eY){
                    position[0] = xx;
                    position[1] = yy;
                    return position;
                }

            }
        }
        return null;
    }


}
