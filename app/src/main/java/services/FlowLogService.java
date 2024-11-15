package services;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;

import config.LoggerConfig;
import utils.Protocol;

public class FlowLogService {
    private static final Logger logger = LoggerConfig.getLogger(FlowLogService.class);

    private final String flowLogFile;
    private final String lookupFile;
    private final String outputFile;

    public FlowLogService(String flowLogFile, String lookupFile, String outputFile) {
        this.flowLogFile = flowLogFile;
        this.lookupFile = lookupFile;
        this.outputFile = outputFile;
    }

    public void process() throws IOException {
        Map<String, List<String>> lookupTable = loadLookupTable();
        processFlowLogs(lookupTable);
    }

    private Map<String, List<String>> loadLookupTable() throws IOException {
        logger.info("Loading lookup table");
        logger.debug("Loading lookup table from {}", lookupFile);
        Map<String, List<String>> lookupTable = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(lookupFile))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 3) {
                    logger.warn("Skipping invalid lookup line: {}", line);
                    continue;
                }

                String key = (parts[0].trim() + "," + parts[1].trim()).toLowerCase();
                String tag = parts[2].trim().toLowerCase();

                lookupTable.computeIfAbsent(key, k -> new ArrayList<>()).add(tag);
                logger.debug("Added lookup entry: key={}, tag={}", key, tag);
            }
        }

        logger.info("Lookup table loaded successfully with {} entries.", lookupTable.size());
        return lookupTable;
    }

    private void processFlowLogs(Map<String, List<String>> lookupTable) throws IOException {
        logger.info("Processing flow logs");
        logger.debug("Processing flow logs from {}", flowLogFile);

        Map<String, Integer> tagCounts = new HashMap<>();
        Map<String, Integer> portProtocolCounts = new HashMap<>();
        int untaggedCount = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(flowLogFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\s+");

                if (!isValidFlowLog(parts)) {
                    logger.warn("Skipping invalid flow log line: {}", line);
                    continue;
                }

                String key = (parts[6].trim() + "," + Protocol.mapProtocol(parts[7].trim())).toLowerCase();
                logger.debug("Processing flow log: key={}", key);

                if (lookupTable.containsKey(key)) {
                    for (String tag : lookupTable.get(key)) {
                        tagCounts.put(tag, tagCounts.getOrDefault(tag, 0) + 1);
                        logger.debug("Matched tag: {} for key={}", tag, key);
                    }
                } else {
                    untaggedCount++;
                    logger.debug("No match found for key: {}", key);
                }

                String portProtocolKey = key;
                portProtocolCounts.put(portProtocolKey, portProtocolCounts.getOrDefault(portProtocolKey, 0) + 1);
            }

            writeTagCounts(writer, tagCounts, untaggedCount);
            writePortProtocolCounts(writer, portProtocolCounts);
        }

        logger.info("Flow logs processed successfully.");
    }

    private boolean isValidFlowLog(String[] parts) {
        if (parts.length <14) return false;
        return parts[0].equals("2") && (!parts[13].equalsIgnoreCase("NODATA") || !parts[13].equalsIgnoreCase("SKIPDATA"));
    }


    private void writeTagCounts(BufferedWriter writer, Map<String, Integer> tagCounts, int untaggedCount) throws IOException{
        writer.write("Tag Counts:\n");
        writer.write("Tag,Count\n");
        for (Map.Entry<String, Integer> entry : tagCounts.entrySet()) {
            writer.write(entry.getKey() + "," + entry.getValue() + "\n");
        }
        writer.write("Untagged," + untaggedCount + "\n\n");
    }

    private void writePortProtocolCounts(BufferedWriter writer, Map<String, Integer> portProtocolCounts) throws IOException{
        writer.write("Port/Protocol Combination Counts:\n");
        writer.write("Port,Protocol,Count\n");
        for (Map.Entry<String, Integer> entry : portProtocolCounts.entrySet()) {
            String[] parts = entry.getKey().split(",");
            writer.write(parts[0] + "," + parts[1] + "," + entry.getValue() + "\n");
        }
    }
}
