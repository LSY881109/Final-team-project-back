### **유영준님 담당 파트 상세 기획안 (설계 구체화)**

(상세 기획 내용은 위와 동일)

---

## **작업 순서 체크리스트**

### **1단계: `FoodReference` 모델 및 리포지토리 구현 (MongoDB)**
- [x] `FoodReference.java` 도메인 모델 생성
- [x] `FoodReferenceRepository.java` 리포지토리 인터페이스 생성
  - `MongoRepository`를 상속받고, `findByFoodName` 메서드를 정의하세요.
  - 추천 위치: `src/main/java/com/busanit501/__team_back/repository/mongo/FoodReferenceRepository.java`

### **2단계: `YoutubeApiService` 구현 및 의존성 설정**
- [x] `build.gradle` 파일에 의존성 추가 (팀원과 논의)
  - **YouTube Data API**: `com.google.apis:google-api-services-youtube` 추가. (기반 라이브러리인 `com.google.api-client`도 명시적으로 추가하는 것을 권장)
  - **ModelMapper**: Entity와 DTO 간의 자동 변환을 위해 `org.modelmapper:modelmapper` 추가.
- [x] `application.properties` 파일에 `youtube.api.key` 추가
- [x] `YoutubeApiService.java` 서비스 클래스 생성
  - `searchRecipes(String foodName)` 메서드를 구현하여 YouTube 영상을 검색하고 `YoutubeRecipeDTO` 리스트를 반환하도록 작성하세요.
  - 추천 위치: `src/main/java/com/busanit501/__team_back/service/api/YoutubeApiService.java`
- [x] `YoutubeApiException` 커스텀 예외 클래스 생성 및 예외 처리 구현

### **3단계: DTO 설계**
- [x] `YoutubeRecipeDTO.java` 생성
  - `title`, `videoId` 필드를 포함하세요.
  - 추천 위치: `src/main/java/com/busanit501/__team_back/dto/analysis/YoutubeRecipeDTO.java`
- [x] `FoodAnalysisResultDTO.java` 확장 또는 수정 (담당 팀원과 협업)
  - `List<YoutubeRecipeDTO>` 필드를 추가하여 유튜브 검색 결과를 담도록 협의.
  - 상세 영양 정보 객체인 `NutritionData` 필드 추가 협의.

### **4단계: 서비스 통합**
- [x] `AnalysisServiceImpl` (상위 서비스)에서 최종 DTO 조립 로직 구현 (담당 팀원과 협업)
  - `FoodReferenceRepository`와 `YoutubeApiService`를 호출하여 각각 `FoodReference` Entity와 `List<YoutubeRecipeDTO>`를 가져옵니다.
  - `AnalysisServiceImpl` 내에서 `ModelMapper`를 사용하여 `FoodReference` Entity를 `NutritionData` DTO로 변환하는 로직을 구현합니다.
  - 조회 및 변환된 모든 데이터를 최종 `FoodAnalysisResultDTO`에 담아 반환하도록 조립합니다.

#### **예외 처리 및 경계 조건 규칙 (팀 합의 사항)**
- **`FoodReferenceRepository` 결과 없음**: `findByFoodName` 조회 시 결과가 없으면 `Optional.empty()`를 반환합니다. `AIAnalysisService`는 `isPresent()`로 확인 후 처리합니다.
- **`YoutubeApiService` 검색 결과 없음**: `searchRecipes` 조회 시 결과가 없으면 `null` 대신 빈 리스트(`Collections.emptyList()`)를 반환합니다.
- **`YoutubeApiService` API 오류**: API 키 만료, 네트워크 문제 등 심각한 오류 발생 시, 원인 예외를 포함한 커스텀 `RuntimeException`(예: `YoutubeApiException`)을 던져서 처리 실패를 알립니다.