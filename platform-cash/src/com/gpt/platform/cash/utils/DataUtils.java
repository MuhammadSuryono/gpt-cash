package com.gpt.platform.cash.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class DataUtils implements Serializable {
	public static byte[] writeObject(Object object) throws IOException {
		OutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream objectOut = new ObjectOutputStream(new BufferedOutputStream(out));
		objectOut.writeObject(object);
		objectOut.flush();
		byte[] data = ((ByteArrayOutputStream) out).toByteArray();
		objectOut.close();
		out.close();
		return data;
	}

	public static Object readObject(byte[] data) throws IOException, ClassNotFoundException {
		InputStream in = new ByteArrayInputStream(data);
		ObjectInputStream objectIn = new ObjectInputStream(new BufferedInputStream(in));
		Object object = objectIn.readObject();
		objectIn.close();
		in.close();
		return object;
	}

	public static byte[] readBytes(InputStream in, int n) throws IOException {
		byte[] ba = new byte[n];

		int i = 0;
		do {
			int read;
			if ((read = in.read()) <= 0)
				break;
			ba[(i++)] = (byte) read;
		} while (i < n);

		if (i < n) {
			return null;
		}
		return ba;
	}

	public static byte[] readBytes(InputStream in) throws IOException {
		byte[] ba = new byte[1000];
		ByteArrayOutputStream bos = new ByteArrayOutputStream(2000);

		int readBytes;
		while ((readBytes = in.read(ba)) != -1) {
			bos.write(ba, 0, readBytes);
		}
		return bos.toByteArray();
	}

	public static String readString(InputStream in, int n) throws IOException {
		return new String(readBytes(in, n));
	}

	public static void writeBytes(OutputStream out, byte[] data) throws IOException {
		out.write(data);
	}

	public static void writeString(OutputStream out, String data) throws IOException {
		writeBytes(out, data.getBytes());
	}

	public static byte[] compress(byte[] data) throws IOException {
		OutputStream out = new ByteArrayOutputStream();
		ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(out));
		zipOut.putNextEntry(new ZipEntry("main"));
		zipOut.write(data);
		zipOut.closeEntry();
		zipOut.flush();
		byte[] zipData = ((ByteArrayOutputStream) out).toByteArray();
		zipOut.close();
		out.close();
		return zipData;
	}

	public static byte[] decompress(byte[] data) throws IOException {
		InputStream in = new ByteArrayInputStream(data);
		ZipInputStream zipIn = new ZipInputStream(new BufferedInputStream(in));
		zipIn.getNextEntry();
		byte[] byteBuffer = readBytes(zipIn);
		zipIn.closeEntry();
		zipIn.close();
		in.close();
		return byteBuffer;
	}
}