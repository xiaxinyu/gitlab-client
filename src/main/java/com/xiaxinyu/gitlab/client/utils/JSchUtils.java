package com.xiaxinyu.gitlab.client.utils;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 生成RSA秘钥对，公钥ssh-rsa, 私钥RSA private key
 *
 * @author XIAXINYU3
 * @date 2019.7.5
 */
public class JSchUtils {
    public static final String PUBLIC_KEY = "public-key";
    public static final String PRIVATE_KEY = "private-key";

    public static Map<String, String> genKeyPair(String comment) throws JSchException {
        Map<String, String> keys = new HashMap<>(2);
        KeyPair keyPair = KeyPair.genKeyPair(new JSch(), KeyPair.RSA);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        //私钥，向OutPutStream中写入
        keyPair.writePrivateKey(outputStream);
        String privateKeyString = outputStream.toString();
        //公钥，向OutPutStream中写入
        outputStream = new ByteArrayOutputStream();
        keyPair.writePublicKey(outputStream, comment);
        String publicKeyString = outputStream.toString();

        // 得到公钥字符串
        keys.put(PUBLIC_KEY, publicKeyString);
        // 得到私钥字符串
        keys.put(PRIVATE_KEY, privateKeyString);

        keyPair.dispose();
        return keys;
    }

    public static Map<String, String> load(String publicKeyPath, String comment) throws JSchException {
        Map<String, String> keys = new HashMap<>(2);

        KeyPair keyPair = KeyPair.load(new JSch(), publicKeyPath);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        //私钥，向OutPutStream中写入
        keyPair.writePrivateKey(outputStream);
        String privateKeyString = outputStream.toString();
        //公钥，向OutPutStream中写入
        outputStream = new ByteArrayOutputStream();
        keyPair.writePublicKey(outputStream, comment);
        String publicKeyString = outputStream.toString();

        // 得到公钥字符串
        keys.put(PUBLIC_KEY, publicKeyString);
        // 得到私钥字符串
        keys.put(PRIVATE_KEY, privateKeyString);


        System.out.println("keyComment:" + keyPair.getPublicKeyComment());

        keyPair.dispose();
        return keys;
    }
}
