package org.example.controller;

import org.example.constants.ErrorCode;
import org.example.dto.request.*;
import org.example.dto.response.*;
import org.example.service.authFogetPassword.AuthForgetPasswordService;
import org.example.service.authLogin.AuthLoginService;
import org.example.service.authLogin.factory.AuthLoginServiceFactoryFinder;
import org.example.service.authRegister.AuthRegisterService;
import org.example.service.data.*;
import org.example.service.publicKeyProvider.PublicKeyProviderService;
import org.example.service.refreshAccessToken.RefreshAccessTokenService;
import org.example.service.userManagement.UserManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private AuthLoginServiceFactoryFinder authLoginServiceFactoryFinder;

    @Autowired
    private AuthRegisterService authRegisterService;

    @Autowired
    private UserManagementService userManagementService;

    @Autowired
    private RefreshAccessTokenService refreshAccessTokenService;

    @Autowired
    private AuthForgetPasswordService authForgetPasswordService;

    @Autowired
    private PublicKeyProviderService publicKeyProviderService;

    /**
     * User login endpoint
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody LoginRequest request) {
        try {
            // Get appropriate login service based on login type
            AuthLoginService authLoginService = authLoginServiceFactoryFinder.findAuthLoginService(request.getLoginType());
            
            // Convert request to service input data
            AuthenticateLoginIData inputData = AuthenticateLoginIData.builder()
                    .email(request.getEmail())
                    .password(request.getPassword())
                    .loginType(request.getLoginType())
                    .googleToken(request.getGoogleToken())
                    .facebookToken(request.getFacebookToken())
                    .build();

            // Call service
            AuthenticateLoginOData outputData = authLoginService.authenticate(inputData);

            // Handle error cases
            if (outputData.getErrCode() != ErrorCode.SUCCESS) {
                return ResponseEntity.status(getHttpStatus(outputData.getErrCode()))
                        .body(ApiResponse.error(getErrorMessage(outputData.getErrCode()),
                                outputData.getErrCode().toString()));
            }

            // Convert to response
            AuthResponse response = AuthResponse.builder()
                    .accessToken(outputData.getAccessToken())
                    .refreshToken(outputData.getRefreshToken())
                    .userId(outputData.getUserId())
                    .userEmail(outputData.getUserEmail())
                    .expiresIn(outputData.getExpiresIn())
                    .build();

            return ResponseEntity.ok(ApiResponse.success("Login successful", response));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error", ErrorCode.SYSTEM_ERROR.toString()));
        }
    }

    /**
     * User registration endpoint
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@RequestBody RegisterRequest request) {
        try {
            // Convert request to service input data
            RegisterIData inputData = RegisterIData.builder()
                    .fullName(request.getUsername())
                    .password(request.getPassword())
                    .email(request.getEmail())
                    .build();

            // Call service
            RegisterOData outputData = authRegisterService.register(inputData);

            // Handle error cases
            if (outputData.getErrCode() != ErrorCode.SUCCESS) {
                return ResponseEntity.status(getHttpStatus(outputData.getErrCode()))
                        .body(ApiResponse.error(getErrorMessage(outputData.getErrCode()),
                                outputData.getErrCode().toString()));
            }

            // Convert to response
            RegisterResponse response = RegisterResponse.builder()
                    .userId(outputData.getUserId())
                    .email(outputData.getEmail())
                    .message("Registration successful. Please check your email for verification.")
                    .emailVerificationRequired(true)
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success("User registered successfully", response));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error", ErrorCode.SYSTEM_ERROR.toString()));
        }
    }

    /**
     * Email verification endpoint
     */
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<String>> verifyEmail(@RequestBody VerifyEmailRequest request) {
        try {
            // Convert request to service input data
            VerifyEmailRegisterIData inputData = VerifyEmailRegisterIData.builder()
                    .token(request.getToken())
                    .build();

            // Call service
            VerifyEmailRegisterOData outputData = authRegisterService.verifyEmail(inputData);

            // Handle error cases
            if (outputData.getErrCode() != ErrorCode.SUCCESS) {
                return ResponseEntity.status(getHttpStatus(outputData.getErrCode()))
                        .body(ApiResponse.error(getErrorMessage(outputData.getErrCode()),
                                outputData.getErrCode().toString()));
            }

            return ResponseEntity.ok(ApiResponse.success("Email verified successfully", "Email verification completed"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error", ErrorCode.SYSTEM_ERROR.toString()));
        }
    }

    /**
     * Refresh access token endpoint
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            // Convert request to service input data
            RefreshAccessTokenIData inputData = RefreshAccessTokenIData.builder()
                    .refreshToken(request.getRefreshToken())
                    .build();

            // Call service
            RefreshAccessTokenOData outputData = refreshAccessTokenService.refresh(inputData);

            // Handle error cases
            if (outputData.getErrCode() != ErrorCode.SUCCESS) {
                return ResponseEntity.status(getHttpStatus(outputData.getErrCode()))
                        .body(ApiResponse.error(getErrorMessage(outputData.getErrCode()),
                                outputData.getErrCode().toString()));
            }

            // Convert to response
            AuthResponse response = AuthResponse.builder()
                    .accessToken(outputData.getAccessToken())
                    .refreshToken(outputData.getRefreshToken())
                    .userId(null) // Not provided in RefreshAccessTokenOData
                    .userEmail(null) // Not provided in RefreshAccessTokenOData
                    .expiresIn(null) // Not provided in RefreshAccessTokenOData
                    .build();

            return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error", ErrorCode.SYSTEM_ERROR.toString()));
        }
    }

    /**
     * Get user profile endpoint
     */
    @GetMapping("/profile/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserProfile(@PathVariable Long userId) {
        try {
            // Convert to service input data
            UserInfoIData inputData = UserInfoIData.builder()
                    .userId(userId)
                    .build();

            // Call service
            UserInfoOData outputData = userManagementService.getUserInfo(inputData);

            // Handle error cases
            if (outputData.getErrCode() != ErrorCode.SUCCESS) {
                return ResponseEntity.status(getHttpStatus(outputData.getErrCode()))
                        .body(ApiResponse.error(getErrorMessage(outputData.getErrCode()),
                                outputData.getErrCode().toString()));
            }

            // Convert to response
            UserResponse response = UserResponse.builder()
                    .id(outputData.getUserId())
                    .username(outputData.getFullName())
                    .email(outputData.getEmail())
                    .createdAt(outputData.getLastUpdate())
                    .build();

            return ResponseEntity.ok(ApiResponse.success(response));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error", ErrorCode.SYSTEM_ERROR.toString()));
        }
    }

    /**
     * Update user profile endpoint
     */
    @PutMapping("/profile/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserProfile(
            @PathVariable Long userId,
            @RequestBody UpdateUserRequest request) {
        try {
            // Convert to service input data
            ChangeUserInfoIData inputData = ChangeUserInfoIData.builder()
                    .userId(userId)
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .phoneNumber(request.getPhoneNumber())
                    .currentPassword(request.getCurrentPassword())
                    .build();

            // Call service
            ChangeUserInfoOData outputData = userManagementService.changeUserInfo(inputData);

            // Handle error cases
            if (!outputData.isSuccess()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error(outputData.getMessage(), "UPDATE_FAILED"));
            }

            // Convert to response  
            UserResponse response = UserResponse.builder()
                    .id(outputData.getUpdatedUserInfo().getUserId())
                    .username(outputData.getUpdatedUserInfo().getFullName())
                    .email(outputData.getUpdatedUserInfo().getEmail())
                    .createdAt(outputData.getUpdatedUserInfo().getLastUpdate())
                    .build();

            return ResponseEntity.ok(ApiResponse.success("User profile updated successfully", response));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error", ErrorCode.SYSTEM_ERROR.toString()));
        }
    }

    /**
     * Resend email verification endpoint
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<String>> resendVerificationEmail(@RequestParam String email) {
        try {
            // Convert to service input data
            SendEmailToVerifyRegisterIData inputData = SendEmailToVerifyRegisterIData.builder()
                    .email(email)
                    .build();

            // Call service
            SendEmailToVerifyRegisterOData outputData = authRegisterService.sendEmailToVerifyRegister(inputData);

            // Handle error cases
            if (outputData.getErrCode() != ErrorCode.SUCCESS) {
                return ResponseEntity.status(getHttpStatus(outputData.getErrCode()))
                        .body(ApiResponse.error(getErrorMessage(outputData.getErrCode()),
                                outputData.getErrCode().toString()));
            }

            return ResponseEntity.ok(ApiResponse.success("Verification email sent successfully", 
                    "Please check your email for verification instructions"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error", ErrorCode.SYSTEM_ERROR.toString()));
        }
    }

    /**
     * Forgot password endpoint - send reset email
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            // Convert to service input data
            ForgetPasswordIData inputData = ForgetPasswordIData.builder()
                    .email(request.getEmail())
                    .build();

            // Call service to generate passcode
            ForgetPasswordOData outputData = authForgetPasswordService.generatePasscode(inputData);

            // Handle error cases
            if (outputData.getErrCode() != ErrorCode.SUCCESS) {
                return ResponseEntity.status(getHttpStatus(outputData.getErrCode()))
                        .body(ApiResponse.error(getErrorMessage(outputData.getErrCode()),
                                outputData.getErrCode().toString()));
            }

            return ResponseEntity.ok(ApiResponse.success("Password reset email sent successfully", 
                    "Please check your email for password reset instructions"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Internal server error", ErrorCode.SYSTEM_ERROR.toString()));
        }
    }

    /**
     * Get public key for JWT verification
     */
    @GetMapping("/public-key")
    public ResponseEntity<ApiResponse<PublicKeyOData>> getPublicKey() {
        try {
            // Call service
            PublicKeyOData outputData = publicKeyProviderService.getCurrentPublicKey();

            return ResponseEntity.ok(ApiResponse.success("Public key retrieved successfully", outputData));

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
            case USER_NOT_FOUND:
            case USER_NOT_EXISTED:
                return HttpStatus.NOT_FOUND;
            case USER_EXISTED:
            case PASSWORD_IN_CORRECT:
                return HttpStatus.BAD_REQUEST;
            case UNIDENTIFIED_TOKEN:
                return HttpStatus.UNAUTHORIZED;
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
            case USER_NOT_FOUND:
            case USER_NOT_EXISTED:
                return "User not found";
            case USER_EXISTED:
                return "User already exists";
            case PASSWORD_IN_CORRECT:
                return "Incorrect password";
            case UNIDENTIFIED_TOKEN:
                return "Invalid or expired token";
            case SYSTEM_ERROR:
                return "System error occurred";
            default:
                return "Unknown error";
        }
    }
}
