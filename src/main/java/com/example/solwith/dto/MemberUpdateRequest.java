package com.example.solwith.dto;

import jakarta.validation.constraints.NotBlank;
import org.springframework.stereotype.Service;

public class MemberUpdateRequest {
    @NotBlank(message = "name은 비워둘 수 없습니다.")
    private String name;
    public String getName() {return name;}
    public void setName(String name) { this.name = name; }
}
