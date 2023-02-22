package com.project.sparta.recommendCourse.service;


import static com.project.sparta.recommendCourse.entity.PostStatusEnum.VAILABLE;

import com.project.sparta.common.dto.PageResponseDto;
import com.project.sparta.exception.CustomException;
import com.project.sparta.exception.api.Status;
import com.project.sparta.like.repository.LikeRecommendRepository;
import com.project.sparta.recommendCourse.dto.RecommendDetailResponseDto;
import com.project.sparta.recommendCourse.dto.RecommendRequestDto;
import com.project.sparta.recommendCourse.dto.RecommendResponseDto;
import com.project.sparta.recommendCourse.entity.RecommendCourseBoard;
import com.project.sparta.recommendCourse.entity.RecommendCourseImg;
import com.project.sparta.recommendCourse.repository.RecommendCourseBoardImgRepository;
import com.project.sparta.recommendCourse.repository.RecommendCourseBoardRepository;
import com.project.sparta.user.entity.User;
import com.project.sparta.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RecommendCourseBoardServiceImpl implements RecommendCourseBoardService {

    private final RecommendCourseBoardRepository recommendCourseBoardRepository;

    private final RecommendCourseBoardImgRepository recommendCourseBoardImgRepository;

    /**
     * 추천코스 게시글 등록 메서드
     *
     * @param requestPostDto:(String title,int score,String season,int altitude,String contents)
     * @return 이미지경로 URL 리턴
     * @throws IOException
     */
    @Override
    public void creatRecommendCourseBoard(RecommendRequestDto requestPostDto, Long userId) {

        RecommendCourseBoard board = RecommendCourseBoard.builder()
            .score(requestPostDto.getScore())
            .title(requestPostDto.getTitle())
            .season(requestPostDto.getSeason())
            .altitude(requestPostDto.getAltitude())
            .contents(requestPostDto.getContents())
            .region(requestPostDto.getRegion())
            .userId(userId)
            .postStatus(VAILABLE)
            .build();

        for (String imgUrl : requestPostDto.getImgList()) {
            RecommendCourseImg courseImg = new RecommendCourseImg(imgUrl, board);
            recommendCourseBoardImgRepository.save(courseImg);
        }
        recommendCourseBoardRepository.save(board);

    }


    /**
     * 코스추천 글수정 메서드
     *
     * @param id : 선택한 게시글 id값 // * @param recommendRequestDto : 타이틀, 컨텐츠
     * @return 이미지 리스트 URL 리턴함
     * @throws IOException
     */

    //코스 수정
    @Override
    public void modifyRecommendCourseBoard(Long id, RecommendRequestDto requestDto, Long userId) {
        //수정하고자 하는 board 있는지 확인
        RecommendCourseBoard checkBoard = recommendCourseBoardRepository.findById(id).orElseThrow(()-> new CustomException(Status.NOT_FOUND_POST));

        //board의 작성자 맞는지 확인
        if (!checkBoard.getUserId().equals(userId)) {
            throw new CustomException(Status.NO_PERMISSIONS_POST);
        }

        RecommendCourseBoard board = RecommendCourseBoard.builder()
            .score(requestDto.getScore())
            .title(requestDto.getTitle())
            .season(requestDto.getSeason())
            .altitude(requestDto.getAltitude())
            .contents(requestDto.getContents())
            .region(requestDto.getRegion())
            .userId(userId)
            .postStatus(VAILABLE)
            .build();

        //기존에 이미지 삭제
        recommendCourseBoardImgRepository.deleteBoard(id);

        //새로운 이미지 추가
        for (String imgUrl : requestDto.getImgList()) {
            RecommendCourseImg courseImg = new RecommendCourseImg(imgUrl, board);
            recommendCourseBoardImgRepository.saveAndFlush(courseImg);
        }

        //게시글 다시 등록
        recommendCourseBoardRepository.saveAndFlush(board);

    }

    /**
     * 코스추천 삭제 메서드
     *
     * @param id : 삭제할 게시글 아이디
     */

    //코스추천 삭제
    @Override
    public void deleteRecommendCourseBoard(Long id, Long userId) {
        //삭제하려는 board 있는지 확인
        RecommendCourseBoard post = recommendCourseBoardRepository.findById(id)
            .orElseThrow(() -> new CustomException(Status.NOT_FOUND_POST));

        //board의 작성자 맞는지 확인
        if (!post.getUserId().equals(userId)) {
            throw new CustomException(Status.NO_PERMISSIONS_POST);
        }

        //게시글 이미지 삭제
        recommendCourseBoardImgRepository.deleteBoard(id);

        //게시글 삭제
        recommendCourseBoardRepository.deleteById(post.getId());
    }

    //단건 코스 조회
    @Override
    public RecommendDetailResponseDto oneSelectRecommendCourseBoard(Long boardId) {
        return recommendCourseBoardRepository.getCourseBoard(boardId, VAILABLE);
    }

    //코스 추천 전체 조회
    @Override
    public PageResponseDto<List<RecommendResponseDto>> allRecommendCourseBoard(int page, int size,
        int score, String season, int altitude, String region, String orderByLike) {

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<RecommendResponseDto> courseAllList = recommendCourseBoardRepository.allRecommendBoardList(
            pageRequest, VAILABLE, score, season, altitude, region, orderByLike);

        List<RecommendResponseDto> content = courseAllList.getContent();
        long totalCount = courseAllList.getTotalElements();
        return new PageResponseDto<>(page, totalCount, content.stream().distinct().collect(
            Collectors.toList()));
    }

    //내가 쓴 코스 추천 조회
    @Override
    public PageResponseDto<List<RecommendResponseDto>> getMyRecommendCourseBoard(int page, int size, User user) {

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<RecommendResponseDto> myCourseBoardList = recommendCourseBoardRepository.myRecommendBoardList(pageRequest, VAILABLE, user.getId());

        List<RecommendResponseDto> content = myCourseBoardList.getContent();
        long totalCount = myCourseBoardList.getTotalElements();

        return new PageResponseDto<>(page, totalCount, content);
    }

    //어드민 코스 수정
    @Override
    public void adminRecommendBoardUpdate(Long id, RecommendRequestDto requestDto, Long userId) {

        //수정하고자 하는 board 있는지 확인
        recommendCourseBoardRepository.findById(id).orElseThrow(()-> new CustomException(Status.NOT_FOUND_POST));

        RecommendCourseBoard board = RecommendCourseBoard.builder()
            .score(requestDto.getScore())
            .title(requestDto.getTitle())
            .season(requestDto.getSeason())
            .altitude(requestDto.getAltitude())
            .contents(requestDto.getContents())
            .region(requestDto.getRegion())
            .userId(userId)
            .postStatus(VAILABLE)
            .build();

        //기존에 이미지 삭제
        recommendCourseBoardImgRepository.deleteBoard(id);

        //새로운 이미지 추가
        for (String imgUrl : requestDto.getImgList()) {
            RecommendCourseImg courseImg = new RecommendCourseImg(imgUrl, board);
            recommendCourseBoardImgRepository.saveAndFlush(courseImg);
        }

        //게시글 다시 등록
        recommendCourseBoardRepository.saveAndFlush(board);
    }

    /**
     * 코스추천 삭제 메서드
     * @param id : 삭제할 게시글 아이디
     */

    //어드민 코스 삭제
    @Override
    public void adminRecommendBoardDelete(Long id) {
        //삭제하려는 board 있는지 확인
        RecommendCourseBoard post = recommendCourseBoardRepository.findById(id)
            .orElseThrow(() -> new CustomException(Status.NOT_FOUND_POST));
        //게시글 이미지 삭제
        recommendCourseBoardImgRepository.deleteBoard(id);

        //게시글 삭제
        recommendCourseBoardRepository.deleteById(post.getId());
    }
}