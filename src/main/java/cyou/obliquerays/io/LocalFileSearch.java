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
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import cyou.obliquerays.config.RadioProperties;

/**
 * ローカルファイル検索
 */
public class LocalFileSearch {
    /** ロガー */
    private static final Logger LOGGER = Logger.getLogger(LocalFileSearch.class.getName());

	/** コンストラクタ */
	public LocalFileSearch() {}

	/**
	 * ローカルファイル一覧を取得
	 * @return ローカルファイル一覧
	 * @throws IOException ファイル操作エラー
	 */
	public List<Path> search() throws IOException {

		List<Path> target = new ArrayList<>(0);
		try {
			Files.walkFileTree(Path.of(RadioProperties.getProperties().getBaseDir()), new SimpleFileVisitor<Path>() {
			    @Override
			    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			    	target.add(file);
			    	return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "ローカルファイル一覧の取得に失敗", e);
			throw e;
		}

		return target;
	}
}