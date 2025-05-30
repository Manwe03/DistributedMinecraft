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
}
