package com.DS.server.aggregation;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class AggregationServer {
    public static void main(String[] args) throws IOException {
        Socket socket = null;
        InputStreamReader inputStreamReader = null;
        OutputStreamWriter outputStreamWriter = null;
        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;
        ServerSocket ss = null;

        ss = new ServerSocket(4567);
        while (true) {
            try {
                socket = ss.accept();

                inputStreamReader = new InputStreamReader(socket.getInputStream());
                outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());

                bufferedReader = new BufferedReader(inputStreamReader);
                bufferedWriter = new BufferedWriter(outputStreamWriter);

                
                while (true) {
                    String msgFromClient = bufferedReader.readLine();
                    System.out.println("Client: " + msgFromClient);
                  
                    bufferedWriter.write("MSG Received.");
                    bufferedWriter.newLine();
                    bufferedWriter.flush();

                    if (msgFromClient.equalsIgnoreCase("BYE"))
                        break;
                }

                socket.close();
                inputStreamReader.close();
                outputStreamWriter.close();
                bufferedReader.close();
                bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
