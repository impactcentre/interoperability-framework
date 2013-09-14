package eu.impact_project.wsclient;

import java.io.UnsupportedEncodingException;
import javax.crypto.*;

import org.apache.commons.codec.binary.Base64;

public class EncryptDecrypt {
	private Cipher ecipher;
	private Cipher dcipher;
    private Base64 base64codec;

	EncryptDecrypt(SecretKey key) {
		try {
            base64codec = new Base64();
			ecipher = Cipher.getInstance("DES");
			dcipher = Cipher.getInstance("DES");
			ecipher.init(Cipher.ENCRYPT_MODE, key);
			dcipher.init(Cipher.DECRYPT_MODE, key);

		} catch (javax.crypto.NoSuchPaddingException e) {
		} catch (java.security.NoSuchAlgorithmException e) {
		} catch (java.security.InvalidKeyException e) {
		}
	}

	public String encrypt(String str) {
		try {
			// Encode the string into bytes using utf-8
			byte[] utf8 = str.getBytes("UTF8");

			// Encrypt
			byte[] enc = ecipher.doFinal(utf8);

			// Encode bytes to base64 to get a string
			return base64codec.encodeToString(enc);
		} catch (javax.crypto.BadPaddingException e) {
		} catch (IllegalBlockSizeException e) {
		} catch (UnsupportedEncodingException e) {
		}
        return null;
	}

	public String decrypt(String str) {
		try {
			// Decode base64 to get bytes
			byte[] dec = base64codec.decode(str);

			// Decrypt
			byte[] utf8 = dcipher.doFinal(dec);

			// Decode using utf-8
			return new String(utf8, "UTF8");
		} catch (javax.crypto.BadPaddingException e) {
		} catch (IllegalBlockSizeException e) {
		} catch (UnsupportedEncodingException e) {
		}
        return null;
	}

	public static void main(String[] args) {
		try {
			// Generate a temporary key. In practice, you would save this key.
			// See also e464 Encrypting with DES Using a Pass Phrase.
			SecretKey key = KeyGenerator.getInstance("DES").generateKey();

			// Create encrypter/decrypter class
			EncryptDecrypt encrypter = new EncryptDecrypt(key);

			// Pass variable I should get from the form with the
			// request.getParameter("Pass");
			String Pass = "Password"; // Just to make it work

			// Encrypt
			String encrypted = encrypter.encrypt(Pass);

			// Decrypt
			String decrypted = encrypter.decrypt(encrypted);

			// Output

			System.out.println("Encrypted: " + encrypted);
			System.out.println("Decrypted: " + decrypted);

		} catch (Exception e) {
		}
	}
}