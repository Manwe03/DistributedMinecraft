package com.manwe.dsl.arbiter;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.manwe.dsl.config.DSLServerConfigs;
import org.checkerframework.checker.units.qual.C;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ArbiterClient {

    private static final Gson GSON = new Gson();
    private final URI endpoint;
    private final HttpClient http = HttpClient.newHttpClient();

    public ArbiterClient(URI endpoint) {
        this.endpoint = endpoint;
    }

    public ArbiterRes fetch() throws IOException, InterruptedException {
        //Send to the arbiter this worker id, if proxy send 0 (this value is ignored)
        ByteBuffer buffer = ByteBuffer.allocate(4).putInt(DSLServerConfigs.IS_PROXY.get() ? 0 : DSLServerConfigs.WORKER_ID.get());

        HttpRequest req = HttpRequest.newBuilder(endpoint.resolve("/allocate")) //Request to /allocate endpoint
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofByteArray(buffer.array())) //Empty body
                .build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) throw new IOException("Arbiter returned " + res.statusCode());

        System.out.println(res.body());

        JsonObject json = GSON.fromJson(res.body(), JsonObject.class);
        int port = json.get("port").getAsInt();

        List<ConnectionInfo>  connections = new ArrayList<>();
        if (DSLServerConfigs.IS_PROXY.get() && json.has("connections")) { //If this is the proxy get the map
            JsonArray connArray = json.getAsJsonArray("connections");
            for (int i = 0; i < connArray.size(); i++) {
                JsonObject entry = connArray.get(i).getAsJsonObject();
                connections.add(ConnectionInfo.read(entry));
            }
        }

        return new ArbiterRes(port, connections);
    }

    public static class ArbiterRes {
        public int port;
        public List<ConnectionInfo> connections;

        public ArbiterRes(int port, List<ConnectionInfo> connections){
            this.port = port;
            this.connections = connections;
        }
    }

}