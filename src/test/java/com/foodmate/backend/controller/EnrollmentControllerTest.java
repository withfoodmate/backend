package com.foodmate.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodmate.backend.dto.EnrollmentDto;
import com.foodmate.backend.enums.EnrollmentStatus;
import com.foodmate.backend.service.EnrollmentService;
import com.foodmate.backend.service.MemberService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@AutoConfigureMockMvc
@SpringBootTest
@MockBean(JpaMetamodelMappingContext.class)
class EnrollmentControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @InjectMocks
    private EnrollmentController enrollmentController;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Mock
    private EnrollmentService enrollmentService;

    @Mock
    private MemberService memberService;

    @Test
    public void testGetMyEnrollment() throws Exception {
        // Given
        String status = "accept"; // 신청 상태 설정
        Pageable pageable = PageRequest.of(0, 5); // 페이지 크기를 5로 설정

        String rawPassword = "password";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // 가상의 사용자 정보 생성
        UserDetails userDetails = new User("test1234@naver.com", encodedPassword, Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, encodedPassword, userDetails.getAuthorities());

        // Mock 서비스 메소드의 반환값 설정
        EnrollmentDto.myEnrollmentResponse enrollmentResponse = EnrollmentDto.myEnrollmentResponse.builder()
                .id(1L)
                .foodGroupId(2L)
                .foodGroupMemberImage("adasdsa")
                .foodGroupTitle("dsadsa")
                .foodGroupName("test")
                .foodGroupFoodType("치킨")
                .foodGroupGroupDateTime(LocalDateTime.now())
                .foodGroupMaximum(7)
                .foodGroupStoreName("zcxczv")
                .foodGroupStoreAddress("서울시 치킨집")
                .status(EnrollmentStatus.ACCEPT)
                .build();

        // enrollmentService를 모의 객체로 만들고 스텁 설정
        when(enrollmentService.getMyEnrollment(eq(status), eq(authentication), eq(pageable)))
                .thenReturn(new PageImpl<>(Collections.singletonList(enrollmentResponse)));


        SecurityContextHolder.getContext().setAuthentication(authentication);

        // When
        String responseJson = mockMvc.perform(MockMvcRequestBuilders
                        .get("/enrollment")
                        .param("status", status)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse()
                .getContentAsString();

        ObjectMapper objectMapper = new ObjectMapper();

        EnrollmentDto.myEnrollmentResponse responseObject = objectMapper.readValue(responseJson, EnrollmentDto.myEnrollmentResponse.class);

        // Then


        // 서비스 메소드 호출 및 응답 결과 검증
        verify(enrollmentService).getMyEnrollment(eq(status), eq(authentication), eq(pageable));
    }


}