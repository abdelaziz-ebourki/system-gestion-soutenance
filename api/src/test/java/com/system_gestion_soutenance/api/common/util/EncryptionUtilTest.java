package com.system_gestion_soutenance.api.common.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {EncryptionUtil.class})
@TestPropertySource(properties = "app.encryption.master-key=test-master-key-1234567890123456")
class EncryptionUtilTest {

	@Autowired
	private EncryptionUtil encryptionUtil;

	@Test
	void encryptDecryptRoundTrip() {
		String plainText = "Hello World 123!";
		String cipherText = encryptionUtil.encrypt(plainText);

		assertNotNull(cipherText);
		assertNotEquals(plainText, cipherText);

		String decryptedText = encryptionUtil.decrypt(cipherText);
		assertEquals(plainText, decryptedText);
	}

	@Test
	void encryptNullReturnsNull() {
		assertNull(encryptionUtil.encrypt(null));
	}

	@Test
	void decryptNullReturnsNull() {
		assertNull(encryptionUtil.decrypt(null));
	}

	@Test
	void encryptProducesDifferentCipherTextsForSamePlaintext() {
		String plainText = "Same Text";
		String cipherText1 = encryptionUtil.encrypt(plainText);
		String cipherText2 = encryptionUtil.encrypt(plainText);

		assertNotEquals(cipherText1, cipherText2);
	}

	@Test
	void decryptInvalidCipherTextThrowsException() {
		String invalidCipherText = "not-a-valid-base64-or-cipher";
		assertThrows(IllegalStateException.class, () -> encryptionUtil.decrypt(invalidCipherText));
	}
}
