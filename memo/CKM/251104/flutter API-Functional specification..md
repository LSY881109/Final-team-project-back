---

### **프론트엔드(Flutter) 개발용 API 기능 명세서 (v1.0)**

**1. 개요**
*   본 문서는 Flutter 앱과 Spring Boot 백엔드 서버 간의 통신을 위한 API 규칙을 정의합니다. 모든 요청의 기본 URL은 `http://{BACKEND_SERVER_IP}:{PORT}` 입니다. (로컬 개발 환경 기준: `http://localhost:8080`)

**2. 공통 사항**
*   **인증**: 로그인이 필요한 모든 API는 요청 헤더(Header)에 JWT Access Token을 포함하여 보내야 합니다.
    *   **Header Key**: `Authorization`
    *   **Header Value**: `Bearer {발급받은 Access Token}`
*   **데이터 형식**: 요청 및 응답 본문(Body)의 기본 데이터 형식은 `JSON` 입니다. (파일 업로드 시 `multipart/form-data` 사용)

---

### **API 상세 명세**

#### **[ 1 ] 회원가입**

- **설명**: 사용자의 아이디, 비밀번호, 이메일, 프로필 이미지를 받아 회원 계정을 생성합니다.
- **Endpoint**: `POST /api/users/signup`
- **인증**: **필요 없음**
- **요청 (Request)**: `multipart/form-data`
  | Key | Type | Description | Required |
  | :--- | :--- | :--- | :--- |
  | `signupData`| JSON (Text) | 사용자의 기본 정보. 아래 JSON 구조를 문자열로 전달. | **Yes** |
  | `profileImage`| File | 사용자의 프로필 이미지 파일. | No |

  **`signupData` JSON 구조:**

  ```json
  {
    "userId": "testuser",
    "password": "password123",
    "passwordConfirm": "password123",
    "email": "test@example.com"
  }
  ```

- **응답 (Response)**:
  - **성공 (200 OK)**: `Content-Type: text/plain`
    ```
    회원가입이 성공적으로 완료되었습니다.
    ```
  - **실패 (400 Bad Request)**: `Content-Type: text/plain` (오류 원인 메시지)
    ```
    이미 사용 중인 아이디입니다.
    ```
    ```
    비밀번호가 일치하지 않습니다.
    ```

---

#### **[ 2 ] 로그인**

- **설명**: 사용자의 아이디와 비밀번호로 인증을 수행하고, 성공 시 API 접근을 위한 JWT(Access/Refresh Token)를 발급합니다.
- **Endpoint**: `POST /api/users/login`
- **인증**: **필요 없음**
- **요청 (Request)**: `application/json`
  ```json
  {
    "userId": "testuser",
    "password": "password123"
  }
  ```
- **응답 (Response)**:
  - **성공 (200 OK)**: `application/json` (발급된 토큰 정보를 반환)
    ```json
    {
      "grantType": "Bearer",
      "accessToken": "eyJhbGciOiJI...",
      "accessTokenExpiresIn": 3600000,
      "refreshToken": "eyJhbGciOiJI..."
    }
    ```
    > **프론트엔드 처리**: `accessToken`은 이후 모든 API 요청 헤더에 사용하고, `refreshToken`은 안전한 곳(보안 스토리지)에 저장하여 Access Token 만료 시 재발급 요청에 사용합니다.

---

#### **[ 3 ] 음식 이미지 분석**

- **설명**: 로그인한 사용자가 음식 이미지를 업로드하면, AI 분석 및 외부 API 조회를 거쳐 최종 분석 결과를 반환합니다.
- **Endpoint**: `POST /api/analysis`
- **인증**: **필수** (`Authorization: Bearer {accessToken}`)
- **요청 (Request)**: `multipart/form-data`
  | Key | Type | Description | Required |
  | :--- | :--- | :--- | :--- |
  | `image`| File | 분석할 음식 이미지 파일. | **Yes** |
  | `userId`| Text | **(임시)** 현재는 테스트를 위해 로그인한 사용자의 숫자 ID를 함께 보냅니다. **추후 JWT 인증이 완성되면 이 파라미터는 삭제될 예정입니다.** | **Yes** |

- **응답 (Response)**:
  - **성공 (200 OK)**: `application/json` (최종 분석 결과 DTO)
    > **TODO**: 현재는 `String`으로 응답하지만, 최종적으로 아래와 같은 JSON 구조로 응답할 예정입니다.
    ```json
    {
      "recognizedFoodName": "파스타",
      "accuracy": 0.85,
      "nutritionInfo": {
        "calories": 650.5,
        "carbohydrate": 80.2,
        "protein": 25.1,
        "fat": 20.5
      },
      "youtubeRecipes": [
        {
          "title": "초간단 토마토 파스타 만들기",
          "url": "https://www.youtube.com/watch?v=...",
          "thumbnailUrl": "https://i.ytimg.com/vi/.../hqdefault.jpg"
        },
        {
          "title": "백종원표 크림 파스타 레시피",
          "url": "https://www.youtube.com/watch?v=...",
          "thumbnailUrl": "https://i.ytimg.com/vi/.../hqdefault.jpg"
        }
      ]
    }
    ```

---

#### **[ 4 ] 마이페이지 (내 분석 기록 조회)**

- **설명**: 로그인한 사용자의 과거 음식 분석 기록을 최신순으로 조회합니다. (페이징 기능 포함)
- **Endpoint**: `GET /api/mypage`
- **인증**: **필수** (`Authorization: Bearer {accessToken}`)
- **요청 (Request)**: Query Parameters
  | Key | Type | Description | Default |
  | :--- | :--- | :--- | :--- |
  | `page` | Integer | 조회할 페이지 번호 (0부터 시작). | 0 |
  | `size` | Integer | 한 페이지에 보여줄 데이터 개수. | 10 |
  > 예시: `/api/mypage?page=0&size=10` -> 첫 번째 페이지의 10개 기록 조회
- **응답 (Response)**:
  - **성공 (200 OK)**: `application/json` (페이징된 분석 기록 목록)
    > **TODO**: `AnalysisHistory` 정보를 가공한 DTO 목록을 아래와 같은 구조로 반환할 예정입니다.
    ```json
    {
      "content": [
        {
          "historyId": "mongo_object_id_1",
          "thumbnailImageUrl": "/api/images/mongo_object_id_1", // 썸네일 이미지를 받아올 수 있는 API 주소
          "recognizedFoodName": "파스타",
          "analysisDate": "2025-11-03T18:00:00"
        },
        {
          "historyId": "mongo_object_id_2",
          "thumbnailImageUrl": "/api/images/mongo_object_id_2",
          "recognizedFoodName": "양념치킨",
          "analysisDate": "2025-11-02T12:30:00"
        }
      ],
      "pageable": { ... }, // 페이징 관련 정보
      "totalPages": 5,
      "totalElements": 48,
      "last": false,
      "first": true,
      "numberOfElements": 10,
      "size": 10,
      "number": 0,
      "empty": false
    }
    ```

---

### **데이터베이스 구조 설명 (프론트엔드 연관)**

앱 화면에 표시될 데이터가 어떤 DB의 어떤 필드에서 오는지 이해를 돕기 위한 설명입니다.

#### **MariaDB (사용자 정보)**

| 테이블    | 필드명           | 설명                                                                            |
| :-------- | :--------------- | :------------------------------------------------------------------------------ |
| **users** | `id`             | 시스템 내부에서 사용하는 사용자의 고유 번호 (PK).                               |
|           | `userId`         | 사용자가 로그인/회원가입 시 사용하는 아이디.                                    |
|           | `email`          | 사용자의 이메일 주소.                                                           |
|           | `profileImageId` | MongoDB에 저장된 프로필 이미지의 고유 ID. (이 ID로 프로필 이미지 조회 API 호출) |
|           | `createdAt`      | 회원가입 시간.                                                                  |

#### **MongoDB (콘텐츠 데이터)**

| 컬렉션                 | 필드명               | 설명                                                                         |
| :--------------------- | :------------------- | :--------------------------------------------------------------------------- |
| **profile_images**     | `_id`                | 프로필 이미지의 고유 ID. (MariaDB의 `profileImageId`와 연결됨)               |
|                        | `imageData`          | 프로필 이미지 원본 바이너리 파일.                                            |
| **analysis_histories** | `_id`                | 각 분석 기록의 고유 ID.                                                      |
|                        | `userId`             | 이 기록이 누구의 것인지 알려주는 사용자의 숫자 ID (MariaDB의 `id`와 연결됨). |
|                        | `thumbnailImageData` | 마이페이지 목록에 보여줄 작은 크기의 썸네일 이미지 바이너리 파일.            |
|                        | `recognizedFoodName` | AI가 분석한 음식 이름.                                                       |
|                        | `accuracy`           | AI 분석 정확도.                                                              |
|                        | `youtubeRecipes`     | 추천된 유튜브 영상 정보 배열 (제목, URL, 썸네일URL 포함).                    |
|                        | `analysisDate`       | 분석을 요청한 날짜와 시간.                                                   |
| **food_references**    | `foodName`           | 음식의 기준 이름 (예: "파스타").                                             |
|                        | `nutritionInfo`      | 해당 음식의 영양 정보 객체 (칼로리, 단백질, 지방 등 포함).                   |
