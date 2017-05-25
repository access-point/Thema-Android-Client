package objects;

import java.io.Serializable;

/**
 * Created by sergios on 28/9/2016.
 */

public class SocialNetwork implements Serializable {

    private String name;
    private int imgId;


    public SocialNetwork(String name, int imgId) {
        this.name = name;
        this.imgId = imgId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getImgId() {
        return imgId;
    }

    public void setImgId(int imgId) {
        this.imgId = imgId;
    }
}
