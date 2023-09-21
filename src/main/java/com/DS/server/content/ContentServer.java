package com.DS.server.content;

import com.DS.utils.CreateMessage;
import com.DS.utils.clock.LamportClock;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.Timer;

public class ContentServer {
    static LamportClock clock = new LamportClock(0L);
    private Socket socket = null;
    private InputStreamReader inputStreamReader = null;
    private OutputStreamWriter outputStreamWriter = null;
    private BufferedReader bufferedReader = null;
    private BufferedWriter bufferedWriter = null;

    public static void main(String[] args) throws IOException {
        int port = 4567;
        if (args.length == 0) {
            System.out.println("Command: java ContentServer <port>, default port: 4567");
        } else {
            port = Integer.parseInt(args[0]);
        }
        System.out.println("Connect Aggregation-server on port: " + port);
        Socket socket = new Socket("localhost", port);
        ContentServer CS = new ContentServer(socket);
        CS.readMessage();
        CS.PUT(port);
    }

    public ContentServer(Socket socket) {
        this.socket = socket;
        try {
            inputStreamReader = new InputStreamReader(socket.getInputStream());
            outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());

            this.bufferedReader = new BufferedReader(inputStreamReader);
            this.bufferedWriter = new BufferedWriter(outputStreamWriter);
        } catch (IOException e) {
            closeAll(socket, bufferedReader, bufferedWriter);
        }
    }

    public void PUT(int port) {
        try {
//            inputStreamReader = new InputStreamReader(socket.getInputStream());
            outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());

//            bufferedReader = new BufferedReader(inputStreamReader);
            bufferedWriter = new BufferedWriter(outputStreamWriter);

            Scanner scanner = new Scanner(System.in);
            StringBuilder msgFromServer = new StringBuilder();
            while (true) {
                String input = scanner.nextLine();
                System.out.println();

                if ("PUT".equalsIgnoreCase(input)) {
                    String msgToSend = CreateMessage.makeWholeMessage("PUT", null);
                    //                    System.out.println(msgToSend);
                    bufferedWriter.write(msgToSend);
                    bufferedWriter.write("Clock:" + clock.getNextNumber(clock.getMaxInCurrentProcess()) + "\n");
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                    System.out.println("#Current clock: " + clock.getMaxInCurrentProcess() + "\n");

                    /*String line = bufferedReader.readLine();
                    while (line != null && !line.isEmpty()) {
                        msgFromServer.append(line).append("\n");
                        line = bufferedReader.readLine();
                    }
                    System.out.println(msgFromServer.substring(0, msgFromServer.indexOf("Clock:")));
                    Long clockFromServer = Long.valueOf(msgFromServer.substring(msgFromServer.indexOf("Clock:") + 6).trim());
                    System.out.println("#ClockFromAS: " + clockFromServer);
                    System.out.println("#Current clock: " + clock.getNextNumber(clockFromServer));*/
                }


//
//                bufferedWriter.write(msgToSend);
//                bufferedWriter.write("Clock:" + clock.getNextNumber(clock.getMaxInCurrentProcess()));
//                bufferedWriter.newLine();
//                bufferedWriter.flush();
                

                
                
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

                msgFromServer.setLength(0);
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

    // method to read messages using thread
    public void readMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                StringBuilder msgFromServer = new StringBuilder();

                try {
                    while (socket.isConnected()) {
                        String line = bufferedReader.readLine();
                        while (line != null && !line.isEmpty()) {
                            msgFromServer.append(line).append("\n");
                            line = bufferedReader.readLine();
                        }
                        if (msgFromServer.toString().startsWith("alive?")) {
//                    bufferedWriter.write(" ");
                            bufferedWriter.write("still alive!\n");
                            bufferedWriter.newLine();
                            bufferedWriter.flush();
                            System.out.println();
                            System.out.println("--------Heartbeat Check--------");
                            Long clockFromServer = Long.valueOf(msgFromServer.substring(msgFromServer.indexOf("Clock:") + 6).trim());
                            System.out.println("#ClockFromAS: " + clockFromServer);
                            clock.setMaxInCurrentProcess(clockFromServer);
                            System.out.println("#Current clock synchronized to " + clock.getMaxInCurrentProcess());
                        } else {
                            System.out.println("----msgFromAggregationServer----\n" + msgFromServer.substring(0, msgFromServer.indexOf("Clock:")));
                            Long clockFromServer = Long.valueOf(msgFromServer.substring(msgFromServer.indexOf("Clock:") + 6).trim());
                            System.out.println("#ClockFromAS: " + clockFromServer);
                            System.out.println("#Current clock: " + clock.getNextNumber(clockFromServer));
                        }
                        msgFromServer.setLength(0);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("Connection stopped!");
                    closeAll(socket, bufferedReader, bufferedWriter);
                }
            }
        }).start();
    }

    public void closeAll(Socket socket, BufferedReader buffReader, BufferedWriter buffWriter) {
        try {
            if (buffReader != null) {
                buffReader.close();
            }
            if (buffWriter != null) {
                buffWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.getStackTrace();
        }
    }

}
