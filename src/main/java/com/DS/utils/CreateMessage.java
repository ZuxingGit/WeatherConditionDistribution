package com.DS.utils;

import com.DS.utils.fileScanner.ReadFile;
import com.DS.utils.json.JSONHandler;

public class CreateMessage {

    public static String createHeader(String type, String status) {
        StringBuilder header = new StringBuilder();

        if ("GET".equals(type)) {
            header.append("GET cache.txt HTTP/1.1").append("\n")
                    .append("Host: aggregation-server").append("\n")
                    .append("Accept: application/json").append("\n");
        } else if ("Response".equals(type)) {
            if ("404".equals(status)) { // No content from AS
                header.append("HTTP/1.1 404 Not Found").append("\n")
                        .append("Content-Type: application/json").append("\n");
            }
            if ("200 Updated".equals(status)) {
                header.append("HTTP/1.1 200 Updated").append("\n");
            }
            if ("200 OK".equals(status)) {
                header.append("HTTP/1.1 200 OK").append("\n")
                        .append("Content-Type: application/json").append("\n");
            }
            if ("201".equals(status)) {
                header.append("HTTP/1.1 201 Created").append("\n");
            }
            if ("400".equals(status)) {
                header.append("HTTP/1.1 400 Bad Request").append("\n");
            }
            if ("204".equals(status)) {
                header.append("HTTP/1.1 204 No Content").append("\n");
            }
            if ("500".equals(status)) {
                header.append("HTTP/1.1 500 Incorrect JSON").append("\n");
            }
        } else if ("PUT".equals(type)) {
            header.append("PUT weather.txt HTTP/1.1").append("\n")
                    .append("User-Agent: ATOMClient/1/0").append("\n")
                    .append("Content-Type: application/json").append("\n");
        }

        return header.toString();
    }

    public static String createBody(String type, String status) {
        StringBuilder body = new StringBuilder();

        if ("Response".equals(type)) {
            if ("404".equals(status)) { //No content from AS
                body.append("{").append("\n")
                        .append("\"message\": \"Can't find anything on AS\"").append("\n")
                        .append("}");
            }
            if ("200 OK".equals(status)) {
                String content = ReadFile.readFrom("", "cache.txt", "aggregationServer");
                content = JSONHandler.string2JSON(content);
                body.append(content);
            }
        } else if ("PUT".equals(type)) {
            String content = ReadFile.readFrom("", "source.txt", "contentServer");
            content = JSONHandler.string2JSON(content);
            body.append(content);
        }

        return body.toString();
    }

    public static String makeWholeMessage(String type, String status) {
        if ("PUT".equals(type)) {
            StringBuilder msg = new StringBuilder();
            String header = createHeader(type, status);
            String body = createBody(type, status);
            return msg.append(header)
                    .append("Content-Length: ").append(body.length()).append("\n")
                    .append(body).append("\n").toString();
        } 
        if ("Response".equals(type)) {
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
