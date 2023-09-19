package com.DS.server.content;

import com.DS.utils.CreateMessage;
import com.DS.utils.clock.LamportClock;
import com.DS.utils.fileScanner.ReadFile;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.Timer;

public class ContentServer {
    static LamportClock clock = new LamportClock(0L);

    public static void main(String[] args) {
        int port = 4567;
        if (args.length == 0) {
            System.out.println("Command: java ContentServer <port>, default port: 4567");
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
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String input = scanner.nextLine();
                System.out.println(input);
                socket = new Socket("localhost", port);

                inputStreamReader = new InputStreamReader(socket.getInputStream());
                outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());

                bufferedReader = new BufferedReader(inputStreamReader);
                bufferedWriter = new BufferedWriter(outputStreamWriter);
                
                if ("PUT".equalsIgnoreCase(input)) {
                    String msgToSend = CreateMessage.makeWholeMessage("PUT", null);
//                    System.out.println(msgToSend);
                    bufferedWriter.write(msgToSend);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                }
//
//                bufferedWriter.write(msgToSend);
//                bufferedWriter.write("Clock:" + clock.getNextNumber(clock.getMaxInCurrentProcess()));
//                bufferedWriter.newLine();
//                bufferedWriter.flush();
                System.out.println(clock.getMaxInCurrentProcess());

                StringBuilder msgFromServer = new StringBuilder();
                String line = bufferedReader.readLine();
                while (line != null && !line.isEmpty()) {
                    msgFromServer.append(line).append("\n");
                    line = bufferedReader.readLine();
                }
                System.out.println(msgFromServer);
                msgFromServer.setLength(0);
                
                /*if ("alive?".equals(msgFromServer)){
//                    bufferedWriter.write(" ");
                    bufferedWriter.write("still alive!\n");
                    bufferedWriter.flush(); 
                    System.out.println("--^v--♡--^v--Heartbeat Check--^v--♡--^v--");
                    continue;
                }
                String clockFromServer = msgFromServer.substring(msgFromServer.indexOf("Clock:") + 6);
                msgFromServer = msgFromServer.substring(0, msgFromServer.indexOf("Clock:"));
                System.out.println("AggregationServer: " + msgFromServer);
                System.out.println("ClockFromAggregationServer: " + clockFromServer);

                clock.getNextNumber(Long.valueOf(clockFromServer));
                System.out.println("Content Server clock:" + clock.getMaxInCurrentProcess());*/

                if (input.equalsIgnoreCase("BYE"))
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                System.out.println("Connection stopped!");
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
