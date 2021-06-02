/**
 *  ラジオ録音ファイルの保存
 */
module radio.archive {
	exports cyou.obliquerays.media;
	opens cyou.obliquerays.logging to java.logging;

	requires java.logging;
	requires google.api.client;
	requires java.net.http;
	requires jakarta.json.bind;
	requires java.jwt;
}