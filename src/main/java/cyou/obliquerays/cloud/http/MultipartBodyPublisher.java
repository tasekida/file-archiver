/**
 *  Copyright 2021 takahiro
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
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.Flow.Subscriber;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class MultipartBodyPublisher implements BodyPublisher {
    /** ロガー */
    private static final Logger LOGGER = Logger.getLogger(MultipartBodyPublisher.class.getName());

	public static final String BOUNDARY = "oblique-rays";

	private static final byte[] NEW_LINE = "\r\n".getBytes(StandardCharsets.UTF_8);

	private static final byte[] NEW_PART = new StringBuilder("--").append(BOUNDARY).append("\r\n").toString().getBytes(StandardCharsets.UTF_8);

	private static final byte[] END_PART = new StringBuilder("--").append(BOUNDARY).append("--").toString().getBytes(StandardCharsets.UTF_8);

	private static final byte[] CONTENT_TYPE_METADATA = "Content-Type: application/json; charset=utf-8\r\n".getBytes(StandardCharsets.UTF_8);

	private static final byte[] CONTENT_TYPE_FILE = "Content-Type: application/octet-stream\r\n".getBytes(StandardCharsets.UTF_8);

	private static final byte[] JSON_METADATA_PREFIX = "{\"name\":\"".getBytes(StandardCharsets.UTF_8);

	private static final byte[] JSON_METADATA_SUFFIX = "\"}\r\n".getBytes(StandardCharsets.UTF_8);

	private final BodyPublisher delegate;

	/**
	 * コンストラクタ
	 * @throws Exception
	 */
	public MultipartBodyPublisher(String _fileName, Path _filePath) throws Exception {
		String fileName = Objects.requireNonNull(_fileName, "fileName must not be null");
		Path filePath = Objects.requireNonNull(_filePath, "filePath must not be null");

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
		this.delegate = BodyPublishers.ofByteArray(outputStream.toByteArray());
	}

	@Override
	public void subscribe(Subscriber<? super ByteBuffer> _subscriber) {
		LOGGER.log(Level.CONFIG, "begin");
		this.delegate.subscribe(_subscriber);
		LOGGER.log(Level.CONFIG, "finish");
	}

	@Override
	public long contentLength() {
		long contentLength = this.delegate.contentLength();
		LOGGER.log(Level.CONFIG, "contentLength = ".concat(String.valueOf(contentLength)));
		return contentLength;
	}

}
