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
package cyou.obliquerays.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * パラメータ一覧
 */
public final class RadioProperties extends Properties {
    /** ロガー */
    private static final Logger LOGGER = Logger.getLogger(RadioProperties.class.getName());

    /** プロパティファイル名 */
	private static final String PROPERTY_FILENAME = "radio.properties";

    /** パラメータ一覧 */
	private static RadioProperties PROP;

	private final boolean process;
	private final String mp3FilePrefix;
	private final String mp3FileName;
	private final String mp3FileSuffix;
	private final String mp3TempSuffix;

	/** コンストラクタ */
	private RadioProperties() {
		LOGGER.log(Level.CONFIG, "RadioProperties");

    	try (InputStream in = this.getInputStream(PROPERTY_FILENAME)) {
    		this.load(in);
    	} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "設定ファイルの読み取りに失敗#" + PROPERTY_FILENAME, e);
			throw new UncheckedIOException(e);
    	}
    	this.process = Boolean.parseBoolean(this.getProperty("process"));

    	this.mp3FilePrefix = Objects.requireNonNull(this.getProperty("mp3.file.prefix"));
    	this.mp3FileName = Objects.requireNonNull(this.getProperty("mp3.file.name"));
    	this.mp3FileSuffix = Objects.requireNonNull(this.getProperty("mp3.file.suffix"));
    	this.mp3TempSuffix = Objects.requireNonNull(this.getProperty("mp3.temp.suffix"));
	}

	/** @return プログラム起動モード */
	public boolean isProcess() {
		return this.process;
	}

	/** @return 録音ファイルディレクトリ */
	public String getBaseDir() {
		return this.getProperty("base.dir");
	}

	/**
	 * 録音ファイル名[prefix]-[name].[suffix]の[prefix]<br>
	 * DateTimeFormatterの解析用パターンをサポート
	 * @return 録音ファイル名の[prefix]
	 */
	public String getMp3FilePrefix() {
		return this.mp3FilePrefix;
	}

	/**
	 * 録音ファイル名[prefix]-[name].[suffix]の[name]<br>
	 * DateTimeFormatterの解析用パターンをサポート
	 * @return 録音ファイル名の[name]
	 */
	public String getMp3FileName() {
		return this.mp3FileName;
	}

	/**
	 * 録音ファイル名[prefix]-[name].[suffix]の[suffix]<br>
	 * DateTimeFormatterの解析用パターンをサポート
	 * @return 録音ファイル名の[suffix]
	 */
	public String getMp3FileSuffix() {
		return this.mp3FileSuffix;
	}

	/**
	 * 録音一次ファイル名[prefix]-[name].[suffix]の[suffix]<br>
	 * DateTimeFormatterの解析用パターンをサポート
	 * @return 録音ファイル名の[suffix]
	 */
	public String getMp3TempSuffix() {
		return this.mp3TempSuffix;
	}

	/**
	 * ファイル読み取りストリームへのアクセス
	 * @param _fileName ファイル名
	 * @return ファイル読み取りストリーム
	 * @throws IOException 読み取りエラー
	 */
	private InputStream getInputStream(String _fileName) throws IOException {
		Objects.requireNonNull(_fileName);
		Path file = Path.of("/data/config", _fileName).toAbsolutePath().normalize();
		if (Files.exists(file, LinkOption.NOFOLLOW_LINKS)) {
			return Files.newInputStream(file, StandardOpenOption.READ);
		} else {
			return ClassLoader.getSystemResourceAsStream(_fileName);
		}
	}

	/** @return パラメータ一覧へアクセス */
	public static RadioProperties getProperties() {
		if (null == PROP) {
			synchronized (RadioProperties.class) {
				if (null == PROP) {
					PROP = new RadioProperties();
				}
			}
		}
		return PROP;
	}
}
