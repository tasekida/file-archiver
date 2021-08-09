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

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import cyou.obliquerays.cloud.http.GoogleDriveFileSearchBodyHandler;
import cyou.obliquerays.cloud.http.GoogleDriveFileSearchRequest;
import cyou.obliquerays.cloud.pojo.GDriveResource;

/**
 * GoogleAPIのFiles:listを実行<br>
 * https://developers.google.com/drive/api/v3/reference/files/list
 */
public class GoogleDriveFileSearch implements Function<String, List<GDriveResource>> {
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
	public List<GDriveResource> apply(String _accessToken) {
		String accessToken = Objects.requireNonNull(_accessToken);
		try {
			HttpResponse<List<GDriveResource>> response = this.client.send(
					new GoogleDriveFileSearchRequest(accessToken), new GoogleDriveFileSearchBodyHandler());
	        LOGGER.log(Level.INFO, "google access token responce code = " + response.statusCode());
	        LOGGER.log(Level.CONFIG, "google access token responce body = " + response.body());
	        return response.body();
		} catch (IOException | InterruptedException e) {
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
