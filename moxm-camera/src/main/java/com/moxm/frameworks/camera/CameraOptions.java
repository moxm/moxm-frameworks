package com.moxm.frameworks.camera;

import android.content.Context;

/**
 * Created by Richard on 15/4/1.
 */
public final class CameraOptions {


    private static final int DATA_URL = 0;              //停用  Return base64 encoded string
    public static final int FILE_URI = 1;               // Return file uri (content://media/external/images/media/2 for Android)
    public static final int NATIVE_URI = 2;			    // On Android, this is the same as FILE_URI

    public static final int PHOTOLIBRARY = 0;          // Choose image from picture library (same as SAVEDPHOTOALBUM for Android)
    public static final int CAMERA = 1;                // Take picture from camera
    public static final int SAVEDPHOTOALBUM = 2;       // Choose image from picture library (same as PHOTOLIBRARY for Android)

    public static final int PICTURE = 0;               // allow selection of still pictures only. DEFAULT. Will return format specified via DestinationType
    public static final int VIDEO = 1;                 // allow selection of video only, ONLY RETURNS URL
    public static final int ALLMEDIA = 2;              // allow selection from all media types

    public static final int JPEG = 0;                  // Take a picture of type JPEG
    public static final int PNG = 1;                   // Take a picture of type PNG



    public final int quality;
    public final int destType;
    public final int srcType;
    public final int targetWidth;
    public final int targetHeight;
    public final int encodingType;
    public final int mediaType;
    public final String cacheDir;
    public final String fileName;
    //this.allowEdit = args.getBoolean(7); // This field is unused.
    public final boolean correctOrientation;
    public final boolean saveToPhotoAlbum;


    private CameraOptions(final Builder builder){
        this.quality = builder.quality;
        this.destType = builder.destType;
        this.srcType = builder.srcType;
        this.targetWidth = builder.targetWidth;
        this.targetHeight = builder.targetHeight;
        this.encodingType = builder.encodingType;
        this.mediaType = builder.mediaType;
        this.correctOrientation = builder.correctOrientation;
        this.saveToPhotoAlbum = builder.saveToPhotoAlbum;
        this.cacheDir = builder.cacheDir;
        this.fileName = builder.fileName;
    }


    public static class Builder {

        private Context context;

        private int quality = 80;
        private int destType = FILE_URI;
        private int srcType;
        private int targetWidth = -1;
        private int targetHeight = -1;
        private int encodingType = JPEG;
        private int mediaType = PICTURE;
        //this.allowEdit = args.getBoolean(7); // This field is unused.
        private boolean correctOrientation;
        private boolean saveToPhotoAlbum = false;
        private String cacheDir;
        private String fileName;


        public Builder(Context context) {
            this.context = context.getApplicationContext();
        }

        public Builder quality(int quality) {
            this.quality = quality;
            return this;
        }
        public Builder destType(int destType) {
            this.destType = destType;
            return this;
        }
        private Builder srcType(int srcType) {
            this.srcType = srcType;
            return this;
        }
        public Builder cacheDir(String cacheDir) {
            this.cacheDir = cacheDir;
            return this;
        }
        public Builder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }
        public Builder targetSize(int targetWidth, int targetHeight) {
            this.targetWidth = targetWidth;
            this.targetHeight = targetHeight;
            return this;
        }
        private Builder targetHeight(int targetHeight) {
            this.targetHeight = targetHeight;
            return this;
        }
        public Builder encodingType(int encodingType) {
            this.encodingType = encodingType;
            return this;
        }
        public Builder mediaType(int mediaType) {
            this.mediaType = mediaType;
            return this;
        }
        public Builder correctOrientation(boolean correctOrientation) {
            this.correctOrientation = correctOrientation;
            return this;
        }
        public Builder saveToPhotoAlbum(boolean saveToPhotoAlbum) {
            this.saveToPhotoAlbum = saveToPhotoAlbum;
            return this;
        }


        public CameraOptions build() {
            return new CameraOptions(this);
        }

    }
}
