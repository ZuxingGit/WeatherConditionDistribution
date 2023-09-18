package com.DS.utils;

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
        }

        return body.toString();
    }

    public String makeWholeMessage(String type, String status) {
        return createHeader(type, status) + createBody(type, status);
    }
}
