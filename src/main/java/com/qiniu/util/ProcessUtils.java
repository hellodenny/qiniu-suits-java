package com.qiniu.util;

import java.util.ArrayList;
import java.util.List;

public class ProcessUtils {

    private static List<String> needUrlProcesses = new ArrayList<String>(){{
        add("asyncfetch");
        add("privateurl");
        add("qhash");
        add("avinfo");
    }};
    private static List<String> needMd5Processes = new ArrayList<String>(){{
        add("asyncfetch");
    }};
    private static List<String> needNewKeyProcesses = new ArrayList<String>(){{
        add("rename");
        add("copy");
    }};
    private static List<String> needFopsProcesses = new ArrayList<String>(){{
        add("pfop");
    }};
    private static List<String> needPidProcesses = new ArrayList<String>(){{
        add("pfopresult");
    }};
    private static List<String> needAvinfoProcesses = new ArrayList<String>(){{
        add("pfopcmd");
    }};
    private static List<String> needBucketProcesses = new ArrayList<String>(){{
        add("status");
        add("type");
        add("lifecycle");
        add("copy");
        add("move");
        add("rename");
        add("delete");
        add("pfop");
        add("stat");
        add("mirror");
    }};
    private static List<String> needAuthProcesses = new ArrayList<String>(){{
        addAll(needBucketProcesses);
        add("asyncfetch");
        add("privateurl");
    }};
    private static List<String> canBatchProcesses = new ArrayList<String>(){{
        add("status");
        add("type");
        add("lifecycle");
        add("copy");
        add("move");
        add("rename");
        add("delete");
        add("stat");
    }};

    public static boolean needUrl(String process) {
        return canBatchProcesses.contains(process);
    }

    public static boolean needMd5(String process) {
        return canBatchProcesses.contains(process);
    }

    public static boolean needNewKey(String process) {
        return canBatchProcesses.contains(process);
    }

    public static boolean needFops(String process) {
        return canBatchProcesses.contains(process);
    }

    public static boolean needPid(String process) {
        return canBatchProcesses.contains(process);
    }

    public static boolean needAvinfo(String process) {
        return canBatchProcesses.contains(process);
    }

    public static boolean needBucket(String process) {
        return canBatchProcesses.contains(process);
    }

    public static boolean needAuth(String process) {
        return canBatchProcesses.contains(process);
    }

    public static boolean canBatch(String process) {
        return canBatchProcesses.contains(process);
    }
}