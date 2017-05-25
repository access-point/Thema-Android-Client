package objects;

import java.io.Serializable;

/**
 * Created by Sergios on 24/9/2016.
 */

public class FabricColor implements Serializable {
    private int thumbId;
    private String thumbURL;
    private String ImgUrl;
    private String nameEn;
    private String nameGr;

    public FabricColor(int thumbId, String imgUrl, String nameEn, String nameGr) {
        this.thumbId = thumbId;
        ImgUrl = imgUrl;
        this.nameEn = nameEn;
        this.nameGr = nameGr;
    }

    public FabricColor(String thumbURL, String imgUrl, String nameEn, String nameGr) {
        this.thumbURL = thumbURL;
        ImgUrl = imgUrl;
        this.nameEn = nameEn;
        this.nameGr = nameGr;
    }

    public String getImgUrl() {
        return ImgUrl;
    }

    public void setImgUrl(String imgUrl) {
        ImgUrl = imgUrl;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getNameGr() {
        return nameGr;
    }

    public void setNameGr(String nameGr) {
        this.nameGr = nameGr;
    }

    public int getThumbId() {
        return thumbId;
    }

    public void setThumbId(int thumbId) {
        this.thumbId = thumbId;
    }

    public String getThumbURL() {
        return thumbURL;
    }

    public void setThumbURL(String thumbURL) {
        this.thumbURL = thumbURL;
    }
}

