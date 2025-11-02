### **유영준님 담당 파트 상세 기획안**

(상세 기획 내용은 위와 동일)

---

## **작업 순서 체크리스트**

### **1단계: `FoodReference` 모델 및 리포지토리 구현 (MongoDB)**
- [x] `FoodReference.java` 도메인 모델 생성
- [x] `FoodReferenceRepository.java` 리포지토리 인터페이스 생성
  - `MongoRepository`를 상속받고, `findByFoodName` 메서드를 정의하세요.
  - 추천 위치: `src/main/java/com/busanit501/__team_back/repository/mongo/FoodReferenceRepository.java`

### **2단계: `YoutubeApiService` 구현**
- [ ] `build.gradle` 파일에 YouTube Data API 의존성 추가 (팀원과 논의)
- [ ] `application.properties` 파일에 `youtube.api.key` 추가
- [x] `YoutubeApiService.java` 서비스 클래스 생성
  - `searchRecipes(String foodName)` 메서드를 구현하여 YouTube 영상을 검색하고 `YoutubeRecipeDTO` 리스트를 반환하도록 작성하세요.
  - 추천 위치: `src/main/java/com/busanit501/__team_back/service/api/YoutubeApiService.java`

### **3단계: DTO 설계**
- [x] `YoutubeRecipeDTO.java` 생성
  - 영상 제목, 썸네일 URL, 영상 ID 필드를 포함하세요.
  - 추천 위치: `src/main/java/com/busanit501/__team_back/dto/analysis/YoutubeRecipeDTO.java`
- [ ] `FoodAnalysisResultDTO.java` 확장 또는 수정 (담당 팀원과 협업)
  - `NutritionData`와 `List<YoutubeRecipeDTO>` 필드를 추가하는 것을 협의하세요.

### **4단계: 서비스 통합**
- [ ] `AIAnalysisService` (또는 상위 서비스)에서 `FoodReferenceRepository`와 `YoutubeApiService`를 호출하여 최종 DTO를 조립하는 로직 구현 (담당 팀원과 협업)