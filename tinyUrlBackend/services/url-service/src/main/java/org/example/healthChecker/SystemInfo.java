package org.example.healthChecker;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemInfo {
    private long totalMemory;
    private long freeMemory;
    private long maxMemory;
    private long usedMemory;
    private int availableProcessors;
    private String javaVersion;
    private String osName;
    private String osVersion;
}