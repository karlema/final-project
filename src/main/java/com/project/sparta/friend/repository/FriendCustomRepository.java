package com.project.sparta.friend.repository;


import com.project.sparta.admin.entity.StatusEnum;
import com.project.sparta.user.entity.Tag;
import com.project.sparta.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FriendCustomRepository {

    Page<User> recommentFriendSearch(List<Tag> tagList, Pageable pageRequest, StatusEnum statusEnum);
    Page<User> serachFriend(String targetUserName, Pageable pageRequest);
}
