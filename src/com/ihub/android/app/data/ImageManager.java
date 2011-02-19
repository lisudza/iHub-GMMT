package com.ihub.android.app.data;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import android.os.Environment;

public class ImageManager {

	private File dir = Environment.getExternalStorageDirectory();
	private File yourFile;
	private static final int IO_BUFFER_SIZE = 512;

	public ImageManager() {

	}

	/**
	 * 
	 * @param filePath
	 * @return
	 */
	public String getProfilePic(String filePath) {
		yourFile = new File(dir, filePath);
		return yourFile.getAbsoluteFile().toString();
	}

	public static byte[] fetchImage(String address)
			throws MalformedURLException, IOException {
		InputStream in = null;
		OutputStream out = null;

		try {
			in = new BufferedInputStream(new URL(address).openStream(),
					IO_BUFFER_SIZE);

			final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
			out = new BufferedOutputStream(dataStream, 4 * 1024);
			copy(in, out);
			out.flush();

			// need to close stream before return statement
			closeStream(in);
			closeStream(out);

			return dataStream.toByteArray();
		} catch (IOException e) {
			// android.util.Log.e("IO", "Could not load buddy icon: " + this,
			// e);

		} finally {
			closeStream(in);
			closeStream(out);

		}
		return null;

	}

	/**
	 * Copy the content of the input stream into the output stream, using a
	 * temporary byte array buffer whose size is defined by
	 * {@link #IO_BUFFER_SIZE}.
	 * 
	 * @param in
	 *            The input stream to copy from.
	 * @param out
	 *            The output stream to copy to.
	 * 
	 * @throws IOException
	 *             If any error occurs during the copy.
	 */
	private static void copy(InputStream in, OutputStream out)
			throws IOException {
		byte[] b = new byte[4 * 1024];
		int read;
		while ((read = in.read(b)) != -1) {
			out.write(b, 0, read);
		}
	}
	
	/**
     * Closes the specified stream.
     *
     * @param stream The stream to close.
     */
    private static void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                android.util.Log.e("IO", "Could not close stream", e);
            }
        }
    } 

	public static File writeImage(byte[] data, String filename) {

		File f = new File("" + filename);
		if (f.exists()) {
			f.delete();
		}
		FileOutputStream fOut;
		try {
			fOut = new FileOutputStream(""+ filename);
			fOut.write(data);
			fOut.flush();
			fOut.close();
			return f;
		} catch (final FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return f;
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return f;
		}
	}

}
