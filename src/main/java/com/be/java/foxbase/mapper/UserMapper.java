package com.be.java.foxbase.mapper;

import com.be.java.foxbase.dto.request.UserCreationRequest;
import com.be.java.foxbase.dto.response.UserResponse;
import com.be.java.foxbase.db.entity.User;
import org.springframework.stereotype.Component;


@Component
public class UserMapper {
    public UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .lName(user.getLName())
                .fName(user.getFName())
                .balance(user.getBalance())
                .avatarUrl(user.getAvatar())
                .build();
    }

    public User toUser(UserCreationRequest userCreationRequest){
        return User.builder()
                .username(userCreationRequest.getUsername())
                .email(userCreationRequest.getEmail())
                .lName(userCreationRequest.getLName())
                .fName(userCreationRequest.getFName())
                .password(userCreationRequest.getPassword())
                .balance(userCreationRequest.getBalance())
                .build();
    }
}
