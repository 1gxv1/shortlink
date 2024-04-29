package com.chr1s.shortlink.admin;

import java.util.UUID;

public class TestUUID {
    public static void main(String[] args) {
        String s = UUID.randomUUID().toString().replaceAll("-", "");
        System.out.println(s);
    }
}
