### **유영준님 담당 파트 상세 기획안**

#### **1단계: `FoodReference` 모델 및 리포지토리 구현 (MongoDB)**

`FoodReference`는 AI가 분석한 음식 이름에 해당하는 영양 정보(칼로리, 영양소 등)를 저장하는 정적 데이터베이스입니다.

1.  **`FoodReference.java` (도메인 모델) 생성**
    *   위치: `src/main/java/com/busanit501/__team_back/domain/mongo/` (가상 경로, `domain` 패키지 내에 `mongo` 패키지를 만들어 관리하는 것을 추천)
    *   `@Document(collection = "food_references")` 어노테이션을 사용하여 MongoDB 컬렉션과 매핑합니다.
    *   **필드 정의:**
        *   `@Id private String id;`
        *   `private String foodName;` (음식 이름, 예: "김치찌개")
        *   `private NutritionData nutritionData;` (영양 정보를 담는 중첩 객체)

2.  **`NutritionData.java` (중첩 객체) 생성**
    *   `FoodReference` 내에서 영양 정보만 그룹화하기 위한 클래스입니다.
    *   **필드 정의:**
        *   `private double calories;` (칼로리)
        *   `private double protein;` (단백질)
        *   `private double fat;` (지방)
        *   `private double carbohydrates;` (탄수화물)

3.  **`FoodReferenceRepository.java` (리포지토리) 생성**
    *   위치: `src/main/java/com/busanit501/__team_back/repository/mongo/` (가상 경로)
    *   `MongoRepository<FoodReference, String>` 인터페이스를 상속받습니다.
    *   **메서드 정의:**
        *   `Optional<FoodReference> findByFoodName(String foodName);`
            *   음식 이름으로 영양 정보를 조회하는 기능을 추가합니다.

#### **2단계: `YoutubeApiService.java` 구현**

음식 이름을 기반으로 YouTube에서 관련 레시피 영상을 검색하는 서비스입니다.

1.  **`build.gradle` 의존성 추가**
    *   YouTube Data API를 사용하기 위한 Google API 클라이언트 라이브러리를 추가해야 합니다. (팀원과 논의하여 추가)

2.  **`application.properties` 설정**
    *   `youtube.api.key=YOUR_API_KEY` 형식으로 발급받은 API 키를 추가합니다. **(키를 코드에 직접 하드코딩하지 않도록 주의)**

3.  **`YoutubeApiService.java` (서비스) 생성**
    *   위치: `src/main/java/com/busanit501/__team_back/service/api/` (가상 경로)
    *   `@Service` 어노테이션을 추가합니다.
    *   **핵심 메서드 정의:** `public List<YoutubeRecipeDTO> searchRecipes(String foodName)`
        *   `foodName + " 레시피"` 또는 `"how to make " + foodName` 과 같은 검색어로 YouTube API에 검색을 요청합니다.
        *   API 응답(JSON)을 파싱하여 영상 제목, 썸네일 URL, 영상 ID 등의 정보를 추출합니다.
        *   추출한 정보를 `YoutubeRecipeDTO` 객체 리스트로 변환하여 반환합니다.

#### **3단계: `dto/analysis` 패키지 내 DTO 설계**

API 응답에 사용될 데이터 전송 객체(DTO)를 설계합니다.

1.  **`YoutubeRecipeDTO.java` 생성**
    *   YouTube 검색 결과를 담을 DTO입니다.
    *   **필드 정의:**
        *   `private String title;` (영상 제목)
        *   `private String thumbnailUrl;` (썸네일 이미지 URL)
        *   `private String videoId;` (영상 ID)

2.  **`FoodAnalysisResultDTO.java` 확장 또는 수정**
    *   기존 DTO에 영양 정보와 YouTube 영상 목록을 추가해야 합니다. (담당 팀원과 협업)
    *   **추가 필드 제안:**
        *   `private NutritionData nutritionData;` (`FoodReference`에서 조회한 영양 정보)
        *   `private List<YoutubeRecipeDTO> youtubeRecipes;` (`YoutubeApiService`가 반환한 영상 목록)

#### **4단계: 서비스 통합 및 흐름**

작성하신 모듈들이 전체 시스템에서 어떻게 동작하는지에 대한 흐름입니다.

1.  `AnalysisController`가 이미지와 함께 요청을 받으면 `AIAnalysisService`를 호출합니다.
2.  `AIAnalysisService`는 AI 모델을 통해 음식 이름(`foodName`)을 얻습니다.
3.  `AIAnalysisService`는 **`foodName`**을 사용하여 다음 두 서비스를 호출합니다.
    *   `FoodReferenceRepository.findByFoodName(foodName)`를 호출하여 영양 정보를 가져옵니다.
    *   `YoutubeApiService.searchRecipes(foodName)`를 호출하여 레시피 영상 목록을 가져옵니다.
4.  `AIAnalysisService`는 AI 분석 결과, 영양 정보, YouTube 영상 목록을 모두 취합하여 최종 `FoodAnalysisResultDTO`를 완성한 후 `AnalysisController`에 반환합니다.
