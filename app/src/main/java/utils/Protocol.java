package utils;

import java.util.HashMap;
import java.util.Map;

public enum Protocol {
    TCP("6"),
    UDP("17"),
    ICMP("1"),
    UNKNOWN("");

    private static final Map<String, Protocol> protocolMap = new HashMap<>();
    private final String protocolNum;

    static {
        for (Protocol protocol : values()) {
            protocolMap.put(protocol.protocolNum, protocol);
        }
    }

    Protocol(String protocolNum) {
        this.protocolNum = protocolNum;
    }

    public static String mapProtocol(String protocolNum) {
        return protocolMap.getOrDefault(protocolNum, UNKNOWN).name().toLowerCase();
    }
}
