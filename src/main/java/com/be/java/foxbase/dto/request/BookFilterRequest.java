package com.be.java.foxbase.dto.request;

import com.be.java.foxbase.utils.Filter;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults (level = AccessLevel.PRIVATE)
public class BookFilterRequest {
    Filter[] filters;
    String keyword;
}
