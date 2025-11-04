package com.busanit501.__team_back.service.api;

import com.busanit501.__team_back.dto.analysis.YoutubeRecipeDTO;
import com.busanit501.__team_back.exception.YoutubeApiException;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class YoutubeApiService {

    private final YouTube youTube; // YouTubeConfig에서 생성된 Bean 주입

    @Value("${youtube.api.key}")
    private String apiKey;

    private static final long MAX_RESULTS = 5; // 검색 결과의 최대 개수

    public List<YoutubeRecipeDTO> searchRecipes(String foodName) {
        log.info("Youtube 레시피 검색 시작: {}", foodName);

        try {
            // 1. YouTube Search API 요청 객체 생성
            YouTube.Search.List search = youTube.search().list(java.util.Arrays.asList("id", "snippet"));

            // 2. 검색 파라미터 설정
            search.setKey(apiKey);
            search.setQ(foodName + " 레시피"); // 검색어에 '레시피'를 추가하여 정확도 향상
            search.setType(java.util.Arrays.asList("video")); // 비디오만 검색
            search.setMaxResults(MAX_RESULTS);
            search.setFields("items(id/videoId,snippet/title)"); // 필요한 정보만 요청

            // 3. API 실행 및 응답 수신
            SearchListResponse searchResponse = search.execute();
            List<SearchResult> searchResultList = searchResponse.getItems();

            // 4. 검색 결과가 있는 경우, DTO 리스트로 변환
            if (searchResultList != null && !searchResultList.isEmpty()) {
                return searchResultList.stream()
                        .map(item -> YoutubeRecipeDTO.builder()
                                .title(item.getSnippet().getTitle())
                                .videoId(item.getId().getVideoId())
                                .build())
                        .collect(Collectors.toList());
            }
        } catch (IOException e) {
            log.error("YouTube API 호출 중 오류 발생", e);
            log.error("오류 상세: {}", e.getMessage());
            // YYJ.md 요구사항: API 키 만료, 네트워크 문제 등 심각한 오류 발생 시, 
            // 원인 예외를 포함한 커스텀 RuntimeException을 던져서 처리 실패를 알립니다.
            throw new YoutubeApiException("YouTube API 호출 실패: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("YouTube API 호출 중 예상치 못한 오류 발생", e);
            throw new YoutubeApiException("YouTube API 호출 중 예상치 못한 오류: " + e.getMessage(), e);
        }

        // 검색 결과가 없는 경우 빈 리스트 반환
        log.warn("YouTube 검색 결과가 없습니다. 음식 이름: {}", foodName);
        return Collections.emptyList();
    }
}
