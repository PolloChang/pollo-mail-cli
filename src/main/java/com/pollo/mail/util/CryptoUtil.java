package com.pollo.mail.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.net.NetworkInterface;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Enumeration;

public class CryptoUtil {
    private static final String ALGORITHM = "AES";
    private static final byte[] FIXED_SALT = "PolloMailCLI_Salt2026!".getBytes();
    private static final String SECRET_KEY;

    static {
        SECRET_KEY = generateMachineBoundKey();
    }

    private static String generateMachineBoundKey() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(System.getProperty("user.name")).append("@").append(System.getProperty("os.name"));
            
            // Extract MAC Address as hardware identifier
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces != null && networkInterfaces.hasMoreElements()) {
                NetworkInterface ni = networkInterfaces.nextElement();
                byte[] hardwareAddress = ni.getHardwareAddress();
                if (hardwareAddress != null && !ni.isLoopback()) {
                    for (byte b : hardwareAddress) {
                        sb.append(String.format("%02X", b));
                    }
                    break; 
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return System.getProperty("user.name") + "@fallbackKeyPollo123";
        }
    }

    private static SecretKeySpec getSecretKey() throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(SECRET_KEY.toCharArray(), FIXED_SALT, 65536, 256);
        return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), ALGORITHM);
    }

    public static String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) return plainText;
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey());
            byte[] encryptedText = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(encryptedText);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt password", e);
        }
    }

    public static String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) return encryptedText;
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey());
            byte[] decryptedText = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decryptedText);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt password. The configuration might have been created on another machine.", e);
        }
    }
}
