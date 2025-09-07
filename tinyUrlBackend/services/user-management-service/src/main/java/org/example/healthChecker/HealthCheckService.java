package org.example.healthChecker;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class HealthCheckService {

    private final String applicationName;
    private final String version;
    private final LocalDateTime startTime;

    public HealthCheckService() {
        this.applicationName = "Spring Boot Application";
        this.version = "1.0.0";
        this.startTime = LocalDateTime.now();
    }

    /**
     * Kiểm tra trạng thái sức khỏe của service
     * @return HealthCheckResponse chứa thông tin trạng thái
     */
    public HealthCheckResponse checkHealth() {
        try {
            // Thực hiện các kiểm tra cơ bản
            long uptime = calculateUptime();
            String status = determineStatus();

            return HealthCheckResponse.builder()
                    .status(status)
                    .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .applicationName(applicationName)
                    .version(version)
                    .uptime(uptime)
                    .message("Service normal")
                    .build();

        } catch (Exception e) {
            return HealthCheckResponse.builder()
                    .status("DOWN")
                    .timestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .applicationName(applicationName)
                    .version(version)
                    .uptime(calculateUptime())
                    .message("Có lỗi xảy ra: " + e.getMessage())
                    .build();
        }
    }

    /**
     * Tính toán thời gian hoạt động của service (tính bằng giây)
     */
    private long calculateUptime() {
        return java.time.Duration.between(startTime, LocalDateTime.now()).getSeconds();
    }

    /**
     * Xác định trạng thái của service
     */
    private String determineStatus() {
        // Có thể thêm logic kiểm tra khác ở đây
        // Ví dụ: kiểm tra memory usage, CPU usage, etc.

        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;

        // Kiểm tra nếu memory usage > 90% thì cảnh báo
        double memoryUsagePercentage = (double) usedMemory / maxMemory * 100;

        if (memoryUsagePercentage > 90) {
            return "WARNING";
        }

        return "UP";
    }

    /**
     * Lấy thông tin chi tiết về system
     */
    public SystemInfo getSystemInfo() {
        Runtime runtime = Runtime.getRuntime();

        return SystemInfo.builder()
                .totalMemory(runtime.totalMemory())
                .freeMemory(runtime.freeMemory())
                .maxMemory(runtime.maxMemory())
                .usedMemory(runtime.totalMemory() - runtime.freeMemory())
                .availableProcessors(runtime.availableProcessors())
                .javaVersion(System.getProperty("java.version"))
                .osName(System.getProperty("os.name"))
                .osVersion(System.getProperty("os.version"))
                .build();
    }
}