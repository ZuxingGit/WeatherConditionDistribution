package com.DS.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class GETClient {
    public static void main(String[] args) throws IOException {
        Socket s = new Socket("localhost", 4567);

        PrintWriter pr = new PrintWriter(s.getOutputStream());
        pr.println("hello server");
        pr.flush();

        InputStreamReader in = new InputStreamReader(s.getInputStream());
        BufferedReader bf = new BufferedReader(in);

        String str = bf.readLine();
        System.out.println("server: " + str);
    }
}
