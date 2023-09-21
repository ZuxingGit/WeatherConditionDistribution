package com.DS.server.aggregation;

import com.DS.utils.CreateMessage;
import com.DS.utils.clock.LamportClock;
import com.DS.utils.fileScanner.ReadFile;
import com.DS.utils.fileScanner.WriteFile;
import com.DS.utils.json.JSONHandler;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Timer;

public class AggregationServer01 extends Thread {
    LamportClock clock = new LamportClock(0L);
    private ServerSocket serverSocket;
    private int port;
    private boolean running = false;
    public boolean hasStarted = false; //whether timer hasStarted
    private Timer timer = new Timer();


    public AggregationServer01(int port) {
        this.port = port;
    }

    public void startServer() {
        try {
            serverSocket = new ServerSocket(port);
            this.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopServer() {
        running = false;
        this.interrupt();
    }

    @Override
    public void run() {
        running = true;
        while (running) {
            try {
                System.out.println("Listening for a connection");

                // Call accept() to receive the next connection
                Socket socket = serverSocket.accept();

                // Pass the socket to the RequestHandler thread for processing
                RequestHandler requestHandler = new RequestHandler(socket, clock, hasStarted, timer);
                requestHandler.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        int port = 4567;
        if (args.length == 0) {
            System.out.println("Command: java AggregationServer <port>, default port: 4567");
        } else {
            port = Integer.parseInt(args[0]);
        }
        System.out.println("Start server on port: " + port);

        AggregationServer01 server = new AggregationServer01(port);
        server.startServer();

        // Automatically shutdown in 1 minute
        /*try {
            Thread.sleep(60000);
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        server.stopServer();
    }
}

class RequestHandler extends Thread {
    private Socket socket;
    private LamportClock clock;
    private boolean hasStarted = false;
    private Timer timer;
    private PriorityQueue<String> feed = new PriorityQueue<>(20, new FeedComparator()); //20 newest entries

    RequestHandler(Socket socket, LamportClock clock, boolean hasStarted, Timer timer) {
        this.socket = socket;
        this.clock = clock;
        this.hasStarted = hasStarted;
        this.timer = timer;
    }

    @Override
    public void run() {
        InputStreamReader inputStreamReader = null;
        OutputStreamWriter outputStreamWriter = null;
        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;

        try {
            inputStreamReader = new InputStreamReader(socket.getInputStream());
            outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());

            bufferedReader = new BufferedReader(inputStreamReader);
            bufferedWriter = new BufferedWriter(outputStreamWriter);

//            Timer timer = new Timer();
//            timer.schedule(new HeartbeatCheck(timer, socket), 5000, 5000);
            //only set timer when a CS connected && sent a PUT request
            while (true) {
                StringBuilder msgReceived = new StringBuilder();
                String line = bufferedReader.readLine();
                while (line != null && !line.isEmpty()) {
                    msgReceived.append(line).append("\n");
                    line = bufferedReader.readLine();
                }
                System.out.println(msgReceived);

                if (msgReceived == null || msgReceived.toString().isEmpty()) {
                    break;
                } else if ("still alive!".equals(msgReceived.toString().trim())) {//heartbeat response
                    continue;
                } else if ("GET".equalsIgnoreCase(msgReceived.substring(0, 3))) {
                    String fileName = msgReceived.substring(4, msgReceived.indexOf(" HTTP"));
                    Long clockFromClient = Long.valueOf(msgReceived.substring(msgReceived.indexOf("Clock:") + 6).trim());
                    clock.getNextNumber(clockFromClient);
                    String returnMsg;
                    String content;
                    if (feed.size() == 0) {
                        content = ReadFile.readFrom("", fileName, "aggregationServer");
                        //                    System.out.println("content: " + content);
                        if ("404".equals(content)) {
                            returnMsg = CreateMessage.makeWholeMessage("Response", "404");
                        } else {    //200 OK
                            returnMsg = CreateMessage.makeWholeMessage("Response", "200 OK");
                        }
                    } else {
                        returnMsg = CreateMessage.createHeader("Response", "200 OK") + feed.peek();
                    }

                    bufferedWriter.write(returnMsg);
                    bufferedWriter.write("Clock:" + clock.getNextNumber(clock.getMaxInCurrentProcess()) + "\n");
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                    System.out.println("#Current clock:" + clock.getMaxInCurrentProcess() + "\n");
                } else if ("PUT".equalsIgnoreCase(msgReceived.substring(0, 3))) {
                    //only set timer when a CS connected && sent a PUT request
                    HeartbeatCheck heartbeatCheck = new HeartbeatCheck(socket, clock, timer);
                    if (!hasStarted) {
                        heartbeatCheck.launchTimer();
                        hasStarted = true;
                    }
                    //500 Incorrect JSON;204 No Content;201 Created;200 Updated;
                    String returnMsg;
                    if (!msgReceived.toString().contains("{") || !msgReceived.toString().contains("}") || !msgReceived.toString().contains("\"id\":")) {
                        returnMsg = CreateMessage.createHeader("Response", "500");
                    } else if (msgReceived.substring(msgReceived.indexOf("{"), msgReceived.indexOf("}") + 1).isEmpty() ||
                            Integer.valueOf(msgReceived.substring(msgReceived.indexOf("Content-Length:") + 15, msgReceived.indexOf("{")).trim()) < 1) {
                        returnMsg = CreateMessage.createHeader("Response", "204");
                    } else {
                        String fileName = "cache.txt";
                        String backFile = "backup.txt";
                        String content = msgReceived.substring(msgReceived.indexOf("{"), msgReceived.indexOf("}") + 1);

                        if (WriteFile.writeTo("", fileName, content, "aggregationServer", true))
                            returnMsg = CreateMessage.createHeader("Response", "200 Updated");  //file exists, updated successfully
                        else
                            returnMsg = CreateMessage.createHeader("Response", "201");  //file non-existent, created one successfully

                        feed.add(msgReceived.toString());
                        while (feed.size() >= 3) {
                            String oldContent = feed.peek();
                            WriteFile.writeTo("", backFile, oldContent, "aggregationServer", true);
                            feed.poll();
                        }

                        String[] entries = feed.toArray(new String[feed.size()]);
                        Arrays.sort(entries, new FeedComparator());
                        String contentInFeed = "";
                        for (String entry :
                                entries) {
                            contentInFeed += JSONHandler.JSON2String(entry.substring(entry.indexOf("{"), msgReceived.indexOf("}") + 1)) + "\n\n";
                        }
                        WriteFile.writeTo("", fileName, contentInFeed, "aggregationServer", false);
                    }

                    Long clockFromCS = Long.valueOf(msgReceived.substring(msgReceived.indexOf("Clock:") + 6).trim());
                    clock.getNextNumber(clockFromCS);
                    bufferedWriter.write(returnMsg);
                    bufferedWriter.write("Clock:" + clock.getNextNumber(clock.getMaxInCurrentProcess()) + "\n");
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                    System.out.println("#Current clock:" + clock.getMaxInCurrentProcess() + "\n");
                } else {    // neither GET nor PUT request
                    String returnMsg = CreateMessage.createHeader("Response", "400");
                    Long clockFromCS = Long.valueOf(msgReceived.substring(msgReceived.indexOf("Clock:") + 6).trim());
                    clock.getNextNumber(clockFromCS);
                    bufferedWriter.write(returnMsg);
                    bufferedWriter.write("Clock:" + clock.getNextNumber(clock.getMaxInCurrentProcess()) + "\n");
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                    System.out.println("#Current clock:" + clock.getMaxInCurrentProcess() + "\n");
                }
                msgReceived.setLength(0);
                
               /* String clockFromClient = msgReceived.substring(msgReceived.indexOf("Clock:") + 6);
                msgReceived = msgReceived.substring(0, msgReceived.indexOf("Clock:"));
                System.out.println("Client: " + msgReceived);
                System.out.println("clockFromClient: " + clockFromClient);
                System.out.println("Clock:" + clock.getNextNumber(Long.valueOf(clockFromClient)));

                bufferedWriter.write("MSG Received.");
                bufferedWriter.write("Clock:" + clock.getNextNumber(clock.getMaxInCurrentProcess()));
                bufferedWriter.newLine();
                bufferedWriter.flush();
                System.out.println("===========");

                */
                if (msgReceived.toString().equalsIgnoreCase("BYE"))
                    break;
            }

            socket.close();
            inputStreamReader.close();
            outputStreamWriter.close();
            bufferedReader.close();
            bufferedWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
