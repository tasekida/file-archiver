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

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.BiPredicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import cyou.obliquerays.cloud.http.MultipartBodyPublisher;
import cyou.obliquerays.cloud.pojo.GDriveFile;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;

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

		try (Jsonb jsonb = JsonbBuilder.create(new JsonbConfig().withFormatting(false))) {

			HttpClient client = HttpClient.newBuilder()
					.version(Version.HTTP_2)
					.followRedirects(Redirect.NORMAL)
					.connectTimeout(Duration.ofSeconds(30))
					.proxy(HttpClient.Builder.NO_PROXY)
					.executor(this.executor)
					.build();
			HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();

			Path path = gDriveFile.getPath();

			if (Files.isDirectory(path)) {
				String fileName = gDriveFile.getName();

				Map<String, String> bodyParam = Map.of("mimeType", "application/vnd.google-apps.folder", "name", fileName);
				String bodyJson = jsonb.toJson(bodyParam);

				requestBuilder.uri(URI.create("https://www.googleapis.com/drive/v3/files?fields=id"))
						.timeout(Duration.ofMinutes(30))
		                .header("Authorization", "Bearer " + accessToken)
		                .header("Accept-Encoding", "gzip")
		                .header("Content-Type", "application/json; charset=UTF-8")
		                .POST(BodyPublishers.ofString(bodyJson));
			} else {

				requestBuilder.uri(URI.create("https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart"))
						.timeout(Duration.ofMinutes(30))
		                .header("Authorization", "Bearer " + accessToken)
		                .header("Content-Type", "multipart/related; boundary=".concat(MultipartBodyPublisher.BOUNDARY))
						.POST(new MultipartBodyPublisher(gDriveFile.getName(), path));
			}

			HttpRequest request = requestBuilder.build();
	        LOGGER.log(Level.CONFIG, "google drive directory create request body = " + request.toString());

	        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());

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
