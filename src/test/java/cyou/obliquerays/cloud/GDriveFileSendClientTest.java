/**
 * Copyright (C) 2021 tasekida
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package cyou.obliquerays.cloud;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cyou.obliquerays.io.LocalFileSearch;

/** GDriveFileSendClientTestのUnitTest */
class GDriveFileSendClientTest {
    /** ロガー */
    private static final Logger LOGGER = Logger.getLogger(GDriveFileSendClientTest.class.getName());

	/** @throws java.lang.Exception */
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
    	try (InputStream resource = ClassLoader.getSystemResourceAsStream("logging.properties")) {
            LogManager.getLogManager().readConfiguration(resource);
        } catch (Throwable t) {
        	LOGGER.log(Level.SEVERE, "エラー終了", t);
        }
	}

	/** @throws java.lang.Exception */
	@AfterAll
	static void tearDownAfterClass() throws Exception {}

	/** @throws java.lang.Exception */
	@BeforeEach
	void setUp() throws Exception {}

	/** @throws java.lang.Exception */
	@AfterEach
	void tearDown() throws Exception {}

	/**
	 * {@link cyou.obliquerays.cloud.GDriveFileSendClient#accept(java.nio.file.Path)} のためのテスト・メソッド。
	 * @throws IOException
	 */
	@Test
	void testAccept01() throws IOException {

		LocalFileSearch localFileSearch = new LocalFileSearch();
		Map<Path,List<Path>> localFiles = localFileSearch.search();

		localFiles.entrySet().stream()
			.peek(e -> LOGGER.log(Level.CONFIG, "dir = " + e.getKey().toString()))
			.flatMap(e -> e.getValue().stream())
			.forEach(p -> LOGGER.log(Level.CONFIG, "file = " + p.toString()));
	}
}
