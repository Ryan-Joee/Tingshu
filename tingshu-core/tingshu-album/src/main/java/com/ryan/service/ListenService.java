package com.ryan.service;

import com.ryan.vo.UserListenProcessVo;

import java.math.BigDecimal;
import java.util.Map;

public interface ListenService {
    Map<String,Object> getRecentlyPlay();

    void updatePlaySecond(UserListenProcessVo userListenProcessVo);

    BigDecimal getLastPlaySecond(Long trackId);

    boolean collectTrack(Long trackId);
}
