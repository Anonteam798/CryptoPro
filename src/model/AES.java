package model;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import java.security.InvalidKeyException;
import java.security.Key;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import javax.crypto.SecretKeyFactory;

import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

/**
 *
 * @author h4ck3r
 */
public class AES {

    private final String ALGORITHM = "AES";
    private final String ALGORITHM_TYPE = "AES";
    private final String PASSWORD_ALGORITHM = "PBKDF2WithHmacSHA256";
    private final String SALT = "&/()=?IO";
    private final int IV_SIZE = 16;
 
    private final int ITERATIONS = 65536;

    public AES() {
    }
    /*
    private byte[] generateIv() {
        byte[] ivBytes = new byte[IV_SIZE];
        new SecureRandom().nextBytes(ivBytes);
        return ivBytes;
        //iv = new IvParameterSpec(ivBytes);

    }
    */

    public Key generateSymetricKey(String plainKey) {
        Key k = null;
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(PASSWORD_ALGORITHM);
            KeySpec spec = new PBEKeySpec(plainKey.toCharArray(), SALT.getBytes(), ITERATIONS, 256);
            k = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), ALGORITHM);
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        } catch (InvalidKeySpecException ex) {
            ex.printStackTrace();
        }
        return k;

    }

    public boolean saveKeyInFile(String filePath, Key k) {
        boolean saved = false;
        String fileName = "/password.kzard";
        try ( FileOutputStream fos = new FileOutputStream(new File(filePath + fileName))) {

            byte[] passwordBytes = Base64.getEncoder().encode(k.getEncoded());
            fos.write(passwordBytes);
            saved = true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return saved;
    }

    public Key loadKeyFromFile(String filePath) {
        Key keyLoaded = null;
        try ( FileInputStream fis = new FileInputStream(new File(filePath))) {
            byte[] passwordLoaded = Base64.getDecoder().decode(fis.readAllBytes());
            keyLoaded = new SecretKeySpec(passwordLoaded, ALGORITHM);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }
        return keyLoaded;
    }

    public byte[] readBytesFromFile(File filePath) {
        byte[] bytesRead = null;
        try {
            bytesRead = Files.readAllBytes(filePath.toPath());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return bytesRead;
    }

    public void storeFile(byte[] arr, String fileName, JProgressBar bar) {
        double percentage = 0;
        try ( InputStream inputStr = new ByteArrayInputStream(arr);  FileOutputStream outputStr = new FileOutputStream(new File(
                fileName))) {

            byte[] buffer = new byte[4096];
            int lenght;

            while ((lenght = inputStr.read(buffer)) > 0) {

                outputStr.write(buffer, 0, lenght);
                percentage += (((double) lenght / arr.length) * 100);
                System.out.println(percentage);
                bar.setValue((int) percentage);

            }

        } catch (Exception es) {
            es.printStackTrace();
        }
        bar.setValue(0);

    }

    public byte[] decrypt(byte[] fileByte, Key password) throws BadPaddingException, IllegalBlockSizeException {
        byte[] decrypted = null;
        try {

            Cipher cipher = Cipher.getInstance(ALGORITHM_TYPE);
            cipher.init(Cipher.DECRYPT_MODE, password);

            decrypted = cipher.doFinal(fileByte);

        } catch (InvalidKeyException ex) {
            Logger.getLogger(AES.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(AES.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchPaddingException ex) {
            Logger.getLogger(AES.class.getName()).log(Level.SEVERE, null, ex);
        }
        return decrypted;
    }

    public byte[] encrypt(byte[] fileByte, Key password) {
        byte[] encrypted = null;

        try {

            Cipher cipher = Cipher.getInstance(ALGORITHM_TYPE);
            cipher.init(Cipher.ENCRYPT_MODE, password);

            encrypted = cipher.doFinal(fileByte);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return encrypted;
    }

}
