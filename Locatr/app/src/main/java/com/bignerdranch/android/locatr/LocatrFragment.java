package com.bignerdranch.android.locatr;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

/**
 * Created by Divya on 11/5/2016.
 */

public class LocatrFragment extends SupportMapFragment {
    private ImageView mImageView;
    private GoogleApiClient mClient;
    private static final String TAG = "LocatrFragment";
    private static final int REQUEST_LOCATION = 0;
    private Bitmap mBitmap;
    private Bitmap mMapImage;
    private GalleryItems mMapItems;
    private Location mCurrentLocation;
    private ProgressDialog mProgressDialog;
    private GoogleMap mMap;

    public static LocatrFragment newInstacne() {
        return new LocatrFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        getActivity().invalidateOptionsMenu();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .build();

        getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                updateUI();
            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        ;

        getActivity().invalidateOptionsMenu();
        mClient.connect();

    }

    @Override
    public void onStop() {
        super.onStop();
        mClient.disconnect();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_locatr, menu);

        MenuItem searchItem = menu.findItem(R.id.action_locatr);
        searchItem.setEnabled(mClient.isConnected());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.action_locatr:
                findImage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void findImage() {
        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setNumUpdates(1);
        request.setInterval(0);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION);
        }
        else {
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setTitle("Download");
            mProgressDialog.show();
            LocationServices.FusedLocationApi
                    .requestLocationUpdates(mClient, request, new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            Log.i(TAG, "Got a fix: " + location);
                            new SearchTask().execute(location);
                        }
                    });
        }
    }

    private void updateUI()
    {
        if (mMap == null || mMapImage == null)
            return;

        LatLng itemPoit = new LatLng(mMapItems.getLat(), mMapItems.getLon());
        LatLng myPoint = new LatLng(
                mCurrentLocation.getLatitude(),mCurrentLocation.getLongitude());

        BitmapDescriptor itemBitmap = BitmapDescriptorFactory.fromBitmap(mMapImage);
        MarkerOptions itemMarker = new MarkerOptions()
                .position(itemPoit)
                .icon(itemBitmap);
        MarkerOptions myMarker = new MarkerOptions()
                .position(myPoint);

        mMap.clear();
        mMap.addMarker(itemMarker);
        mMap.addMarker(myMarker);



        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(itemPoit)
                .include(myPoint)
                .build();

        int margin =getResources().getDimensionPixelSize(R.dimen.map_insert_margin);
        CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds,margin);
        mMap.animateCamera(update);

    }

    private class SearchTask extends AsyncTask<Location, Void,Void> {
        private GalleryItems mGalleryItems;
        private Location mLocation;

        @Override
        protected Void doInBackground(Location... params){
            mLocation =params[0];
            FlickerFetchr fetchr = new FlickerFetchr();
            List<GalleryItems> items = fetchr.searchPhotos(params[0]);

            if (items.size() == 0){
                return null;
            }

            mGalleryItems =items.get(0);

            try {
                byte[] bytes = fetchr.getUrlBytes(mGalleryItems.getUrl());
                mBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

            } catch (IOException ioe){
                Log.i(TAG, " Unable to download bitmap",ioe);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mProgressDialog.cancel();
         mMapImage =mBitmap;
            mMapItems = mGalleryItems;
            mCurrentLocation = mLocation;

            updateUI();
        }
    }
}
