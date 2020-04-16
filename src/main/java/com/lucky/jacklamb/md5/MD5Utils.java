package com.lucky.jacklamb.md5;

import org.apache.commons.codec.digest.DigestUtils;

public class MD5Utils {

    public static void main(String[] args) {
        System.out.println(DigestUtils.md5Hex("付康"));
    }
}
