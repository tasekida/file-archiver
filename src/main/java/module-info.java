/**
 *  ラジオ録音ファイルの保存
 */
module radio.archive {
	opens cyou.obliquerays.logging to java.logging;

	requires java.logging;
	requires java.net.http;
	requires jakarta.json.bind;
}