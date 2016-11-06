package com.bignerdranch.android.locatr;

import android.location.Location;
import android.net.Uri;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Divya on 11/5/2016.
 */

public class FlickerFetchr {

    private static final String TAG = "FlickrFetchr";
    private static final String API_KEY = "fce87740623e343ebadb7eaea8db2b31";
    private static final String SEARCH_METHOD = "flickr.photos.search";
    private static final Uri ENDPOINT = Uri
            .parse("https://api.flickr.com/services/rest/")
            .buildUpon()
            .appendQueryParameter("api_key",API_KEY)
            .appendQueryParameter("format","json")
            .appendQueryParameter("nojsoncallback","1")
            .appendQueryParameter("extras","url_s, geo")
            .build();

    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection =(HttpURLConnection)url.openConnection();

        try
        {
            ByteArrayOutputStream out =new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if(connection.getResponseCode() !=HttpURLConnection.HTTP_OK)
            {
                throw new IOException(connection.getResponseMessage() +
                ": with " +
                urlSpec);
            }
            int bytesRead =0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0)
            {
                out.write(buffer, 0 ,bytesRead);
            }
            out.close();
            return out.toByteArray();
        }
        finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public List<GalleryItems> fetchItems() {

        List<GalleryItems> items = new ArrayList<>();

        try {
            String url = Uri.parse("https://api.flickr.com/services/rest")
                    .buildUpon()
                    .appendQueryParameter("method", "flickr.photos.getRecent")
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("extra", "url_s")
                    .build().toString();
            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);
            parsItems(items,jsonBody);
        }
        catch (IOException ioe)
        {
            Log.e(TAG,"Failed to fetch items",ioe);
        }
        catch (JSONException je)
        {
            Log.e(TAG,"Failed to parse JSON", je);
        }
        return items;
    }

    private List<GalleryItems> downloadGalleryItems(String url) {
        List<GalleryItems> items = new ArrayList<>();

        try {
            String jsonString = getUrlString(url);
            Log.i(TAG, "Received JSON: " + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);
            parsItems(items, jsonBody);
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse JSON", je);
        }

        return items;
    }

    private String buildUrl(Location location)
    {
        return ENDPOINT.buildUpon()
                .appendQueryParameter("method", SEARCH_METHOD)
                .appendQueryParameter("lat", "" + location.getLatitude())
                .appendQueryParameter("lon", "" + location.getLongitude())
                .build().toString();
    }

    public List<GalleryItems> searchPhotos (Location location){
        String url = buildUrl(location);
        return downloadGalleryItems(url);
    }

    private void parsItems(List<GalleryItems> itemes, JSONObject jsonBody) throws IOException,JSONException
    {
        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");
        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");

        for(int i=0; i<photoJsonArray.length();i++) {
            JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);

            GalleryItems item = new GalleryItems();
            item.setId(photoJsonObject.getString("title"));
            item.setCaption(photoJsonObject.getString("title"));

            if(!photoJsonObject.has("url_s")) {
                continue;
            }

            item.setUrl(photoJsonObject.getString("url_s"));
            item.setLat(photoJsonObject.getDouble("latitude"));
            item.setLon(photoJsonObject.getDouble("longitude"));
            itemes.add(item);
        }
    }
}
