package com.DS.client;

import com.DS.utils.CreateMessage;
import com.DS.utils.clock.LamportClock;
import com.DS.utils.json.JSONHandler;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class GETClient {
    static LamportClock clock = new LamportClock(0L);

    public static void main(String[] args) throws IOException {
        int port = 4567;
        if (args.length == 0) {
            System.out.println("Command: java GETClient <port>, default port: 4567");
        } else {
            port = Integer.parseInt(args[0]);
        }
        System.out.println("Connect Aggregation-server on port: " + port);
        GET(port);
    }

    public static void GET(int port) {
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
                String input = scanner.nextLine();
                System.out.println();

                if ("GET".equalsIgnoreCase(input)) {
                    String msgToSend = CreateMessage.createHeader("GET", null);
//                    System.out.println(msgToSend);
                    bufferedWriter.write(msgToSend);
                    bufferedWriter.write("Clock:" + clock.getNextNumber(clock.getMaxInCurrentProcess()) + "\n");
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                    System.out.println("#Current clock: " + clock.getMaxInCurrentProcess()+ "\n");

                    StringBuilder msgFromServer = new StringBuilder();
                    String line = bufferedReader.readLine();
                    while (line != null && !line.isEmpty()) {
                        msgFromServer.append(line).append("\n");
                        line = bufferedReader.readLine();
                    }
//                    System.out.println(msgFromServer);
                    if (msgFromServer.toString().contains("404 Not Found")) {
                        System.err.println("Can't find anything on AS");
                    } else {
                        Long clockFromServer = Long.valueOf(msgFromServer.substring(msgFromServer.indexOf("Clock:") + 6).trim());
                        String json = msgFromServer.substring(msgFromServer.indexOf("{"), msgFromServer.indexOf("}") + 1);
                        System.out.println(JSONHandler.JSON2String(json));
                        System.out.println("\n#ClockFromAS: " + clockFromServer);
                        System.out.println("#Current clock: " + clock.getNextNumber(clockFromServer));
                    }
                }
                if (input.equalsIgnoreCase("BYE"))
                    break;
                    
                    /*if ("alive?".equals(msgFromServer)) {
                        msgFromServer = bufferedReader.readLine();
                    }
                    String clockFromServer = msgFromServer.substring(msgFromServer.indexOf("Clock:") + 6);
                    msgFromServer = msgFromServer.substring(0, msgFromServer.indexOf("Clock:"));
                    System.out.println("Server: " + msgFromServer);
                    System.out.println("ClockFromServer: " + clockFromServer);

                    clock.getNextNumber(Long.valueOf(clockFromServer));
                    System.out.println("Client own clock:" + clock.getMaxInCurrentProcess());
                    System.out.println("============");
                    if (msgToSend.equalsIgnoreCase("BYE")) {
//                    break;
                        socket.close();
                        inputStreamReader.close();
                        outputStreamWriter.close();
                        bufferedReader.close();
                        bufferedWriter.close();
                    }*/

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
