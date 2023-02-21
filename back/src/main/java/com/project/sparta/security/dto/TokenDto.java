package com.project.sparta.security.dto;

import com.project.sparta.user.entity.UserRoleEnum;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TokenDto {
    private String accessToken;
    private String refreshToken;
    private UserRoleEnum role;

    @Builder
    public TokenDto(String accessToken, String refreshToken, UserRoleEnum role) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.role = role;
    }
}
