package com.be.java.foxbase.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ZaloPayWebhookRequest {
    @JsonProperty("appid")
    String appId;
    
    @JsonProperty("apptransid")
    String appTransId;
    
    @JsonProperty("pmcid")
    String pmcId;
    
    @JsonProperty("bankcode")
    String bankCode;
    
    @JsonProperty("amount")
    Long amount;
    
    @JsonProperty("discountamount")
    Long discountAmount;
    
    @JsonProperty("status")
    Integer status;
    
    @JsonProperty("checksum")
    String checksum;
    
    @JsonProperty("zptransid")
    String zpTransId;
    
    @JsonProperty("serverdate")
    Long serverDate;
}

