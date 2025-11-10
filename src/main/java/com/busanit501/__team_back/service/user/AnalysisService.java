package com.busanit501.__team_back.service.user;

import com.busanit501.__team_back.dto.analysis.AnalysisHistoryDTO;
import com.busanit501.__team_back.dto.analysis.FoodAnalysisResultDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AnalysisService {
    // 이미지 분석을 요청하고, 최종 결과(영양 정보, 유튜브 링크 등)를 반환하는 메소드
    FoodAnalysisResultDTO analyzeImage(Long userId, MultipartFile image);
    
    // YouTube 검색 옵션을 포함한 이미지 분석
    FoodAnalysisResultDTO analyzeImage(Long userId, MultipartFile image, String youtubeKeyword, String youtubeOrder);
    
    // 분석 히스토리 조회
    List<AnalysisHistoryDTO> getAnalysisHistory(Long userId, int page, int size);
    
    // YouTube 레시피 클릭 시 저장
    void saveClickedYouTubeRecipe(Long userId, String historyId, String title, String url);
}