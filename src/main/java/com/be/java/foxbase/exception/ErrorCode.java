package com.be.java.foxbase.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    USER_EXIST(1002, "User existed", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXIST(1005, "User not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_DOB(1008, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    BOOK_NOT_FOUND(1009, "Book not found", HttpStatus.NOT_FOUND),
    INVALID_INPUT(1010, "Invalid or missing input fields.", HttpStatus.BAD_REQUEST),
    INVALID_USERNAME_FORMAT(1011, "Username must be 6-32 characters, no spaces, no special characters.", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL_FORMAT(1012, "Email must be in format <name>@<domain>.com, no spaces, <name> 6-32 characters.", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD_FORMAT(1013, "Password must be 6-32 characters, include at least 1 uppercase, 1 lowercase, 1 number, 1 special character, and no spaces.", HttpStatus.BAD_REQUEST),
    INVALID_RATE(1014, "Rate must be between 0 and 5", HttpStatus.BAD_REQUEST),
    INVALID_COMMENT(1015, "Comment must be at most 100 characters", HttpStatus.BAD_REQUEST),
    RATING_NOT_FOUND(2001, "Rating not found", HttpStatus.NOT_FOUND),
    UNMATCHED_EMAIL(2002, "Unmatched email", HttpStatus.BAD_REQUEST),
    NO_SECURITY_OTP(2003, "No Security OTP", HttpStatus.BAD_REQUEST),
    EMAIL_ALREADY_USED(2004, "Email already used", HttpStatus.BAD_REQUEST),
    FILE_SIZE_EXCEEDED(413, "File size exceeds 50MB limit!", HttpStatus.PAYLOAD_TOO_LARGE),
    INVALID_FILE_FORMAT(400, "Unsupported file format", HttpStatus.BAD_REQUEST),
    MISSING_FILE(400, "Missing required upload file", HttpStatus.BAD_REQUEST),
    MISSING_FILE_PDF(400, "PDF file is required.", HttpStatus.BAD_REQUEST),
    MISSING_FILE_COVER(400, "Cover image is required.", HttpStatus.BAD_REQUEST),
    FILE_UPLOAD_FAILED(500, "Upload failed.", HttpStatus.INTERNAL_SERVER_ERROR),
    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}