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
import java.util.zip.GZIPInputStream;

public class JTar {
	private static int DEFAULT_BUFFER_SIZE = 4096;

	public void unpackTar(String pathToTar, String pathToTargetDir) throws IOException {
		unpackFromStream(checkAndReturnFile(pathToTar), new File(pathToTargetDir), new InputStreamForTar());
	}

	private File checkAndReturnFile(String pathToTar) {
		File file = new File(pathToTar);
		if (!file.exists()) {
			throw new IllegalArgumentException("Tar does not exist and cannot be unpacked: " + file.getAbsolutePath());
		}
		if (!file.isFile()) {
			throw new IllegalArgumentException("Can only unpack files: " + file.getAbsolutePath());
		}
		if (!file.canRead()) {
			throw new IllegalArgumentException("Cannot read tar for unpacking: " + file.getAbsolutePath());
		}
		return file;
	}
	
	public void unpackTarGz(String pathToTarGz, String pathToTargetDir) throws IOException {
		unpackFromStream(checkAndReturnFile(pathToTarGz), new File(pathToTargetDir), new InputStreamForTarGz());
	}
	
	private void unpackFromStream(File tar, File targetDir, IInputStreamCreator streamCreator) throws IOException {
		targetDir.mkdirs();
		TarInputStream tis = null;
		try {
			tis = streamCreator.getStream(tar);
			extractAll(tis, targetDir);
		} finally {
			if (tis != null) {
				tis.close();
			}
		}
	}

	interface IInputStreamCreator {
		TarInputStream getStream(File tar) throws IOException;
	}
	
	class InputStreamForTar implements IInputStreamCreator {
		@Override
		public TarInputStream getStream(File tar) throws IOException {
			return new TarInputStream(new BufferedInputStream(
					new FileInputStream(tar)));
		}
	}
	
	class InputStreamForTarGz implements IInputStreamCreator {
		@Override
		public TarInputStream getStream(File tar) throws IOException {
			return new TarInputStream(new BufferedInputStream(
					new GZIPInputStream(new FileInputStream(tar))));
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
