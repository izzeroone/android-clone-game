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
    //Paint to draw text
    private final Paint paint = new Paint();
    public Rect playButtonRect = new Rect();
    private Context mContext;
    private int iconSize;
    private int iconPadding;
    private int sXLeaderBoard;
    private int sXShare;
    private int sXSound;
    // All icon start on the same line
    private int sYIcon;
    // Background
    private Drawable backgroundRectangle;
    private Bitmap background;
    private Drawable backgroundPic;
    private Drawable shareIcon;
    private Drawable playIcon;
    private Drawable soundIcon;


    public MenuView(Context context) {
        super(context);
        mContext = context;

        try {
            //Getting assets
            backgroundRectangle = getResources().getDrawable(R.drawable.background_rectangle);
            backgroundPic = getResources().getDrawable(R.drawable.back_ground_pic);
            shareIcon = getResources().getDrawable(R.drawable.share);
            playIcon = getResources().getDrawable(R.drawable.play_button);
            soundIcon = getResources().getDrawable(R.drawable.mute);
            Typeface font = Typeface.createFromAsset(getResources().getAssets(), "fonts/ClearSans-Bold.ttf");
            paint.setTypeface(font);
            paint.setAntiAlias(true);

        } catch (Exception e) {
            Log.e(TAG, "Error getting assets?", e);
        }
        setOnTouchListener(new MenuListener(this, context));
    }

    public void onDraw(Canvas canvas) {
        //Reset the transparency of the screen
        canvas.drawBitmap(background, 0, 0, paint);

    }

    @Override
    protected void onSizeChanged(int width, int height, int oldW, int oldH) {
        super.onSizeChanged(width, height, oldW, oldH);
        makeLayout(width, height);
        createBackgroundBitmap(width, height);

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
        drawDrawable(canvas, backgroundPic, 0, 0, width, height);
        //Draw play, sound and share icon
        drawDrawable(canvas, playIcon, playButtonRect);
        drawDrawable(canvas, shareIcon, sXShare, sYIcon, sXShare + iconSize, sYIcon + iconSize);
        drawDrawable(canvas, soundIcon, sXSound, sYIcon, sXSound + iconSize, sYIcon + iconSize);

    }


    public void makeLayout(int width, int height) {
        int screenMidX = width / 2;
        int screenMidY = height / 2;
        iconSize = width / 10;
        iconPadding = iconSize * 2 / 3;
        playButtonRect.left = (int) (screenMidX - iconSize * 1.2);
        playButtonRect.right = (int) (screenMidX + iconSize * 1.2);

        int playButtonHeight = iconSize * playIcon.getIntrinsicHeight() / playIcon.getIntrinsicWidth();
        playButtonRect.top = (int) (screenMidY - playButtonHeight * 1.2);
        playButtonRect.bottom = (int) (screenMidY + playButtonHeight * 1.2);

        sYIcon = screenMidY + iconSize * 4 + iconPadding;
        sXShare = screenMidX - iconSize / 2;
        sXLeaderBoard = sXShare - iconPadding - iconSize;
        sXSound = sXShare + iconSize + iconPadding;
    }
}
