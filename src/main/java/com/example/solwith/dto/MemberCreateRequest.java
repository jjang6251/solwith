package com.example.solwith.dto;


import jakarta.validation.constraints.NotBlank;

public class MemberCreateRequest {
    @NotBlank(message = "name은 필수입니다.")
    private String name;
    public String getName() {return name;}
    public void setName(String name) {this.name = name;}
}
