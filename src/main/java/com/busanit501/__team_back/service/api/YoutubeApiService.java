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
        return searchRecipes(foodName, null, "relevance");
    }

    /**
     * YouTube 레시피 검색 (정렬 옵션 포함, 키워드 없음)
     * @param foodName 음식 이름
     * @param orderBy 정렬 방식 ("viewCount": 조회순, "date": 최신순, "relevance": 관련도순)
     * @return YouTube 레시피 목록
     */
    public List<YoutubeRecipeDTO> searchRecipes(String foodName, String orderBy) {
        return searchRecipes(foodName, null, orderBy);
    }

    /**
     * YouTube 레시피 검색 (기본 메서드 - 내부 구현)
     */
    private List<YoutubeRecipeDTO> searchRecipesInternal(String foodName, String userKeyword, String orderBy) {
        log.info("Youtube 레시피 검색 시작: {}, 키워드: {}, 정렬: {}", foodName, userKeyword, orderBy);

        try {
            // 1. YouTube Search API 요청 객체 생성
            YouTube.Search.List search = youTube.search().list(java.util.Arrays.asList("id", "snippet"));

            // 2. 검색 파라미터 설정
            search.setKey(apiKey);
            
            // 검색 쿼리 생성
            String finalQuery;
            if (userKeyword != null && !userKeyword.trim().isEmpty()) {
                finalQuery = userKeyword.trim() + " " + foodName + " 레시피 만들기 -shorts";
            } else {
                finalQuery = foodName + " 레시피 -shorts";
            }
            search.setQ(finalQuery);
            search.setType(java.util.Arrays.asList("video")); // 비디오만 검색
            search.setVideoDuration("medium"); // 4분 이상 영상만 검색하여 Shorts 제외
            search.setMaxResults(MAX_RESULTS);
            search.setFields("items(id/videoId,snippet/title)"); // 필요한 정보만 요청
            
            // 정렬 방식 설정
            if (orderBy != null && !orderBy.isEmpty()) {
                search.setOrder(orderBy);
            } else {
                search.setOrder("relevance");
            }

            // 3. API 실행 및 응답 수신
            SearchListResponse searchResponse = search.execute();
            List<SearchResult> searchResultList = searchResponse.getItems();

            // 4. 검색 결과가 있는 경우, DTO 리스트로 변환 (쇼츠 제외)
            if (searchResultList != null && !searchResultList.isEmpty()) {
                return searchResultList.stream()
                        .map(item -> {
                            String videoId = item.getId().getVideoId();
                            String title = decodeHtmlEntities(item.getSnippet().getTitle()); // HTML 엔티티 디코딩
                            // YouTube URL 생성 (Flutter에서 바로 사용 가능하도록)
                            String url = "https://www.youtube.com/watch?v=" + videoId;
                            return YoutubeRecipeDTO.builder()
                                    .title(title)
                                    .videoId(videoId)
                                    .url(url)
                                    .build();
                        })
                        .filter(dto -> {
                            // 제목에 "shorts" 또는 "#shorts"가 포함된 영상 제외
                            String titleLower = dto.getTitle().toLowerCase();
                            return !titleLower.contains("shorts") && !titleLower.contains("#shorts");
                        })
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

    /**
     * YouTube 레시피 검색 (사용자 키워드 및 정렬 옵션 포함)
     * @param foodName AI가 인식한 음식 이름
     * @param userKeyword 사용자가 직접 입력한 키워드 (예: "김치")
     * @param orderBy 정렬 방식 ("viewCount": 조회순, "date": 최신순, "relevance": 관련도순)
     * @return YouTube 레시피 목록
     */
    public List<YoutubeRecipeDTO> searchRecipes(String foodName, String userKeyword, String orderBy) {
        return searchRecipesInternal(foodName, userKeyword, orderBy);
    }

    /**
     * 검색 쿼리 생성 (사용자 키워드 + 음식 이름)
     * 예: "김치" + "후라이드" -> "김치 후라이드"
     */
    private String buildSearchQuery(String foodName, String userKeyword) {
        if (userKeyword == null || userKeyword.trim().isEmpty()) {
            return foodName;
        }
        String keyword = userKeyword.trim();
        return keyword + " " + foodName; // "김치 후라이드"
    }

    /**
     * HTML 엔티티를 디코딩합니다.
     * 예: &quot; -> ", &amp; -> &, &lt; -> <, &gt; -> >, &#39; -> '
     */
    private String decodeHtmlEntities(String text) {
        if (text == null) {
            return null;
        }
        return text
                .replace("&quot;", "\"")
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&#39;", "'")
                .replace("&apos;", "'")
                .replace("&nbsp;", " ")
                .replace("&copy;", "©")
                .replace("&reg;", "®")
                .replace("&trade;", "™");
    }
}
