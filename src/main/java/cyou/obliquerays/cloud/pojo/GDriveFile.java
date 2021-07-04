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
package cyou.obliquerays.cloud.pojo;

import java.nio.file.Path;

/**
 * Google Drive上のファイル
 */
public class GDriveFile {

	/** Google Drive ファイルID */
	private String id;

	/** ファイル名 */
	private String name;

	/** ファイルオブジェクト */
	private Path path;

	/** 親フォルダ */
	private GDriveFile parent;

	/** コンストラクタ */
	public GDriveFile() {}

	/** @return id ファイルID */
	public String getId() {
		return this.id;
	}

	/** @return name ファイル名 */
	public String getName() {
		return this.name;
	}

	/** @return path ファイルオブジェクト */
	public Path getPath() {
		return this.path;
	}

	/** @return parent ファイルオブジェクト */
	public GDriveFile getParent() {
		return this.parent;
	}

	/** @param _id ファイルID */
	public void setId(String _id) {
		this.id = _id;
	}

	/** @param _name ファイル名 */
	public void setName(String _name) {
		this.name = _name;
	}

	/** @param _path ファイルオブジェクト */
	public void setPath(Path _path) {
		this.path = _path;
	}

	/** @param _path ファイルオブジェクト */
	public void setParent(GDriveFile _parent) {
		this.parent = _parent;
	}

	@Override
	public String toString() {
		return new StringBuilder("GDriveFile [")
				.append("id=").append(this.id)
				.append(", name=").append(this.name)
				.append(", path=").append(this.path)
				.append(", parent=").append(this.parent)
				.append("]").toString();
	}
}
