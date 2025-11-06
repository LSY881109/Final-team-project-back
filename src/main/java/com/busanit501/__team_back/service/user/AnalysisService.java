package com.busanit501.__team_back.service.user;

import com.busanit501.__team_back.dto.analysis.FoodAnalysisResultDTO;
import org.springframework.web.multipart.MultipartFile;

public interface AnalysisService {
    // 이미지 분석을 요청하고, 최종 결과(영양 정보, 유튜브 링크 등)를 반환하는 메소드
    FoodAnalysisResultDTO analyzeImage(Long userId, MultipartFile image);
    
    // YouTube 검색 옵션을 포함한 이미지 분석
    FoodAnalysisResultDTO analyzeImage(Long userId, MultipartFile image, String youtubeKeyword, String youtubeOrder);
}