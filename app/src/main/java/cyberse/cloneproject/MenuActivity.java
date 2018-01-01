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

        mView = new MenuView(this, this);
        setContentView(mView);


    }

    public void startGame(){
        Intent menuIntent = new Intent(this, GameActivity.class);
        int numCellX, numCellY, time;
        switch (mView.gridIndex){
            case 0:
                numCellX = 3;
                numCellY = 3;
                time = 60000;
                break;
            case 1:
                numCellX = 4;
                numCellY = 4;
                time = 90000;
                break;
            case 2:
                numCellX = 5;
                numCellY = 5;
                time = 120000;
                break;
            case 3:
                numCellX = 6;
                numCellY = 6;
                time = 150000;
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
}
