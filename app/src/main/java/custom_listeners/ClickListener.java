package custom_listeners;

import android.view.View;

/**
 * Created by Sergios on 25/9/2016.
 */

public interface ClickListener {
    void onClick(View view, int position);

    void onLongClick(View view, int position);
}