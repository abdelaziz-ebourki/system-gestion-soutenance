package com.system_gestion_soutenance.api.common.util;

import jakarta.annotation.PostConstruct;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EncryptionUtil {

	private static final String AES_GCM_ALGORITHM = "AES/GCM/NoPadding";
	private static final int GCM_IV_LENGTH = 12;
	private static final int GCM_TAG_LENGTH = 128;

	@Value("${app.encryption.master-key}")
	private String masterKey;

	private SecretKey secretKey;

	@PostConstruct
	public void init() {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] keyBytes = digest.digest(masterKey.getBytes(StandardCharsets.UTF_8));
			this.secretKey = new SecretKeySpec(keyBytes, "AES");
		} catch (Exception e) {
			throw new IllegalStateException("Failed to initialize encryption key", e);
		}
	}

	public String encrypt(String plainText) {
		if (plainText == null) {
			return null;
		}
		try {
			byte[] iv = new byte[GCM_IV_LENGTH];
			SecureRandom.getInstanceStrong().nextBytes(iv);

			Cipher cipher = Cipher.getInstance(AES_GCM_ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));

			byte[] ciphertext = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

			ByteBuffer buffer = ByteBuffer.allocate(iv.length + ciphertext.length);
			buffer.put(iv);
			buffer.put(ciphertext);
			return Base64.getEncoder().encodeToString(buffer.array());
		} catch (Exception e) {
			throw new IllegalStateException("Encryption failed", e);
		}
	}

	public String decrypt(String cipherText) {
		if (cipherText == null) {
			return null;
		}
		try {
			byte[] decoded = Base64.getDecoder().decode(cipherText);
			ByteBuffer buffer = ByteBuffer.wrap(decoded);

			byte[] iv = new byte[GCM_IV_LENGTH];
			buffer.get(iv);

			byte[] ciphertext = new byte[buffer.remaining()];
			buffer.get(ciphertext);

			Cipher cipher = Cipher.getInstance(AES_GCM_ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH, iv));

			return new String(cipher.doFinal(ciphertext), StandardCharsets.UTF_8);
		} catch (Exception e) {
			throw new IllegalStateException("Decryption failed", e);
		}
	}
}
