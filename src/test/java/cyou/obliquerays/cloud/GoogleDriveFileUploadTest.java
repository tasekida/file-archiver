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

import static org.junit.jupiter.api.Assertions.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

import cyou.obliquerays.cloud.pojo.GDriveFile;
import cyou.obliquerays.config.RadioProperties;

/** GoogleDriveFileUploadTestのUnitTest */
class GoogleDriveFileUploadTest {
    /** ロガー */
    private static final Logger LOGGER = Logger.getLogger(GoogleDriveFileUploadTest.class.getName());

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

	@Test
	void testApply001() {

		GoogleOAuth2AccessToken goat = GoogleOAuth2AccessToken.getInstance();
		GoogleDriveFileUpload gDriveFileUpload = GoogleDriveFileUpload.getInstance();

		String strToken = goat.get();

		Path basePath = Path.of(RadioProperties.getProperties().getBaseDir(), "english0-20210529.mp3");
//		Path basePath = Path.of(RadioProperties.getProperties().getBaseDir(), "20210526");

		GDriveFile gDriveFile = new GDriveFile();
		gDriveFile.setPath(basePath);
		gDriveFile.setName(basePath.getFileName().toString());

		boolean retFlg = gDriveFileUpload.test(strToken, gDriveFile);

		assertTrue(retFlg);
	}

	@Test
	void testUpload001() throws GeneralSecurityException, IOException {
		String APPLICATION_NAME = "radiodrive";

	    JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
	    String TOKENS_DIRECTORY_PATH = "tokens";

	    List<String> SCOPES = List.of(DriveScopes.DRIVE_APPDATA, DriveScopes.DRIVE_FILE);
	    String CREDENTIALS_FILE_PATH = "/credentials.json";

        NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        InputStream in = GoogleDriveFileUploadTest.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
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

        Drive driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

		File fileMetadata = new File();
		fileMetadata.setName("english0-20210529.mp3");
		java.io.File filePath = new java.io.File(RadioProperties.getProperties().getBaseDir(), "english0-20210529.mp3");
		FileContent mediaContent = new FileContent("audio/mpeg", filePath);
		File file = driveService.files().create(fileMetadata, mediaContent)
		    .setFields("id")
		    .execute();
		System.out.println("File ID: " + file.getId());
	}
}
