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

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** GoogleAccessTokenのUnitTest */
class GoogleAccessTokenTest {
    /** ロガー */
    private static final Logger LOGGER = Logger.getLogger(GoogleJsonWebTokenTest.class.getName());

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
	static void tearDownAfterClass() throws Exception {
	}

	/** @throws java.lang.Exception */
	@BeforeEach
	void setUp() throws Exception {
	}

	/** @throws java.lang.Exception */
	@AfterEach
	void tearDown() throws Exception {
	}

	/**
	 * {@link cyou.obliquerays.cloud.GoogleAccessToken#apply(java.lang.String)} のためのテスト・メソッド。
	 */
	@Test
	void testApply() {
		GoogleJsonWebToken jwt = GoogleJsonWebToken.getInstance();
		GoogleAccessToken gtoken = GoogleAccessToken.getInstance();

		String strToken = gtoken.apply(jwt.get());

		LOGGER.log(Level.INFO, strToken);
	}

}
