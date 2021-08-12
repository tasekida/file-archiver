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

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient.Version;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Flow.Subscriber;
import java.util.logging.Level;
import java.util.logging.Logger;

import cyou.obliquerays.cloud.pojo.GDriveFile;

/**
 *
 */
public class GoogleDriveFileUploadRequest extends HttpRequest {
    /** ロガー */
    private static final Logger LOGGER = Logger.getLogger(GoogleDriveFileSearchRequest.class.getName());

    private static final String BOUNDARY = "oblique-rays";

	private static final byte[] NEW_LINE = "\r\n".getBytes(StandardCharsets.UTF_8);

	private static final byte[] NEW_PART = new StringBuilder("--").append(BOUNDARY).append("\r\n").toString().getBytes(StandardCharsets.UTF_8);

	private static final byte[] END_PART = new StringBuilder("--").append(BOUNDARY).append("--").toString().getBytes(StandardCharsets.UTF_8);

	private static final byte[] CONTENT_TYPE_METADATA = "Content-Type: application/json; charset=utf-8\r\n".getBytes(StandardCharsets.UTF_8);

	private static final byte[] CONTENT_TYPE_FILE = "Content-Type: application/octet-stream\r\n".getBytes(StandardCharsets.UTF_8);

	private static final byte[] JSON_METADATA_PREFIX = "{\"name\":\"".getBytes(StandardCharsets.UTF_8);

	private static final byte[] JSON_METADATA_SUFFIX = "\"}\r\n".getBytes(StandardCharsets.UTF_8);

	private static final long MAX_FILESIZE = 104857600L;

    /** オリジナル */
	private final HttpRequest orig;

	/**
	 * コンストラクタ
	 * @throws Exception
	 */
	public GoogleDriveFileUploadRequest(String _accessToken, GDriveFile _gDriveFile) throws Exception {
		String accessToken = Objects.requireNonNull(_accessToken);
		GDriveFile gDriveFile = Objects.requireNonNull(_gDriveFile);

		HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();

		Path path = gDriveFile.getPath();

		if (Files.isDirectory(path)) {
			String bodyJson = new StringBuilder("{\"mimeType\":\"application/vnd.google-apps.folder\",\"name\":\"")
					.append(gDriveFile.getName()).append("\"}").toString();
			requestBuilder.uri(URI.create("https://www.googleapis.com/drive/v3/files?fields=id,kind,name,mimeType"))
					.timeout(Duration.ofMinutes(30))
	                .header("Authorization", "Bearer " + accessToken)
	                .header("Content-Type", "application/json; charset=UTF-8")
	                .POST(BodyPublishers.ofString(bodyJson));
		} else {
			requestBuilder.uri(URI.create("https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart"))
					.timeout(Duration.ofMinutes(30))
	                .header("Authorization", "Bearer " + accessToken)
	                .header("Content-Type", "multipart/related; boundary=".concat(BOUNDARY))
					.POST(new GoogleDriveFileUploadPublisher(gDriveFile.getName(), path));
		}

		this.orig = requestBuilder.build();
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

	/**
	 *
	 */
	private class GoogleDriveFileUploadPublisher implements BodyPublisher {

		private final BodyPublisher orig;

		/**
		 * コンストラクタ
		 * @throws Exception
		 */
		private GoogleDriveFileUploadPublisher(String _fileName, Path _filePath) throws Exception {
			String fileName = Objects.requireNonNull(_fileName, "fileName must not be null");
			Path filePath = Objects.requireNonNull(_filePath, "filePath must not be null");

			long fileSize = Files.size(filePath);
			if (fileSize > MAX_FILESIZE) {
				String msg = new StringBuilder("[").append(filePath.toString()).append("] is too large. [").append((double)fileSize).append("Bytes]").toString();
				throw new IllegalArgumentException(msg);
			}

			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			outputStream.writeBytes(NEW_PART);
			outputStream.writeBytes(CONTENT_TYPE_METADATA);
			outputStream.writeBytes(NEW_LINE);
			outputStream.writeBytes(JSON_METADATA_PREFIX);
			outputStream.writeBytes(fileName.getBytes(StandardCharsets.UTF_8));
			outputStream.writeBytes(JSON_METADATA_SUFFIX);
			outputStream.writeBytes(NEW_PART);
			outputStream.writeBytes(CONTENT_TYPE_FILE);
			outputStream.writeBytes(NEW_LINE);
			outputStream.writeBytes(Files.readAllBytes(filePath));
			outputStream.writeBytes(NEW_LINE);
			outputStream.writeBytes(END_PART);
			this.orig = BodyPublishers.ofByteArray(outputStream.toByteArray());
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
			return new StringBuilder("GoogleDriveFileUploadPublisher [")
					.append("orig=").append(this.orig)
					.append("]").toString();
		}
	}
}
