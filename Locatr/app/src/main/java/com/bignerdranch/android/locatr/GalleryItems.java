package com.bignerdranch.android.locatr;


/**
 * Created by Divya on 11/5/2016.
 */

public class GalleryItems {
    private String mCaption;
    private String mId;
    private String mUrl;
    private double mLat;
    private double mLon;

    @Override
    public String toString() {
        return mCaption;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public void setCaption(String caption) {
        mCaption = caption;
    }

    public double getLat() {
        return mLat;
    }

    public void setLat(double lat) {
        mLat = lat;
    }

    public double getLon() {
        return mLon;
    }

    public void setLon(double lon) {
        mLon = lon;
    }
}
