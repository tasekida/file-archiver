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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.api.client.auth.oauth2.StoredCredential;

import cyou.obliquerays.cloud.http.GoogleOAuth2AccessTokenBodyHandler;
import cyou.obliquerays.cloud.http.GoogleOAuth2AccessTokenRequest;
import cyou.obliquerays.config.RadioProperties;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;

/** GoogleOAuth2AccessTokenのUnitTest */
class GoogleOAuth2AccessTokenTest {
    /** ロガー */
    private static final Logger LOGGER = Logger.getLogger(GoogleOAuth2AccessTokenTest.class.getName());

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
	void setUp() throws Exception {	}

	/** @throws java.lang.Exception */
	@AfterEach
	void tearDown() throws Exception {}

	/**
	 * {@link cyou.obliquerays.cloud.GoogleOAuth2AccessToken#apply(java.lang.String)} のためのテスト・メソッド。
	 */
	@Test
	void testApply() {
		GoogleOAuth2AccessToken goat = GoogleOAuth2AccessToken.getInstance();

		String strToken = goat.get();

		LOGGER.log(Level.INFO, strToken);
	}

	/**
	 * {@link cyou.obliquerays.cloud.GoogleOAuth2AccessToken#apply(java.lang.String)} のためのテスト・メソッド。
	 */
	@Test
	void testExample() {
		StoredCredential storedObj = null;
		try (InputStream inStore = ClassLoader.getSystemResourceAsStream(RadioProperties.getProperties().getProperty("google.credentials.stored"));
				ObjectInputStream objectInStore = new ObjectInputStream(new ByteArrayInputStream(inStore.readAllBytes()))) {

			@SuppressWarnings("unchecked")
			Map<String, byte[]> stored = (Map<String, byte[]>) objectInStore.readObject();
			storedObj = (StoredCredential) new ObjectInputStream(new ByteArrayInputStream(stored.get("user"))).readObject();
		} catch (Exception e) {

			throw new IllegalStateException(e);
		} finally {

			LOGGER.log(Level.CONFIG, storedObj.toString());
			Objects.requireNonNull(storedObj);
		}

		String accessToken = null;
		try (InputStream inJson = ClassLoader.getSystemResourceAsStream(RadioProperties.getProperties().getProperty("google.credentials.json"));
				Jsonb jsonb = JsonbBuilder.create(new JsonbConfig().withFormatting(false))) {

			LocalDateTime expiration =
					LocalDateTime.ofInstant(Instant.ofEpochMilli(storedObj.getExpirationTimeMilliseconds()), ZoneId.systemDefault());

			if (expiration.isBefore(LocalDateTime.now())) {
				@SuppressWarnings("unchecked")
				Map<String, Object> credentials = jsonb.fromJson(inJson, Map.class);
				@SuppressWarnings("unchecked")
				Map<String, Object> installed = (Map<String, Object>) credentials.get("installed");

				HttpRequest refreshRequest = new GoogleOAuth2AccessTokenRequest(storedObj.getRefreshToken(), installed.get("client_id").toString(), installed.get("client_secret").toString());

				HttpClient client = HttpClient.newBuilder()
		                .version(Version.HTTP_2)
		                .followRedirects(Redirect.NORMAL)
		                .connectTimeout(Duration.ofSeconds(30))
		                .proxy(HttpClient.Builder.NO_PROXY)
		                .build();

				HttpResponse<Map<String, String>> refreshResponse = client.send(refreshRequest, new GoogleOAuth2AccessTokenBodyHandler());

				accessToken = refreshResponse.body().get("access_token");

			} else {

				accessToken = storedObj.getAccessToken();

			}
		} catch (Exception e) {

			throw new IllegalStateException(e);
		} finally {

			LOGGER.log(Level.CONFIG, accessToken);
			Objects.requireNonNull(accessToken);
		}

		LOGGER.log(Level.INFO, accessToken);
	}
}
