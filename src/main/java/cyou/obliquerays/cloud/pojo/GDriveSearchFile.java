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

import java.util.ArrayList;
import java.util.List;

/**
 * Google DriveのFile情報を保持するオブジェクト
 */
public final class GDriveSearchFile {

	private List<GDriveFile> files = new ArrayList<>(0);

	/** @param _files ファイル一覧 */
	public void setFiles(List<GDriveFile> _files) {
		this.files.clear();
		if (null != _files && !_files.isEmpty()) {
			this.files.addAll(_files);
		}
	}

	/** @return files ファイル一覧 */
	public List<GDriveFile> getFiles() {
		return List.copyOf(this.files);
	}
}
