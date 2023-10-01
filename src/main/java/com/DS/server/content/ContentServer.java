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
    private Socket socket = null;
    private InputStreamReader inputStreamReader = null;
    private OutputStreamWriter outputStreamWriter = null;
    private BufferedReader bufferedReader = null;
    private BufferedWriter bufferedWriter = null;

    public static void main(String[] args) throws IOException {
        String address = "localhost:4567";
        String IP = "localhost";
        int port = 4567;
        if (args.length == 0) {
            System.out.println("Command: make \"ContentServer <IP>:<port>\", default address: localhost:4567");
        } else {
            address = args[0];
            IP = address.substring(0, address.indexOf(":"));
            port = Integer.parseInt(address.substring(address.indexOf(":") + 1));
            System.out.println("Command: make \"ContentServer <IP>:<port>\", given address: " + IP + ":" + port);
        }
        System.out.println("Connecting Aggregation-server on: " + address);
        Socket socket = new Socket(IP, port);
        ContentServer CS = new ContentServer(socket);
        CS.readMessage();
        CS.PUT();
    }

    public ContentServer(Socket socket) {
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

    public void PUT() {
        try {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String input = scanner.nextLine();
                System.out.println();

                if ("PUT".equalsIgnoreCase(input)) {
                    String content = ReadFile.readFrom("", "source.txt", "contentServer");
                    if ("404".equals(content)) {
                        System.err.println("No source of weather information for now");
                        continue;
                    }
                    String msgToSend = CreateMessage.makeWholeMessage("PUT", null);
                    //                    System.out.println(msgToSend);
                    bufferedWriter.write(msgToSend);
                    bufferedWriter.write("Clock:" + clock.getNextNumber(clock.getMaxInCurrentProcess()) + "\n");
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                    System.out.println("#Current clock: " + clock.getMaxInCurrentProcess() + "\n");
                } else if ("GET".equalsIgnoreCase(input)) {
                    System.err.println("CS can't send GET request!");
                } else if (input.equalsIgnoreCase("BYE"))
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeAll(socket, bufferedReader, bufferedWriter, inputStreamReader, outputStreamWriter);
        }
    }

    // method to read messages using thread
    public void readMessage() {
        new Thread(() -> {
            StringBuilder msgFromServer = new StringBuilder();

            try {
                while (socket.isConnected()) {
                    String line = bufferedReader.readLine();
                    while (line != null && !line.isEmpty()) {
                        msgFromServer.append(line).append("\n");
                        line = bufferedReader.readLine();
                    }
                    
                    if (msgFromServer == null || msgFromServer.toString().isEmpty()) {
                        System.out.println("AS disconnected");
                        break;
                    } else if (msgFromServer.toString().startsWith("alive?")) {
//                    bufferedWriter.write(" ");
                        bufferedWriter.write("still alive!\n");
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                        System.out.println();
                        System.out.println("--------Heartbeat Check--------");
                        Long clockFromServer = Long.valueOf(msgFromServer.substring(msgFromServer.indexOf("Clock:") + 6).trim());
                        System.out.println("#ClockFromAS: " + clockFromServer);
                        clock.setMaxInCurrentProcess(clockFromServer);
                    } else {
//                        System.out.println("msgFrom-server:" + msgFromServer);//delete
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
