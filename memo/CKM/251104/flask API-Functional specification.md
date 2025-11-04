### ** Flask 연동 준비 및 API 명세서 초안**

---

### **Flask AI 서버 역할 및 데이터베이스 연관 설명**

Flask AI 서버는 우리 프로젝트에서 **'데이터 처리'** 와 **'데이터 저장'** 에는 전혀 관여하지 않습니다. 오직 **'이미지 분석 및 예측'** 이라는 단 하나의 임무만 수행하는 전문적인 **컴퓨팅 엔진**입니다.

- **입력(Input)**: Spring Boot로부터 받은 **이미지 파일**
- **처리(Process)**: 학습된 AI 모델(EfficientNet 등)을 사용하여 이미지 분석
- **출력(Output)**: 분석 결과(**음식 이름**, **정확도**)를 담은 `JSON` 데이터

즉, Flask AI 서버는 데이터베이스에 직접 접속하거나 데이터를 저장하지 않으며, 상태가 없는(Stateless) 구조를 가집니다.

#### **전체 데이터 흐름에서 Flask의 위치와 DB 연관 관계**

아래 순서도는 사용자가 이미지 분석을 요청했을 때, Flask 서버가 어느 단계에서 작동하고, 그 결과가 어떻게 데이터베이스와 연관되는지를 명확하게 보여줍니다.

```
+---------------+      (1. 이미지 업로드)      +-------------------+      (2. 이미지 전달)      +--------------------+
|               | ------------------------> |                   | ---------------------> |                    |
| Flutter App   |                           | Spring Boot 서버  |                        |   Flask AI 서버    |
|               | <-----------------------  |                   | <--------------------- |                    |
+---------------+   (8. 최종 분석 결과 응답)    +--------+----------+      (3. 예측 결과 JSON 반환) +----------+----------+
                                                     |
                                                     |
            +----------------------------------------+----------------------------------------+
            |                                                                                 |
(4. 음식 이름으로 조회)                                                                      (6. 학습용 이미지 저장)
            |                                                                                 |
            v                                                                                 v
+------------------------+                                                           +--------------------------+
|                        |                                                           |                          |
|  MongoDB               |                                                           |  MongoDB                 |
| (food_references 컬렉션)|                                                           | (food_analysis_data 컬렉션)|
| - 영양 정보 조회         |                                                           | - 원본 이미지 저장         |
+------------------------+                                                           +--------------------------+
            |
(5. 외부 API 호출 - YouTube)
            |
            v
+------------------------+
|                        |                                 (7. 최종 결과 종합 및 저장)
|  YouTube Data API      | ----------------------------------------------------------------------> +--------------------------+
| - 레시피 영상 검색       |                                                                         |                          |
+------------------------+                                                                         |  MongoDB                 |
                                                                                                  | (analysis_histories 컬렉션)|
                                                                                                  | - 마이페이지용 기록 저장   |
                                                                                                  +--------------------------+

```

#### **단계별 상세 설명 (DB 연관 중심)**

1.  **[프론트엔드 → 백엔드]**: Flutter 앱이 음식 이미지를 Spring Boot 서버로 전송합니다.

2.  **[백엔드 → Flask]**: Spring Boot 서버는 받은 이미지 파일을 그대로 Flask AI 서버로 전달합니다. **(DB와 무관)**

3.  **[Flask → 백엔드]**: Flask AI 서버는 이미지를 분석하여 `{"foodName": "파스타", "accuracy": 0.85}` 와 같은 **JSON 데이터**를 Spring Boot 서버로 반환합니다. 이 `foodName`이 **모든 DB 작업의 시작점이 되는 핵심 키(Key)**가 됩니다.

4.  **[백엔드 ↔ MongoDB `food_references`]**:

    - Spring Boot 서버는 Flask로부터 받은 `foodName`("파스타")을 사용하여 `food_references` 컬렉션을 조회합니다.
    - `db.food_references.findOne({"foodName": "파스타"})` 와 같은 쿼리를 통해 해당 음식의 **영양 정보(칼로리, 단백질 등)**를 가져옵니다.

5.  **[백엔드 ↔ YouTube API]**:

    - Spring Boot 서버는 다시 한번 `foodName`("파스타")을 검색 키워드로 사용하여 YouTube Data API를 호출하고, 관련 레시피 영상 목록을 가져옵니다. **(DB와 무관)**

6.  **[백엔드 → MongoDB `food_analysis_data`]**:

    - Spring Boot 서버는 사용자가 업로드했던 **원본 이미지 파일**과, Flask가 알려준 `foodName`("파스타")을 함께 `food_analysis_data` 컬렉션에 저장합니다.
    - 이는 나중에 AI 모델의 성능을 개선하기 위한 **학습 데이터를 축적**하는 과정입니다.

7.  **[백엔드 → MongoDB `analysis_histories`]**:

    - Spring Boot 서버는 지금까지 수집한 모든 정보(사용자 ID, 썸네일 이미지, AI 분석 결과, 4번에서 얻은 영양 정보, 5번에서 얻은 유튜브 링크)를 하나의 문서(Document)로 종합하여 `analysis_histories` 컬렉션에 저장합니다.
    - 이 데이터가 바로 **사용자의 마이페이지에 표시될 최종 기록**이 됩니다.

8.  **[백엔드 → 프론트엔드]**: 7번에서 종합한 데이터 중 프론트엔드에 필요한 부분만 DTO로 가공하여 최종 응답으로 전달합니다.

결론적으로, **Flask AI 서버는 데이터베이스 구조에 대해 전혀 알 필요가 없으며, 오직 이미지 분석이라는 순수 계산 역할만 수행합니다.**
그리고 Flask가 반환한 **예측 결과(`foodName`)**를 가지고 Spring Boot 서버가 주도적으로 각 데이터베이스 컬렉션과 상호작용하여 최종 결과물을 만들어내는 구조입니다.

---

#### **[STEP 1] `AnalysisService`에서 Flask 호출 로직 구체화**

`OkHttp`를 사용하여 실제 Flask 서버와 통신하는 로직을 담당할 별도의 서비스(`AIAnalysisService`)를 만듭니다. 이는 역할 분리(SoC) 원칙에 따라 코드를 깔끔하게 유지하기 위함입니다.

- **파일**: `AIAnalysisService.java` (인터페이스 생성)
  - **경로**: `com/busanit501/__team_back/service/ai/`

```java
package com.busanit501.__team_back.service.ai;

import com.busanit501.__team_back.dto.ai.AiResponse;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface AIAnalysisService {
    AiResponse analyzeImage(MultipartFile imageFile) throws IOException;
}
```

- **파일**: `AIAnalysisServiceImpl.java` (구현체 생성)

```java
package com.busanit501.__team_back.service.ai;

import com.busanit501.__team_back.dto.ai.AiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class AIAnalysisServiceImpl implements AIAnalysisService {

    private final OkHttpClient okHttpClient; // OkHttpConfig에서 등록한 Bean 주입
    private final ObjectMapper objectMapper;

    @Value("${flask.api.url}") // application.properties에서 Flask 서버 주소 가져오기
    private String flaskApiUrl;

    @Override
    public AiResponse analyzeImage(MultipartFile imageFile) throws IOException {
        // 1. 요청 바디(Body) 생성: Multipart 형식
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                        "image", // Flask에서 받을 파일의 key 이름
                        imageFile.getOriginalFilename(),
                        RequestBody.create(imageFile.getBytes(), MediaType.parse(imageFile.getContentType()))
                )
                .build();

        // 2. HTTP 요청(Request) 생성
        Request request = new Request.Builder()
                .url(flaskApiUrl + "/analyze") // Flask API 엔드포인트
                .post(requestBody)
                .build();

        // 3. 요청 실행 및 응답 수신
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Flask 서버 응답 실패: " + response);
            }
            // 4. 응답받은 JSON 문자열을 DTO 객체로 변환하여 반환
            String responseBody = response.body().string();
            return objectMapper.readValue(responseBody, AiResponse.class);
        }
    }
}
```

- **파일**: `AiResponse.java` (DTO 생성)
  - **경로**: `com/busanit501/__team_back/dto/ai/`

```java
package com.busanit501.__team_back.dto.ai;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiResponse {
    private String foodName;
    private double accuracy;
}
```

- **파일**: `AnalysisServiceImpl.java` (수정)
  - `AIAnalysisService`를 주입받아 사용하도록 수정합니다.

```java
// ...
@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class AnalysisServiceImpl implements AnalysisService {

    private final AIAnalysisService aiAnalysisService; // AIAnalysisService 주입
    // ... (다른 Repository들)

    @Override
    public String analyzeImage(Long userId, MultipartFile image) {
        log.info("AnalysisService - analyzeImage 실행...");

        try {
            // [STEP 1] Flask AI 서버로 이미지 전송 및 결과 수신 (실제 호출로 변경)
            AiResponse aiResult = aiAnalysisService.analyzeImage(image);
            String foodName = aiResult.getFoodName();
            double accuracy = aiResult.getAccuracy();
            log.info("AI 분석 결과: " + foodName + " (" + accuracy * 100 + "%)");

            // ... (STEP 2 ~ 6 로직은 이전과 동일, 현재는 임시 로그 상태)

            return "분석 완료: " + foodName;
        } catch (IOException e) {
            log.error("Flask 서버 통신 중 오류 발생", e);
            throw new RuntimeException("AI 서버와 통신하는 중 오류가 발생했습니다.");
        }
    }
}
```

---

### **[STEP 2] Flask 팀원에게 전달할 API 기능 명세서**

이제 위에서 만든 `AIAnalysisServiceImpl` 코드를 기반으로, Flask 개발을 담당하는 팀원에게 아래와 같은 명세서를 전달하면 됩니다.

---

### **Flask AI 서버 API 기능 명세서 (v1.0)**

**1. 개요**

- Spring Boot 백엔드 서버로부터 음식 이미지를 받아, 학습된 AI 모델을 통해 해당 음식의 이름과 예측 정확도를 반환하는 API입니다.

**2. 엔드포인트 (Endpoint)**

- **URL**: `http://{FLASK_SERVER_IP}:{PORT}/analyze`
  - 로컬 개발 환경 기준: `http://localhost:5000/analyze`
- **HTTP Method**: `POST`

**3. 요청 (Request)**

- **Content-Type**: `multipart/form-data`
- **Body (form-data)**:
  | Key | Type | Description | Required |
  | :--- | :--- | :--- | :--- |
  | `image`| File | 분석할 음식 이미지 파일 (e.g., `.jpg`, `.png`). | **Yes** |

**4. 응답 (Response)**

- **Content-Type**: `application/json`

- **성공 시 (Status Code: `200 OK`)**:

  - **Body**:

  ```json
  {
    "foodName": "파스타",
    "accuracy": 0.85
  }
  ```

  | Key        | Type   | Description                                       |
  | :--------- | :----- | :------------------------------------------------ |
  | `foodName` | String | AI 모델이 예측한 음식의 이름.                     |
  | `accuracy` | Double | 예측 결과의 신뢰도(정확도). (0.0 ~ 1.0 사이의 값) |

- **실패 시 (Status Code: `400 Bad Request` 또는 `500 Internal Server Error`)**:
  - **Body**:
  ```json
  {
    "error": "이미지 파일을 찾을 수 없습니다."
  }
  ```
  | Key     | Type   | Description                 |
  | :------ | :----- | :-------------------------- |
  | `error` | String | 오류 발생 원인에 대한 설명. |

---
