package cyberse.cloneproject;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;

public class MainActivity extends AppCompatActivity {

    //debug string
    private static final String WIDTH = "width";
    private static final String HEIGHT = "height";
    private static final String HIGH_SCORE = "high score";
    private static final String UNDO_SCORE = "undo score";
    private static final String GAME_STATE = "game state";
    private static final String UNDO_GAME_STATE = "undo game state";
    public static Thread timerThread;
    public static TimerUpdateRunnable timerRunnable;
    private MainView view;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       //Create a view
        view = new MainView(this);

        //Load prefencense
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);

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
                                // update TextView here!
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };


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
