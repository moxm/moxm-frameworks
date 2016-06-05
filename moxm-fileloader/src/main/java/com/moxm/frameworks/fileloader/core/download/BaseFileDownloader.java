package com.moxm.frameworks.fileloader.core.download;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import com.moxm.frameworks.fileloader.core.assist.ContentLengthInputStream;
import com.moxm.frameworks.fileloader.utils.IoUtils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Provides retrieving of {@link InputStream} of image by URI from network or file system or app resources.<br />
 * {@link URLConnection} is used to retrieve image stream from network.
 *
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 * @since 1.8.0
 */
public class BaseFileDownloader implements FileDownloader {
	/** {@value} */
	public static final int DEFAULT_HTTP_CONNECT_TIMEOUT = 5 * 1000; // milliseconds
	/** {@value} */
	public static final int DEFAULT_HTTP_READ_TIMEOUT = 20 * 1000; // milliseconds

	/** {@value} */
	protected static final int BUFFER_SIZE = 32 * 1024; // 32 Kb
	/** {@value} */
	protected static final String ALLOWED_URI_CHARS = "@#&=*+-_.,:!?()/~'%";

	protected static final int MAX_REDIRECT_COUNT = 5;

	protected static final String CONTENT_CONTACTS_URI_PREFIX = "content://com.android.contacts/";

	private static final String ERROR_UNSUPPORTED_SCHEME = "UIL doesn't support scheme(protocol) by default [%s]. " + "You should implement this support yourself (BaseImageDownloader.getStreamFromOtherSource(...))";

	protected final Context context;
	protected final int connectTimeout;
	protected final int readTimeout;

	public BaseFileDownloader(Context context) {
		this(context, DEFAULT_HTTP_CONNECT_TIMEOUT, DEFAULT_HTTP_READ_TIMEOUT);
	}

	public BaseFileDownloader(Context context, int connectTimeout, int readTimeout) {
		this.context = context.getApplicationContext();
		this.connectTimeout = connectTimeout;
		this.readTimeout = readTimeout;
	}

	@Override
	public InputStream getStream(String fileUri, Object extra) throws IOException {
		switch (Scheme.ofUri(fileUri)) {
			case HTTP:
			case HTTPS:
				return getStreamFromNetwork(fileUri, extra);
			case FILE:
				return getStreamFromFile(fileUri, extra);
			case CONTENT:
				return getStreamFromContent(fileUri, extra);
			case ASSETS:
				return getStreamFromAssets(fileUri, extra);
			case DRAWABLE:
				return getStreamFromDrawable(fileUri, extra);
			case UNKNOWN:
			default:
				return getStreamFromOtherSource(fileUri, extra);
		}
	}

	/**
	 * Retrieves {@link InputStream} of image by URI (image is located in the network).
	 *
	 * @param fileUri Image URI
	 * @param extra
	 */
	protected InputStream getStreamFromNetwork(String fileUri, Object extra) throws IOException {
		HttpURLConnection conn = createConnection(fileUri, extra);

		int redirectCount = 0;
		while (conn.getResponseCode() / 100 == 3 && redirectCount < MAX_REDIRECT_COUNT) {
			conn = createConnection(conn.getHeaderField("Location"), extra);
			redirectCount++;
		}

		InputStream inputStream;
		try {
			inputStream = conn.getInputStream();
		} catch (IOException e) {
			// Read all data to allow reuse connection (http://bit.ly/1ad35PY)
			IoUtils.readAndCloseStream(conn.getErrorStream());
			throw e;
		}
		if (!shouldBeProcessed(conn)) {
			IoUtils.closeSilently(inputStream);
			throw new IOException("Image request failed with response code " + conn.getResponseCode());
		}

		return new ContentLengthInputStream(new BufferedInputStream(inputStream, BUFFER_SIZE), conn.getContentLength());
	}

	/**
	 * @param conn Opened request connection (response code is available)
	 * @return <b>true</b> - if data from connection is correct and should be read and processed;
	 *         <b>false</b> - if response contains irrelevant data and shouldn't be processed
	 * @throws IOException
	 */
	protected boolean shouldBeProcessed(HttpURLConnection conn) throws IOException {
		return conn.getResponseCode() == 200;
	}

	/**
	 * Create {@linkplain HttpURLConnection HTTP connection} for incoming URL
	 *
	 * @param url   URL to connect to
	 * @param extra
	 * @return {@linkplain HttpURLConnection Connection} for incoming URL. Connection isn't established so it still configurable.
	 * @throws IOException if some I/O error occurs during network request or if no InputStream could be created for
	 *                     URL.
	 */
	protected HttpURLConnection createConnection(String url, Object extra) throws IOException {
		String encodedUrl = Uri.encode(url, ALLOWED_URI_CHARS);
		HttpURLConnection conn = (HttpURLConnection) new URL(encodedUrl).openConnection();
		conn.setConnectTimeout(connectTimeout);
		conn.setReadTimeout(readTimeout);
		return conn;
	}

	/**
	 * Retrieves {@link InputStream} of image by URI (image is located on the local file system or SD card).
	 *
	 * @param fileUri Image URI
	 * @param extra
	 * @return {@link InputStream} of image
	 * @throws IOException if some I/O error occurs reading from file system
	 */
	protected InputStream getStreamFromFile(String fileUri, Object extra) throws IOException {
		String filePath = Scheme.FILE.crop(fileUri);
		if (isVideoFileUri(fileUri)) {
			return getVideoThumbnailStream(filePath);
		} else {
			BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(filePath), BUFFER_SIZE);
			return new ContentLengthInputStream(inputStream, (int) new File(filePath).length());
		}
	}

	@TargetApi(Build.VERSION_CODES.FROYO)
	private InputStream getVideoThumbnailStream(String filePath) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
			Bitmap bitmap = ThumbnailUtils
					.createVideoThumbnail(filePath, MediaStore.Images.Thumbnails.FULL_SCREEN_KIND);
			if (bitmap != null) {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				bitmap.compress(CompressFormat.PNG, 0, bos);
				return new ByteArrayInputStream(bos.toByteArray());
			}
		}
		return null;
	}

	/**
	 * Retrieves {@link InputStream} of image by URI (image is accessed using {@link ContentResolver}).
	 *
	 * @param fileUri Image URI
	 * @param extra
	 * @return {@link InputStream} of image
	 * @throws FileNotFoundException if the provided URI could not be opened
	 */
	protected InputStream getStreamFromContent(String fileUri, Object extra) throws FileNotFoundException {
		ContentResolver res = context.getContentResolver();

		Uri uri = Uri.parse(fileUri);
		if (isVideoContentUri(uri)) { // video thumbnail
			Long origId = Long.valueOf(uri.getLastPathSegment());
			Bitmap bitmap = MediaStore.Video.Thumbnails
					.getThumbnail(res, origId, MediaStore.Images.Thumbnails.MINI_KIND, null);
			if (bitmap != null) {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				bitmap.compress(CompressFormat.PNG, 0, bos);
				return new ByteArrayInputStream(bos.toByteArray());
			}
		} else if (fileUri.startsWith(CONTENT_CONTACTS_URI_PREFIX)) { // contacts photo
			return getContactPhotoStream(uri);
		}

		return res.openInputStream(uri);
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	protected InputStream getContactPhotoStream(Uri uri) {
		ContentResolver res = context.getContentResolver();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			return ContactsContract.Contacts.openContactPhotoInputStream(res, uri, true);
		} else {
			return ContactsContract.Contacts.openContactPhotoInputStream(res, uri);
		}
	}

	/**
	 * Retrieves {@link InputStream} of image by URI (image is located in assets of application).
	 *
	 * @param fileUri Image URI
	 * @param extra
	 * @return {@link InputStream} of image
	 * @throws IOException if some I/O error occurs file reading
	 */
	protected InputStream getStreamFromAssets(String fileUri, Object extra) throws IOException {
		String filePath = Scheme.ASSETS.crop(fileUri);
		return context.getAssets().open(filePath);
	}

	/**
	 * Retrieves {@link InputStream} of image by URI (image is located in drawable resources of application).
	 *
	 * @param fileUri Image URI
	 * @param extra
	 * @return {@link InputStream} of image
	 */
	protected InputStream getStreamFromDrawable(String fileUri, Object extra) {
		String drawableIdString = Scheme.DRAWABLE.crop(fileUri);
		int drawableId = Integer.parseInt(drawableIdString);
		return context.getResources().openRawResource(drawableId);
	}

	/**
	 * Retrieves {@link InputStream} of image by URI from other source with unsupported scheme. Should be overriden by
	 * successors to implement image downloading from special sources.<br />
	 * This method is called only if image URI has unsupported scheme. Throws {@link UnsupportedOperationException} by
	 * default.
	 *
	 * @param fileUri Image URI
	 * @param extra
	 * @return {@link InputStream} of image
	 * @throws IOException                   if some I/O error occurs
	 * @throws UnsupportedOperationException if image URI has unsupported scheme(protocol)
	 */
	protected InputStream getStreamFromOtherSource(String fileUri, Object extra) throws IOException {
		throw new UnsupportedOperationException(String.format(ERROR_UNSUPPORTED_SCHEME, fileUri));
	}

	private boolean isVideoContentUri(Uri uri) {
		String mimeType = context.getContentResolver().getType(uri);
		return mimeType != null && mimeType.startsWith("video/");
	}

	private boolean isVideoFileUri(String uri) {
		String extension = MimeTypeMap.getFileExtensionFromUrl(uri);
		String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
		return mimeType != null && mimeType.startsWith("video/");
	}
}
