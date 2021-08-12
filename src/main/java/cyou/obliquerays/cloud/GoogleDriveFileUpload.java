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

import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.BiPredicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import cyou.obliquerays.cloud.http.GoogleDriveFileUploadRequest;
import cyou.obliquerays.cloud.pojo.GDriveFile;

/**
 * GoogleAPIのAccessTokenを取得
 */
public class GoogleDriveFileUpload implements BiPredicate<String, GDriveFile> {
    /** ロガー */
    private static final Logger LOGGER = Logger.getLogger(GoogleDriveFileUpload.class.getName());

    /** インスタンス */
	private static GoogleDriveFileUpload INSTANCE;

	/** 並列処理管理オブジェクト */
	private final Executor executor = Executors.newSingleThreadExecutor();

	/** コンストラクタ */
	private GoogleDriveFileUpload() {}

	/**
	 * スケルトン
	 */
	@Override
	public boolean test(String _accessToken, GDriveFile _gDriveFile) {
		String accessToken = Objects.requireNonNull(_accessToken);
		GDriveFile gDriveFile = Objects.requireNonNull(_gDriveFile);

		try {
			HttpClient client = HttpClient.newBuilder()
					.version(Version.HTTP_2)
					.followRedirects(Redirect.NORMAL)
					.connectTimeout(Duration.ofSeconds(30))
					.proxy(HttpClient.Builder.NO_PROXY)
					.executor(this.executor)
					.build();
			HttpRequest request = new GoogleDriveFileUploadRequest(accessToken, gDriveFile);
	        LOGGER.log(Level.CONFIG, "google drive directory create request body = " + request.toString());

	        HttpResponse<String> response = client.send(request, BodyHandlers.ofString(StandardCharsets.UTF_8));

	        LOGGER.log(Level.INFO, "google drive directory create responce code = " + response.statusCode());
	        LOGGER.log(Level.CONFIG, "google drive directory create responce body = " + response.body());

			return response.body().isEmpty();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * インスタンス取得
	 * @return インスタンス取得へアクセス
	 */
	public static GoogleDriveFileUpload getInstance() {
		if (null == INSTANCE) {
			synchronized (GoogleDriveFileUpload.class) {
				if (null == INSTANCE) {
					INSTANCE = new GoogleDriveFileUpload();
				}
			}
		}
		return INSTANCE;
	}
}
