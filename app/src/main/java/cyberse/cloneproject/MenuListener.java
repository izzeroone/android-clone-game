package cyberse.cloneproject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Cyber on 12/6/2017.
 */

public class MenuListener implements View.OnTouchListener {
    private static final int SWIPE_MIN_DISTANCE = 0;
    private static final int SWIPE_THRESHOLD_VELOCITY = 10;
    private static final int MOVE_THRESHOLD = 100;
    private static final int RESET_STARTING = 10;
    private final MenuView mView;
    private final Context mContext;
    private float x;
    private float y;
    private float previousX;
    private float previousY;
    private float startingX;
    private float startingY;
    private boolean hasMove;
    private boolean beganOnIcon = false;
    private boolean beganOnGrid = false;

    public MenuListener(MenuView view, Context context) {
        super();
        this.mView = view;
        this.mContext = context;
    }

    public boolean onTouch(View view, MotionEvent event) {
        boolean moved = false;
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                x = event.getX();
                y = event.getY();
                startingX = x;
                startingY = y;
                previousX = x;
                previousY = y;
                hasMove = false;
                beganOnIcon = rectanglePressed(mView.playButtonRect)
                                || iconPressed(mView.sXLeftArrow, mView.sYArrow)
                                || iconPressed(mView.sXRightArrow, mView.sYArrow);
                beganOnGrid = rectanglePressed(mView.imgDisplayRect);

                return true;
            case MotionEvent.ACTION_UP:
                x = event.getX();
                y = event.getY();
                if (beganOnIcon) {
                    if (rectanglePressed(mView.playButtonRect)) {
                        if(mContext instanceof MenuActivity){
                            ((MenuActivity)mContext).startGame();
                        }
                    }
                    if(iconPressed(mView.sXLeftArrow, mView.sYArrow)){
                        mView.swipeLeft();
                    }
                    if(iconPressed(mView.sXRightArrow, mView.sYArrow)){
                        mView.swipeRight();
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                x = event.getX();
                y = event.getY();
                if (true) {
                    float dx = x - previousX;
                    float dy = y - previousY;
                    if (pathMoved() > SWIPE_MIN_DISTANCE * SWIPE_MIN_DISTANCE && !hasMove) {
                        //Horizontal
                        if (((dx >= SWIPE_THRESHOLD_VELOCITY && Math.abs(dx) >= Math.abs(dy)) || x - startingX >= MOVE_THRESHOLD)) {
                            moved = true;
                            mView.swipeRight();
                        } else if (((dx <= -SWIPE_THRESHOLD_VELOCITY && Math.abs(dx) >= Math.abs(dy)) || x - startingX <= -MOVE_THRESHOLD)) {
                            moved = true;
                            mView.swipeLeft();
                        }
                        if (moved) {
                            hasMove = true;
                            startingX = x;
                            startingY = y;
                        }

                    }
                }
                previousX = x;
                previousY = y;
                return true;



        }
        return true;
    }

    private boolean rectanglePressed(Rect rect) {
        return inRange(rect.left, x, rect.right) && inRange(rect.top, y, rect.bottom);
    }

    private boolean inRange(float starting, float check, float ending) {
        return (starting <= check && check <= ending);
    }

    private boolean iconPressed(int sx, int sy) {
        return inRange(sx, x, sx + mView.iconSize)
                && inRange(sy, y, sy + mView.iconSize);
    }

    private float pathMoved() {
        return (x - startingX) * (x - startingX) + (y - startingY) * (y - startingY);
    }

}
