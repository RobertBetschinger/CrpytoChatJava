package de.ur.pdits.cryptchat.security;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

public class Encryption {

    private Cipher cipherEnc;
    private Cipher cipherDec;

    public Encryption(SecretKey symKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
        // TODO: Init ciphers
        // Wir initiieren immer mit einem leeren Vektor
        byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        //Initierung von den Enc und Dec Objekten
        IvParameterSpec iniVektor = new IvParameterSpec(iv);
        cipherEnc = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipherEnc.init(Cipher.ENCRYPT_MODE, symKey, iniVektor);

        cipherDec = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipherDec.init(Cipher.DECRYPT_MODE, symKey, iniVektor);
    }


    /**
     * @param plaintext The plaintext to be encrypted
     * @return The plaintext encrypted with the provided symKey by the cipher
     * defined inside the constructor
     */
    public byte[] encryptSymmetrically(byte[] plaintext) throws IllegalBlockSizeException, BadPaddingException {
        // TODO: Execute encryption
        //Verschlüsselung des übergebenen Klartextes
        byte[] encryptedText;
        encryptedText = this.cipherEnc.doFinal(plaintext);
        return encryptedText;
    }

    /**
     * @param ciphertext The ciphertext to be decrypted
     * @return The ciphertext decrypted with the provided symKey by the cipher
     * defined inside the constructor
     */
    public byte[] decryptSymmetrically(byte[] ciphertext) throws IllegalBlockSizeException, BadPaddingException {
        // TODO: Execute decryption
        //Lediglich ein Aufruf der Decryption Methode notwenig
       return cipherDec.doFinal(ciphertext);
    }

}
