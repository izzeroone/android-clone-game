package cyberse.cloneproject;


import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.SparseIntArray;


public class SoundPoolManager {
    private static SoundPoolManager singleton;
    SoundPool soundPool;

    SparseIntArray soundId = new SparseIntArray();

    public SoundPoolManager(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            soundPool = (new SoundPool.Builder()).setMaxStreams(1).build();
        }else{
            soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 5);
        }
    }

    public void loadSound(Context context){
        int sound = soundPool.load(context, R.raw.step, 1);
        soundId.put(R.raw.step, sound);
        // load other sound if you like
    }

    public void playSound(int resid){
        int sound = soundId.get(resid);
        soundPool.play(sound, 1.0F, 1.0F, 0, 0, 1.0F);
    }



    public static void initialize(Context context){
        SoundPoolManager soundManager = getInstance();
        soundManager.loadSound(context);
    }

    public static synchronized SoundPoolManager getInstance(){
        if(singleton == null){
            singleton = new SoundPoolManager();
        }
        return singleton;
    }
}
