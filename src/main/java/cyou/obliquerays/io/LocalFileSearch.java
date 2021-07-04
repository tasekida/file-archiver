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
package cyou.obliquerays.io;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cyou.obliquerays.config.RadioProperties;

/**
 * ローカルファイル検索
 */
public class LocalFileSearch implements Supplier<Set<Path>> {
    /** ロガー */
    private static final Logger LOGGER = Logger.getLogger(LocalFileSearch.class.getName());

	/** コンストラクタ */
	public LocalFileSearch() {}

	/**
	 * ローカルファイル一覧を取得
	 * @return ローカルファイル一覧
	 */
	@Override
	public Set<Path> get() {
		try (Stream<Path> stream = Files.walk(Path.of(RadioProperties.getProperties().getBaseDir()))) {
			Set<Path> dirFiles =
					stream.parallel()
						.collect(Collectors.toCollection(ConcurrentSkipListSet::new));
			return dirFiles;
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Local File search error",e);
			throw new UncheckedIOException(e);
		}
	}
}