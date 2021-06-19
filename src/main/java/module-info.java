/**
 *  ラジオ録音ファイルの保存
 */
module radio.archive {
	opens cyou.obliquerays.logging to java.logging;
	exports cyou.obliquerays.cloud.pojo;

	requires java.logging;
	requires java.net.http;
	requires jakarta.json.bind;
	requires google.http.client;
	requires google.api.services.drive.v3.rev197;
	requires google.api.client;
	requires google.oauth.client;
	requires google.oauth.client.java6;
	requires google.http.client.jackson2;
	requires google.oauth.client.jetty;
}