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

import java.util.Arrays;

/**
 * Google Drive上のリソース
 */
public class GDriveResource {

	/** Google Drive ファイルID */
	private String id;

	/** ファイル名 */
	private String name;

	/** 親フォルダID */
	private String[] parents;

	/** コンストラクタ */
	public GDriveResource() {}

	/** @return id ファイルID */
	public String getId() {
		return this.id;
	}

	/** @return name ファイル名 */
	public String getName() {
		return this.name;
	}

	/** @return parents 親フォルダID */
	public String[] getParents() {
		return this.parents;
	}

	/** @param _id ファイルID */
	public void setId(String _id) {
		this.id = _id;
	}

	/** @param _name ファイル名 */
	public void setName(String _name) {
		this.name = _name;
	}

	/** @param _parents 親フォルダID */
	public void setParents(String[] _parents) {
		this.parents = _parents;
	}

	@Override
	public String toString() {
		return new StringBuilder()
			.append("id=").append(this.id)
			.append(", name=").append(this.name)
			.append(", parents=").append(Arrays.toString(this.parents))
			.toString();
	}
}
