package com.manwe.dsl.arbiter;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public final class ArbiterClient {

    private static final Gson GSON = new Gson();
    private final URI endpoint;
    private final HttpClient http = HttpClient.newHttpClient();

    public ArbiterClient(URI endpoint) {
        this.endpoint = endpoint;
    }

    public ArbiterRes fetch() throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(endpoint.resolve("/allocate")) //Request to /allocate endpoint
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.noBody()) //Empty body
                .build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) throw new IOException("Arbiter returned " + res.statusCode());

        System.out.println(res.body());

        JsonObject json = GSON.fromJson(res.body(), JsonObject.class);
        int port = json.get("port").getAsInt();
        boolean proxy = json.get("proxy").getAsBoolean();

        List<InetSocketAddress> connections = new ArrayList<>();
        if (proxy && json.has("connections")) {
            JsonArray connArray = json.getAsJsonArray("connections");
            for (int i = 0; i < connArray.size(); i++) {
                JsonObject entry = connArray.get(i).getAsJsonObject();
                String ip = entry.get("ip").getAsString();
                int assignedPort = entry.get("port").getAsInt();
                connections.add(new InetSocketAddress(ip, assignedPort));
            }
        }

        return new ArbiterRes(port, proxy, connections);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        URI uri = URI.create("http://localhost:8080");

        ArbiterRes res = new ArbiterClient(uri).fetch();
        System.out.println("Assigned port "+ res.port);
        if (res.proxy) {
            System.out.println("Running in proxy mode. Connection list:");
            for (InetSocketAddress info : res.connections) {
                System.out.println("- " + info.getHostName() + ":" + info.getPort());
            }
        }
    }

    public static class ArbiterRes {
        public int port;
        public boolean proxy;
        public List<InetSocketAddress> connections;

        public ArbiterRes(int port, boolean proxy, List<InetSocketAddress> connections){
            this.port = port;
            this.proxy = proxy;
            this.connections = connections;
        }
    }

}