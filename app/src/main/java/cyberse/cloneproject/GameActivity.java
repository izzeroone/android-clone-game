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


        MediaPlayerManager.getInstance().play(this, R.raw.music1);
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

    @Override
    public void onBackPressed() {
        view.game.revertUndoState();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();
        MediaPlayerManager.getInstance().play(this, R.raw.music1);
    }


    @Override
    protected void onPause(){
        MediaPlayerManager.getInstance().stop();
        super.onPause();
    }


}
