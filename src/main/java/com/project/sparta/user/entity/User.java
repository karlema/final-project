package com.project.sparta.user.entity;

import com.project.sparta.admin.entity.Admin;
import com.project.sparta.admin.entity.StatusEnum;
import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity(name = "USERS")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class User extends Admin {

    @Column(nullable = false)
    private int age;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Column(nullable = false)
    private String userImageUrl;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Tag> tags = new ArrayList<>();


    public User(String password, String nickName, int age, String phoneNumber, String email, UserRoleEnum role, String userImageUrl, StatusEnum status, List<Tag> tags) {
        this.password = password;
        this.nickName = nickName;
        this.age = age;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.role = role;
        this.userImageUrl = userImageUrl;
        this.status = status;
        this.tags = tags;
    }

    public void addTag(Tag tag){
        this.tags.add(tag);
    }
}
