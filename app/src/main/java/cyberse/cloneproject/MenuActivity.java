package cyberse.cloneproject;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageButton;

/**
 * Created by Cyber on 12/6/2017.
 */

public class MenuActivity extends AppCompatActivity {
    private MenuView mView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //init Soundpool
        SoundPoolManager.initialize(this);
        //play menu sound
        MediaPlayerManager.getInstance().play(this, R.raw.menu);
        mView = new MenuView(this);
        setContentView(mView);


    }

    public void startGame(){
        Intent menuIntent = new Intent(this, GameActivity.class);
        int numCellX, numCellY, time;
        switch (mView.gridIndex){
            case 0:
                numCellX = 3;
                numCellY = 3;
                time = 20000;
                break;
            case 1:
                numCellX = 4;
                numCellY = 4;
                time = 40000;
                break;
            case 2:
                numCellX = 5;
                numCellY = 5;
                time = 80000;
                break;
            case 3:
                numCellX = 6;
                numCellY = 6;
                time = 100000;
                break;
            default:
                numCellX = 3;
                numCellY = 3;
                time = 60000;
                break;


        }
        menuIntent.putExtra("numCellX", numCellX);
        menuIntent.putExtra("numCellY", numCellY);
        menuIntent.putExtra("time", time);
        this.startActivity(menuIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();
        MediaPlayerManager.getInstance().play(this, R.raw.menu);
    }

    @Override
    protected void onDestroy(){
        MediaPlayerManager.getInstance().stop();
        super.onDestroy();
    }

    @Override
    protected void onPause(){
        MediaPlayerManager.getInstance().stop();
        super.onPause();
    }


}
