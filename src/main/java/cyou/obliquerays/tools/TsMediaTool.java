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
package cyou.obliquerays.tools;

import java.nio.file.Path;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import cyou.obliquerays.config.RadioProperties;

/**
 * HLSセグメントファイルを操作するクラス
 */
public class TsMediaTool {
    /** ロガー */
    private static final Logger LOGGER = Logger.getLogger(TsMediaTool.class.getName());

    /** MP3ファイル（.mp3）フルパスの正規表現 */
	private static final Pattern MP3_FILE_PATERN = Pattern.compile(".+\\.mp3$");

    /** MP3ファイル（.ts）フルパスの正規表現 */
	private static final Pattern TS_FILE_PATERN = Pattern.compile(".+\\.ts$");

	/**
	 * MP3ファイルを判定するPredicateを取得
	 * @return MP3ファイルを判定するPredicate
	 */
	public static Predicate<Path> predicateMp3Path () {
		return p -> MP3_FILE_PATERN.matcher(p.toAbsolutePath().normalize().toString()).matches();
	}

	/**
	 * TSファイルを判定するPredicateを取得
	 * @return TSファイルを判定するPredicate
	 */
	public static Predicate<Path> predicateTsPath () {
		return p -> TS_FILE_PATERN.matcher(p.toAbsolutePath().normalize().toString()).matches();
	}

	/**
	 * 録音ファイル名[prefix]-[name].[suffix]の絶対パスを取得
	 * @return 録音ファイル名[prefix]-[name].[suffix]の絶対パス
	 */
	public static synchronized Path getMp3FilePath () {
		String baseDir = RadioProperties.getProperties().getBaseDir();
		String mp3FilePrefix = Objects.requireNonNull(RadioProperties.getProperties().getMp3FilePrefix());
    	try {
    		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(mp3FilePrefix);
    		mp3FilePrefix = LocalDate.now().format(formatter);
    	} catch (IllegalArgumentException | DateTimeException e) {
    		// ignore
    	}
    	String mp3FileName = Objects.requireNonNull(RadioProperties.getProperties().getMp3FileName());
    	try {
    		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(mp3FileName);
    		mp3FileName = LocalDate.now().format(formatter);
    	} catch (IllegalArgumentException | DateTimeException e) {
    		// ignore
    	}
    	String mp3FileSuffix = Objects.requireNonNull(RadioProperties.getProperties().getMp3FileSuffix());
    	StringBuilder mp3File =
    			new StringBuilder(mp3FilePrefix).append("-")
    			.append(mp3FileName).append(".").append(mp3FileSuffix);
		return Path.of(baseDir, mp3File.toString()).toAbsolutePath().normalize();
	}
}
