package com.project.sparta.friend.repository;

import com.project.sparta.friend.entity.Friend;
import com.project.sparta.friend.entity.QFriend;
import com.project.sparta.hashtag.entity.QHashtag;
import com.project.sparta.user.entity.StatusEnum;
import com.project.sparta.user.entity.*;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RequiredArgsConstructor
public class FriendRepositoryImpl implements FriendCustomRepository {

    private final JPAQueryFactory queryFactory;

    QUser user = new QUser("user");

    QUserTag userTag = new QUserTag("userTag");

    QHashtag hashtag = new QHashtag("Hashtag");

    QFriend friend = new QFriend("friend");

    @Override
    public PageImpl<User> randomUser(User userInfo, Pageable pageable, StatusEnum statusEnum) {

        BooleanBuilder builder = new BooleanBuilder();

        for (int i = 0; i < userInfo.getTags().size(); i++) {
            if (userInfo.getTags().get(i) != null) {
                builder.or(userTag.tag.eq(userInfo.getTags().get(i).getTag()));
            }
        }

        // 현재 회원을 뺀 가입 상태인 사용자의 태그 리스트 추출
        // 현재 회원의 태그와 맞는 회원 추출
        List<User> userList = queryFactory.select(user)
            .from(userTag)
            .leftJoin(userTag.user, user)
            .where(user.status.eq(statusEnum),
                    user.Id.ne(userInfo.getId()),
                    builder)
            .distinct()
            .orderBy(NumberExpression.random().asc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        getUserTagList(userList);

        return new PageImpl<>(userList, pageable, userList.size());
    }

    @Override
    public PageImpl<User> serachFriend(String targetUserName, Pageable pageable) {
        List<User> userList = queryFactory.select(user)
            .from(user)
            .where(user.nickName.contains(targetUserName))
            .orderBy(user.nickName.desc())
            .distinct()
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        getUserTagList(userList);

        return new PageImpl<>(userList, pageable, userList.size());
    }


    public void getUserTagList(List<User> userList) {
        for (int i = 0; i < userList.size(); i++) {
            List<UserTag> tags = queryFactory.select(userTag)
                .from(userTag)
                .where(userTag.user.Id.eq(userList.get(i).getId())).fetch();
            userList.get(i).getUserTagList(tags);
        }
    }
}
