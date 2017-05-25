package objects;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Sergios on 24/9/2016.
 */

public class Fabric implements Serializable {
    private int imageId;
    private String title;
    private ArrayList<FabricColor> fabricColors;
    private ArrayList<String> couches;
    private ArrayList<String> couchBodys;
    private ArrayList<String> couchPillows;
    private ArrayList<String> curtains;

    public Fabric() {
    }

    public Fabric(int imageId, String title, ArrayList<FabricColor> fabricColors) {
        this.imageId = imageId;
        this.title = title;
        this.fabricColors = fabricColors;
    }



    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ArrayList<FabricColor> getFabricColors() {
        return fabricColors;
    }

    public void setFabricColors(ArrayList<FabricColor> fabricColors) {
        this.fabricColors = fabricColors;
    }

    public ArrayList<String> getCouches() {
        return couches;
    }

    public void setCouches(ArrayList<String> couches) {
        this.couches = couches;
    }

    public ArrayList<String> getCurtains() {
        return curtains;
    }

    public void setCurtains(ArrayList<String> curtains) {
        this.curtains = curtains;
    }

    public ArrayList<String> getCouchBodys() {
        return couchBodys;
    }

    public void setCouchBodys(ArrayList<String> couchBodys) {
        this.couchBodys = couchBodys;
    }

    public ArrayList<String> getCouchPillows() {
        return couchPillows;
    }

    public void setCouchPillows(ArrayList<String> couchPillows) {
        this.couchPillows = couchPillows;
    }
}
