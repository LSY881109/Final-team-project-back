## **사전 준비사항, 구체적인 백엔드 구현 계획, YouTube API 연동 상세 정보, 그리고 최종 통합 지점**

### **YouTube API 연동 개발 가이드 (v1.0)**

**1. 개요 및 목표**

- **목표**: AI 서버가 분석한 음식 이름(예: "파스타")을 검색 키워드로 사용하여, YouTube에서 관련 요리법(레시피) 영상을 검색하고, 유용한 정보(영상 제목, URL, 썸네일 이미지 주소)를 추출하는 기능을 개발합니다.
- **역할**: 이 기능은 백엔드(Spring Boot) 내에서 별도의 `YoutubeService`로 구현되며, `AnalysisService`가 이 서비스를 호출하여 최종 분석 결과에 레시피 정보를 통합하게 됩니다.

**2. 사전 준비사항 (가장 먼저 해야 할 일)**
YouTube Data API를 사용하려면 Google Cloud Platform(GCP)에서 **API 키**를 발급받아야 합니다.

1.  **Google Cloud Platform(GCP) 접속**: [https://console.cloud.google.com/](https://console.cloud.google.com/)
2.  **새 프로젝트 생성**: (기존 프로젝트가 없다면) `Final-team-project`와 같은 이름으로 새 프로젝트를 생성합니다.
3.  **API 및 서비스 활성화**:
    - 좌측 메뉴에서 `API 및 서비스 > 라이브러리`로 이동합니다.
    - `YouTube Data API v3`를 검색하여 **'사용 설정'** 버튼을 클릭합니다.
4.  **사용자 인증 정보 만들기**:
    - `API 및 서비스 > 사용자 인증 정보`로 이동합니다.
    - 상단의 `+ 사용자 인증 정보 만들기 > API 키`를 클릭하여 새로운 API 키를 생성합니다.
5.  **API 키 저장**:
    - 생성된 API 키 문자열을 복사하여 백엔드 프로젝트의 `src/main/resources/application.properties` 파일에 안전하게 저장합니다.
    ```properties
    # YouTube Data API Key
    youtube.api.key=여기에_발급받은_API_키를_붙여넣으세요
    ```

**3. 백엔드 구현 계획**

#### **[STEP 1] `build.gradle` 의존성 추가**

Google API 클라이언트 라이브러리를 추가하여 YouTube API를 쉽게 호출할 수 있도록 준비합니다.

```groovy
// build.gradle
dependencies {
    // ... 다른 의존성들

    // Google API Client for Java
    implementation 'com.google.api-client:google-api-client:2.2.0'
    implementation 'com.google.apis:google-api-services-youtube:v3-rev222-1.25.0'
}
```

> **참고**: 버전은 최신 안정 버전으로 변경될 수 있습니다.

#### **[STEP 2] DTO(Data Transfer Object) 생성**

YouTube 검색 결과를 담을 DTO 클래스를 먼저 정의합니다.

- **경로**: `com/busanit501/__team_back/dto/youtube/`
- **파일**: `YoutubeRecipeDto.java`

```java
package com.busanit501.__team_back.dto.youtube;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class YoutubeRecipeDto {
    private String title;
    private String url;
    private String thumbnailUrl;
}
```

#### **[STEP 3] `YoutubeService` 생성**

YouTube API 연동 로직을 전담할 서비스 인터페이스와 구현 클래스를 생성합니다.

- **파일**: `YoutubeService.java` (인터페이스)
  - **경로**: `com/busanit501/__team_back/service/youtube/`

```java
package com.busanit501.__team_back.service.youtube;

import com.busanit501.__team_back.dto.youtube.YoutubeRecipeDto;
import java.io.IOException;
import java.util.List;

public interface YoutubeService {
    List<YoutubeRecipeDto> searchRecipes(String foodName) throws IOException;
}
```

- **파일**: `YoutubeServiceImpl.java` (구현체)

```java
package com.busanit501.__team_back.service.youtube;

import com.busanit501.__team_back.dto.youtube.YoutubeRecipeDto;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
public class YoutubeServiceImpl implements YoutubeService {

    @Value("${youtube.api.key}")
    private String apiKey;

    private final YouTube youtube;

    public YoutubeServiceImpl() {
        this.youtube = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(), request -> {})
                .setApplicationName("food-analysis-app")
                .build();
    }

    @Override
    public List<YoutubeRecipeDto> searchRecipes(String foodName) throws IOException {
        log.info("YouTube 레시피 검색 시작: " + foodName);

        // 1. YouTube Search API 요청 객체 생성
        YouTube.Search.List searchRequest = youtube.search().list("snippet");

        // 2. 검색 파라미터 설정
        searchRequest.setKey(apiKey);
        searchRequest.setQ(foodName + " 레시피"); // 검색어 설정 (예: "파스타 레시피")
        searchRequest.setType("video"); // 검색 유형을 비디오로 한정
        searchRequest.setMaxResults(3L); // 최대 3개의 결과만 가져오도록 설정

        // 3. API 요청 실행
        SearchListResponse searchResponse = searchRequest.execute();

        // 4. 검색 결과(SearchResult) 목록을 DTO 목록으로 변환
        List<SearchResult> searchResultList = searchResponse.getItems();
        return searchResultList.stream()
                .map(item -> YoutubeRecipeDto.builder()
                        .title(item.getSnippet().getTitle())
                        .url("https://www.youtube.com/watch?v=" + item.getId().getVideoId())
                        .thumbnailUrl(item.getSnippet().getThumbnails().getHigh().getUrl())
                        .build())
                .collect(Collectors.toList());
    }
}
```

#### **[STEP 4] `AnalysisService`에 통합**

`AnalysisServiceImpl`이 `YoutubeService`를 호출하여 최종 결과에 레시피 정보를 포함하도록 수정합니다.

- **파일**: `AnalysisServiceImpl.java` (수정)

```java
// ...
@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class AnalysisServiceImpl implements AnalysisService {

    private final AIAnalysisService aiAnalysisService;
    private final YoutubeService youtubeService; // YoutubeService 주입
    // ... (다른 Repository들)

    @Override
    public String analyzeImage(Long userId, MultipartFile image) {
        log.info("AnalysisService - analyzeImage 실행...");

        try {
            // ... (STEP 1: Flask AI 서버 호출)
            AiResponse aiResult = aiAnalysisService.analyzeImage(image);
            String foodName = aiResult.getFoodName();

            // ... (STEP 2: FoodReference DB에서 영양 정보 조회)

            // [STEP 3] YouTube API 검색
            List<YoutubeRecipeDto> recipes = youtubeService.searchRecipes(foodName);
            log.info(foodName + " 관련 유튜브 레시피 " + recipes.size() + "개 검색 완료");

            // ... (STEP 4 & 5: 결과 종합 및 AnalysisHistory DB에 저장)
            // AnalysisHistory 객체를 생성할 때, 위에서 받은 recipes 목록을 함께 저장합니다.

            return "분석 완료: " + foodName;
        } catch (IOException e) {
            // ...
        }
    }
}
```

이 가이드를 통해 YouTube 연동 담당 팀원은 필요한 설정부터 실제 코드 구현, 그리고 기존 시스템과의 통합까지의 전체 과정을 명확하게 파악하고 작업을 시작할 수 있을 것입니다.
