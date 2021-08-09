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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import cyou.obliquerays.cloud.pojo.GDriveResource;
import cyou.obliquerays.config.RadioProperties;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;

/** GoogleDriveFileSearchTestのUnitTest */
class GoogleDriveFileSearchTest {
    /** ロガー */
    private static final Logger LOGGER = Logger.getLogger(GoogleDriveFileSearchTest.class.getName());

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
	 * {@link cyou.obliquerays.cloud.GoogleDriveFileSearch#apply(java.lang.String)} のためのテスト・メソッド。
	 */
	@Test
	void testJwtExample01() {
		GoogleJsonWebToken jwt = GoogleJsonWebToken.getInstance();
		GoogleJWTAccessToken gtoken = GoogleJWTAccessToken.getInstance();
		GoogleDriveFileSearch gDriveFileSearch = GoogleDriveFileSearch.getInstance();

		String strToken = gtoken.apply(jwt.get());
		gDriveFileSearch.apply(strToken);

		LOGGER.log(Level.INFO, strToken);
	}

	/**
	 * {@link cyou.obliquerays.cloud.GoogleDriveFileSearch#apply(java.lang.String)} のためのテスト・メソッド。
	 * @throws Exception
	 */
	@Test
	void testJwtExample02() throws Exception {

		try(InputStream in = ClassLoader.getSystemResourceAsStream(RadioProperties.getProperties().getProperty("service.accounts.json"))) {
			GoogleCredential credential = GoogleCredential.fromStream(in);
			PrivateKey privateKey = credential.getServiceAccountPrivateKey();
			String privateKeyId = credential.getServiceAccountPrivateKeyId();

			Instant now = Instant.now();

		    Algorithm algorithm = Algorithm.RSA256(null, (RSAPrivateKey) privateKey);
		    String signedJwt = JWT.create()
		        .withKeyId(privateKeyId)
		        .withIssuer(RadioProperties.getProperties().getProperty("service.accounts.issuer"))
		        .withSubject(RadioProperties.getProperties().getProperty("service.accounts.issuer"))
		        .withAudience("https://oauth2.googleapis.com/token")
		        .withIssuedAt(Date.from(now))
		        .withExpiresAt(Date.from(now.plusSeconds(30L)))
		        .sign(algorithm);

		    GoogleJWTAccessToken gtoken = GoogleJWTAccessToken.getInstance();
		    String strToken = gtoken.apply(signedJwt);

			HttpClient client =	HttpClient.newBuilder()
	                .version(Version.HTTP_2)
	                .followRedirects(Redirect.NORMAL)
	                .connectTimeout(Duration.ofSeconds(30))
	                .proxy(HttpClient.Builder.NO_PROXY)
	                .build();
	        HttpRequest request = HttpRequest.newBuilder()
	                .uri(URI.create("https://www.googleapis.com/drive/v3/files?fields=nextPageToken,%20files(id,%20name)"))
	                .timeout(Duration.ofMinutes(30))
	                .header("Accept-Encoding", "gzip")
	                .header("Authorization", "Bearer " + strToken)
	                .GET()
	                .build();
	        LOGGER.log(Level.INFO, request.toString());

	        HttpResponse<String> response = client.send(request, BodyHandlers.ofString(StandardCharsets.UTF_8));

	        LOGGER.log(Level.INFO, response.body());

		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "JWTトークン取得エラー", e);
			throw e;
		}
	}

	/**
	 * {@link cyou.obliquerays.cloud.GoogleDriveFileSearch#apply(java.lang.String)} のためのテスト・メソッド。
	 * @throws Exception
	 */
	@Test
	void testOAuth2Example01() throws Exception {
		String APPLICATION_NAME = "Google Drive API Java Quickstart";

	    JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	    String TOKENS_DIRECTORY_PATH = "tokens";

	    List<String> SCOPES = List.of(DriveScopes.DRIVE_APPDATA, DriveScopes.DRIVE_FILE, DriveScopes.DRIVE_METADATA, DriveScopes.DRIVE);
	    String CREDENTIALS_FILE_PATH = "/credentials.json";

        NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        InputStream in = GoogleDriveFileSearchTest.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        // Print the names and IDs for up to 10 files.
        FileList result = service.files().list()
                .setPageSize(10)
                .setFields("nextPageToken, files(id, name)")
                .execute();
        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
        } else {
            System.out.println("Files:");
            for (File file : files) {
                System.out.printf("%s (%s)\n", file.getName(), file.getId());
            }
        }

		LOGGER.log(Level.INFO, credential.getAccessToken());
	}

	/**
	 * {@link cyou.obliquerays.cloud.GoogleDriveFileSearch#apply(java.lang.String)} のためのテスト・メソッド。
	 * @throws Exception
	 */
	@Test
	void testOAuth2Example02() throws Exception {

		GoogleOAuth2AccessToken goat = GoogleOAuth2AccessToken.getInstance();
		GoogleDriveFileSearch gDriveFileSearch = GoogleDriveFileSearch.getInstance();

		String strToken = goat.get();

		List<GDriveResource> files = gDriveFileSearch.apply(strToken);

		files.forEach(s -> LOGGER.log(Level.INFO, s.toString()));
	}

	/**
	 * {@link cyou.obliquerays.cloud.GoogleDriveFileSearch#apply(java.lang.String)} のためのテスト・メソッド。
	 * @throws Exception
	 */
	@Test
	void testOAuth2Example03() {

		try (InputStream inStore = ClassLoader.getSystemResourceAsStream(RadioProperties.getProperties().getProperty("google.credentials.stored"));
				ObjectInputStream objectInStore = new ObjectInputStream(new ByteArrayInputStream(inStore.readAllBytes()));
				InputStream inJson = ClassLoader.getSystemResourceAsStream(RadioProperties.getProperties().getProperty("google.credentials.json"));
				Jsonb jsonb = JsonbBuilder.create(new JsonbConfig().withFormatting(false))) {
			String accessToken = "";

			Map<String, byte[]> stored = (Map<String, byte[]>) objectInStore.readObject();

			StoredCredential storedObj = (StoredCredential) new ObjectInputStream(new ByteArrayInputStream(stored.get("user"))).readObject();

			LocalDateTime expiration =
					LocalDateTime.ofInstant(Instant.ofEpochMilli(storedObj.getExpirationTimeMilliseconds()), ZoneId.systemDefault());
			if (expiration.isBefore(LocalDateTime.now())) {
				@SuppressWarnings("unchecked")
				Map<String, Object> credentials = jsonb.fromJson(inJson, Map.class);
				@SuppressWarnings("unchecked")
				Map<String, Object> installed = (Map<String, Object>) credentials.get("installed");

				HttpClient client1 = HttpClient.newBuilder()
						.version(Version.HTTP_2).followRedirects(Redirect.NORMAL)
						.connectTimeout(Duration.ofSeconds(30)).proxy(HttpClient.Builder.NO_PROXY).build();

				StringBuilder strParam1 = new StringBuilder("")
						.append("grant_type").append("=").append("refresh_token")
						.append("&").append("refresh_token").append("=").append(storedObj.getRefreshToken())
						.append("&").append("client_id").append("=").append(installed.get("client_id").toString())
						.append("&").append("client_secret").append("=").append(installed.get("client_secret").toString());

				HttpRequest request1 = HttpRequest.newBuilder()
		                .uri(URI.create("https://oauth2.googleapis.com/token"))
		                .timeout(Duration.ofMinutes(30))
		                .header("Accept-Encoding", "gzip")
		                .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
		                .POST(BodyPublishers.ofString(strParam1.toString()))
		                .build();

				HttpResponse<InputStream> response = client1.send(request1, BodyHandlers.ofInputStream());

				InputStream gin = new GZIPInputStream(response.body());
				@SuppressWarnings("unchecked")
				Map<String, Object> tokenResponse = jsonb.fromJson(gin, Map.class);

				accessToken = tokenResponse.get("access_token").toString();

			} else {

				accessToken = storedObj.getAccessToken();

			}
			LOGGER.log(Level.INFO, accessToken);

			GoogleDriveFileSearch gDriveFileSearch = GoogleDriveFileSearch.getInstance();
			gDriveFileSearch.apply(accessToken);

		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (Exception e1) {
			// TODO 自動生成された catch ブロック
			e1.printStackTrace();
		}

//		LOGGER.log(Level.INFO, strToken);
	}
}
