package ndbc;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import com.google.api.client.util.Base64;

public class AES
{
    
    private static SecretKeySpec secretKey ;
    private static byte[] key ;
    
    private static String decryptedString;
    private static String encryptedString;
    
    public static void setKey(String myKey){
        
   
        MessageDigest sha = null;
        try {
            key = myKey.getBytes("UTF-8");
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16); // use only first 128 bit
            secretKey = new SecretKeySpec(key, "AES");       
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    public static String getDecryptedString() {
        return decryptedString;
    }
    public static void setDecryptedString(String decryptedString) {
        AES.decryptedString = decryptedString;
    }
    public static String getEncryptedString() {
        return encryptedString;
    }
    public static void setEncryptedString(String encryptedString) {
        AES.encryptedString = encryptedString;
    }
    public static String encrypt(String strToEncrypt)
    {
        try
        {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            setEncryptedString(Base64.encodeBase64String(cipher.doFinal(strToEncrypt.getBytes("UTF-8"))));
        
        }
        catch (Exception e)
        {
            System.out.println("Error while encrypting: "+e.toString());
        }
        return encryptedString;
    }
    public static String decrypt(String strToDecrypt)
    {
        try
        {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            setDecryptedString(new String(cipher.doFinal(Base64.decodeBase64(strToDecrypt))));
            
        }
        catch (Exception e)
        {
         
            System.out.println("Error while decrypting: "+e.toString());
        }
        return decryptedString;
    }
    public static void main(String args[])
    {
    	
    	
    	/*
                final String strToEncrypt = "AQU:101.71-103.28-91.40 AIN:99.97-90.64-106.26 TAN:99.56-85.81-92.27";
                final String strPssword = "thisismykey";
                AES.setKey(strPssword);
               
                AES.encrypt(strToEncrypt.trim());
                
                System.out.println("String to Encrypt: " + strToEncrypt); 
                System.out.println("Encrypted: " + AES.getEncryptedString());
           
                final String strToDecrypt =  AES.getEncryptedString();
                AES.decrypt(strToDecrypt.trim());
               
                System.out.println("String To Decrypt : " + strToDecrypt);
                System.out.println("Decrypted : " + AES.getDecryptedString());
                */
        
    }
     
}
  