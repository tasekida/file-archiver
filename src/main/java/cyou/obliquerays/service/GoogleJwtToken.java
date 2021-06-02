/**
 *  Copyright 2021 tasekida
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
package cyou.obliquerays.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Provider.Service;
import java.security.Security;
import java.security.Signature;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import cyou.obliquerays.media.config.RadioProperties;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;

/**
 * GoogleAPIのJWTトークンを取得
 */
public class GoogleJwtToken implements Supplier<String> {
    /** ロガー */
    private static final Logger LOGGER = Logger.getLogger(GoogleJwtToken.class.getName());
    /** 改行文字列の正規表現 */
    private static final Pattern LINE_FEED = Pattern.compile("\n");
    /** 秘密鍵開始位置の正規表現 */
    private static final Pattern BEGIN_PRIVATE_KEY = Pattern.compile("-----BEGIN ([A-Z ]+)-----");
    /** 秘密鍵終了位置の正規表現 */
    private static final Pattern END_PRIVATE_KEY = Pattern.compile("-----END ([A-Z ]+)-----");
    /** RSA暗号鍵の仕様 */
    private static final Service RSA_SPEC = Security.getProvider("SunRsaSign").getService("KeyFactory", "RSA");
    /** SHA256withRSA署名の仕様 */
    private static final Service SHA256withRSA_SPEC = Security.getProvider("SunRsaSign").getService("Signature", "SHA256withRSA");
    /** BASE64URLエンコーダー */
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();

    /** インスタンス */
	private static GoogleJwtToken INSTANCE;

	/** サービスアカウントのメールアドレス */
	private final String strServiceAccountsIssuer;
	/** サービスアカウントの秘密鍵 */
	private final PrivateKey privateKey;
	/** JWTヘッダー */
	private final String base64Header;

	/**
	 * コンストラクタ
	 * @throws Exception GoogleAPIのJWTトークン取得処理の初期化に失敗
	 */
	private GoogleJwtToken() {
		String strServiceAccountsJson = Objects.requireNonNull(RadioProperties.getProperties().getProperty("service.accounts.json"));
		this.strServiceAccountsIssuer = Objects.requireNonNull(RadioProperties.getProperties().getProperty("service.accounts.issuer"));

		try (InputStream in = this.getInputStream(strServiceAccountsJson);
				Jsonb jsonb = JsonbBuilder.create(new JsonbConfig().withFormatting(false))) {
			@SuppressWarnings("unchecked")
			Map<String, String> mapServiceAccount = jsonb.fromJson(in, Map.class);
			String strPrivateKey = mapServiceAccount.get("private_key");
			strPrivateKey = LINE_FEED.splitAsStream(strPrivateKey)
					.filter(s -> !BEGIN_PRIVATE_KEY.matcher(s).matches())
					.filter(s -> !END_PRIVATE_KEY.matcher(s).matches())
					.collect(Collectors.joining());
			byte[] bytePrivateKey = Base64.getDecoder().decode(strPrivateKey);
			KeySpec keySpec = new PKCS8EncodedKeySpec(bytePrivateKey);
			KeyFactory keyFactory = KeyFactory.getInstance(RSA_SPEC.getAlgorithm(), RSA_SPEC.getProvider());
			this.privateKey = keyFactory.generatePrivate(keySpec);

			String headerJson = jsonb.toJson(Map.of("alg", "RS256", "typ", "JWT"));
			this.base64Header = BASE64_URL_ENCODER.encodeToString(headerJson.getBytes(StandardCharsets.UTF_8));
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "GoogleAPIのJWTトークン取得処理の初期化に失敗", e);
			throw new IllegalStateException(e);
		}
	}

	/**
	 * GoogleAPIのJWTトークンを取得
	 */
	@Override
	public String get() {
		try (Jsonb jsonb = JsonbBuilder.create(new JsonbConfig().withFormatting(false))) {

			Instant now = Instant.now();
			String claimJson = jsonb.toJson(
					Map.of("iss", this.strServiceAccountsIssuer
							, "scope", "https://www.googleapis.com/auth/drive.file"
							, "aud", "https://oauth2.googleapis.com/token"
							, "exp", now.plusSeconds(30L).getEpochSecond()
							, "iat", now.getEpochSecond()));
			String base64Claim = BASE64_URL_ENCODER.encodeToString(claimJson.getBytes(StandardCharsets.UTF_8));

			Signature sha256withRsa = Signature.getInstance(SHA256withRSA_SPEC.getAlgorithm(), SHA256withRSA_SPEC.getProvider());
			sha256withRsa.initSign(this.privateKey);
			sha256withRsa.update(this.base64Header.getBytes(StandardCharsets.UTF_8));
			sha256withRsa.update((byte)46);
			sha256withRsa.update(base64Claim.getBytes(StandardCharsets.UTF_8));
	        byte[] signatureBytes = sha256withRsa.sign();
	        String base64Signature = BASE64_URL_ENCODER.encodeToString((signatureBytes));

	        String signedJwt = new StringJoiner(".")
	        		.add(this.base64Header)
	        		.add(base64Claim)
	        		.add(base64Signature)
	        		.toString();

	        LOGGER.log(Level.CONFIG, "GoogleAPIのJWTトークン=" + signedJwt);
			return signedJwt;
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "GoogleAPIのJWTトークン取得失敗", e);
			throw new IllegalStateException(e);
		}
	}

	/**
	 * ファイル読み取りストリームへのアクセス
	 * @param _fileName ファイル名
	 * @return ファイル読み取りストリーム
	 * @throws IOException 読み取りエラー
	 */
	private InputStream getInputStream(String _serviceAccountsJson) throws IOException {
		Objects.requireNonNull(_serviceAccountsJson);
		Path file = Path.of("/data/config", _serviceAccountsJson).toAbsolutePath().normalize();
		if (Files.exists(file, LinkOption.NOFOLLOW_LINKS)) {
			return Files.newInputStream(file, StandardOpenOption.READ);
		} else {
			return ClassLoader.getSystemResourceAsStream(_serviceAccountsJson);
		}
	}

	/**
	 * インスタンス取得
	 * @return インスタンス取得へアクセス
	 */
	public static GoogleJwtToken getInstance() {
		if (null == INSTANCE) {
			synchronized (GoogleJwtToken.class) {
				if (null == INSTANCE) {
					INSTANCE = new GoogleJwtToken();
				}
			}
		}
		return INSTANCE;
	}
}
