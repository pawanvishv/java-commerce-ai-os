package com.commerceos.platformadmin.application.commands;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterClientCommand {

    @NotBlank
    private String businessName;

    @Email
    @NotBlank
    private String email;

    private String phone;
    private String gstin;
    private String pan;

    @NotBlank
    private String profile;

    private String plan;
}
