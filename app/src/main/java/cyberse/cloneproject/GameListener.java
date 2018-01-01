package cyberse.cloneproject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.MotionEvent;
import android.view.View;


public class GameListener implements View.OnTouchListener {
    private static final int SWIPE_MIN_DISTANCE = 0;
    private static final int SWIPE_THRESHOLD_VELOCITY = 10;
    private static final int MOVE_THRESHOLD = 100;
    private static final int RESET_STARTING = 10;
    private static final long LONG_PRESS_TIME = 300; // 1,5s
    private final GameView mView;
    private float x;
    private float y;
    private float lastDx;
    private float lastDy;
    private float previousX;
    private float previousY;
    private float startingX;
    private float startingY;
    private int previousDirection = 1;
    private int veryLastDirection = 1;
    // Whether or not we have made a move, i.e. the blocks shifted or tried to shift.
    private boolean hasMoved = false;
    // Whether or not we began the press on an icon. This is to disable swipes if the user began
    // the press on an icon.
    private boolean beganOnIcon = false;
    // Timer whether user long press or not
    private boolean beganOnGrid = false;
    private long timer;

    public GameListener(GameView view) {
        super();
        this.mView = view;
    }

    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                x = event.getX();
                y = event.getY();
                startingX = x;
                startingY = y;
                previousX = x;
                previousY = y;
                lastDx = 0;
                lastDy = 0;
                hasMoved = false;
                beganOnIcon = iconPressed(mView.sXNewGame, mView.sYIcons)
                        || iconPressed(mView.sXUndo, mView.sYIcons)
                        || rectanglePressed(mView.checkRect);
                beganOnGrid = rectanglePressed(mView.gridRect);
                if (beganOnGrid) {
                    timer = System.currentTimeMillis();
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                x = event.getX();
                y = event.getY();
                if (mView.game.isActive() && !beganOnIcon) {
                    float dx = x - previousX;
                    if (Math.abs(lastDx + dx) < Math.abs(lastDx) + Math.abs(dx) && Math.abs(dx) > RESET_STARTING
                            && Math.abs(x - startingX) > SWIPE_MIN_DISTANCE) {
                        startingX = x;
                        startingY = y;
                        lastDx = dx;
                        previousDirection = veryLastDirection;
                    }
                    if (lastDx == 0) {
                        lastDx = dx;
                    }
                    float dy = y - previousY;
                    if (Math.abs(lastDy + dy) < Math.abs(lastDy) + Math.abs(dy) && Math.abs(dy) > RESET_STARTING
                            && Math.abs(y - startingY) > SWIPE_MIN_DISTANCE) {
                        startingX = x;
                        startingY = y;
                        lastDy = dy;
                        previousDirection = veryLastDirection;
                    }
                    if (lastDy == 0) {
                        lastDy = dy;
                    }
                    int position[] = mView.clickedCell((int)previousX, (int)previousY);
                    if (pathMoved() > SWIPE_MIN_DISTANCE * SWIPE_MIN_DISTANCE && !hasMoved && position != null) {
                        int xx = position[0];
                        int yy = position[1];
                        boolean moved = false;
                        //Vertical
                        if (((dy >= SWIPE_THRESHOLD_VELOCITY && Math.abs(dy) >= Math.abs(dx)) || y - startingY >= MOVE_THRESHOLD) && previousDirection % 2 != 0) {
                            moved = true;
                            previousDirection = previousDirection * 2;
                            veryLastDirection = 2;
                            mView.game.move(xx, yy, 2);
                        } else if (((dy <= -SWIPE_THRESHOLD_VELOCITY && Math.abs(dy) >= Math.abs(dx)) || y - startingY <= -MOVE_THRESHOLD) && previousDirection % 3 != 0) {
                            moved = true;
                            previousDirection = previousDirection * 3;
                            veryLastDirection = 3;
                            mView.game.move(xx, yy, 0);
                        }
                        //Horizontal
                        if (((dx >= SWIPE_THRESHOLD_VELOCITY && Math.abs(dx) >= Math.abs(dy)) || x - startingX >= MOVE_THRESHOLD) && previousDirection % 5 != 0) {
                            moved = true;
                            previousDirection = previousDirection * 5;
                            veryLastDirection = 5;
                            mView.game.move(xx, yy, 1);
                        } else if (((dx <= -SWIPE_THRESHOLD_VELOCITY && Math.abs(dx) >= Math.abs(dy)) || x - startingX <= -MOVE_THRESHOLD) && previousDirection % 7 != 0) {
                            moved = true;
                            previousDirection = previousDirection * 7;
                            veryLastDirection = 7;
                            mView.game.move(xx, yy, 3);
                        }
                        if (moved) {
                            hasMoved = true;
                            startingX = x;
                            startingY = y;
                        }
                    }
                }
                previousX = x;
                previousY = y;
                return true;
            case MotionEvent.ACTION_UP:
                x = event.getX();
                y = event.getY();
                previousDirection = 1;
                veryLastDirection = 1;
                //Double tap check
                if (beganOnGrid) {
                    if (System.currentTimeMillis() - timer >= LONG_PRESS_TIME) {
                        mView.game.checkWinState();
                    }
                }
                //"Menu" inputs
                if (!hasMoved) {
                    if (iconPressed(mView.sXNewGame, mView.sYIcons)) {
                        if (!(mView.game.gameState == GameState.LOST)) {
                            new AlertDialog.Builder(mView.getContext())
                                    .setPositiveButton(R.string.reset, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            mView.game.newGame();
                                            SoundPoolManager.getInstance().playSound(R.raw.restart);
                                        }
                                    })
                                    .setNegativeButton(R.string.continue_game, null)
                                    .setTitle(R.string.reset_dialog_title)
                                    .setMessage(R.string.reset_dialog_message)
                                    .show();
                        } else {
                            mView.game.newGame();
                        }

                    } else if (iconPressed(mView.sXUndo, mView.sYIcons)) {
                        mView.game.revertUndoState();
                    } else if (isTap(2) && rectanglePressed(mView.checkRect)) {
                        mView.game.checkWinState();
                    } else if (isTap(2) && inRange(mView.gridRect.left, x, mView.gridRect.right)
                            && inRange(mView.gridRect.top, x, mView.gridRect.bottom)) {
                      switch (mView.game.gameState){
                          case READY:
                              mView.game.gameStart();
                              break;
                          case WIN: case LOST:
                              mView.game.newGame();
                              break;
                      }
                    } else if (iconPressed(mView.sXHome, mView.sYHome)) {
                        mView.game.finish();
                    }
                }
        }
        return true;
    }

    private float pathMoved() {
        return (x - startingX) * (x - startingX) + (y - startingY) * (y - startingY);
    }

    private boolean iconPressed(int sx, int sy) {
        return isTap(1) && inRange(sx, x, sx + mView.iconSize)
                && inRange(sy, y, sy + mView.iconSize);
    }

    private boolean rectanglePressed(int sx, int sy, int ex, int ey)
    {
        return isTap(1) && inRange(sx, x, ex) && inRange(sy, y, ey);
    }

    private boolean rectanglePressed(Rectangle rect) {
        return isTap(1) && inRange(rect.left, x, rect.right) && inRange(rect.top, y, rect.bottom);
    }
    private boolean inRange(float starting, float check, float ending) {
        return (starting <= check && check <= ending);
    }

    private boolean isTap(int factor) {
        return pathMoved() <= mView.iconSize * mView.iconSize * factor;
    }


}
