package com.busanit501.__team_back.exception;

/**
 * YouTube API 호출 중 발생하는 예외를 처리하기 위한 커스텀 예외 클래스
 * YYJ.md 요구사항: API 키 만료, 네트워크 문제 등 심각한 오류 발생 시, 
 * 원인 예외를 포함한 커스텀 RuntimeException을 던져서 처리 실패를 알립니다.
 */
public class YoutubeApiException extends RuntimeException {

    public YoutubeApiException(String message) {
        super(message);
    }

    public YoutubeApiException(String message, Throwable cause) {
        super(message, cause);
    }
}

