package com.busanit501.__team_back.exception;

/**
 * 사용자 ID (username)이 이미 존재할 때 발생하는 커스텀 예외
 */
public class DuplicateUsernameException extends RuntimeException {

    // 기본 생성자
    public DuplicateUsernameException() {
        super("이미 존재하는 사용자 ID입니다.");
    }

    // 메시지를 받아 생성하는 생성자
    public DuplicateUsernameException(String message) {
        super(message);
    }
}