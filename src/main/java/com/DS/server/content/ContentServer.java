package com.DS.server.content;

import com.DS.utils.clock.LamportClock;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.Timer;

public class ContentServer {
    static LamportClock clock = new LamportClock(0L);
    
    public static void main(String[] args) {
        int port = 4567;
        if (args.length == 0) {
            System.out.println("Command: java SimpleSocketServer <port>, default port: 4567");
        } else {
            port = Integer.parseInt(args[0]);
        }
        System.out.println("Connect Aggregation-server on port: " + port);
        PUT(port);
    }
    
    public static void PUT(int port) {
        Socket socket = null;
        InputStreamReader inputStreamReader = null;
        OutputStreamWriter outputStreamWriter = null;
        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;

        try {
            socket = new Socket("localhost", port);

            inputStreamReader = new InputStreamReader(socket.getInputStream());
            outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());

            bufferedReader = new BufferedReader(inputStreamReader);
            bufferedWriter = new BufferedWriter(outputStreamWriter);

            Scanner scanner = new Scanner(System.in);
            while (true) {
//                String msgToSend = scanner.nextLine();
//
//                bufferedWriter.write(msgToSend);
//                bufferedWriter.write("Clock:" + clock.getNextNumber(clock.getMaxInCurrentProcess()));
//                bufferedWriter.newLine();
//                bufferedWriter.flush();
                System.out.println(clock.getMaxInCurrentProcess());

                String msgFromServer = bufferedReader.readLine();
                if ("alive?".equals(msgFromServer)){
//                    bufferedWriter.write(" ");
                    bufferedWriter.write("still alive!\n");
                    bufferedWriter.flush(); 
                    System.out.println("--^v--♡--^v--Heartbeat Check");
                    continue;
                }
                String clockFromServer = msgFromServer.substring(msgFromServer.indexOf("Clock:") + 6);
                msgFromServer = msgFromServer.substring(0, msgFromServer.indexOf("Clock:"));
                System.out.println("AggregationServer: " + msgFromServer);
                System.out.println("ClockFromAggregationServer: " + clockFromServer);

                clock.getNextNumber(Long.valueOf(clockFromServer));
                System.out.println("Content Server clock:" + clock.getMaxInCurrentProcess());

//                if (msgToSend.equalsIgnoreCase("BYE"))
//                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (socket != null)
                    socket.close();
                if (inputStreamReader != null)
                    inputStreamReader.close();
                if (outputStreamWriter != null)
                    outputStreamWriter.close();
                if (bufferedReader != null)
                    bufferedReader.close();
                if (bufferedWriter != null)
                    bufferedWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
