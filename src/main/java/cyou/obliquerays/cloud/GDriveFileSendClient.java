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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import cyou.obliquerays.cloud.pojo.GDriveFile;
import cyou.obliquerays.cloud.pojo.GDriveResource;

/**
 * Google Drive 送信処理
 */
public class GDriveFileSendClient implements Consumer<Set<Path>> {
    /** ロガー */
    private static final Logger LOGGER = Logger.getLogger(GDriveFileSendClient.class.getName());

	/** コンストラクタ */
	public GDriveFileSendClient() {	}

	/**
	 * Google Drive 送信処理<br>
	 * 送信に成功した場合、すでに送信済みの場合は、ローカルファイルを削除
	 */
	@Override
	public void accept(Set<Path> _setLocalFiles) {
		Set<Path> setLocalFiles = Objects.requireNonNull(_setLocalFiles);

		// Google Drive 検索
		List<GDriveResource> listGResources = this.getGoogleDriveResources(
				GoogleOAuth2AccessToken.getInstance(), GoogleDriveFileSearch.getInstance());

		// ディレクトリ情報を保持する Google Drive 上のオブジェクト一覧
		Map<String, GDriveFile> mapGfiles = this.getGoogleDriveMap(listGResources);
		listGResources.clear();

		// ローカルに存在して、GoogleDriveに存在しないファイル、フォルダの一覧
		List<GDriveFile> listLocalFiles = this.getGoogleDriveFiles(setLocalFiles, mapGfiles);
		setLocalFiles.clear();
		mapGfiles.clear();

		listLocalFiles.forEach(p -> LOGGER.log(Level.CONFIG, p.toString()));
	}

	/**
	 * GoogleDriveAPIを利用して、GoogleDriveからリソース情報を取得する
	 * @param _tokenSupplier GoogleDriveAPIアクセス用のアクセストークンを取得する関数
	 * @param _gDriveSearch GoogleDrive上のリソースを検索する関数
	 * @return GoogleDriveから取得したリソース情報一覧
	 */
	private List<GDriveResource> getGoogleDriveResources(
			Supplier<String> _tokenSupplier, Function<String, List<GDriveResource>> _gDriveSearch) {
		List<GDriveResource> listGResources = _gDriveSearch.apply(_tokenSupplier.get());
		return listGResources;
	}

	/**
	 * GoogleDriveから取得したリソース情報一覧からGoogleDriveに存在するファイル、フォルダ情報の一覧を作成する
	 * @param _gDriveResources GoogleDriveから取得したリソース情報一覧
	 * @return GoogleDriveに存在するファイル、フォルダ情報の一覧
	 */
	private Map<String, GDriveFile> getGoogleDriveMap(List<GDriveResource> _gDriveResources) {

		// ファイル名、フォルダ名検索用Map
		Map<String, String> mapGfileNames = _gDriveResources.stream()
				.collect(Collectors.toMap(GDriveResource::getId, GDriveResource::getName));

		// 親フォルダ名を反映したファイル・フォルダ情報一覧
		Map<String, GDriveFile> mapGfiles = _gDriveResources.stream()
				.flatMap(gResource -> {
					Optional<String[]> optParents = Optional.ofNullable(gResource.getParents());
					if (optParents.isPresent()) {
						Stream<GDriveFile> gFileStream =
								optParents.map(Arrays::stream).orElseGet(Stream::empty)
									.map(parentId -> {
										GDriveFile gFileParent = new GDriveFile();
										gFileParent.setId(parentId);
										gFileParent.setName(mapGfileNames.get(parentId));
										GDriveFile gFileChild = new GDriveFile();
										gFileChild.setId(gResource.getId());
										gFileChild.setName(gResource.getName());
										gFileChild.setParent(gFileParent);
										return gFileChild;
									});
						return gFileStream;
					} else {
						GDriveFile gFile = new GDriveFile();
						gFile.setId(gResource.getId());
						gFile.setName(gResource.getName());
						return Stream.of(gFile);
					}
				})
				.collect(Collectors.toMap(GDriveFile::getId, Function.identity()));

		return mapGfiles;
	}

	/**
	 * ローカルとGoogleDriveに存在するファイル、フォルダを比較して、GoogleDriveに存在しないローカルファイル、フォルダの一覧を作成する
	 * @param _setLocalFiles ローカルに存在するファイル、フォルダの一覧
	 * @param _mapGfiles GoogleDriveに存在するファイル、フォルダの一覧
	 * @return ローカルに存在して、GoogleDriveに存在しないファイル、フォルダの一覧
	 */
	private List<GDriveFile> getGoogleDriveFiles(Set<Path> _setLocalFiles, Map<String, GDriveFile> _mapGfiles) {

		List<GDriveFile> listLocalFiles = _setLocalFiles.stream()
				.flatMap(localFile -> {
					// ローカルファイル、フォルダの親フォルダ名
					String localParentName = Objects.nonNull(localFile.getParent()) && Objects.nonNull(localFile.getParent().getFileName())
							? localFile.getParent().getFileName().toString() : null;
					// ローカルのファイル、フォルダ名と親フォルダ名と一致するGoogleDrive上のファイルまたはフォルダ
					Optional<GDriveFile> optGDriveFile =
							_mapGfiles.values().stream()
								// ファイル、フォルダ名が一致
								.filter(gDriveFile -> Objects.equals(gDriveFile.getName(), localFile.getFileName().toString()))
								// 親フォルダ名が一致
								.filter(gDriveFile -> {
									String gDriveParent = Objects.nonNull(gDriveFile.getParent()) ? gDriveFile.getParent().getName() : null;
									return Objects.equals(gDriveParent, localParentName);
								})
								.findFirst();
					if (optGDriveFile.isPresent()) {	// GoogleDriveに存在するので対象外
						return Stream.empty();
					} else {							// GoogleDriveに存在しないので対象
						// GoogleDriveのファイル一覧から親フォルダを検索
						Optional<String> gDriveParentId = _mapGfiles.entrySet().stream()
								.filter(gDriveEntry -> Objects.equals(gDriveEntry.getValue().getName(), localParentName))
								.map(Map.Entry::getKey)
								.findFirst();
						// 親フォルダ情報
						GDriveFile gDriveParent = new GDriveFile();
						gDriveParent.setId(gDriveParentId.orElse(null));
						gDriveParent.setName(localParentName);
						gDriveParent.setPath(localFile.getParent());
						// ファイル・フォルダ情報
						GDriveFile gDriveFile = new GDriveFile();
						gDriveFile.setName(localFile.getFileName().toString());
						gDriveFile.setPath(localFile);
						gDriveFile.setParent(gDriveParent);
						return Stream.of(gDriveFile);
					}
				})
				// フォルダ階層が浅い順にソート
				.sorted((g1, g2) -> Integer.compare(g1.getPath().getNameCount(), g2.getPath().getNameCount()))
				.collect(Collectors.toList());

		return listLocalFiles;
	}
}
