/**
 * Copyright 2014 Murat Knecht
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 */

package org.kamranzafar.jtar;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

/**
 * <p>Convenience functions to unpack a tar.</p>
 * 
 * <p>This is merely a wrapper around some of the low-level functionality of JTar.
 * If there is no method here that does what you want, use the low-level API as.</p>
 * 
 * <p><i>Currently, only unpacking complete archives is supported.</i></p>
 */
public class JTar {
	private static int DEFAULT_BUFFER_SIZE = 4096;

	/**
	 * Unpack the uncompressed tar archive into the target directory!
	 * 
	 * @param pathToTar
	 *            path to the tar archive file. Valid is whatever
	 *            <code>new File()</code> accepts.
	 * @param pathToTargetDir
	 *            path to the directory into which to unpack the archives
	 *            contents to. Valid is whatever <code>new File()</code>
	 *            accepts.
	 * @throws IllegalArgumentException
	 *             if the archive file is not a readable file
	 * @throws IOException
	 * @see {@link #unpackTar(File, File)}
	 */
	public void unpackTar(String pathToTar, String pathToTargetDir)
			throws IOException {
		unpackTarUsingFiles( //
				checkAndReturnArchiveFile(pathToTar), //
				new File(pathToTargetDir));
	}

	/**
	 * Unpack the uncompressed tar archive into the target directory!
	 * 
	 * @param tar
	 *            the archive file to unpack.
	 * @param targetDir
	 *            the directory into which to unpack the archive.
	 * @throws IOException
	 */
	public void unpackTar(File tar, File targetDir) throws IOException {
		unpackTarUsingFiles(tar, targetDir);
	}

	/**
	 * Unpack the uncompressed tar archive into the target directory!
	 * 
	 * @param inputStream
	 *            the archive to unpack.
	 * @param pathToTargetDir
	 *            path to the directory into which to unpack the archives
	 *            contents to. Valid is whatever <code>new File()</code>
	 *            accepts.
	 * @throws IOException
	 * @see {@link #unpackTar(File, File)}
	 */
	public void unpackTarFromStream(InputStream inputStream,
			String pathToTargetDir) throws IOException {
		unpackFromStream( //
				new Buffering( //
						new UseStream(inputStream)), //
				new File(pathToTargetDir));
	}

	/**
	 * Unpack the gzip-compressed tar archive into the target directory!
	 * 
	 * @param pathToTar
	 *            path to the tar archive file. Valid is whatever
	 *            <code>new File()</code> accepts.
	 * @param pathToTargetDir
	 *            path to the directory into which to unpack the archives
	 *            contents to. Valid is whatever <code>new File()</code>
	 *            accepts.
	 * @throws IllegalArgumentException
	 *             if the archive file is not a readable file
	 * @throws IOException
	 * @see {@link #unpackTarGz(File, File)}
	 */
	public void unpackTarGz(String pathToTarGz, String pathToTargetDir)
			throws IOException {
		unpackTarGzUsingFiles( //
				checkAndReturnArchiveFile(pathToTarGz), //
				new File(pathToTargetDir));
	}

	/**
	 * Unpack the gzip-compressed tar archive into the target directory!
	 * 
	 * @param tarGz
	 *            the archive file to unpack.
	 * @param targetDir
	 *            the directory into which to unpack the archive.
	 * @throws IOException
	 */
	public void unpackTarGz(File tarGz, File targetDir) throws IOException {
		unpackTarGzUsingFiles(tarGz, targetDir);
	}

	/**
	 * Unpack the gzip-compressed tar archive into the target directory!
	 * 
	 * @param inputStream
	 *            the archive to unpack.
	 * @param pathToTargetDir
	 *            path to the directory into which to unpack the archives
	 *            contents to. Valid is whatever <code>new File()</code>
	 *            accepts.
	 * @throws IOException
	 * @see {@link #unpackTarFromStream(InputStream, String)} for the
	 *      uncompressed variant
	 */
	public void unpackTarGzFromStream(InputStream inputStream,
			String pathToTargetDir) throws IOException {
		unpackFromStream( //
				new Buffering( //
						new GUnzipping( //
								new UseStream(inputStream))), //
				new File(pathToTargetDir));
	}

	private void unpackTarUsingFiles(File tar, File targetDir)
			throws IOException {
		unpackFromStream( //
				new Buffering( //
						new FileInputStreamFromFile(tar) //
				), targetDir);
	}

	private void unpackTarGzUsingFiles(File tarGz, File targetDir)
			throws IOException {
		unpackFromStream( //
				new Buffering( //
						new GUnzipping(new FileInputStreamFromFile(tarGz) //
						)), targetDir);
	}

	private File checkAndReturnArchiveFile(String pathToTar) {
		File file = new File(pathToTar);
		if (!file.exists()) {
			throw new IllegalArgumentException(
					"Tar does not exist and cannot be unpacked: "
							+ file.getAbsolutePath());
		}
		if (!file.isFile()) {
			throw new IllegalArgumentException("Can only unpack files: "
					+ file.getAbsolutePath());
		}
		if (!file.canRead()) {
			throw new IllegalArgumentException(
					"Cannot read tar for unpacking: " + file.getAbsolutePath());
		}
		return file;
	}

	private void unpackFromStream(IInputStreamFactory streamFactory,
			File targetDir) throws IOException {
		targetDir.mkdirs();
		TarInputStream tis = null;
		try {
			tis = new TarInputStream(streamFactory.getStream());
			extractAll(tis, targetDir);
		} finally {
			if (tis != null) {
				tis.close();
			}
		}
	}

	public static interface IInputStreamFactory {
		InputStream getStream() throws IOException;
	}

	public static class FileInputStreamFromFile implements IInputStreamFactory {
		File file;

		public FileInputStreamFromFile(File file) {
			this.file = file;
		}

		@Override
		public InputStream getStream() throws IOException {
			return new FileInputStream(this.file);
		}
	}

	public static class GUnzipping implements IInputStreamFactory {
		IInputStreamFactory factory;

		public GUnzipping(IInputStreamFactory factory) {
			this.factory = factory;
		}

		@Override
		public InputStream getStream() throws IOException {
			return new GZIPInputStream(this.factory.getStream());
		}
	}

	public static class Buffering implements IInputStreamFactory {
		IInputStreamFactory factory;

		public Buffering(IInputStreamFactory factory) {
			this.factory = factory;
		}

		@Override
		public InputStream getStream() throws IOException {
			return new BufferedInputStream(factory.getStream());
		}
	}

	public static class UseStream implements IInputStreamFactory {
		InputStream is;

		public UseStream(InputStream is) {
			this.is = is;
		}

		@Override
		public InputStream getStream() throws IOException {
			return is;
		}
	}

	private void extractAll(TarInputStream tis, File destFolder)
			throws IOException {
		BufferedOutputStream dest = null;

		TarEntry entry;
		while ((entry = tis.getNextEntry()) != null) {
			int count;
			byte data[] = new byte[DEFAULT_BUFFER_SIZE];

			if (entry.isDirectory()) {
				new File(destFolder + "/" + entry.getName()).mkdirs();
				continue;
			} else {
				int di = entry.getName().lastIndexOf('/');
				if (di != -1) {
					new File(destFolder + "/"
							+ entry.getName().substring(0, di)).mkdirs();
				}
			}

			FileOutputStream fos = new FileOutputStream(destFolder + "/"
					+ entry.getName());
			dest = new BufferedOutputStream(fos);

			while ((count = tis.read(data)) != -1) {
				dest.write(data, 0, count);
			}

			dest.flush();
			dest.close();
		}
	}

}
