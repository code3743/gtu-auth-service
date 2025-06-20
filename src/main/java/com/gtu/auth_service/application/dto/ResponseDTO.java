package com.gtu.auth_service.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO that represents the response of an API endpoint")
public class ResponseDTO<T> {

    @Schema(description = "Message of the response", example = "Request was successful")
    private String message;

    @Schema(description = "Data of the response")
    private T data;

    @Schema(description = "Status code of the response", example = "200")
    private int status;
}