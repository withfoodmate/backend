package com.foodmate.backend.service;

import com.foodmate.backend.dto.GroupDto;
import org.springframework.security.core.Authentication;

public interface GroupService {

    String addGroup(Authentication authentication, GroupDto.Request request);

    GroupDto.DetailResponse getGroupDetail(Long groupId);

    String updateGroup(Long groupId, Authentication authentication, GroupDto.Request request);

    String deleteGroup(Long groupId, Authentication authentication);

    String enrollInGroup(Long groupId, Authentication authentication);

}
