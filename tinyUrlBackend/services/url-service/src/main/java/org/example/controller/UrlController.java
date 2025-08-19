package org.example.controller;

import org.example.constants.ErrorCode;
import org.example.dto.request.CreateUrlRequest;
import org.example.dto.request.RedirectRequest;
import org.example.dto.request.UpdateUrlRequest;
import org.example.dto.response.*;
import org.example.entity.Url;
import org.example.service.UrlManagement.UrlManagementService;
import org.example.service.UrlRedirect.UrlRedirectService;
import org.example.service.data.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/urls")
public class UrlController {

    @Autowired
    private UrlManagementService urlManagementService;

    @Autowired
    private UrlRedirectService urlRedirectService;

    /**
     * Create a new short URL
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CreateUrlResponse>> createUrl(@RequestBody CreateUrlRequest request) {
        try {
            // Convert request to service input data
            CreateUrlInfoIData inputData = CreateUrlInfoIData.builder()
                    .userId(request.getUserId())
                    .originalUrl(request.getOriginalUrl())
                    .title(request.getTitle())
                    .password(request.getPassword())
                    .expiresAt(request.getExpiresAt())
                    .build();

            // Call service
            CreateUrlInfoOData outputData = urlManagementService.createUrlInfo(inputData);

            // Handle error cases
            if (outputData.getErrorCode() != ErrorCode.SUCCESS) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(getErrorMessage(outputData.getErrorCode()),
                                outputData.getErrorCode().toString()));
            }

            // Convert to response
            CreateUrlResponse response = CreateUrlResponse.builder()
                    .shortCode(outputData.getShortCode())
                    .originalUrl(outputData.getOriginalUrl())
                    .title(outputData.getTitle())
                    .status(outputData.getStatus() != null ? outputData.getStatus().toString() : null)
                    .createdAt(outputData.getCreateAt())
                    .expiresAt(outputData.getExpiredAt())
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("URL created successfully", response));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error", ErrorCode.SYSTEM_ERROR.toString()));
        }
    }

    /**
     * Get URL information by short code
     */
    @GetMapping("/{shortCode}")
    public ResponseEntity<ApiResponse<UrlInfoResponse>> getUrlInfo(@PathVariable String shortCode) {
        try {
            // Convert to service input data
            GetUrlInfoByIdIData inputData = GetUrlInfoByIdIData.builder()
                    .shortCode(shortCode)
                    .build();

            // Call service
            GetUrlInfoByIdOData outputData = urlManagementService.getUrlInfoById(inputData);

            // Handle error cases
            if (outputData.getErrorCode() != ErrorCode.SUCCESS) {
                return ResponseEntity.status(getHttpStatus(outputData.getErrorCode()))
                        .body(ApiResponse.error(getErrorMessage(outputData.getErrorCode()),
                                outputData.getErrorCode().toString()));
            }

            // Convert to response
            UrlInfoResponse response = UrlInfoResponse.builder()
                    .shortCode(shortCode)
                    .originalUrl(outputData.getOriginalUrl())
                    .title(outputData.getTitle())
                    .status(outputData.getStatus() != null ? outputData.getStatus().toString() : null)
                    .hasPassword(outputData.getPasswordHash() != null && !outputData.getPasswordHash().isEmpty())
                    .createdAt(outputData.getCreatedAt())
                    .lastUpdated(outputData.getLastUpdate())
                    .expiresAt(outputData.getExpiredAt())
                    .build();

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error", ErrorCode.SYSTEM_ERROR.toString()));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<UrlProjection>>> getAllUrlLimit(@RequestParam int page, @RequestParam int size) {
        try {
            Page<UrlProjection> urlProjections = urlManagementService.getAllUrlInfo(page, size);

            return ResponseEntity.ok(ApiResponse.success(urlProjections));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error", ErrorCode.SYSTEM_ERROR.toString()));
        }
    }

    /**
     * Update URL information
     */
    @PutMapping("/{shortCode}")
    public ResponseEntity<ApiResponse<UpdateUrlResponse>> updateUrl(
            @PathVariable String shortCode,
            @RequestBody UpdateUrlRequest request) {
        try {
            // Convert status string to enum if provided
            Url.UrlStatus status = null;
            if (request.getStatus() != null) {
                try {
                    status = Url.UrlStatus.valueOf(request.getStatus().toUpperCase());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest()
                            .body(ApiResponse.error("Invalid status value", "INVALID_STATUS"));
                }
            }

            // Convert to service input data
            UpdateUrlInfoIData inputData = UpdateUrlInfoIData.builder()
                    .shortCode(shortCode)
                    .title(request.getTitle())
                    .password(request.getPassword())
                    .status(status)
                    .expiredAt(request.getExpiresAt())
                    .build();

            // Call service
            UpdateUrlInfoOData outputData = urlManagementService.updateUrlInfo(inputData);

            // Handle error cases
            if (outputData.getErrorCode() != ErrorCode.SUCCESS) {
                return ResponseEntity.status(getHttpStatus(outputData.getErrorCode()))
                        .body(ApiResponse.error(getErrorMessage(outputData.getErrorCode()),
                                outputData.getErrorCode().toString()));
            }

            // Convert to response
            UpdateUrlResponse response = UpdateUrlResponse.builder()
                    .shortCode(outputData.getShortCode())
                    .originalUrl(outputData.getOriginalUrl())
                    .title(outputData.getTitle())
                    .status(outputData.getStatus() != null ? outputData.getStatus().toString() : null)
                    .updatedAt(outputData.getUpdateAt())
                    .expiresAt(outputData.getExpireAt())
                    .build();

            return ResponseEntity.ok(ApiResponse.success("URL updated successfully", response));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error", ErrorCode.SYSTEM_ERROR.toString()));
        }
    }

    /**
     * Delete URL
     */
    @DeleteMapping("/{shortCode}")
    public ResponseEntity<ApiResponse<Void>> deleteUrl(@PathVariable String shortCode) {
        try {
            // Convert to service input data
            DeleteUrlInfoIData inputData = DeleteUrlInfoIData.builder()
                    .shortCode(shortCode)
                    .build();

            // Call service
            DeleteUrlInfoOData outputData = urlManagementService.deleteUrlInfo(inputData);

            // Handle error cases
            if (outputData.getErrorCode() != ErrorCode.SUCCESS) {
                return ResponseEntity.status(getHttpStatus(outputData.getErrorCode()))
                        .body(ApiResponse.error(getErrorMessage(outputData.getErrorCode()),
                                outputData.getErrorCode().toString()));
            }

            return ResponseEntity.ok(ApiResponse.success("URL deleted successfully", null));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error", ErrorCode.SYSTEM_ERROR.toString()));
        }
    }

    /**
     * Get redirect URL (for backend API - frontend handles actual redirect)
     */
    @GetMapping("/{shortCode}/redirect")
    public ResponseEntity<ApiResponse<RedirectResponse>> getRedirectUrl(@PathVariable String shortCode) {
        try {
            // Convert to service input data
            RedirectWithoutPasswordIData inputData = RedirectWithoutPasswordIData.builder()
                    .shortCode(shortCode)
                    .build();

            // Call service
            RedirectWithoutPasswordOData outputData = urlRedirectService.redirectWithoutPassword(inputData);

            // Handle specific cases
            if (outputData.getErrorCode() == ErrorCode.PASSWORD_REQUIRED) {
                RedirectResponse response = RedirectResponse.builder()
                        .passwordRequired(true)
                        .shortCode(outputData.getShortCode())
                        .build();
                return ResponseEntity.ok(ApiResponse.success("Password required", response));
            }

            // Handle other error cases
            if (outputData.getErrorCode() != ErrorCode.SUCCESS) {
                return ResponseEntity.status(getHttpStatus(outputData.getErrorCode()))
                        .body(ApiResponse.error(getErrorMessage(outputData.getErrorCode()),
                                outputData.getErrorCode().toString()));
            }

            // Success case - return original URL for frontend to redirect
            RedirectResponse response = RedirectResponse.builder()
                    .originalUrl(outputData.getOriginalUrl())
                    .passwordRequired(false)
                    .shortCode(shortCode)
                    .build();

            return ResponseEntity.ok(ApiResponse.success("Redirect URL retrieved", response));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error", ErrorCode.SYSTEM_ERROR.toString()));
        }
    }

    /**
     * Verify password and get redirect URL (for backend API)
     */
    @PostMapping("/{shortCode}/redirect")
    public ResponseEntity<ApiResponse<RedirectResponse>> verifyPasswordAndGetRedirectUrl(
            @PathVariable String shortCode,
            @RequestBody RedirectRequest request) {
        try {
            // Convert to service input data
            RedirectWithPasswordIData inputData = RedirectWithPasswordIData.builder()
                    .shortCode(shortCode)
                    .password(request.getPassword())
                    .build();

            // Call service
            RedirectWithPasswordOData outputData = urlRedirectService.redirectWithPassword(inputData);

            // Handle error cases
            if (outputData.getErrorCode() != ErrorCode.SUCCESS) {
                return ResponseEntity.status(getHttpStatus(outputData.getErrorCode()))
                        .body(ApiResponse.error(getErrorMessage(outputData.getErrorCode()),
                                outputData.getErrorCode().toString()));
            }

            // Success case - return original URL for frontend to redirect
            RedirectResponse response = RedirectResponse.builder()
                    .originalUrl(outputData.getOriginUrl())
                    .passwordRequired(false)
                    .shortCode(shortCode)
                    .build();

            return ResponseEntity.ok(ApiResponse.success("Password verified, redirect URL retrieved", response));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error", ErrorCode.SYSTEM_ERROR.toString()));
        }
    }

    /**
     * Helper method to convert ErrorCode to HTTP status
     */
    private HttpStatus getHttpStatus(ErrorCode errorCode) {
        switch (errorCode) {
            case URL_NOT_FOUND:
                return HttpStatus.NOT_FOUND;
            case URL_EXISTED:
            case PASSWORD_IN_CORRECT:
            case PASSWORD_REQUIRED:
                return HttpStatus.BAD_REQUEST;
            case URL_EXPIRED:
            case URL_DISABLED:
                return HttpStatus.GONE;
            case SYSTEM_ERROR:
            default:
                return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    /**
     * Helper method to get user-friendly error messages
     */
    private String getErrorMessage(ErrorCode errorCode) {
        switch (errorCode) {
            case URL_NOT_FOUND:
                return "URL not found";
            case URL_EXISTED:
                return "URL already exists";
            case PASSWORD_IN_CORRECT:
                return "Incorrect password";
            case URL_EXPIRED:
                return "URL has expired";
            case URL_DISABLED:
                return "URL has been disabled";
            case PASSWORD_REQUIRED:
                return "Password is required";
            case SYSTEM_ERROR:
                return "System error occurred";
            default:
                return "Unknown error";
        }
    }
}
