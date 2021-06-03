/**
 *  Copyright 2021 tasekida
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
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;

/**
 * GoogleAPIのAccessTokenを取得
 */
public class GoogleAccessToken implements UnaryOperator<String> {
    /** ロガー */
    private static final Logger LOGGER = Logger.getLogger(GoogleAccessToken.class.getName());

    /** インスタンス */
	private static GoogleAccessToken INSTANCE;

	/** HTTPクライアント */
	private final HttpClient client;
	/** HTTP Postパラメータのベース */
	private final String bodyBase;

	/**
	 * コンストラクタ
	 * @throws Exception GoogleAPIのAccessToken取得処理の初期化に失敗
	 */
	private GoogleAccessToken() {
		this.client =	HttpClient.newBuilder()
                .version(Version.HTTP_2)
                .followRedirects(Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(30))
                .proxy(HttpClient.Builder.NO_PROXY)
                .build();
		this.bodyBase = new StringBuilder("grant_type=")
				.append(URLEncoder.encode("urn:ietf:params:oauth:grant-type:jwt-bearer", StandardCharsets.UTF_8))
				.append("&assertion=").toString();
	}

	/**
	 * GoogleAPIのAccessTokenを取得
	 */
	@Override
	public String apply(String _signedJwt) {

		try (Jsonb jsonb = JsonbBuilder.create(new JsonbConfig().withFormatting(false))) {

			String body = new StringBuilder(this.bodyBase)
					.append(_signedJwt).toString();

	        HttpRequest request = HttpRequest.newBuilder()
	                .uri(URI.create("https://oauth2.googleapis.com/token"))
	                .timeout(Duration.ofMinutes(30))
	                .header("Content-Type", "application/x-www-form-urlencoded")
	                .POST(BodyPublishers.ofString(body))
	                .build();
	        LOGGER.log(Level.CONFIG, "google access token request body = " + request.toString());

	        HttpResponse<String> response = this.client.send(request, BodyHandlers.ofString());

	        LOGGER.log(Level.INFO, "google access token responce code = " + response.statusCode());
	        LOGGER.log(Level.CONFIG, "google access token responce body = " + response.body());

	        @SuppressWarnings("unchecked")
			Map<String, String> responseParam = jsonb.fromJson(response.body(), Map.class);

			return responseParam.get("access_token");

		} catch (Exception e) {

			throw new IllegalStateException(e);
		}
	}

	/**
	 * インスタンス取得
	 * @return インスタンス取得へアクセス
	 */
	public static GoogleAccessToken getInstance() {
		if (null == INSTANCE) {
			synchronized (GoogleAccessToken.class) {
				if (null == INSTANCE) {
					INSTANCE = new GoogleAccessToken();
				}
			}
		}
		return INSTANCE;
	}
}
