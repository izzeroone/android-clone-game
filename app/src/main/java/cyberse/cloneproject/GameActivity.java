package cyberse.cloneproject;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;

public class GameActivity extends AppCompatActivity {

    //debug string
    private static final String WIDTH = "width";
    private static final String HEIGHT = "height";
    private static final String HIGH_SCORE = "high score";
    private static final String UNDO_SCORE = "undo score";
    private static final String GAME_STATE = "game state";
    private static final String UNDO_GAME_STATE = "undo game state";
    public static Thread timerThread;
    public static TimerUpdateRunnable timerRunnable;
    private GameView view;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //load extra intent
        Intent iin= getIntent();
        Bundle b = iin.getExtras();

        int numCellX, numCellY, time;
        if(b!=null)
        {
            numCellX =(int) b.get("numCellX");
            numCellY =(int) b.get("numCellY");
            time =(int) b.get("time");
        }
        else {
            numCellX = 3;
            numCellY = 3;
            time = 60000;
        }
       //Create a view
        view = new GameView(this);
        view.game.setSize(numCellX, numCellY, time);
        view.game.newGame();


        //check if have state and load

        //create runnable
        timerRunnable = new TimerUpdateRunnable(view.game);
        //create timer thread to update timer
        timerThread = new Thread(timerRunnable);
        //create thread to update the view
        Thread processUIThread = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(100);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                view.redrawTimePercent();
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };
        MediaPlayerManager.getInstance().play(this, R.raw.music1);
        timerThread.start();
        processUIThread.start();

        //set content view
        setContentView(view);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            //Do nothing
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            view.game.move(2);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            view.game.move(0);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            view.game.move(3);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            view.game.move(1);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


}
