package com.foodmate.backend.service;

import com.foodmate.backend.dto.RankingDto;
import com.foodmate.backend.entity.Member;
import com.foodmate.backend.repository.FoodGroupRepository;
import com.foodmate.backend.repository.FoodRepository;
import com.foodmate.backend.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)   // Mockito 사용해서 테스트 할거다
class RankingServiceTest {

    @Mock   // 가짜
    private MemberRepository memberRepository;

    @Mock   // 가짜
    private FoodGroupRepository foodGroupRepository;

    @Mock   // 가짜
    private FoodRepository foodRepository;

    @InjectMocks   // 위의 가짜 리포지토리들을 rankingService 에 넣어주겠다는 의미
    private RankingService rankingService;

    @Test
    @DisplayName("좋아요 랭킹")
    void getLikesRanking() {
        // given - 가짜 데이터 생성
        Member member1 = Member.builder()
                .id(1L)
                .nickname("사용자1")
                .image("사용자1.png")
                .likes(100)
                .build();
        Member member2 = Member.builder()
                .id(2L)
                .nickname("사용자2")
                .image("사용자2.png")
                .likes(200)
                .build();
        Member member3 = Member.builder()
                .id(3L)
                .nickname("사용자3")
                .image("사용자3.png")
                .likes(300)
                .build();

        // 가짜 리스트 생성
        List<Member> mockMembers = Arrays.asList(member3, member2, member1);
        // 해당 리포지토리 메소드 호출시 위의 가짜 리스트가 반환될 것이다
        when(memberRepository.findTop10ByOrderByLikesDesc()).thenReturn(mockMembers);

        // when - 테스트하려는 서비스 메소드 호출
        List<RankingDto.Likes> result = rankingService.getLikesRanking();

        // then - 결과 검증
        assertEquals(3L, result.get(0).getMemberId());
        assertEquals("사용자3", result.get(0).getNickname());
        assertEquals("사용자3.png", result.get(0).getImage());
        assertEquals(300, result.get(0).getLikesCount());

        assertEquals(2L, result.get(1).getMemberId());
        assertEquals("사용자2", result.get(1).getNickname());
        assertEquals("사용자2.png", result.get(1).getImage());
        assertEquals(200, result.get(1).getLikesCount());

        assertEquals(1L, result.get(2).getMemberId());
        assertEquals("사용자1", result.get(2).getNickname());
        assertEquals("사용자1.png", result.get(2).getImage());
        assertEquals(100, result.get(2).getLikesCount());

        // memberRepository 의 findTop10ByOrderByLikesDesc 메소드가 한번 호출되었는지 확인
        verify(memberRepository, times(1)).findTop10ByOrderByLikesDesc();
    }

    @Test
    @DisplayName("모임왕 랭킹")
    void getMeetingRanking() {
        // given - 가짜 데이터 생성
        Object[] member1 = {1L, "사용자1", "사용자1.png", 10L};
        Object[] member2 = {2L, "사용자2", "사용자2.png", 20L};
        Object[] member3 = {3L, "사용자3", "사용자3.png", 30L};

        // 가짜 리스트 생성
        List<Object[]> mockList = List.of(member3, member2, member1);
        // 해당 리포지토리 메소드 호출시 위의 가짜 리스트가 반환될 것이다
        when(memberRepository.findTop10MemberWithCount(any(LocalDateTime.class), any())).thenReturn(mockList);

        // when - 테스트하려는 서비스 메소드 호출
        List<RankingDto.Meeting> result = rankingService.getMeetingRanking();

        // then - 결과 검증
        assertEquals(3L, result.get(0).getMemberId());
        assertEquals("사용자3", result.get(0).getNickname());
        assertEquals("사용자3.png", result.get(0).getImage());
        assertEquals(30L, result.get(0).getCount());

        assertEquals(2L, result.get(1).getMemberId());
        assertEquals("사용자2", result.get(1).getNickname());
        assertEquals("사용자2.png", result.get(1).getImage());
        assertEquals(20L, result.get(1).getCount());

        assertEquals(1L, result.get(2).getMemberId());
        assertEquals("사용자1", result.get(2).getNickname());
        assertEquals("사용자1.png", result.get(2).getImage());
        assertEquals(10L, result.get(2).getCount());

        // memberRepository 의 findTop10MemberWithCount 메소드가 한번 호출되었는지 확인
        verify(memberRepository, times(1)).findTop10MemberWithCount(any(LocalDateTime.class), any());
    }

    @Test
    @DisplayName("많이찾는 식당 랭킹")
    void getStoreRanking() {
        // given - 가짜 데이터 생성
        Object[] store1 = {"식당1", "식당1의 주소", 10L};
        Object[] store2 = {"식당2", "식당2의 주소", 20L};
        Object[] store3 = {"식당3", "식당3의 주소", 30L};

        // 가짜 리스트 생성
        List<Object[]> mockList = List.of(store3, store2, store1);
        // 해당 리포지토리 메소드 호출시 위의 가짜 리스트가 반환될 것이다
        when(foodGroupRepository.findTop10StoreWithCount(any(LocalDateTime.class), any())).thenReturn(mockList);

        // when - 테스트하려는 서비스 메소드 호출
        List<RankingDto.Store> result = rankingService.getStoreRanking();

        // then - 결과 검증
        assertEquals("식당3", result.get(0).getStoreName());
        assertEquals("식당3의 주소", result.get(0).getAddress());
        assertEquals(30L, result.get(0).getCount());

        assertEquals("식당2", result.get(1).getStoreName());
        assertEquals("식당2의 주소", result.get(1).getAddress());
        assertEquals(20L, result.get(1).getCount());

        assertEquals("식당1", result.get(2).getStoreName());
        assertEquals("식당1의 주소", result.get(2).getAddress());
        assertEquals(10L, result.get(2).getCount());

        // foodGroupRepository 의 findTop10StoreWithCount 메소드가 한번 호출되었는지 확인
        verify(foodGroupRepository, times(1)).findTop10StoreWithCount(any(LocalDateTime.class), any());
    }

    @Test
    @DisplayName("음식 카테고리 랭킹")
    void getFoodRanking() {
        // given - 가짜 데이터 생성
        Object[] food1 = {"음식1", "음식1.png", 10L};
        Object[] food2 = {"음식2", "음식2.png", 20L};
        Object[] food3 = {"음식3", "음식3.png", 30L};

        // 가짜 리스트 생성
        List<Object[]> mockList = List.of(food3, food2, food1);
        // 해당 리포지토리 메소드 호출시 위의 가짜 리스트가 반환될 것이다
        when(foodRepository.findTop10FoodWithCount(any(LocalDateTime.class), any())).thenReturn(mockList);

        // when - 테스트하려는 서비스 메소드 호출
        List<RankingDto.Food> result = rankingService.getFoodRanking();

        // then - 결과 검증
        assertEquals("음식3", result.get(0).getFoodName());
        assertEquals("음식3.png", result.get(0).getImage());
        assertEquals(30L, result.get(0).getCount());

        assertEquals("음식2", result.get(1).getFoodName());
        assertEquals("음식2.png", result.get(1).getImage());
        assertEquals(20L, result.get(1).getCount());

        assertEquals("음식1", result.get(2).getFoodName());
        assertEquals("음식1.png", result.get(2).getImage());
        assertEquals(10L, result.get(2).getCount());

        // foodRepository 의 findTop10FoodWithCount 메소드가 한번 호출되었는지 확인
        verify(foodRepository, times(1)).findTop10FoodWithCount(any(LocalDateTime.class), any());
    }

}