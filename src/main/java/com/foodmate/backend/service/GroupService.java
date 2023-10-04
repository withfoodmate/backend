package com.foodmate.backend.service;

import com.foodmate.backend.dto.GroupDto;
import org.springframework.security.core.Authentication;

public interface GroupService {

    String addGroup(Authentication authentication, GroupDto.Request request);

}
