package com.example.solwith.auth;

import com.example.solwith.common.ApiResponse;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@RequestBody LoginRequset req){
        //데모 버전: 인메모리 계정
        if (!"user".equals(req.username) || !"pass".equals(req.password)) {
            return ResponseEntity.status(401).body(ApiResponse.error(
                    401, "Invalid credentials", "/api/auth/login", null, null));
        }

        String token = "Bearer " + jwtProvider.createToken(req.username, "USER", Map.of("nickname", "홍길동"));
        return ResponseEntity.ok(ApiResponse.success(
                Map.of("token", token), "/api/auth/login", null));
    }

    private final JwtProvider jwtProvider;
    public AuthController(JwtProvider jwtProvider) { this.jwtProvider = jwtProvider; }

    public static class LoginRequset {
        @NotBlank public String username;
        @NotBlank public String password;
    }
}
