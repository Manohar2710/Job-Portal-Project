package com.learning.security.dto;

import com.learning.security.entity.Role;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @Schema(example = "John")
    @NotBlank(message = "firstName is required")
    String firstName,
    @Schema(example = "Paul")
    @NotBlank(message = "lastname is required")
    String lastname,
    @Schema(example = "john@gmail.com")
    @NotBlank(message = "email is required")
    @Email(message = "must be a valid email address")
    String email,
    @Schema(example = "secret@123")
    @NotBlank(message = "password is required")
    @Size(min = 8, message = "password must be atlead 8 characters")
    String password,
    @Schema(example = "+919999999999")
    String phone,
    @Schema(example = "ROLE_RECRUITER")
    Role role
) {
}
