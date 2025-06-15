package com.manwe.dsl.arbiter;

import com.google.gson.JsonObject;

public record ConnectionInfo(String ip, int port, int id) {

    /**
     * @param entry Json object representing this ConnectionInfo
     * @return new ConnectionInfo
     */
    public static ConnectionInfo read(JsonObject entry){
        String ip = entry.get("ip").getAsString();
        int port = entry.get("port").getAsInt();
        int id = entry.get("id").getAsInt();
        return new ConnectionInfo(ip,port,id);
    }

    public static ConnectionInfo read(String entry){
        int lastColon = entry.lastIndexOf(':');
        int secondColon = entry.lastIndexOf(':', lastColon - 1);

        if (secondColon < 0 || lastColon < 0) throw new IllegalArgumentException("Missing fields in " + entry);

        String host = entry.substring(0, secondColon);
        String portStr = entry.substring(secondColon + 1, lastColon);
        String idStr   = entry.substring(lastColon + 1);

        if (host.startsWith("[") && host.endsWith("]"))
            host = host.substring(1, host.length() - 1);

        int port;
        int id;
        try {
            port = Integer.parseInt(portStr);
            id   = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("None numeric values " + entry);
        }
        if (port < 0 || port > 65_535)
            throw new IllegalArgumentException("Port out of range (0-65535): " + port);

        return new ConnectionInfo(host, port, id);
    }
}
