package com.commerceos.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    private final String code;
    private final String message;
    private final Map<String, String> fieldErrors;
}
