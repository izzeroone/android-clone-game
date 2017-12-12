package cyberse.cloneproject;

/**
 * Created by Cyber on 12/5/2017.
 */

public class TimerUpdateRunnable implements Runnable {
    private Object mPauseLock;
    private boolean mPaused;
    private boolean mFinished;
    private MainGame mGame;

    public TimerUpdateRunnable(MainGame game) {
        mPauseLock = new Object();
        mPaused = false;
        mFinished = false;
        mGame = game;
    }

    public void run() {
        while (!mFinished) {
            mGame.update();
            synchronized (mPauseLock) {
                while (mPaused) {
                    try {
                        mPauseLock.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }

    /**
     * Call this on pause.
     */
    public void onPause() {
        synchronized (mPauseLock) {
            mPaused = true;
        }
    }

    /**
     * Call this on resume.
     */
    public void onResume() {
        synchronized (mPauseLock) {
            mPaused = false;
            mPauseLock.notifyAll();
        }
    }

}
