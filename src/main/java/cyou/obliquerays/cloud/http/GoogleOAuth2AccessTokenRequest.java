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
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.Flow.Subscriber;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class GoogleOAuth2AccessTokenRequest extends HttpRequest {
    /** ロガー */
    private static final Logger LOGGER = Logger.getLogger(GoogleOAuth2AccessTokenRequest.class.getName());

	private final HttpRequest orig;

	/**
	 *
	 */
	public GoogleOAuth2AccessTokenRequest(String _refreshToken, String _clientId, String _clientSecret) {
		this.orig = HttpRequest.newBuilder(URI.create("https://oauth2.googleapis.com/token"))
	        .timeout(Duration.ofMinutes(30))
	        .header("Accept-Encoding", "gzip")
	        .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
	        .POST(new GoogleOAuth2AccessTokenBodyPublisher(_refreshToken, _clientId, _clientSecret))
	        .build();
		LOGGER.log(Level.CONFIG, this.orig.toString());
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
		return new StringBuilder("GoogleOAuth2AccessTokenRequest [")
				.append("orig=").append(this.orig)
				.append("]").toString();
	}

	private class GoogleOAuth2AccessTokenBodyPublisher implements BodyPublisher {

		private final BodyPublisher orig;

		private GoogleOAuth2AccessTokenBodyPublisher(String _refreshToken, String _clientId, String _clientSecret) {
			String strBody = new StringBuilder("grant_type=refresh_token")
					.append("&refresh_token=").append(_refreshToken)
					.append("&client_id=").append(_clientId)
					.append("&client_secret=").append(_clientSecret)
					.toString();
			this.orig = BodyPublishers.ofString(strBody);
		}

		@Override
		public void subscribe(Subscriber<? super ByteBuffer> _subscriber) {
			LOGGER.log(Level.CONFIG, "begin");
			this.orig.subscribe(_subscriber);
			LOGGER.log(Level.CONFIG, "finish");
		}

		@Override
		public long contentLength() {
			long contentLength = this.orig.contentLength();
			LOGGER.log(Level.CONFIG, "contentLength = ".concat(String.valueOf(contentLength)));
			return contentLength;
		}

		@Override
		public String toString() {
			return new StringBuilder("GoogleOAuth2AccessTokenBodyPublisher [")
					.append("orig=").append(this.orig)
					.append("]").toString();
		}
	}
}
