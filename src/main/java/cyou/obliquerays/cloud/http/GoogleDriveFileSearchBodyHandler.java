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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodySubscriber;
import java.net.http.HttpResponse.BodySubscribers;
import java.net.http.HttpResponse.ResponseInfo;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow.Subscription;
import java.util.logging.Level;
import java.util.logging.Logger;

import cyou.obliquerays.cloud.pojo.GDriveResource;
import cyou.obliquerays.cloud.pojo.GDriveSearchFile;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;

/**
 *
 */
public class GoogleDriveFileSearchBodyHandler implements BodyHandler<List<GDriveResource>> {
    /** ロガー */
    private static final Logger LOGGER = Logger.getLogger(GoogleDriveFileSearchBodyHandler.class.getName());

	/** コンストラクタ */
	public GoogleDriveFileSearchBodyHandler() {}

	@Override
	public BodySubscriber<List<GDriveResource>> apply(ResponseInfo _responseInfo) {
		// debug log
		_responseInfo.headers().map().entrySet().stream()
				.map(Map.Entry::toString)
				.forEach(s -> LOGGER.log(Level.CONFIG, s));
		return new GoogleDriveFileSearchBodySubscriber();
	}


	/**
	 *
	 */
	public class GoogleDriveFileSearchBodySubscriber implements BodySubscriber<List<GDriveResource>> {

		/** 実装 */
		private final BodySubscriber<List<GDriveResource>> orig;

		/**
		 * コンストラクタ
		 */
		public GoogleDriveFileSearchBodySubscriber() {
			this.orig = BodySubscribers.mapping(
					BodySubscribers.ofString(StandardCharsets.UTF_8)
					, str -> {
						try (Jsonb jsonb = JsonbBuilder.create(new JsonbConfig().withFormatting(false))) {
					        GDriveSearchFile gDriveSearchFile = jsonb.fromJson(str, GDriveSearchFile.class);
							return gDriveSearchFile.getFiles();
						} catch (IOException e) {
							LOGGER.log(Level.SEVERE, "レスポンス情報の取得に失敗", e);
							throw new UncheckedIOException(e);
						} catch (Exception e) {
							LOGGER.log(Level.SEVERE, "レスポンス情報の取得に失敗", e);
							throw new IllegalStateException(e);
						}
					});
		}

		@Override
		public void onSubscribe(Subscription _subscription) {
			LOGGER.log(Level.CONFIG, new StringBuilder("subscription=").append(_subscription).toString());
			this.orig.onSubscribe(_subscription);;
		}

		@Override
		public void onNext(List<ByteBuffer> _item) {
			LOGGER.log(Level.CONFIG, new StringBuilder("item=").append(_item).toString());
			this.orig.onNext(_item);
		}

		@Override
		public void onError(Throwable _throwable) {
			LOGGER.log(Level.CONFIG, new StringBuilder("throwable=").append(_throwable).toString());
			this.orig.onError(_throwable);
		}

		@Override
		public void onComplete() {
			LOGGER.log(Level.CONFIG, "");
			this.orig.onComplete();
		}

		@Override
		public CompletionStage<List<GDriveResource>> getBody() {
			return this.orig.getBody();
		}
	}
}
