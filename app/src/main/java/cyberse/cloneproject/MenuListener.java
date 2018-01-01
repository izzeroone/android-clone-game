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
    private static final int RESET_STARTING = 10;
    private final MenuView mView;
    private final Context mContext;
    private final MenuActivity mActivity;
    private float x;
    private float y;
    private boolean beganOnIcon = false;

    public MenuListener(MenuView view, Context context, MenuActivity activity) {
        super();
        this.mView = view;
        this.mContext = context;
        this.mActivity = activity;
    }

    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                x = event.getX();
                y = event.getY();
                beganOnIcon = rectanglePressed(mView.playButtonRect)
                                || iconPressed(mView.sXLeftArrow, mView.sYArrow)
                                || iconPressed(mView.sXRightArrow, mView.sYArrow);

                return true;
            case MotionEvent.ACTION_UP:
                x = event.getX();
                y = event.getY();
                if (beganOnIcon) {
                    if (rectanglePressed(mView.playButtonRect)) {
                        mActivity.startGame();
                    }
                    if(iconPressed(mView.sXLeftArrow, mView.sYArrow)){
                        mView.swipeLeft();
                    }
                    if(iconPressed(mView.sXRightArrow, mView.sYArrow)){
                        mView.swipeRight();
                    }
                }



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

}
