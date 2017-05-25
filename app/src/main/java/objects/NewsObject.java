package objects;

import java.io.Serializable;

/**
 * Created by Sergios on 21/11/2016.
 */

public class NewsObject implements Serializable {
    private String title;
    private String text;
    private String imgURL;
    private String date;

    public NewsObject() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImgURL() {
        return imgURL;
    }

    public void setImgURL(String imgURL) {
        this.imgURL = imgURL;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
