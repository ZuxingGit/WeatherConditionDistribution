package com.DS.server.aggregation;

import com.DS.utils.clock.LamportClock;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class AggregationServer {
    static LamportClock clock = new LamportClock(0L);

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
                    String clockFromClient = msgFromClient.substring(msgFromClient.indexOf("Clock:") + 6);
                    msgFromClient = msgFromClient.substring(0, msgFromClient.indexOf("Clock:"));
                    System.out.println("Client: " + msgFromClient);
                    System.out.println("clockFromClient: " + clockFromClient);
                    System.out.println("Clock:" + clock.getNextNumber(Long.valueOf(clockFromClient)));

                    bufferedWriter.write("MSG Received.");
                    bufferedWriter.write("Clock:" + clock.getNextNumber(clock.getMaxInCurrentProcess()));
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
