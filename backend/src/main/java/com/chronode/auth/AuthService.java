package com.chronode.auth;

import com.chronode.auth.dto.AuthResponse;
import com.chronode.auth.dto.LoginRequest;
import com.chronode.auth.dto.RegisterRequest;

public interface AuthService {

    AuthResponse login(LoginRequest request);

    AuthResponse register(RegisterRequest request);
}
