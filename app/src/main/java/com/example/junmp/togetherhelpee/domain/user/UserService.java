package com.example.junmp.togetherhelpee.domain.user;

import com.example.junmp.togetherhelpee.common.util.network.RetrofitBuilder;

import java.io.IOException;

public class UserService {

    private UserRepository userRepository = RetrofitBuilder.builder().create(UserRepository.class);
    /**
     * 회원 가입 여부를 서버에서 조회한 이후 boolean 값을 리턴한다.
     * @return true , false
     */
    public boolean isRegistered(String deviceId) {
        User loggedUser = getLoggedUser(deviceId);
        return loggedUser != null;
    }


    /**
     * 로그인 여부를  서버에서 조회한 이후 boolean 값을 리턴한다.
     * @return
     */
    public boolean isLogged(String deviceId) {
        return getLoggedUser(deviceId) != null;
    }

    /**
     * 로그인 한 유저 정보를 가져올것
     * @return
     */
    public User getLoggedUser(String deviceId)  {
        try {
            return userRepository.getOne(deviceId).execute().body();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
