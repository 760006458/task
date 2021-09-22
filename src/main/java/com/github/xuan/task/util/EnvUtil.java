package com.github.xuan.task.util;

import java.net.InetAddress;

public class EnvUtil {
    private static String localIp;

    public static String getLocalIp() {
        if (localIp == null) {
            try {
                localIp = InetAddress.getLocalHost().getHostAddress();
            } catch (Exception e) {
                localIp = "127.0.0.1";
            }
        }
        return localIp;
    }
}
