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
            closeAll(socket, bufferedReader, bufferedWriter);
        }
    }

    public void PUT() {
        try {
            outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());
            bufferedWriter = new BufferedWriter(outputStreamWriter);

            Scanner scanner = new Scanner(System.in);
            StringBuilder msgFromServer = new StringBuilder();
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
                msgFromServer.setLength(0);
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
