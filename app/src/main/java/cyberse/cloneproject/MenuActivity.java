package cyberse.cloneproject;

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

        mView = new MenuView(this);
        setContentView(mView);

        ImageButton button = new ImageButton(this);

    }
}
