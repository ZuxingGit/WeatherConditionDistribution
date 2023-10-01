package com.DS.client;

import com.DS.utils.CreateMessage;
import com.DS.utils.clock.LamportClock;
import com.DS.utils.json.JSONHandler;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Scanner;

public class GETClient {
    static LamportClock clock = new LamportClock(0L);
    Socket socket = null;
    InputStreamReader inputStreamReader = null;
    OutputStreamWriter outputStreamWriter = null;
    BufferedReader bufferedReader = null;
    BufferedWriter bufferedWriter = null;

    public GETClient(Socket socket) {
        this.socket = socket;
        try {
            inputStreamReader = new InputStreamReader(socket.getInputStream());
            outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());

            this.bufferedReader = new BufferedReader(inputStreamReader);
            this.bufferedWriter = new BufferedWriter(outputStreamWriter);
        } catch (IOException e) {
            closeAll(socket, bufferedReader, bufferedWriter, inputStreamReader, outputStreamWriter);
        }
    }

    public static void main(String[] args) throws IOException {
        int port = 4567;
        if (args.length == 0) {
            System.out.println("Command: java GETClient <port>, default port: 4567");
        } else {
            port = Integer.parseInt(args[0]);
        }
        System.out.println("Connect Aggregation-server on port: " + port);
        Socket socket = new Socket("localhost", port);
        GETClient client = new GETClient(socket);
        client.readMessage();
        client.GET();
    }

    public void GET() {
        try {
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
                    System.out.println("#Current clock: " + clock.getMaxInCurrentProcess() + "\n");
                } else if ("PUT".equalsIgnoreCase(input)) {
                    System.err.println("Clients can't PUT!");
                } else if (input.equalsIgnoreCase("BYE"))
                    break;
            }
        } catch (ConnectException e) {
            System.err.println("AS might be turned off. Connection failed!");
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
                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // method to read messages using thread
    public void readMessage() {
        new Thread(() -> {
            try {
                while (socket.isConnected()) {
                    StringBuilder msgFromServer = new StringBuilder();
                    String line = bufferedReader.readLine();
                    while (line != null && !line.isEmpty()) {
                        msgFromServer.append(line).append("\n");
                        line = bufferedReader.readLine();
                    }
                    //                    System.out.println("msgFromServer:" + msgFromServer);//delete
                    if (msgFromServer == null || msgFromServer.toString().isEmpty()) {
                        System.out.println("AS disconnected");
                        break;
                    } else {
                        Long clockFromServer = Long.valueOf(msgFromServer.substring(msgFromServer.indexOf("Clock:") + 6).trim());
                        if (msgFromServer.toString().contains("404 Not Found")) {
                            System.err.println("Can't find anything on AS");
                        } else {
                            String json = msgFromServer.substring(msgFromServer.indexOf("{"), msgFromServer.indexOf("}") + 1);
                            System.out.println(JSONHandler.JSON2String(json));
                        }
                        System.out.println("\n#ClockFromAS: " + clockFromServer);
                        System.out.println("#Current clock: " + clock.getNextNumber(clockFromServer));
                    }
                    msgFromServer.setLength(0);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                System.out.println("Connection stopped!");
                closeAll(socket, bufferedReader, bufferedWriter, inputStreamReader, outputStreamWriter);
            }
        }).start();
    }

    public void closeAll(Socket socket, BufferedReader buffReader, BufferedWriter buffWriter, InputStreamReader ir, OutputStreamWriter ow) {
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
            System.exit(0);
        } catch (IOException e) {
            e.getStackTrace();
        }
    }
}
