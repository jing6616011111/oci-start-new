package com.ocistart.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

@Slf4j
public class IpUtils {

    public static String getPublicIp() {
        try {
            URL url = new URL("https://api.ipify.org");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            byte[] bytes = new byte[64];
            int len = conn.getInputStream().read(bytes);
            if (len > 0) {
                return new String(bytes, 0, len).trim();
            }
        } catch (Exception e) {
            log.warn("获取公网 IP 失败：{}", e.getMessage());
        }
        return "0.0.0.0";
    }

    public static boolean isReachable(String ip, int timeout) {
        try {
            return InetAddress.getByName(ip).isReachable(timeout);
        } catch (Exception e) {
            return false;
        }
    }

    public static String getHostIp() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "127.0.0.1";
        }
    }
}
