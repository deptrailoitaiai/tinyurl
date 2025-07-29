package org.example.service;

import org.example.dto.request.RegisterRequest;
import org.example.dto.response.UserResponse;

public interface UserService {
    UserResponse registerUser(RegisterRequest registerRequest);
}
