package utilities;

import android.app.Application;

import com.onesignal.OneSignal;

/**
 * Created by Sergios on 18/12/2016.
 */

public class AppSingleton extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        OneSignal.startInit(this)
        .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification).
                init();


    }
}
