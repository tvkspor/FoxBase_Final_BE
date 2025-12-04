package com.be.java.foxbase.dto.zalopay;

import lombok.*;
import lombok.experimental.FieldDefaults;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ZaloPayOrder {
    int appid;
    String appuser;
    Long apptime;
    Long amount;
    String apptransid;
    String embeddata;
    String item;
    String mac;
}
