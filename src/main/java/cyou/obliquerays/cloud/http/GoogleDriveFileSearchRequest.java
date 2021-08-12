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
package cyou.obliquerays.cloud.http;

import java.net.URI;
import java.net.http.HttpClient.Version;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class GoogleDriveFileSearchRequest extends HttpRequest {
    /** ロガー */
    private static final Logger LOGGER = Logger.getLogger(GoogleDriveFileSearchRequest.class.getName());

    /** オリジナル */
	private final HttpRequest orig;

	/** コンストラクタ */
	public GoogleDriveFileSearchRequest(String _accessToken) {
		this.orig = HttpRequest.newBuilder()
                .uri(URI.create("https://www.googleapis.com/drive/v3/files?fields=nextPageToken,files(id,name,parents)"))
                .timeout(Duration.ofMinutes(30))
                .header("Authorization", "Bearer " + _accessToken)
                .GET()
                .build();
		// debug log
		LOGGER.log(Level.CONFIG, this.orig.toString());
		this.orig.headers().map().entrySet().stream()
				.map(Map.Entry::toString)
				.forEach(s -> LOGGER.log(Level.CONFIG, s));
	}

	@Override
	public Optional<BodyPublisher> bodyPublisher() {
		return this.orig.bodyPublisher();
	}

	@Override
	public String method() {
		return this.orig.method();
	}

	@Override
	public Optional<Duration> timeout() {
		return this.orig.timeout();
	}

	@Override
	public boolean expectContinue() {
		return this.orig.expectContinue();
	}

	@Override
	public URI uri() {
		return this.orig.uri();
	}

	@Override
	public Optional<Version> version() {
		return this.orig.version();
	}

	@Override
	public HttpHeaders headers() {
		return this.orig.headers();
	}

	@Override
	public String toString() {
		return new StringBuilder("GoogleDriveFileSearchRequest [")
				.append("orig=").append(this.orig)
				.append("]").toString();
	}
}
