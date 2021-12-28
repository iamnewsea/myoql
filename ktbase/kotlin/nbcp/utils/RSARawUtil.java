package nbcp.utils;

import javax.crypto.Cipher;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * https://juejin.cn/post/6844904116628488205
 */
public class RSARawUtil {
    public static String RSA_ALGORITHM = "RSA";

    /**
     * 密钥长度，DSA算法的默认密钥长度是1024
     * 密钥长度必须是64的倍数，在512到65536位之间
     */
    private static final int KEY_SIZE = 1024;

    private RSARawUtil() {
    }

    /**
     * 生成密钥对
     *
     * @return 密钥对对象
     * @throws NoSuchAlgorithmException
     */
    public static RSARawUtil create() {
        //KeyPairGenerator用于生成公钥和私钥对。密钥对生成器是使用 getInstance 工厂方法
        KeyPairGenerator keyPairGenerator = null;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance(RSA_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage());
        }
        keyPairGenerator.initialize(KEY_SIZE);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        RSARawUtil rsa = new RSARawUtil();
        rsa.privateKey = (RSAPrivateKey) keyPair.getPrivate();
        rsa.publicKey = (RSAPublicKey) keyPair.getPublic();
        return rsa;
    }


    private RSAPublicKey publicKey;
    private RSAPrivateKey privateKey;

    /**
     * 获取私钥
     *
     * @return
     */
    public byte[] getPrivateKeyBytes() {
        return this.privateKey.getEncoded();
    }

    /**
     * 获取公钥
     *
     * @return
     */
    public byte[] getPublicKeyBytes() {
        return this.publicKey.getEncoded();
    }


    public String getPrivateKeyString() {
        return java.util.Base64.getEncoder().encodeToString(this.getPrivateKeyBytes());
    }

    /**
     * 获取公钥
     *
     * @return
     */
    public String getPublicKeyString() {
        return java.util.Base64.getEncoder().encodeToString(this.getPublicKeyBytes());
    }


    /**
     * 私钥加密
     *
     * @param data 待加密数据
     * @return byte[] 加密数据
     */
    public byte[] encryptByPrivateKey(byte[] data) {
        return encryptByPrivateKey(data, this.getPrivateKeyBytes());
    }

    /**
     * 公钥加密
     *
     * @param data
     */
    public byte[] encryptByPublicKey(byte[] data) {
        return encryptByPublicKey(data, this.getPublicKeyBytes());
    }

    /**
     * 私钥解密
     *
     * @param data 待解密数据
     * @return byte[] 解密数据
     */
    public byte[] decryptByPrivateKey(byte[] data) {
        return decryptByPrivateKey(data, this.getPrivateKeyBytes());
    }

    /**
     * 公钥解密
     *
     * @param data 待解密数据
     * @return byte[] 解密数据
     */
    public byte[] decryptByPublicKey(byte[] data) {
        return decryptByPublicKey(data, this.getPublicKeyBytes());
    }


    /**
     * 私钥加密
     *
     * @param data 待加密数据
     * @return byte[] 加密数据
     */
    public static byte[] encryptByPrivateKey(byte[] data, byte[] key) {
        try {
            //取得私钥
            PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(key);
            KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
            //生成私钥
            PrivateKey privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
            //数据加密
            Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 公钥加密
     *
     * @param data
     */
    public static byte[] encryptByPublicKey(byte[] data, byte[] key) {
        try {
            //实例化密钥工厂
            KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
            //初始化公钥,根据给定的编码密钥创建一个新的 X509EncodedKeySpec。
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(key);
            PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec);
            //数据加密
            Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 私钥解密
     *
     * @param data 待解密数据
     * @return byte[] 解密数据
     */
    public static byte[] decryptByPrivateKey(byte[] data, byte[] key) {
        try {
            //取得私钥
            PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(key);
            KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
            //生成私钥
            PrivateKey privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
            //数据解密
            Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * 公钥解密
     *
     * @param data 待解密数据
     * @return byte[] 解密数据
     */
    public static byte[] decryptByPublicKey(byte[] data, byte[] key) {
        try {

            //实例化密钥工厂
            KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
            //初始化公钥
            //密钥材料转换
            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(key);
            //产生公钥
            PublicKey pubKey = keyFactory.generatePublic(x509KeySpec);
            //数据解密
            Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
            cipher.init(Cipher.DECRYPT_MODE, pubKey);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}