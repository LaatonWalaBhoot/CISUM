package home.cisum;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.KeyEvent;
import android.widget.Toast;

/**
 * Created by Aishwarya on 8/6/2017.
 */

public class HardButtonReceiver extends BroadcastReceiver {

    Handler handler = new Handler();
    Runnable r;
    int count=0;

    @Override
    public void onReceive(final Context context, Intent intent) {

        String intentAction = intent.getAction();
        KeyEvent event = (KeyEvent)intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        if (!Intent.ACTION_MEDIA_BUTTON.equals(intentAction) && event.getAction() == KeyEvent.ACTION_DOWN) {
            return;
        }
        if (!Intent.ACTION_MEDIA_BUTTON.equals(intentAction) && event.getAction() == KeyEvent.ACTION_UP) {
            count++;
        }
        if (event == null) {
            return;
        }

        if(event.getKeyCode()==KeyEvent.ACTION_DOWN) {

        }
        abortBroadcast();
    }
}