package com.DS;

public class Main {
    public static void main(String[] args) {

        System.out.println(args.length);
        String s="Content-Length: 23 \n{";
//        System.exit(1);
        System.out.println(s.substring(s.indexOf("Content-Length:")+15, s.indexOf("{")).trim());
    }
}