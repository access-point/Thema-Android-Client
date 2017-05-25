package objects;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Sergios on 22/11/2016.
 */

public class FabricType implements Serializable {
    private ArrayList<Fabric>fabrics;
    private int imgId;
    private String name;

    public ArrayList<Fabric> getFabrics() {
        return fabrics;
    }

    public void setFabrics(ArrayList<Fabric> fabrics) {
        this.fabrics = fabrics;
    }

    public int getImgId() {
        return imgId;
    }

    public void setImgId(int imgId) {
        this.imgId = imgId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
