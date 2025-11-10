package com.busanit501.__team_back.controller;

import com.busanit501.__team_back.dto.analysis.YoutubeRecipeDTO;
import com.busanit501.__team_back.service.api.YoutubeApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/youtube")
@RequiredArgsConstructor
@Log4j2
@CrossOrigin(origins = "*") // CORS 허용 (개발 환경)
public class YouTubeSearchController {

    private final YoutubeApiService youtubeApiService;

    /**
     * YouTube 레시피 검색 API
     * @param foodName 음식 이름 (필수)
     * @param keyword 검색 키워드 (선택)
     * @param order 정렬 방식: relevance, viewCount, date (선택, 기본값: relevance)
     * @return YouTube 레시피 목록
     */
    @GetMapping("/search")
    public ResponseEntity<List<YoutubeRecipeDTO>> searchYouTube(
            @RequestParam("foodName") String foodName,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "order", required = false, defaultValue = "relevance") String order) {
        
        log.info("YouTube 검색 요청 - 음식: {}, 키워드: {}, 정렬: {}", foodName, keyword, order);
        
        try {
            List<YoutubeRecipeDTO> results;
            if (keyword != null && !keyword.trim().isEmpty()) {
                results = youtubeApiService.searchRecipes(foodName, keyword, order);
            } else {
                // 키워드가 없으면 정렬 옵션만 적용하여 검색
                results = youtubeApiService.searchRecipes(foodName, order);
            }
            
            log.info("YouTube 검색 완료 - 결과 개수: {}", results.size());
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("YouTube 검색 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

