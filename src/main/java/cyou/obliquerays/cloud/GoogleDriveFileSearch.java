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
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;

/**
 * GoogleAPIのFiles:listを実行<br>
 * https://developers.google.com/drive/api/v3/reference/files/list
 */
public class GoogleDriveFileSearch implements UnaryOperator<String> {
    /** ロガー */
    private static final Logger LOGGER = Logger.getLogger(GoogleDriveFileSearch.class.getName());

    /** インスタンス */
	private static GoogleDriveFileSearch INSTANCE;

	/** HTTPクライアント */
	private final HttpClient client;

	/**
	 * コンストラクタ
	 * @throws Exception GoogleAPIのAccessToken取得処理の初期化に失敗
	 */
	private GoogleDriveFileSearch() {
		this.client =	HttpClient.newBuilder()
                .version(Version.HTTP_2)
                .followRedirects(Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(30))
                .proxy(HttpClient.Builder.NO_PROXY)
                .build();
	}

	/**
	 * スケルトン
	 */
	@Override
	public String apply(String _accessToken) {
		String accessToken = Objects.requireNonNull(_accessToken);

		try (Jsonb jsonb = JsonbBuilder.create(new JsonbConfig().withFormatting(false))) {

			StringBuilder strUri = new StringBuilder("https://www.googleapis.com/drive/v3/files");
			strUri.append("?fields=nextPageToken,%20files(id,%20name)");

			HttpRequest request = HttpRequest.newBuilder()
	                .uri(URI.create(strUri.toString()))
	                .timeout(Duration.ofMinutes(30))
	                .header("Authorization", "Bearer " + accessToken)
	                .GET()
	                .build();
	        LOGGER.log(Level.CONFIG, "google access token request body = " + request.toString());

	        HttpResponse<String> response = this.client.send(request, BodyHandlers.ofString());

	        LOGGER.log(Level.INFO, "google access token responce code = " + response.statusCode());
	        LOGGER.log(Level.CONFIG, "google access token responce body = " + response.body());

	        String strResponseBody = response.body();

//	        FIXME
//	        @SuppressWarnings("unchecked")
//			Map<String, Object> responseBody = jsonb.fromJson(strResponseBody, Map.class);
//
//	        boolean incompleteSearch = (boolean) responseBody.get("incompleteSearch");
//	        String kind = (String) responseBody.get("kind");
//	        @SuppressWarnings("unchecked")
//			List<Map<String, String>> files = (List<Map<String, String>>) responseBody.get("files");

			return strResponseBody;

		} catch (Exception e) {

			throw new IllegalStateException(e);

		}
	}

	/**
	 * インスタンス取得
	 * @return インスタンス取得へアクセス
	 */
	public static GoogleDriveFileSearch getInstance() {
		if (null == INSTANCE) {
			synchronized (GoogleDriveFileSearch.class) {
				if (null == INSTANCE) {
					INSTANCE = new GoogleDriveFileSearch();
				}
			}
		}
		return INSTANCE;
	}
}
