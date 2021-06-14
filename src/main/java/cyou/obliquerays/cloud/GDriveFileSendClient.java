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

import java.nio.file.Path;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * Google Drive 送信処理
 */
public class GDriveFileSendClient implements Consumer<Path> {
    /** ロガー */
    private static final Logger LOGGER = Logger.getLogger(GDriveFileSendClient.class.getName());

	/** コンストラクタ */
	public GDriveFileSendClient() {}

	/**
	 * Google Drive 送信処理<br>
	 * 送信に成功した場合、すでに送信済みの場合は、ローカルファイルを削除
	 */
	public void send() {
		// FIXME 送信
	}

	@Override
	public void accept(Path _sourceFile) {
		Path sourceFile = Objects.requireNonNull(_sourceFile);
	}
}
