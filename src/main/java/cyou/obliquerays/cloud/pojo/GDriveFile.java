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

import jakarta.json.bind.annotation.JsonbProperty;

/**
 *
 */
public class GDriveFile {

	/** ファイルID */
	private String id;

	/** ファイル名 */
	private String name;

	/** コンストラクタ */
	public GDriveFile() {}

	/** @return id ファイルID */
	public String getId() {
		return this.id;
	}

	/** @param _id ファイルID */
	public void setId(String _id) {
		this.id = _id;
	}

	/** @return name ファイル名 */
	public String getName() {
		return this.name;
	}

	/** @param _name ファイル名 */
	public void setName(String _name) {
		this.name = _name;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GDriveFile [id=").append(id).append(", name=").append(name).append("]");
		return builder.toString();
	}
}
