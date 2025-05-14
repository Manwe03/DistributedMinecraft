package com.manwe.dsl.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class DSLServerConfigs {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    //public static final ModConfigSpec.ConfigValue<Integer> BASE_PORT;
    public static final ModConfigSpec.ConfigValue<String> ARBITER_ADDR;


    static {
        BUILDER.push("server");

        //BASE_PORT = BUILDER.comment("Proxy Port all workers will have base_port + (worker number) as port").define("base_port",25565);
        ARBITER_ADDR = BUILDER.comment("Complete address of the arbiter").define("arbiter_addr","http://localhost:8080");

        BUILDER.pop();
    }

    public static final ModConfigSpec SPEC = BUILDER.build();
}
