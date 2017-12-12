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
public class MainView extends View {

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
    public boolean hasSavestate = false;
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
    private float titleTextSize;
    private float bodyTextSize;
    private float headerTextSize;
    private float overlayTextSize;
    //Asset
    private Drawable backgroundRectangle;
    private Drawable lightUpRectangle;
    private Drawable fadeRectangle;
    private Drawable overlayRectangle;
    private Drawable iconLeftRectangle;
    private Drawable iconRightRectangle;
    private Drawable iconRectangle;
    private Bitmap background = null;
    private BitmapDrawable loseGameOverlay;
    private BitmapDrawable winGameOverlay;
    private BitmapDrawable startGameOverlay;
    //text position
    private int sYAll;
    private int titleStartYAll;
    private int bodyStartYAll;
    private int eYAll;
    private int titleWidthScore;
    //Timing
    private long lastFPSTime = System.nanoTime();

    public MainView(Context context) {
        super(context);

        Resources resources = context.getResources();
        //Loading resources
        game = new MainGame(context, this);
        try {
            //Getting assets
            backgroundRectangle = resources.getDrawable(R.drawable.background_rectangle);
            lightUpRectangle = resources.getDrawable(R.drawable.light_up_rectangle);
            fadeRectangle = resources.getDrawable(R.drawable.fade_rectangle);
            overlayRectangle = resources.getDrawable(R.drawable.overlay_rectangle);
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
        setOnTouchListener(new InputListener(this));
        game.newGame();
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

        if (!game.isActive()) {
            drawEndGameState(canvas);
        }


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
        createOverlays();
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
        paint.setTextSize(bodyTextSize);
        paint.setTextAlign(Paint.Align.CENTER);

        int bodyWidthScore = (int) (paint.measureText("" + game.score));

        int textWidthScore = Math.max(titleWidthScore, bodyWidthScore) + textPaddingSize * 2;

        int textMiddleScore = textWidthScore / 2;


        int eXScore = gridRect.right;
        int sXScore = eXScore - textWidthScore;



        //Outputting scores box
        backgroundRectangle.setBounds(sXScore, sYAll, eXScore, eYAll);
        backgroundRectangle.draw(canvas);
        paint.setTextSize(titleTextSize);
        paint.setColor(getResources().getColor(R.color.text_brown));
        canvas.drawText(getResources().getString(R.string.score), sXScore + textMiddleScore, titleStartYAll, paint);
        paint.setTextSize(bodyTextSize);
        paint.setColor(getResources().getColor(R.color.text_white));
        canvas.drawText(String.valueOf(game.score), sXScore + textMiddleScore, bodyStartYAll, paint);
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


    private void drawHeader(Canvas canvas) {
        paint.setTextSize(headerTextSize);
        paint.setColor(getResources().getColor(R.color.text_black));
        paint.setTextAlign(Paint.Align.LEFT);
        int textShiftY = centerText() * 2;
        int headerStartY = sYAll - textShiftY;
        canvas.drawText(getResources().getString(R.string.header), gridRect.left, headerStartY, paint);
    }


    private void drawBackground(Canvas canvas) {
        drawDrawable(canvas, backgroundRectangle, gridRect.left, gridRect.top, gridRect.right, gridRect.bottom);
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
//                                sweepAngle = (int) (360 * (aCell.getPercentageDone()));
//                                startAngle = 270 - sweepAngle / 2;
                                startAngle = 90;
                                sweepAngle = 90 + (int) (360 * aCell.getPercentageDone());
                                canvas.drawArc(new RectF(sX - cellSize / 10, sY - cellSize / 10, eX + cellSize / 10, eY + cellSize / 10), startAngle, sweepAngle, false, paint);
                                break;
                            case CHECK_WRONG_CIRCLE:
                                bitmapCell[index].setBounds(sX, sY, eX, eY);
                                bitmapCell[index].draw(canvas);
                                paint.setStrokeWidth(cellSize / 15);
                                paint.setStyle(Paint.Style.STROKE);
                                paint.setColor(getResources().getColor(R.color.check_wrong));
                                sweepAngle = (int) (360 * (aCell.getPercentageDone()));
                                startAngle = 270 - sweepAngle / 2;
                                canvas.drawArc(new RectF(sX - cellSize / 15, sY - cellSize / 15, eX + cellSize / 15, eY + cellSize / 15), startAngle, sweepAngle, false, paint);
                                break;

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

    private void drawEndGameState(Canvas canvas) {
        double alphaChange = 1;
        restartButtonEnabled = false;
        for (AnimationCell animation : game.animationGrid.globalAnimation) {
            if (animation.getAnimationType() == AnimationType.FADE_GLOBAL) {
                alphaChange = animation.getPercentageDone();
            }
        }
        BitmapDrawable displayOverlay = null;

        switch (game.gameState) {
            case WIN:
                displayOverlay = winGameOverlay;
                restartButtonEnabled = true;
                break;
            case LOST:
                displayOverlay = loseGameOverlay;
                break;
            case READY:
                displayOverlay = startGameOverlay;
                restartButtonEnabled = true;
                break;
        }

        if (displayOverlay != null) {
            displayOverlay.setBounds(gridRect.left, gridRect.top, gridRect.right, gridRect.bottom);
            displayOverlay.setAlpha((int) (255 * alphaChange));
            displayOverlay.draw(canvas);
        }
    }


    private void createEndGameStates(Canvas canvas, boolean win, boolean ready) {
        int width = gridRect.right - gridRect.left;
        int length = gridRect.bottom - gridRect.top;
        int middleX = width / 2;
        int middleY = length / 2;
        if (ready){
            overlayRectangle.setAlpha(70);
            drawDrawable(canvas, overlayRectangle, 0, 0, width, length);
            overlayRectangle.setAlpha(255);
            paint.setColor(getResources().getColor(R.color.text_white));
            paint.setAlpha(255);
            paint.setTextSize(overlayTextSize);
            paint.setTextAlign(Paint.Align.CENTER);
            int textBottom = middleY - centerText();
            canvas.drawText(getResources().getString(R.string.show), middleX, textBottom, paint);
            paint.setTextSize(bodyTextSize);
            String text = getResources().getString(R.string.go_on);
            canvas.drawText(text, middleX, textBottom + textPaddingSize * 2 - centerText() * 2, paint);
        }
        else if (win) {
            lightUpRectangle.setAlpha(127);
            drawDrawable(canvas, lightUpRectangle, 0, 0, width, length);
            lightUpRectangle.setAlpha(255);
            paint.setColor(getResources().getColor(R.color.text_white));
            paint.setAlpha(255);
            paint.setTextSize(overlayTextSize);
            paint.setTextAlign(Paint.Align.CENTER);
            int textBottom = middleY - centerText();
            canvas.drawText(getResources().getString(R.string.you_win), middleX, textBottom, paint);
            paint.setTextSize(bodyTextSize);
            String text = getResources().getString(R.string.go_on);
            canvas.drawText(text, middleX, textBottom + textPaddingSize * 2 - centerText() * 2, paint);
        } else {
            fadeRectangle.setAlpha(127);
            drawDrawable(canvas, fadeRectangle, 0, 0, width, length);
            fadeRectangle.setAlpha(255);
            paint.setColor(getResources().getColor(R.color.text_black));
            paint.setAlpha(255);
            paint.setTextSize(overlayTextSize);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(getResources().getString(R.string.game_over), middleX, middleY - centerText(), paint);
        }
    }

    private void createBackgroundBitmap(int width, int height) {
        background = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(background);
        drawHeader(canvas);
        drawNewGameButton(canvas, false);
        drawUndoButton(canvas);
        //drawBackground(canvas);
        drawCheckButton(canvas);

        //drawReadyButton(canvas);

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

    private void createOverlays() {
        Resources resources = getResources();
        //Initialize overlays
        Bitmap bitmap = Bitmap.createBitmap(gridRect.right - gridRect.left, gridRect.bottom - gridRect.top, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        createEndGameStates(canvas, false, true);
        startGameOverlay = new BitmapDrawable(resources, bitmap);

        bitmap = Bitmap.createBitmap(gridRect.right - gridRect.left, gridRect.bottom - gridRect.top, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        createEndGameStates(canvas, true, false);
        winGameOverlay = new BitmapDrawable(resources, bitmap);

        bitmap = Bitmap.createBitmap(gridRect.right - gridRect.left, gridRect.bottom - gridRect.top, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        createEndGameStates(canvas, false, false);
        loseGameOverlay = new BitmapDrawable(resources, bitmap);
    }

    private void update() {
        long currentTime = System.nanoTime();
        game.animationGrid.updateAll(currentTime - lastFPSTime);
        lastFPSTime = currentTime;
    }

    private void makeLayout(int width, int height){
        cellSize = (5 * width / 9) / game.numCellY;
        //padding width
        gridWidth = cellSize * 2 / 3;
        int screenMidX = width / 2;
        int screenMidY = height / 2;
        int boardMidY = screenMidY + cellSize/2;
        iconSize = (int) (cellSize * 1.5);

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
        overlayTextSize = Math.min(
                Math.min(
                        1000f * ((widthWithPadding - gridWidth * 2) / (paint.measureText(getResources().getString(R.string.game_over)))),
                        textSize * 2
                ),
                1000f * ((widthWithPadding - gridWidth * 2) / (paint.measureText(getResources().getString(R.string.you_win))))
        );

        paint.setTextSize(cellSize);
        cellTextSize = textSize * 1.3f;
        titleTextSize = textSize / 3;
        bodyTextSize = (int) (textSize / 1.5);
        headerTextSize = textSize * 2;
        textPaddingSize = (int) (textSize / 3);
        iconPaddingSize = iconSize / 4;

        paint.setTextSize(titleTextSize);

        int textShiftYAll = centerText();
        //static variables
        sYAll = (int) (gridRect.top - cellSize * 1.5);
        titleStartYAll = (int) (sYAll + textPaddingSize + titleTextSize / 2 - textShiftYAll);
        bodyStartYAll = (int) (titleStartYAll + textPaddingSize + titleTextSize / 2 + bodyTextSize / 2);

        titleWidthScore = (int) (paint.measureText(getResources().getString(R.string.score)));
        paint.setTextSize(bodyTextSize);
        textShiftYAll = centerText();
        eYAll = (int) (bodyStartYAll + textShiftYAll + bodyTextSize / 2 + textPaddingSize);

        sYIcons = gridRect.bottom + cellSize / 2;
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
