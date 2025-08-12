package org.example.healthChecker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/health")
public class HealthCheckController {

    @Autowired
    private HealthCheckService healthCheckService;

    /**
     * Endpoint chính để kiểm tra sức khỏe của service
     * @return ResponseEntity chứa thông tin health check
     */
    @GetMapping
    public ResponseEntity<HealthCheckResponse> healthCheck() {
        try {
            HealthCheckResponse response = healthCheckService.checkHealth();

            // Xác định HTTP status code dựa trên trạng thái service
            HttpStatus httpStatus = determineHttpStatus(response.getStatus());

            return new ResponseEntity<>(response, httpStatus);

        } catch (Exception e) {
            HealthCheckResponse errorResponse = HealthCheckResponse.builder()
                    .status("DOWN")
                    .message("Không thể thực hiện health check: " + e.getMessage())
                    .build();

            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Endpoint đơn giản chỉ trả về status UP/DOWN
     */
    @GetMapping("/status")
    public ResponseEntity<String> simpleHealthCheck() {
        try {
            HealthCheckResponse response = healthCheckService.checkHealth();
            HttpStatus httpStatus = determineHttpStatus(response.getStatus());

            return new ResponseEntity<>(response.getStatus(), httpStatus);

        } catch (Exception e) {
            return new ResponseEntity<>("DOWN", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Endpoint để lấy thông tin chi tiết về system
     */
    @GetMapping("/details")
    public ResponseEntity<SystemInfo> getSystemDetails() {
        try {
            SystemInfo systemInfo = healthCheckService.getSystemInfo();
            return new ResponseEntity<>(systemInfo, HttpStatus.OK);

        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Endpoint kết hợp health check với system info
     */
    @GetMapping("/full")
    public ResponseEntity<HealthCheckResponse> fullHealthCheck() {
        try {
            HealthCheckResponse response = healthCheckService.checkHealth();
            SystemInfo systemInfo = healthCheckService.getSystemInfo();

            // Thêm system info vào response
            response.setSystemInfo(systemInfo);

            HttpStatus httpStatus = determineHttpStatus(response.getStatus());

            return new ResponseEntity<>(response, httpStatus);

        } catch (Exception e) {
            HealthCheckResponse errorResponse = HealthCheckResponse.builder()
                    .status("DOWN")
                    .message("Không thể thực hiện full health check: " + e.getMessage())
                    .build();

            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Xác định HTTP status code dựa trên trạng thái service
     */
    private HttpStatus determineHttpStatus(String status) {
        switch (status.toUpperCase()) {
            case "UP":
                return HttpStatus.OK;
            case "WARNING":
                return HttpStatus.OK; // Vẫn OK nhưng có cảnh báo
            case "DOWN":
                return HttpStatus.SERVICE_UNAVAILABLE;
            default:
                return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}