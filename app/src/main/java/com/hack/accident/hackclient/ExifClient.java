package com.hack.accident.hackclient;

import android.content.Context;
import android.media.ExifInterface;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
* Created by jackm on 17/04/2015.
*/
public class ExifClient {

    private String imageFile = null;

    public ExifClient( String imageFile) {
        this.imageFile = imageFile;
    }

    public void loadExifData(){
        try {
            ExifInterface mExif = new ExifInterface(imageFile);
            Log.i("EXIF", "LATITUDE:  " + mExif.getAttribute(ExifInterface.TAG_GPS_LATITUDE));
            Log.i("EXIF", "LONGITUDE: " + mExif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE));
        } catch (  IOException ex) {
            Log.e("ERROR", "cannot read exif", ex);
        }
    }

}
