package com.DS.server.aggregation;

import com.DS.utils.CreateMessage;
import com.DS.utils.clock.LamportClock;
import com.DS.utils.fileScanner.ReadFile;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.stream.Collectors;

public class AggregationServer01 extends Thread {
    LamportClock clock = new LamportClock(0L);
    private ServerSocket serverSocket;
    private int port;
    private boolean running = false;

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
                RequestHandler requestHandler = new RequestHandler(socket, clock);
                requestHandler.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        int port = 4567;
        if (args.length == 0) {
            System.out.println("Command: java SimpleSocketServer <port>, default port: 4567");
        } else {
            port = Integer.parseInt(args[0]);
        }
        System.out.println("Start server on port: " + port);

        AggregationServer01 server = new AggregationServer01(port);
        server.startServer();

        // Automatically shutdown in 1 minute
        try {
            Thread.sleep(60000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        server.stopServer();
    }
}

class RequestHandler extends Thread {
    private Socket socket;
    private LamportClock clock;

    RequestHandler(Socket socket, LamportClock clock) {
        this.socket = socket;
        this.clock = clock;
    }

    @Override
    public void run() {
        InputStreamReader inputStreamReader = null;
        OutputStreamWriter outputStreamWriter = null;
        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;
        CreateMessage createMessage = new CreateMessage();

        try {
            inputStreamReader = new InputStreamReader(socket.getInputStream());
            outputStreamWriter = new OutputStreamWriter(socket.getOutputStream());

            bufferedReader = new BufferedReader(inputStreamReader);
            bufferedWriter = new BufferedWriter(outputStreamWriter);

            Timer timer = new Timer();
//            timer.schedule(new HeartbeatCheck(timer, socket), 5000, 5000);
            //only set timer when a CS connected && sent a PUT request
            while (true) {
                StringBuilder msgReceived = new StringBuilder();
//                msgReceived.append(bufferedReader.lines().collect(Collectors.joining(System.lineSeparator())));
                String line= bufferedReader.readLine();
                while (line != null&&!line.isEmpty()) {
                    msgReceived.append(line).append("\n");
                    line= bufferedReader.readLine();
                }
//                System.out.println(msgReceived);

                if (msgReceived == null || msgReceived.toString().isEmpty()) {
                    break;
                } else if ("still alive!".equals(msgReceived.toString().trim())) {
                    continue;
                } else if (msgReceived.substring(0, 3).equalsIgnoreCase("GET")) {
                    ReadFile readFile = new ReadFile();
                    String fileName = msgReceived.substring(4, msgReceived.indexOf(" HTTP"));
                    String content = readFile.readFrom("", fileName, "aggregationServer");
                    System.out.println("content: " + content);
                    if ("404".equals(content)) {
                        String returnMsg = createMessage.makeWholeMessage("Response", "404");
                        bufferedWriter.write(returnMsg);
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                    }
                    continue;
                }

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

                if (msgReceived.equalsIgnoreCase("BYE"))
                    break;*/
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
