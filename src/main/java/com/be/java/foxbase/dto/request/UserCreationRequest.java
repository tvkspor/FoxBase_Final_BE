package com.be.java.foxbase.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {
    String username;
    String email;
    String password;

    @JsonProperty("lName")
    String lName;

    @JsonProperty("fName")
    String fName;

    Long balance;
    @JsonProperty("captchaToken")
    String captchaToken;
}