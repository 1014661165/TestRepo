package security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

/**
 * 加密工具类
 */
public class CipherUtil {


    /**
     * 非对称加密-生成指定算法的公钥和私钥
     * @param keySize 密匙长度
     * @param algorithm 加密算法
     * @return 包含生成的公钥和私钥的列表
     */
    public static List<byte[]> generateKeyPair(int keySize, String algorithm){
        List<byte[]> keys = new ArrayList<>();
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(algorithm);
            generator.initialize(keySize,  new SecureRandom());
            KeyPair keyPair =  generator.genKeyPair();
            byte[] privateKey = keyPair.getPrivate().getEncoded();
            byte[] publicKey = keyPair.getPublic().getEncoded();
            keys.add(publicKey);
            keys.add(privateKey);
        }catch (Exception e){
            e.printStackTrace();
        }
        return keys;
    }

    /**
     * 非对称加密-使用公钥加密信息
     * RSA加密对信息的长度有限制，而且长度限制与密码生成的时候指定的keySize线性相关
     * 加密信息最大长度 = keySize/8 - 11
     * @param msg
     * @param publicKey
     * @return
     */
    public static byte[] encrypt(String algorithm, byte[] msg, byte[] publicKey){
        byte[] encodedMsg = null;
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
            PublicKey key = keyFactory.generatePublic(new X509EncodedKeySpec(publicKey));
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            encodedMsg = cipher.doFinal(msg);
        }catch (Exception e){
            e.printStackTrace();
        }
        return encodedMsg;
    }

    /**
     * 非对称加密-使用私钥解密信息
     * RSA解密对信息的长度有限制，而且长度限制与密码生成的时候指定的keySize线性相关
     * 解密信息最大长度 = keySize/8
     * @param algorithm
     * @param encodedMsg
     * @param privateKey
     * @return
     */
    public static byte[] decrypt(String algorithm, byte[] encodedMsg, byte[] privateKey){
        byte[] msg = null;
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(algorithm);
            PrivateKey key = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(privateKey));
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, key);
            msg = cipher.doFinal(encodedMsg);
        }catch (Exception e){
            e.printStackTrace();
        }
        return msg;
    }


    /**
     * 对称加密-生成对称加密密匙
     * @param algorithm
     * @return
     */
    public static byte[] generateKey(String algorithm){
        byte[] key = null;
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(algorithm);
            SecretKey secretKey = keyGenerator.generateKey();
            key = secretKey.getEncoded();
        }catch (Exception e){
            e.printStackTrace();
        }
        return key;
    }

    /**
     * 对称加密-使用密匙加密信息
     * @param algorithm
     * @param msg
     * @param secretKey
     * @return
     */
    public static byte[] encrypt2(String algorithm, byte[] msg, byte[] secretKey){
        byte[] encodedMsg = null;
        try {
            SecretKey key = new SecretKeySpec(secretKey, algorithm);
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            encodedMsg = cipher.doFinal(msg);
        }catch (Exception e){
            e.printStackTrace();
        }
        return encodedMsg;
    }

    /**
     * 对称加密-使用密匙解密信息
     * @param algorithm
     * @param encodedMsg
     * @param secretKey
     * @return
     */
    public static byte[] decrypt2(String algorithm, byte[] encodedMsg, byte[] secretKey){
        byte[] msg = null;
        try {
            SecretKey key = new SecretKeySpec(secretKey, algorithm);
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, key);
            msg = cipher.doFinal(encodedMsg);
        }catch (Exception e){
            e.printStackTrace();
        }
        return msg;
    }
}
