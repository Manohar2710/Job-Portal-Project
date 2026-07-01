package com.learning.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "firstName is required")
    String firstName,
    @NotBlank(message = "lastname is required")
    String lastname,
    @NotBlank(message = "email is required")
    @Email(message = "must be a valid email address")
    String email,
    @NotBlank(message = "password is required")
    @Size(min = 8, message = "password must be atlead 8 characters")
    String password,
    String phone
) {
}
