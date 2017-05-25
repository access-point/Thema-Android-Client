package custom_views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Camera;
import android.view.SurfaceHolder;

/**
 * Created by Sergios on 29/10/2016.
 */

public class CustomCameraPreview extends CameraPreview implements SurfaceHolder.Callback  {
    Camera camera;
    SurfaceHolder holder;
    public CustomCameraPreview(Context context, Camera camera) {
        super(context, camera);
        holder=super.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        this.camera=camera;
    }




    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        // nothing gets drawn :(
        Paint p = new Paint(Color.RED);
        canvas.drawText("PREVIEW", canvas.getWidth() / 2,
                canvas.getHeight() / 2, p);
    }
}
