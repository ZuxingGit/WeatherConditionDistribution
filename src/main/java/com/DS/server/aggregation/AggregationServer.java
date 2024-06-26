package com.DS.server.aggregation;

import com.DS.utils.CreateMessage;
import com.DS.utils.clock.LamportClock;
import com.DS.utils.fileScanner.ReadFile;
import com.DS.utils.fileScanner.WriteFile;
import com.DS.utils.json.JSONHandler;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;
import java.util.PriorityQueue;

public class AggregationServer extends Thread {
    LamportClock clock = new LamportClock(0L);
    private ServerSocket serverSocket;
    private int port;
    private boolean running = false;
    PriorityQueue<String> feed = new PriorityQueue<>(20, new FeedComparator());


    public AggregationServer(int port) {
        this.port = port;
    }

    public void startServer() {
        try {
            serverSocket = new ServerSocket(port);
            URL whatismyip = null;
            try {
                whatismyip = new URL("http://checkip.amazonaws.com");
                BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
                String ip = in.readLine(); //you get the IP as a String
                System.out.println("Public IP address: " + ip + ":" + port);
            } catch (IOException e) {
                System.err.println("trying get Public IP Address failed");
            }

            System.out.println("Private IP address: " + "localhost:" + port);
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
                System.out.println("Listening for a connection...");

                // Call accept() to receive the next connection
                Socket socket = serverSocket.accept();

                // Pass the socket to the RequestHandler thread for processing
                RequestHandler requestHandler = new RequestHandler(socket, clock, feed);
                requestHandler.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        int port = 4567;
        if (args.length == 0) {
            System.out.println("Command: make \"AggregationServer <port>\", default port: 4567");
        } else {
            port = Integer.parseInt(args[0]);
            System.out.println("Command: make \"AggregationServer <port>\", given port: " + port);
        }
        System.out.println("Start server on port: " + port);
        System.out.println("Connect to AS through one of these two Addresses:");
        AggregationServer server = new AggregationServer(port);
        server.startServer();

        server.stopServer();
    }
}

class RequestHandler extends Thread {
    private Socket socket;
    private LamportClock clock;
    private boolean hasStarted = false;
    private PriorityQueue<String> feed; //20 newest entries
    private PriorityQueue<String> subFeed = new PriorityQueue<>(20, new FeedComparator()); //at most 20 newest entries

    RequestHandler(Socket socket, LamportClock clock, PriorityQueue<String> feed) {
        this.socket = socket;
        this.clock = clock;
        this.feed = feed;
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

            int count = 0; //store heartbeat no response times
            String fileName = "cache.txt";
            String backupFile = "backup.txt";
            String clientType = "";
            while (true) {
                StringBuilder msgReceived = new StringBuilder();
                String line = bufferedReader.readLine();
                while (line != null && !line.isEmpty()) {
                    msgReceived.append(line).append("\n");
                    line = bufferedReader.readLine();
                }

                if (msgReceived == null || msgReceived.toString().isEmpty()) {//heartbeat no response= disconnected
                    if (clientType == "CS") {
                        System.out.println("a CS not connected anymore. Clearing its old entries.");
                        for (String entry : subFeed
                        ) {
                            feed.remove(entry);
                        }
                        String[] entries = feed.toArray(new String[feed.size()]);
                        Arrays.sort(entries, new FeedComparator());
                        String contentInFeed = "";
                        for (int i = entries.length - 1; i >= 0; i--) {
                            String entry = entries[i];
                            contentInFeed += JSONHandler.JSON2String(entry.substring(entry.indexOf("{"), entry.indexOf("}") + 1)) + "\n\n";
                        }
                        WriteFile.writeTo("", fileName, contentInFeed, "aggregationServer", false);
                        break;
                    }
                } else if ("still alive!".equals(msgReceived.toString().trim())) {//heartbeat response
                    count++;
                    if (count == 2 && clientType == "CS") {//no response for heartbeat check 2 times= 15s*2= 30s
                        System.out.println("a CS has not communicated within the last 30s. Clearing its old entries.");
                        for (String entry : subFeed
                        ) {
                            feed.remove(entry);
                        }
                        subFeed.clear();
                        String[] entries = feed.toArray(new String[feed.size()]);
                        Arrays.sort(entries, new FeedComparator());
                        String contentInFeed = "";
                        for (int i = entries.length - 1; i >= 0; i--) {
                            String entry = entries[i];
                            contentInFeed += JSONHandler.JSON2String(entry.substring(entry.indexOf("{"), entry.indexOf("}") + 1)) + "\n\n";
                        }
                        WriteFile.writeTo("", fileName, contentInFeed, "aggregationServer", false);
                        continue;
                    } else
                        continue;
                } else if ("GET".equalsIgnoreCase(msgReceived.substring(0, 3))) {
                    if (clientType.isEmpty())
                        clientType = "client";

                    System.out.println("\n" + msgReceived);
                    String cacheFile = msgReceived.substring(4, msgReceived.indexOf(" HTTP"));
                    Long clockFromClient = Long.valueOf(msgReceived.substring(msgReceived.indexOf("Clock:") + 6).trim());
                    clock.getNextNumber(clockFromClient);
                    String returnMsg;
                    String content = "";
                    System.out.println("feed.size()=" + feed.size());//delete
                    if (msgReceived.toString().contains("stationID")) {
                        String stationID = msgReceived.substring(msgReceived.indexOf("stationID\":") + 11, msgReceived.indexOf("\n}")).replace("\"","").trim();
                        content = ReadFile.readFrom("", cacheFile, "aggregationServer", stationID);
                        if ("404".equals(content) || content.equals("\n") || content.isEmpty()) {
                            returnMsg = CreateMessage.makeWholeMessage("Response", "404", null);
                        } else {    //200 OK
                            returnMsg = CreateMessage.makeWholeMessage("Response", "200 OK", stationID);
                        }
                    } else {
                        content = ReadFile.readFrom("", cacheFile, "aggregationServer");
                        if ("404".equals(content) || content.equals("\n") || content.isEmpty()) {
                            returnMsg = CreateMessage.makeWholeMessage("Response", "404", null);
                        } else {    //200 OK
                            returnMsg = CreateMessage.makeWholeMessage("Response", "200 OK", null);
                        }
                    }

                    bufferedWriter.write(returnMsg);
                    bufferedWriter.write("Clock:" + clock.getNextNumber(clock.getMaxInCurrentProcess()) + "\n");
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                    System.out.println("#Current clock:" + clock.getMaxInCurrentProcess() + "\n");
                } else if ("PUT".equalsIgnoreCase(msgReceived.substring(0, 3))) {
                    if (clientType.isEmpty())
                        clientType = "CS";

                    System.out.println("\n" + msgReceived);
                    //only set timer when a CS connected && sent a PUT request
                    HeartbeatCheck heartbeatCheck = new HeartbeatCheck(socket, clock);
                    if (!hasStarted) {
                        heartbeatCheck.launchTimer();
                        hasStarted = true;
                    } else {
                        if (count == 1 || count == 2)
                            count = 0;
                    }
                    //500 Incorrect JSON; 204 No Content; 201 Created; 200 Updated;
                    String returnMsg;
                    if (!msgReceived.toString().contains("{") || !msgReceived.toString().contains("}") || !msgReceived.toString().contains("\"id\":")) {
                        returnMsg = CreateMessage.createHeader("Response", "500");
                    } else if (msgReceived.substring(msgReceived.indexOf("{"), msgReceived.indexOf("}") + 1).isEmpty() ||
                            Integer.valueOf(msgReceived.substring(msgReceived.indexOf("Content-Length:") + 15, msgReceived.indexOf("{")).trim()) < 1) {
                        returnMsg = CreateMessage.createHeader("Response", "204");
                    } else {
                        String content = msgReceived.substring(msgReceived.indexOf("{"), msgReceived.indexOf("}") + 1);

                        if (WriteFile.writeTo("", fileName, content, "aggregationServer", true))
                            returnMsg = CreateMessage.createHeader("Response", "200 Updated");  //file exists, updated successfully
                        else
                            returnMsg = CreateMessage.createHeader("Response", "201");  //file non-existent, created one successfully

                        feed.add(msgReceived.toString());
                        subFeed.add(msgReceived.toString());
                        System.out.println("feed.size()=" + feed.size());//delete
                        while (feed.size() >= 21) {
                            String oldContent = feed.peek();
                            WriteFile.writeTo("", backupFile, oldContent, "aggregationServer", true);
                            feed.poll();
                            for (String entry : subFeed
                            ) {
                                if (!feed.contains(entry))
                                    subFeed.remove(entry);
                            }
                        }

                        String[] entries = feed.toArray(new String[feed.size()]);
                        Arrays.sort(entries, new FeedComparator());
                        String contentInFeed = "";
                        for (int i = entries.length - 1; i >= 0; i--) {
                            String entry = entries[i];
                            contentInFeed += JSONHandler.JSON2String(entry.substring(entry.indexOf("{"), entry.indexOf("}") + 1)) + "\n\n";
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
                    Long clockReceived = Long.valueOf(msgReceived.substring(msgReceived.indexOf("Clock:") + 6).trim());
                    clock.getNextNumber(clockReceived);
                    bufferedWriter.write(returnMsg);
                    bufferedWriter.write("Clock:" + clock.getNextNumber(clock.getMaxInCurrentProcess()) + "\n");
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                    System.out.println("#Current clock:" + clock.getMaxInCurrentProcess() + "\n");
                }
                msgReceived.setLength(0);

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
