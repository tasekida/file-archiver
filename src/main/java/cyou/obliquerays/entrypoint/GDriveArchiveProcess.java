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
package cyou.obliquerays.entrypoint;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import cyou.obliquerays.status.LockFileStatus;


/**
 * ラジオ録音ファイル保存
 */
public class GDriveArchiveProcess {
    /** ロガー */
    private static final Logger LOGGER = Logger.getLogger(GDriveArchiveProcess.class.getName());

    /** スレッド管理 */
	private final ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

	/**
	 * デフォルトコンストラクタ
	 * @throws IOException ファイル操作失敗
	 */
	public GDriveArchiveProcess() throws IOException {
		var lockFile = Path.of(this.getClass().getSimpleName() + ".lock");
    	try {
			var lockFileStatus =
					new LockFileStatus(Thread.currentThread(), lockFile);
			this.executor.scheduleAtFixedRate(lockFileStatus, 5L, 10L, TimeUnit.SECONDS);
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "プロセス実行時存在ファイルの管理に失敗#" + lockFile, e);
			throw e;
		}
	}

	public void execute() throws InterruptedException {

		try {

			LOGGER.log(Level.INFO, "プロトタイプ");

		} catch (Exception e) {

			LOGGER.log(Level.SEVERE, "エラー終了", e);

		} finally {

			this.executor.shutdown();
			if (!this.executor.awaitTermination(10L, TimeUnit.SECONDS) && !this.executor.isTerminated())
				this.executor.shutdownNow();

		}
	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		int returnCode = 0;// プログラムのリターンコード

        try (InputStream propLogging = ClassLoader.getSystemResourceAsStream("logging.properties")) {
            LogManager.getLogManager().readConfiguration(propLogging);
            LOGGER.log(Level.CONFIG, "logging.properties#handlers=" + LogManager.getLogManager().getProperty("handlers"));
            LOGGER.log(Level.CONFIG, "logging.properties#.level=" + LogManager.getLogManager().getProperty(".level"));
            LOGGER.log(Level.CONFIG, "logging.properties#java.util.logging.ConsoleHandler.level=" + LogManager.getLogManager().getProperty("java.util.logging.ConsoleHandler.level"));
            LOGGER.log(Level.CONFIG, "logging.properties#java.util.logging.ConsoleHandler.formatter=" + LogManager.getLogManager().getProperty("java.util.logging.ConsoleHandler.formatter"));
        } catch (Exception e) {
        	LOGGER.log(Level.SEVERE, "エラー終了", e);
        	returnCode = 1;
        	System.exit(returnCode);
        }

        try {
        	GDriveArchiveProcess radioArchive = new GDriveArchiveProcess();
        	radioArchive.execute();
        } catch (InterruptedException e) {
        	LOGGER.log(Level.INFO, "割り込み終了", e);
        	returnCode = 0;
        } catch (Exception e) {
        	LOGGER.log(Level.SEVERE, "エラー終了", e);
        	returnCode = 1;
        	throw e;
        } finally {
            System.exit(returnCode);
        }
	}
}
