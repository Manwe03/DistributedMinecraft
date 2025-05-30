package com.manwe.dsl.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class DSLServerConfigs {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    //public static final ModConfigSpec.ConfigValue<Integer> BASE_PORT;
    public static final ModConfigSpec.ConfigValue<String> ARBITER_ADDR;
    public static final ModConfigSpec.ConfigValue<Boolean> IS_PROXY;
    public static final ModConfigSpec.ConfigValue<Integer> WORKER_ID;
    public static final ModConfigSpec.ConfigValue<Integer> REGION_SIZE;
    public static final ModConfigSpec.ConfigValue<Integer> WORKER_SIZE;


    static {
        BUILDER.push("server");
        ARBITER_ADDR = BUILDER.comment("Complete address of the arbiter").define("arbiter_addr","http://localhost:8080");
        IS_PROXY = BUILDER.comment("Is this node a proxy? (only one can be the proxy)").define("is_proxy",true);
        WORKER_ID = BUILDER.comment("If false which worker id?").define("worker_id",1);
        BUILDER.pop();

        BUILDER.push("topology").comment("This values need to be equal in all nodes");
        WORKER_SIZE = BUILDER.comment("Number of workers").define("worker_size",1);
        REGION_SIZE = BUILDER.comment("Size of the smallest server in region cords. (1 Region = 32x32 chunks)").define("region_size",4);
        BUILDER.pop();

        /*
        BUILDER.push("Into Pattern").comment(
                """
                This is the ID pattern, form center to edge clockwise
                +-----------------------------------+
                |                 |                 |
                |      8          |         2       |
                |        +--------+--------+        |
                |        |        |        |        |
                |        |      7 | 1      |        |
                |--------+--------+--------+--------|
                |        |      5 | 3      |        |
                |        |        |        |        |
                |        +--------+--------+        |
                |      6          |          4      |
                |                 |                 |
                +-----------------------------------+
                
                +-----------------------------------+
                |                 |                 |
                |                 |                 |
                |     (-x+z)      |      (+x+z)     |
                |                 |                 |
                |               4 | 1               |
                |-----------------+-----------------|
                |               3 | 2               |
                |                 |                 |
                |     (-x-z)      |      (+x-z)     |
                |                 |                 |
                |                 |                 |
                +-----------------------------------+
                
                +-----------------------------------+
                |                 |                 |
                |                 |                 |
                |                 |                 |
                |                 |                 |
                |                 |                 |
                |               2 | 1               |
                |                 |                 |
                |                 |                 |
                |                 |                 |
                |                 |                 |
                |                 |                 |
                +-----------------------------------+
                """
        );
        BUILDER.pop();
        */
    }

    public static final ModConfigSpec SPEC = BUILDER.build();
}
