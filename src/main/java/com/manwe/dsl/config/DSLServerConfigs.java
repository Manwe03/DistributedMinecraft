package com.manwe.dsl.config;

import com.manwe.dsl.arbiter.ConnectionInfo;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class DSLServerConfigs {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    //public static final ModConfigSpec.ConfigValue<Integer> BASE_PORT;
    public static final ModConfigSpec.ConfigValue<String> ARBITER_ADDR;
    public static final ModConfigSpec.ConfigValue<Boolean> IS_PROXY;
    public static final ModConfigSpec.ConfigValue<Integer> WORKER_ID;
    public static final ModConfigSpec.ConfigValue<Integer> REGION_SIZE;
    public static final ModConfigSpec.ConfigValue<Integer> WORKER_SIZE;

    public static final ModConfigSpec.ConfigValue<Boolean> USE_ARBITER;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> CONNECTIONS;
    public static final ModConfigSpec.ConfigValue<Integer> PORT;

    static {
        BUILDER.push("Server");
        IS_PROXY = BUILDER.comment("Is this node a proxy? (only one can be the proxy)").define("is_proxy",true);
        WORKER_ID = BUILDER.comment("If false which worker id?").define("worker_id",1);
        BUILDER.pop();

        BUILDER.push("Nodes").comment("This values need to be equal in all nodes");
        WORKER_SIZE = BUILDER.comment("Number of workers").define("worker_size",1);
        REGION_SIZE = BUILDER.comment("Size of each region 2^n chunks. 2 = (4 chunks) 5 = (32 chunks)").define("region_size",5);
        BUILDER.pop();

        BUILDER.push("Auto start").comment("Address assignment for workers");
        USE_ARBITER = BUILDER.comment("Use the arbiter").define("use_arbiter",true);
        BUILDER.comment("use_arbiter = TRUE ->");
        ARBITER_ADDR = BUILDER.comment("Complete address of the arbiter, This is ignored if use_arbiter = false").define("arbiter_addr","http://localhost:8080");
        BUILDER.comment("use_arbiter = FALSE ->");
        PORT = BUILDER.define("this_port",25565);
        CONNECTIONS = BUILDER.comment("If this is proxy and not using arbiter. Define worker address. ip:port:worker_id").defineListAllowEmpty("connections",List.of("127.0.0.1:25565:1","127.0.0.1:25566:2"),object -> {
            if (object instanceof String s) return true;
            return false;
        });

        BUILDER.pop();

    }

    public static final ModConfigSpec SPEC = BUILDER.build();

    public static List<ConnectionInfo> getConnectionAddresses() {
        return CONNECTIONS.get().stream().map(ConnectionInfo::read).toList();
    }
}
