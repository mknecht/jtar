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
 */package org.kamranzafar.jtar;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

public class JTarTest {

	private static final String CONTENT3 = "KG889vdgjPHQXUEXCqrr";
	private static final String CONTENT2 = "gTzyuQjfhrnyX9cTBSy";
	private static final String CONTENT1 = "HPeX2kD5kSTc7pzCDX";
	private static final String PATH_TO_TEST_TAR = "src/test/resources/tartest.tar";
	private static final String PATH_TO_TEST_TAR_GZ = "src/test/resources/tartest.tar.gz";

	private File sourceDir;
	private File targetDir;

	@Before
	public void setup() throws IOException {
		sourceDir = new File(
				Files.createTempDirectory("tartest").toFile(),
				"source"
				);
		sourceDir.mkdirs();
		targetDir = Files.createTempDirectory("tartest").toFile();
		targetDir.mkdirs();
	}

	@Test
	public void givenDir_whenUnpackingTar_thenTarContentsAreThere()
			throws IOException {
		new JTar().unpackTar(PATH_TO_TEST_TAR, targetDir.getAbsolutePath());
		assertThat(new File(targetDir, "tartest"),
				containsExactlyFiles("one", "two", "four", "five", "six"));
	}

	@Test
	public void givenListOfFiles_whenUnpackingTar_thenTarContentsAreThere()
			throws IOException {
		new JTar().unpackTar(PATH_TO_TEST_TAR, targetDir.getAbsolutePath());
		assertThat(new File(targetDir, "tartest"),
				containsExactlyFiles("one", "two", "four", "five", "six"));
	}

	@Test
	public void givenListOfFiles_whenUnpackingTarGz_thenTarContentsAreThere()
			throws IOException {
		new JTar().unpackTarGz(PATH_TO_TEST_TAR_GZ, targetDir.getAbsolutePath());
		assertThat(new File(targetDir, "tartest"),
				containsExactlyFiles("one", "two", "four", "five", "six"));
	}

	private Matcher<File> containsExactlyFiles(final String... filenames) {
		return new BaseMatcher<File>() {

			@Override
			public boolean matches(Object dirObject) {
				File dir = (File) dirObject;
				for (String wantedFilename : filenames) {
					if (!findFileInDirectory(wantedFilename, dir)) {
						return false;
					}
				}
				return true;
			}

			private boolean findFileInDirectory(String wantedFilename, File dir) {
				for (String foundFilename : dir.list()) {
					if (foundFilename.equals(wantedFilename)) {
						return true;
					}
				}
				return false;
			}

			@Override
			public void describeTo(Description description) {
				description
						.appendText("Directory contains exactly the files/directories "
								+ filenames.toString());
			}
		};
	}
}
