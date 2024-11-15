# Flow Log Analyzer
The Flow Log Processor is a Java application that processes AWS VPC flow logs, maps each record to a tag based on a lookup table, and generates a report summarizing the results. It supports the default AWS VPC flow log format (version 2) and includes features for tagging records, counting untagged logs, and identifying unique port/protocol combinations.

## Goal
The goal of this application is to automate the analysis of AWS VPC flow logs and make the data actionable by tagging each flow log record. The process includes:

## Input Files
Flow Logs: The raw logs in AWS VPC's default format (version 2).
Lookup Table: A CSV file mapping destination ports (dstport) and protocols (protocol) to specific tags.

## Processing Steps
1. Load the lookup table and map the dstport,protocol combinations to tags.
2. Parse and validate each line of the flow log file.
3. Map each log entry to one or more tags based on the lookup table.
4. Count untagged entries for unmatched combinations.
5. Generate an output file summarizing-
    - Tag counts.
    - Untagged record counts.
    - Port/Protocol combination counts.
    

## Output
A report in output.txt that provides a clear summary of the tagging process.


## Assumptions:
- Supports AWS VPC flow logs in version 2 only.
- Logs with NODATA or SKIPDATA statuses are ignored.
- Matching for ports and protocols is case-insensitive.

## Preprocessing Requirements
- Input file as well as the file containing tag mappings are plain text (ascii) files.
- The flow log file size can be up to 10 MB.
- The lookup file can have up to 10000 mappings.
- The tags can map to more than one port, protocol combinations.  for e.g. sv_P1 and sv_P2 in the sample above. 
- The matches should be case insensitive.

## How to Run

### Prerequisites
- Java 17+ installed.
- Gradle installed (can be downloaded from Gradle's official website).


### Steps to Run the Application
1. Clone the Repository
    ```
    git clone https://github.com/shipra-aeron/FlowLogAnalyzer.git
    cd app
    ```
2. Add Input Files

    - Place the flow log file in src/main/resources/flow_logs.txt.
    - Place the lookup table file in src/main/resources/lookup_table.csv.
    
3. Build the Project
    ```
    gradle build

    ```

4. Run the Application
    ```
    gradle run
    ```
5. View the Output

    The output report is generated at src/main/resources/output.txt.
