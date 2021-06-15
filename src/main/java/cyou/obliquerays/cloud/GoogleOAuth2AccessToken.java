/**
 *  Copyright (C) 2021 tasekida
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
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import cyou.obliquerays.config.RadioProperties;

/**
 * GoogleAPIのAccessTokenを取得
 */
public class GoogleOAuth2AccessToken implements Supplier<String> {
    /** ロガー */
    private static final Logger LOGGER = Logger.getLogger(GoogleOAuth2AccessToken.class.getName());

    /** インスタンス */
	private static GoogleOAuth2AccessToken INSTANCE;

	/**
	 * コンストラクタ
	 * @throws Exception GoogleAPIのAccessToken取得処理の初期化に失敗
	 */
	private GoogleOAuth2AccessToken() {}

	/**
	 * GoogleAPIのAccessTokenを取得
	 */
	@Override
	public String get() {

		try (InputStream in = ClassLoader.getSystemResourceAsStream(RadioProperties.getProperties().getProperty("google.credentials.json"))) {

			JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
	        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(jsonFactory, new InputStreamReader(in));

	        NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
		    List<String> scopes = Collections.singletonList("https://www.googleapis.com/auth/drive.appdata");

	        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
	                httpTransport, jsonFactory, clientSecrets, scopes)
	                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File("tokens")))
	                .setAccessType("offline")
	                .build();
	        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
	        Credential credential = new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

	        LOGGER.log(Level.CONFIG, "google access token = " + credential.getAccessToken());

			return credential.getAccessToken();

		} catch (Exception e) {

			throw new IllegalStateException(e);
		}
	}

	/**
	 * インスタンス取得
	 * @return インスタンス取得へアクセス
	 */
	public static GoogleOAuth2AccessToken getInstance() {
		if (null == INSTANCE) {
			synchronized (GoogleOAuth2AccessToken.class) {
				if (null == INSTANCE) {
					INSTANCE = new GoogleOAuth2AccessToken();
				}
			}
		}
		return INSTANCE;
	}
}
