package cyberse.cloneproject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;

import static android.content.ContentValues.TAG;

/**
 * Created by Cyber on 12/5/2017.
 */

public class MenuView extends View {
    private static final long SWIPE_ANIMATION_TIME = 100;
    //Paint to draw text
    private final Paint paint = new Paint();
    //Item position
    public Rect imgDisplayRect = new Rect();
    public Rect playButtonRect = new Rect();
    private Context mContext;
    private final MenuActivity mActivity;
    public int iconSize;
    private int iconPadding;
    public int sXLeftArrow;
    public int sXRightArrow;
    public int sYArrow;
    // Background
    private Bitmap background;
    private Drawable playIcon;
    private Drawable leftArrow;
    private Drawable rightArrow;
    // Grid size string
    private final int NUM_GRID_TYPE = 4;
    private String[] gridSizeText = new String[NUM_GRID_TYPE];
    private Drawable[] gridSizeBitmap = new Drawable[NUM_GRID_TYPE];
    public int gridIndex = 0;
    // Animation thing
    private AnimationType swipeAnimationType = AnimationType.NONE;
    private long elapseTime;
    private long animationTime = SWIPE_ANIMATION_TIME;
    // Time
    private long lastFPSTime;


    public MenuView(Context context, MenuActivity activity) {
        super(context);

        mActivity = activity;
        mContext = context;

        try {
            //Getting assets
            playIcon = getResources().getDrawable(R.drawable.ic_play_button);
            leftArrow = getResources().getDrawable(R.drawable.ic_left_arrow);
            rightArrow = getResources().getDrawable(R.drawable.ic_right_arrow);
            gridSizeText[0] = getResources().getString(R.string._3x3);
            gridSizeText[1] = getResources().getString(R.string._4x4);
            gridSizeText[2] = getResources().getString(R.string._5x5);
            gridSizeText[3] = getResources().getString(R.string._6x6);
            gridSizeBitmap[0] = getResources().getDrawable(R.drawable.grid_33);
            gridSizeBitmap[1] = getResources().getDrawable(R.drawable.grid_44);
            gridSizeBitmap[2] = getResources().getDrawable(R.drawable.grid_55);
            gridSizeBitmap[3] = getResources().getDrawable(R.drawable.grid_66);
            this.setBackgroundColor(getResources().getColor(R.color.background));
            Typeface font = Typeface.createFromAsset(getResources().getAssets(), "fonts/ClearSans-Bold.ttf");
            paint.setTypeface(font);
            paint.setAntiAlias(true);

        } catch (Exception e) {
            Log.e(TAG, "Error getting assets?", e);
        }

        setOnTouchListener(new MenuListener(this, context, mActivity));
    }

    public void onDraw(Canvas canvas) {
        //Reset the transparency of the screen
        canvas.drawBitmap(background, 0, 0, paint);
;
        //Calculator animation time
        if(elapseTime < animationTime){
            update();
            invalidate();
        }else{
            swipeAnimationType = AnimationType.NONE;
        }
        drawLevel(canvas);
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldW, int oldH) {
        super.onSizeChanged(width, height, oldW, oldH);
        makeLayout(width, height);
        createBackgroundBitmap(width, height);

    }
    private void drawLevel(Canvas canvas){

        double percentDone = Math.max(0, 1.0 * elapseTime / animationTime);
        int dX;
        int prev =  (NUM_GRID_TYPE + gridIndex - 1) % NUM_GRID_TYPE;
        int next = (gridIndex + 1) % NUM_GRID_TYPE;
        paint.setColor(getResources().getColor(R.color.text_black));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(iconSize);
        switch(swipeAnimationType)
        {
            case SWIPE_LEFT:
                dX = (int) ((getWidth() / 2 - iconSize * 2 - sXLeftArrow) * (percentDone) * 1.0);

                paint.setAlpha((int)((1-percentDone) * 255));
                canvas.drawText(gridSizeText[prev], getWidth() / 2, sYArrow + iconSize - 10, paint);

                paint.setAlpha(255);
                canvas.drawText(gridSizeText[gridIndex], sXRightArrow - iconSize - dX, sYArrow + iconSize - 10, paint);

                //avoid over 255
                gridSizeBitmap[gridIndex].setAlpha((int)(((percentDone) * 255) >= 255 ? 255 : (percentDone) * 255));
                drawDrawable(canvas, gridSizeBitmap[gridIndex], imgDisplayRect);
                break;
            case SWIPE_RIGHT:
                dX = (int) ((getWidth() / 2 - iconSize * 2 - sXLeftArrow) * (percentDone) * 1.0);

                paint.setAlpha((int)((1-percentDone) * 255));
                canvas.drawText(gridSizeText[next], getWidth() / 2, sYArrow + iconSize - 10, paint);

                paint.setAlpha(255);
                canvas.drawText(gridSizeText[gridIndex], sXLeftArrow + iconSize * 2 + dX, sYArrow + iconSize - 10, paint);

                //avoid over 255
                gridSizeBitmap[gridIndex].setAlpha((int)(((percentDone) * 255) >= 255 ? 255 : (percentDone) * 255));
                drawDrawable(canvas, gridSizeBitmap[gridIndex], imgDisplayRect);
                break;
            default:

                canvas.drawText(gridSizeText[gridIndex], getWidth() / 2, sYArrow + iconSize - 10, paint);
                gridSizeBitmap[gridIndex].setAlpha(255);
                drawDrawable(canvas, gridSizeBitmap[gridIndex], imgDisplayRect);
                //draw image
                break;

        }

    }
    private void drawDrawable(Canvas canvas, Drawable draw, int left, int top, int right, int bottom) {
        //Draw to canvas with bound
        draw.setBounds(left, top, right, bottom);
        draw.draw(canvas);
    }

    private void drawDrawable(Canvas canvas, Drawable draw, Rect rect) {
        //Draw to canvas with bound
        draw.setBounds(rect.left, rect.top, rect.right, rect.bottom);
        draw.draw(canvas);
    }

    private void createBackgroundBitmap(int width, int height) {
        background = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(background);
        //draw back color
        paint.setColor(getResources().getColor(R.color.background));
        paint.setStyle(Paint.Style.FILL);

//        canvas.drawPaint(paint);
        //Draw play, sound and share icon
        drawDrawable(canvas, playIcon, playButtonRect);
        drawDrawable(canvas, leftArrow, sXLeftArrow, sYArrow, sXLeftArrow + iconSize, sYArrow + iconSize);
        drawDrawable(canvas, rightArrow, sXRightArrow, sYArrow, sXRightArrow + iconSize, sYArrow + iconSize);
    }


    public void makeLayout(int width, int height) {
        int screenMidX = width / 2;
        int screenMidY = height / 2;
        iconSize = width / 10;
        iconPadding = iconSize * 2 / 3;
        playButtonRect.left = (int) (screenMidX - iconSize * 1.5);
        playButtonRect.right = (int) (screenMidX + iconSize * 1.5);

        int playButtonHeight = iconSize * playIcon.getIntrinsicHeight() / playIcon.getIntrinsicWidth();
        playButtonRect.top = (int) (height * 3 / 4 - playButtonHeight * 1.5);
        playButtonRect.bottom = (int) (height * 3 / 4 + playButtonHeight * 1.5);

        sYArrow = height / 2;
        sXLeftArrow = width / 8;
        sXRightArrow = width * 7 / 8 - iconSize;
        imgDisplayRect.left = width / 6;
        imgDisplayRect.right = width * 5 / 6;
        imgDisplayRect.bottom = height / 2 - iconPadding;
        imgDisplayRect.top = imgDisplayRect.bottom - width * 2 / 3;
        resyncTime();
    }

    public void resyncTime() {
        lastFPSTime = System.currentTimeMillis();
    }

    private void update() {
        long currentTime = System.currentTimeMillis();
        elapseTime += currentTime - lastFPSTime;
        lastFPSTime = currentTime;
    }

    public void swipeLeft(){
        swipeAnimationType = AnimationType.SWIPE_LEFT;
        elapseTime = 0;
        animationTime = SWIPE_ANIMATION_TIME;
        gridIndex = (gridIndex + 1) % NUM_GRID_TYPE;
        //play sound
        SoundPoolManager.getInstance().playSound(R.raw.flip);
        resyncTime();
        invalidate();
    }

    public void swipeRight(){
        swipeAnimationType = AnimationType.SWIPE_RIGHT;
        elapseTime = 0;
        animationTime = SWIPE_ANIMATION_TIME;
        gridIndex = (NUM_GRID_TYPE + gridIndex - 1) % NUM_GRID_TYPE;
        //play sound
        SoundPoolManager.getInstance().playSound(R.raw.flip);
        resyncTime();
        invalidate();
    }
}
