package com.busanit501.__team_back.service.user;

import org.springframework.web.multipart.MultipartFile;

public interface AnalysisService {
    // 이미지 분석을 요청하고, 최종 결과(영양 정보, 유튜브 링크 등)를 반환하는 메소드
    // 반환 타입은 나중에 만들 AnalysisResponseDto가 될 것입니다.
    // 우선은 String으로 임시 정의
    String analyzeImage(Long userId, MultipartFile image);
}