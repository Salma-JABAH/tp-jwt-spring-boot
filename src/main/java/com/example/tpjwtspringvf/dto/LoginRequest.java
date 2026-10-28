package com.example.tpjwtspringvf.dto;

public record LoginRequest(
        String email,
        String password
) {
}
