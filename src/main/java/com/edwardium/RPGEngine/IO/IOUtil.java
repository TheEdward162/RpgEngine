package com.edwardium.RPGEngine.IO;

import org.lwjgl.BufferUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class IOUtil {
	private IOUtil() {

	}

	public static ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
		ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCapacity);
		buffer.flip();
		newBuffer.put(buffer);
		return newBuffer;
	}

	/**
	 * Reads the specified resource and returns the raw data as a ByteBuffer.
	 *
	 * @param path   the resource to read
	 * @param bufferSize the initial buffer size
	 *
	 * @return the resource data
	 */
	public static ByteBuffer pathToByteBuffer(String path, int bufferSize) {
		ByteBuffer buffer = null;
		File file = new File(path);

		if (file.isFile()) {
			FileInputStream fis;
			try {
				fis = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return null;
			}

			FileChannel fc = fis.getChannel();
			try {
				buffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
				fc.close();
				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return buffer;
	}
}
