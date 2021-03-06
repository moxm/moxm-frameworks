/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/
package com.moxm.frameworks.camera;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This class launches the camera view, allows the user to take a picture, closes the camera view,
 * and returns the captured image.  When the camera view is closed, the screen displayed before
 * the camera view was shown is redisplayed.
 */
public class CameraLauncher implements MediaScannerConnectionClient {


    protected static final int DATA_URL = 0;              // Return base64 encoded string
    protected static final int FILE_URI = CameraOptions.FILE_URI;              // Return file uri (content://media/external/images/media/2 for Android)
    protected static final int NATIVE_URI = CameraOptions.NATIVE_URI;			// On Android, this is the same as FILE_URI

    protected static final int PHOTOLIBRARY = CameraOptions.PHOTOLIBRARY;          // Choose image from picture library (same as SAVEDPHOTOALBUM for Android)
    protected static final int CAMERA = CameraOptions.CAMERA;                // Take picture from camera
    protected static final int SAVEDPHOTOALBUM = CameraOptions.SAVEDPHOTOALBUM;       // Choose image from picture library (same as PHOTOLIBRARY for Android)

    protected static final int PICTURE = CameraOptions.PICTURE;               // allow selection of still pictures only. DEFAULT. Will return format specified via DestinationType
    protected static final int VIDEO = CameraOptions.VIDEO;                 // allow selection of video only, ONLY RETURNS URL
    protected static final int ALLMEDIA = CameraOptions.ALLMEDIA;              // allow selection from all media types

    protected static final int JPEG = CameraOptions.JPEG;                  // Take a picture of type JPEG
    protected static final int PNG = CameraOptions.PNG;                   // Take a picture of type PNG

    protected static final String GET_PICTURE = "Get Picture";
    protected static final String GET_VIDEO = "Get Video";
    protected static final String GET_All = "Get All";

    protected static final String LOG_TAG = "CameraLauncher";

    protected int mQuality;                   // Compression quality hint (0-100: 0=low quality & high compression, 100=compress of max quality)
    protected int targetWidth;                // desired width of the image
    protected int targetHeight;               // desired height of the image
    protected Uri imageUri;                   // Uri of captured image
    protected int encodingType;               // Type of encoding to use
    protected int mediaType;                  // What type of media to retrieve
    protected boolean saveToPhotoAlbum;       // Should the picture be saved to the device's photo album
    protected boolean correctOrientation;     // Should the pictures orientation be corrected
    protected String cacheDir;
    protected String fileName;
    //private boolean allowEdit;              // Should we allow the user to crop the image. UNUSED.

//    public CallbackContext callbackContext;

    protected CameraInterface mCameraInterface;
    protected OnCallbackListener mOnCallbackListener;
    protected CameraOptions options;
    protected int numPics;

    protected MediaScannerConnection conn;    // Used to update gallery app with newly-written files
    protected Uri scanMe;                     // Uri of image to be added to content store

    public CameraLauncher(CameraInterface cameraInterface, CameraOptions options){
        this.mCameraInterface = cameraInterface;
        this.options = options;
        initOptions(options);
    }

    /**
     * 通过相机拍照
     */
    public void takePicture(){
        int destType = options.destType;
        int srcType = options.CAMERA;
        try {
            if (srcType == CAMERA) {
                this.takePicture(destType, encodingType);
            }
        } catch (IllegalArgumentException e) {
            failPicture("Illegal Argument Exception");
        }
    }

    /**
     * 通过相册选取
     */
    public void choosePicture(){
        int destType = options.destType;
        int srcType = options.PHOTOLIBRARY;
        try {
            if ((srcType == PHOTOLIBRARY) || (srcType == SAVEDPHOTOALBUM)) {
                this.getImage(srcType, destType);
            }
        } catch (IllegalArgumentException e) {
            failPicture("Illegal Argument Exception");
        }
    }

    private void initOptions(CameraOptions options){
        this.mQuality = options.quality;
        int destType = options.destType;
        int srcType = options.srcType;
        this.targetWidth = options.targetWidth;
        this.targetHeight = options.targetHeight;
        this.encodingType = options.encodingType;
        this.mediaType = options.mediaType;
        //this.allowEdit = args.getBoolean(7); // This field is unused.
        this.correctOrientation = options.correctOrientation;
        this.saveToPhotoAlbum = options.saveToPhotoAlbum;
        this.cacheDir = options.cacheDir;
        this.fileName = options.fileName;

        // If the user specifies a 0 or smaller width/height
        // make it -1 so later comparisons succeed
        if (this.targetWidth < 1) {
            this.targetWidth = -1;
        }
        if (this.targetHeight < 1) {
            this.targetHeight = -1;
        }
    }


    //--------------------------------------------------------------------------
    // LOCAL METHODS
    //--------------------------------------------------------------------------

    private String getTempDirectoryPath() {
        File cache = null;
        if (cacheDir != null && !cacheDir.equals("")) {
            cache = new File(cacheDir);
        } else if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {// SD Card Mounted
            cache = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                    "/Android/data/" + mCameraInterface.getSupportActivity().getPackageName() + "/cache/");
        } else {// Use internal storage
            cache = mCameraInterface.getSupportActivity().getCacheDir();
        }

        // Create the cache directory if it doesn't exist
        cache.mkdirs();
        return cache.getAbsolutePath();
    }

    /**
     * Take a picture with the camera.
     * When an image is captured or the camera view is cancelled, the result is returned
     * in CordovaActivity.onActivityResult, which forwards the result to this.onActivityResult.
     *
     * The image can either be returned as a base64 string or a URI that points to the file.
     * To display base64 string in an img tag, set the source to:
     *      img.src="data:image/jpeg;base64,"+result;
     * or to display URI in an img tag
     *      img.src=result;
     *
     * @param encodingType
     * @param returnType        Set the type of image to return.
     */
    private void takePicture(int returnType, int encodingType) {
        // Save the number of images currently on disk for later
        this.numPics = queryImgDB(whichContentStore()).getCount();

        // Display camera
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");

        // Specify file so that large image is captured and returned
        File photo = createCaptureFile(encodingType);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
        this.imageUri = Uri.fromFile(photo);

        if (this.mCameraInterface != null) {
            this.mCameraInterface.startActivityForResult(this, intent, (CAMERA + 1) * 16 + returnType + 1);
        }
//        else
//            LOG.d(LOG_TAG, "ERROR: You must use the CordovaInterface for this to work correctly. Please implement it in your activity");
    }

    /**
     * Create a file in the applications temporary directory based upon the supplied encoding.
     *
     * @param encodingType of the image to be taken
     * @return a File object pointing to the temporary picture
     */
    private File createCaptureFile(int encodingType) {
        File photo = null;
        if (encodingType == JPEG) {
            photo = new File(getTempDirectoryPath(), ".Pic.jpg");
        } else if (encodingType == PNG) {
            photo = new File(getTempDirectoryPath(), ".Pic.png");
        } else {
            throw new IllegalArgumentException("Invalid Encoding Type: " + encodingType);
        }
        return photo;
    }

    /**
     * Get image from photo library.
     *
     * @param srcType           The album to get image from.
     * @param returnType        Set the type of image to return.
     */
    // TODO: Images selected from SDCARD don't display correctly, but from CAMERA ALBUM do!
    private void getImage(int srcType, int returnType) {
        Intent intent = new Intent();
        String title = GET_PICTURE;
        if (this.mediaType == PICTURE) {
            intent.setType("image/*");
        }
        else if (this.mediaType == VIDEO) {
            intent.setType("video/*");
            title = GET_VIDEO;
        }
        else if (this.mediaType == ALLMEDIA) {
            // I wanted to make the type 'image/*, video/*' but this does not work on all versions
            // of android so I had to go with the wildcard search.
            intent.setType("*/*");
            title = GET_All;
        }

        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        if (this.mCameraInterface != null) {
            this.mCameraInterface.startActivityForResult(this, Intent.createChooser(intent,
                    new String(title)), (srcType + 1) * 16 + returnType + 1);
        }
    }

    /**
     * Called when the camera view exits.
     *
     * @param requestCode       The request code originally supplied to startActivityForResult(),
     *                          allowing you to identify who this result came from.
     * @param resultCode        The integer result code returned by the child activity through its setResult().
     * @param intent            An Intent, which can return result data to the caller (various data can be attached to Intent "extras").
     */
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        Log.d("CameraLauncher", "------>onActivityResult");
        // Get src and dest types from request code
        int srcType = (requestCode / 16) - 1;
        int destType = (requestCode % 16) - 1;
        int rotate = 0;

        // If CAMERA
        if (srcType == CAMERA) {
            // If image available
            if (resultCode == Activity.RESULT_OK) {
                try {
                    // Create an ExifHelper to save the exif data that is lost during compression
                    ExifHelper exif = new ExifHelper();
                    try {
                        if (this.encodingType == JPEG) {
                            exif.createInFile(getTempDirectoryPath() + "/.Pic.jpg");
                            exif.readExifData();
                            rotate = exif.getOrientation();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Bitmap bitmap = null;
                    Uri uri = null;

                    // If sending base64 image back
                    if (destType == DATA_URL) {
                        bitmap = getScaledBitmap(FileHelper.stripFileProtocol(imageUri.toString()));
                        if (bitmap == null) {
                            // Try to get the bitmap from intent.
                            bitmap = (Bitmap)intent.getExtras().get("data");
                        }

                        // Double-check the bitmap.
                        if (bitmap == null) {
                            Log.d(LOG_TAG, "I either have a null image path or bitmap");
                            this.failPicture("Unable to create bitmap!");
                            return;
                        }

                        if (rotate != 0 && this.correctOrientation) {
                            bitmap = getRotatedBitmap(rotate, bitmap, exif);
                        }

                        this.processPicture(bitmap);
                        checkForDuplicateImage(DATA_URL);
                    }

                    // If sending filename back
                    else if (destType == FILE_URI || destType == NATIVE_URI) {
                        if (this.saveToPhotoAlbum) {
                            Uri inputUri = getUriFromMediaStore();
                            //Just because we have a media URI doesn't mean we have a real file, we need to make it
                            uri = Uri.fromFile(new File(FileHelper.getRealPath(inputUri, this.mCameraInterface)));
                        } else {
                            uri = Uri.fromFile(new File(getTempDirectoryPath(), (TextUtils.isEmpty(this.fileName) ? (System.currentTimeMillis() + ".jpg") : (this.fileName + ".jpg"))));
                        }

                        if (uri == null) {
                            this.failPicture("Error capturing image - no media storage found.");
                        }

                        // If all this is true we shouldn't compress the image.
                        if (this.targetHeight == -1 && this.targetWidth == -1 && this.mQuality == 100 &&
                                !this.correctOrientation) {
                            writeUncompressedImage(uri);

                            if(mOnCallbackListener != null){
                                this.mOnCallbackListener.success(uri);//.toString()
                            }
                        } else {
                            bitmap = getScaledBitmap(FileHelper.stripFileProtocol(imageUri.toString()));

                            if (rotate != 0 && this.correctOrientation) {
                                bitmap = getRotatedBitmap(rotate, bitmap, exif);
                            }

                            // Add compressed version of captured image to returned media store Uri
                            OutputStream os = this.mCameraInterface.getSupportActivity().getContentResolver().openOutputStream(uri);
                            bitmap.compress(CompressFormat.JPEG, this.mQuality, os);
                            os.close();

                            // Restore exif data to file
                            if (this.encodingType == JPEG) {
                                String exifPath;
                                if (this.saveToPhotoAlbum) {
                                    exifPath = FileHelper.getRealPath(uri, this.mCameraInterface);
                                } else {
                                    exifPath = uri.getPath();
                                }
                                exif.createOutFile(exifPath);
                                exif.writeExifData();
                            }

                        }
                        // Send Uri back to JavaScript for viewing image
                        if(mOnCallbackListener != null) {
                            this.mOnCallbackListener.success(uri);//.toString()
                        }
                    }

                    this.cleanup(FILE_URI, this.imageUri, uri, bitmap);
                    bitmap = null;

                } catch (IOException e) {
                    e.printStackTrace();
                    this.failPicture("Error capturing image.");
                }
            }

            // If cancelled
            else if (resultCode == Activity.RESULT_CANCELED) {
                this.failPicture("Camera cancelled.");
            }

            // If something else
            else {
                this.failPicture("Did not complete!");
            }
        }

        // If retrieving photo from library
        else if ((srcType == PHOTOLIBRARY) || (srcType == SAVEDPHOTOALBUM)) {
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = intent.getData();

                // If you ask for video or all media type you will automatically get back a file URI
                // and there will be no attempt to resize any returned data
                if (this.mediaType != PICTURE) {
                    if(mOnCallbackListener != null) {
                        this.mOnCallbackListener.success(uri);//.toString()
                    }
                }
                else {
                    // This is a special case to just return the path as no scaling,
                    // rotating, nor compressing needs to be done
                    if (this.targetHeight == -1 && this.targetWidth == -1 &&
                            (destType == FILE_URI || destType == NATIVE_URI) && !this.correctOrientation) {
                        if(mOnCallbackListener != null) {
                            this.mOnCallbackListener.success(uri);//.toString()
                        }
                    } else {
                        String uriString = uri.toString();
                        // Get the path to the image. Makes loading so much easier.
                        String mimeType = FileHelper.getMimeType(uriString, this.mCameraInterface);
                        // If we don't have a valid image so quit.
                        if (!("image/jpeg".equalsIgnoreCase(mimeType) || "image/png".equalsIgnoreCase(mimeType))) {
                        	Log.d(LOG_TAG, "I either have a null image path or bitmap");
                            this.failPicture("Unable to retrieve path to picture!");
                            return;
                        }
                        Bitmap bitmap = null;
                        try {
                            bitmap = getScaledBitmap(uriString);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (bitmap == null) {
                        	Log.d(LOG_TAG, "I either have a null image path or bitmap");
                            this.failPicture("Unable to create bitmap!");
                            return;
                        }

                        if (this.correctOrientation) {
                            rotate = getImageOrientation(uri);
                            if (rotate != 0) {
                                Matrix matrix = new Matrix();
                                matrix.setRotate(rotate);
                                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                            }
                        }

                        // If sending base64 image back
                        if (destType == DATA_URL) {
                            this.processPicture(bitmap);
                        }

                        // If sending filename back
                        else if (destType == FILE_URI || destType == NATIVE_URI) {
                            // Do we need to scale the returned file
                            if (this.targetHeight > 0 && this.targetWidth > 0) {
                                try {
                                    // Create an ExifHelper to save the exif data that is lost during compression
                                    String resizePath = getTempDirectoryPath() + (TextUtils.isEmpty(this.fileName) ? "/resize.jpg" : ("/" + this.fileName + ".jpg"));
                                    // Some content: URIs do not map to file paths (e.g. picasa).
                                    String realPath = FileHelper.getRealPath(uri, this.mCameraInterface);
                                    ExifHelper exif = new ExifHelper();
                                    if (realPath != null && this.encodingType == JPEG) {
                                        try {
                                            exif.createInFile(realPath);
                                            exif.readExifData();
                                            rotate = exif.getOrientation();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    OutputStream os = new FileOutputStream(resizePath);
                                    bitmap.compress(CompressFormat.JPEG, this.mQuality, os);
                                    os.close();

                                    // Restore exif data to file
                                    if (realPath != null && this.encodingType == JPEG) {
                                        exif.createOutFile(resizePath);
                                        exif.writeExifData();
                                    }

                                    // The resized image is cached by the app in order to get around this and not have to delete you
                                    // application cache I'm adding the current system time to the end of the file url.
                                    if(mOnCallbackListener != null) {
                                        //??????
//                                        this.mOnCallbackListener.success("file://" + resizePath + "?" + System.currentTimeMillis());
                                        this.mOnCallbackListener.success(Uri.parse("file://" + resizePath));
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                    this.failPicture("Error retrieving image.");
                                }
                            }
                            else {
                                if(mOnCallbackListener != null) {
                                    this.mOnCallbackListener.success(uri);//.toString()
                                }
                            }
                        }
                        if (bitmap != null) {
	                        bitmap.recycle();
	                        bitmap = null;
                        }
                        System.gc();
                    }
                }
            }
            else if (resultCode == Activity.RESULT_CANCELED) {
                this.failPicture("Selection cancelled.");
            }
            else {
                this.failPicture("Selection did not complete!");
            }
        }
    }

    private int getImageOrientation(Uri uri) {
        String[] cols = { MediaStore.Images.Media.ORIENTATION };
        Cursor cursor = mCameraInterface.getSupportActivity().getContentResolver().query(uri,
                cols, null, null, null);
        int rotate = 0;
        if (cursor != null) {
            cursor.moveToPosition(0);
            rotate = cursor.getInt(0);
            cursor.close();
        }
        return rotate;
    }

    /**
     * Figure out if the bitmap should be rotated. For instance if the picture was taken in
     * portrait mode
     *
     * @param rotate
     * @param bitmap
     * @return rotated bitmap
     */
    private Bitmap getRotatedBitmap(int rotate, Bitmap bitmap, ExifHelper exif) {
        Matrix matrix = new Matrix();
        if (rotate == 180) {
            matrix.setRotate(rotate);
        } else {
            matrix.setRotate(rotate, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);
        }

        try
        {
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            exif.resetOrientation();
        }
        catch (OutOfMemoryError oom)
        {
            // You can run out of memory if the image is very large:
            // http://simonmacdonald.blogspot.ca/2012/07/change-to-camera-code-in-phonegap-190.html
            // If this happens, simply do not rotate the image and return it unmodified.
            // If you do not catch the OutOfMemoryError, the Android app crashes.
        }

        return bitmap;
    }

    /**
     * In the special case where the default width, height and quality are unchanged
     * we just write the file out to disk saving the expensive Bitmap.compress function.
     *
     * @param uri
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void writeUncompressedImage(Uri uri) throws FileNotFoundException,
            IOException {
        FileInputStream fis = new FileInputStream(FileHelper.stripFileProtocol(imageUri.toString()));
        OutputStream os = this.mCameraInterface.getSupportActivity().getContentResolver().openOutputStream(uri);
        byte[] buffer = new byte[4096];
        int len;
        while ((len = fis.read(buffer)) != -1) {
            os.write(buffer, 0, len);
        }
        os.flush();
        os.close();
        fis.close();
    }

    /**
     * Create entry in media store for image
     *
     * @return uri
     */
    private Uri getUriFromMediaStore() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        Uri uri;
        try {
            uri = this.mCameraInterface.getSupportActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        } catch (UnsupportedOperationException e) {
            Log.d(LOG_TAG, "Can't write to external media storage.");
            try {
                uri = this.mCameraInterface.getSupportActivity().getContentResolver().insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, values);
            } catch (UnsupportedOperationException ex) {
                Log.d(LOG_TAG, "Can't write to internal media storage.");
                return null;
            }
        }
        return uri;
    }

    /**
     * Return a scaled bitmap based on the target width and height
     *
     * @param imageUrl
     * @return
     * @throws IOException
     */
    private Bitmap getScaledBitmap(String imageUrl) throws IOException {
        // If no new width or height were specified return the original bitmap
        if (this.targetWidth <= 0 && this.targetHeight <= 0) {
            return BitmapFactory.decodeStream(FileHelper.getInputStreamFromUriString(imageUrl, mCameraInterface));
        }

        // figure out the original width and height of the image
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(FileHelper.getInputStreamFromUriString(imageUrl, mCameraInterface), null, options);

        //CB-2292: WTF? Why is the width null?
        if(options.outWidth == 0 || options.outHeight == 0)
        {
            return null;
        }

        // determine the correct aspect ratio
        int[] widthHeight = calculateAspectRatio(options.outWidth, options.outHeight);

        // Load in the smallest bitmap possible that is closest to the size we want
        options.inJustDecodeBounds = false;
        options.inSampleSize = calculateSampleSize(options.outWidth, options.outHeight, this.targetWidth, this.targetHeight);
        Bitmap unscaledBitmap = BitmapFactory.decodeStream(FileHelper.getInputStreamFromUriString(imageUrl, mCameraInterface), null, options);
        if (unscaledBitmap == null) {
            return null;
        }

        return Bitmap.createScaledBitmap(unscaledBitmap, widthHeight[0], widthHeight[1], true);
    }

    /**
     * Maintain the aspect ratio so the resulting image does not look smooshed
     *
     * @param origWidth
     * @param origHeight
     * @return
     */
    public int[] calculateAspectRatio(int origWidth, int origHeight) {
        int newWidth = this.targetWidth;
        int newHeight = this.targetHeight;

        // If no new width or height were specified return the original bitmap
        if (newWidth <= 0 && newHeight <= 0) {
            newWidth = origWidth;
            newHeight = origHeight;
        }
        // Only the width was specified
        else if (newWidth > 0 && newHeight <= 0) {
            newHeight = (newWidth * origHeight) / origWidth;
        }
        // only the height was specified
        else if (newWidth <= 0 && newHeight > 0) {
            newWidth = (newHeight * origWidth) / origHeight;
        }
        // If the user specified both a positive width and height
        // (potentially different aspect ratio) then the width or height is
        // scaled so that the image fits while maintaining aspect ratio.
        // Alternatively, the specified width and height could have been
        // kept and Bitmap.SCALE_TO_FIT specified when scaling, but this
        // would result in whitespace in the new image.
        else {
            double newRatio = newWidth / (double) newHeight;
            double origRatio = origWidth / (double) origHeight;

            if (origRatio > newRatio) {
                newHeight = (newWidth * origHeight) / origWidth;
            } else if (origRatio < newRatio) {
                newWidth = (newHeight * origWidth) / origHeight;
            }
        }

        int[] retval = new int[2];
        retval[0] = newWidth;
        retval[1] = newHeight;
        return retval;
    }

    /**
     * Figure out what ratio we can load our image into memory at while still being bigger than
     * our desired width and height
     *
     * @param srcWidth
     * @param srcHeight
     * @param dstWidth
     * @param dstHeight
     * @return
     */
    public static int calculateSampleSize(int srcWidth, int srcHeight, int dstWidth, int dstHeight) {
        final float srcAspect = (float)srcWidth / (float)srcHeight;
        final float dstAspect = (float)dstWidth / (float)dstHeight;

        if (srcAspect > dstAspect) {
            return srcWidth / dstWidth;
        } else {
            return srcHeight / dstHeight;
        }
      }

    /**
     * Creates a cursor that can be used to determine how many images we have.
     *
     * @return a cursor
     */
    private Cursor queryImgDB(Uri contentStore) {
        return this.mCameraInterface.getSupportActivity().getContentResolver().query(
                contentStore,
                new String[] { MediaStore.Images.Media._ID },
                null,
                null,
                null);
    }

    /**
     * Cleans up after picture taking. Checking for duplicates and that kind of stuff.
     * @param newImage
     */
    private void cleanup(int imageType, Uri oldImage, Uri newImage, Bitmap bitmap) {
        if (bitmap != null) {
            bitmap.recycle();
        }

        // Clean up initial camera-written image file.
        (new File(FileHelper.stripFileProtocol(oldImage.toString()))).delete();

        checkForDuplicateImage(imageType);
        // Scan for the gallery to update pic refs in gallery
        if (this.saveToPhotoAlbum && newImage != null) {
            this.scanForGallery(newImage);
        }

        System.gc();
    }

    /**
     * Used to find out if we are in a situation where the Camera Intent adds to images
     * to the content store. If we are using a FILE_URI and the number of images in the DB
     * increases by 2 we have a duplicate, when using a DATA_URL the number is 1.
     *
     * @param type FILE_URI or DATA_URL
     */
    private void checkForDuplicateImage(int type) {
        int diff = 1;
        Uri contentStore = whichContentStore();
        Cursor cursor = queryImgDB(contentStore);
        int currentNumOfImages = cursor.getCount();

        if (type == FILE_URI && this.saveToPhotoAlbum) {
            diff = 2;
        }

        // delete the duplicate file if the difference is 2 for file URI or 1 for Data URL
        if ((currentNumOfImages - numPics) == diff) {
            cursor.moveToLast();
            int id = Integer.valueOf(cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media._ID)));
            if (diff == 2) {
                id--;
            }
            Uri uri = Uri.parse(contentStore + "/" + id);
            this.mCameraInterface.getSupportActivity().getContentResolver().delete(uri, null, null);
            cursor.close();
        }
    }

    /**
     * Determine if we are storing the images in internal or external storage
     * @return Uri
     */
    private Uri whichContentStore() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else {
            return MediaStore.Images.Media.INTERNAL_CONTENT_URI;
        }
    }

    /**
     * Compress bitmap using jpeg, convert to Base64 encoded string, and return to JavaScript.
     *
     * @param bitmap
     */
    public void processPicture(Bitmap bitmap) {
        ByteArrayOutputStream jpeg_data = new ByteArrayOutputStream();
        try {
            if (bitmap.compress(CompressFormat.JPEG, mQuality, jpeg_data)) {
                byte[] code = jpeg_data.toByteArray();
                byte[] output = Base64.encode(code, Base64.NO_WRAP);
                String js_out = new String(output);
                if(mOnCallbackListener != null) {
//                    this.mOnCallbackListener.success(js_out);
                }
                Log.d(CameraLauncher.class.getSimpleName(), "---------->processPicture:js_out");
                js_out = null;
                output = null;
                code = null;
            }
        } catch (Exception e) {
            this.failPicture("Error compressing image.");
        }
        jpeg_data = null;
    }

    /**
     * Send error message to JavaScript.
     *
     * @param err
     */
    public void failPicture(String err) {
        if(mOnCallbackListener != null) {
            this.mOnCallbackListener.error(err);
        }
    }


    public void setOnCallbackListener(OnCallbackListener listener){
        this.mOnCallbackListener = listener;
    }

    public interface OnCallbackListener {


//        public void success(String message);



        public void success(Uri uri);

        /**
         * Helper for error callbacks that just returns the Status.ERROR by default
         *
         * @param message           The message to add to the error result.
         */
        public void error(String message);


    }


    private void scanForGallery(Uri newImage) {
        this.scanMe = newImage;
        if(this.conn != null) {
            this.conn.disconnect();
        }
        this.conn = new MediaScannerConnection(this.mCameraInterface.getSupportActivity().getApplicationContext(), this);
        conn.connect();
    }

    public void onMediaScannerConnected() {
        try{
            this.conn.scanFile(this.scanMe.toString(), "image/*");
        } catch (IllegalStateException e){
            Log.e(LOG_TAG, "Can't scan file in MediaScanner after taking picture");
        }

    }

    public void onScanCompleted(String path, Uri uri) {
        this.conn.disconnect();
    }
}
