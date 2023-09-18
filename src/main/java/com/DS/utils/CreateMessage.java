package com.DS.utils;

import com.DS.utils.fileScanner.ReadFile;
import com.DS.utils.json.JSONHandler;

public class CreateMessage {
    StringBuilder header = new StringBuilder();
    StringBuilder body = new StringBuilder();

    public String createHeader(String type, String status) {
        if ("GET".equals(type)) {
            header.append("GET weather.txt HTTP/1.1").append("\n")
                    .append("Host: aggregation-server").append("\n")
                    .append("Accept: application/json").append("\n");
        } else if ("Response".equals(type)) {
            if ("404".equals(status)) { // No content from AS
                header.append("HTTP/1.1 404 Not Found").append("\n")
                        .append("Content-Type: application/json").append("\n");
            }
        } else if ("PUT".equals(type)) {
            header.append("PUT weather.txt HTTP/1.1").append("\n")
                    .append("Host: aggregation-server").append("\n")
                    .append("Content-Type: application/json").append("\n");
        }

        return header.toString();
    }

    public String createBody(String type, String status) {
        if ("Response".equals(type)) {
            if ("404".equals(status)) { //No content from AS
                body.append("{").append("\n")
                        .append("\"message\": \"Can't find anything on AS\"").append("\n")
                        .append("}").append("\n");
            }
        } else if ("PUT".equals(type)) {
            ReadFile readFile = new ReadFile();
            String content = readFile.readFrom("", "source.txt", "contentServer");
            content = JSONHandler.string2JSON(content);
            body.append(content);
        }

        return body.toString();
    }

    public String makeWholeMessage(String type, String status) {
        if ("PUT".equals(type)) {
            StringBuilder msg = new StringBuilder();
            String header = createHeader(type, status);
            String body = createBody(type, status);
            return msg.append(header)
                    .append("Content-Length: ").append(body.length()).append("\n")
                    .append(body).append("\n").toString();
        }
        return createHeader(type, status) + createBody(type, status);
    }
}
