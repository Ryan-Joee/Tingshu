package com.ryan.service;

import com.ryan.vo.UserListenProcessVo;

public interface ListenService {
    void getRecentlyPlay();

    void updatePlaySecond(UserListenProcessVo userListenProcessVo);

}
