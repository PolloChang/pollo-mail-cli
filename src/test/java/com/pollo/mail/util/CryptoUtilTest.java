package com.pollo.mail.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CryptoUtilTest {

    @Test
    void testEncryptAndDecrypt() {
        String originalPassword = "MySuperSecretPassword123!";
        
        String encrypted = CryptoUtil.encrypt(originalPassword);
        assertNotNull(encrypted);
        assertNotEquals(originalPassword, encrypted);
        
        String decrypted = CryptoUtil.decrypt(encrypted);
        assertEquals(originalPassword, decrypted);
    }
    
    @Test
    void testNullOrEmpty() {
        assertNull(CryptoUtil.encrypt(null));
        assertNull(CryptoUtil.decrypt(null));
        
        assertEquals("", CryptoUtil.encrypt(""));
        assertEquals("", CryptoUtil.decrypt(""));
    }
}
