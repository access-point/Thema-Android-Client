package utilities;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

/**
 * Created by Sergios on 22/11/2016.
 */

public class Tools {
    public static String removePunctuationGreek(String input) {

        input=input.replace("ά","α");
        input=input.replace("έ","ε");
        input=input.replace("ή","η");
        input=input.replace("ί","ι");
        input=input.replace("ύ","υ");
        input=input.replace("ό","ο");
        input=input.replace("ώ","ω");

        input=input.replace("Ά","Α");
        input=input.replace("Έ","Ε");
        input=input.replace("Ή","Η");
        input=input.replace("Ί","Ι");
        input=input.replace("Ύ","Υ");
        input=input.replace("Ό","Ο");
        input=input.replace("Ώ","Ω");

        return input;
    }

    public static float convertDpToPixel(float dp, Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }
}
