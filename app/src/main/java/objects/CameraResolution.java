package objects;

/**
 * Created by Sergios on 29/10/2016.
 */
import android.hardware.Camera;

/**
 * Created by sergios on 18/3/2016.
 */
public class CameraResolution {
    private Camera.Size size;
    private int megapixels;

    public CameraResolution(Camera.Size size, int megapixels) {
        this.size = size;
        this.megapixels = megapixels;
    }

    public Camera.Size getSize() {
        return size;
    }

    public void setSize(Camera.Size size) {
        this.size = size;
    }

    public int getMegapixels() {
        return megapixels;
    }

    public void setMegapixels(int megapixels) {
        this.megapixels = megapixels;
    }
}