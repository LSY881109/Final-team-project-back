# Final-team-project-back

## 🚀 최종 프로젝트 핵심 기획서 (하이브리드 DB 및 백엔드 아키텍처)

---

## 1. 프로젝트 개요 및 목표

* **팀 프로젝트명**: (현재 미정)
* **프로젝트 개요 및 목표**:
    음식 이미지를 모바일 앱(Flutter)으로 캡처하여 백엔드(Spring Boot, Flask)로 전송하고, AI(Flask) 분석을 통해 음식 이름과 정확도를 도출합니다. 이 분석 결과를 기반으로 영양소/칼로리 정보(MongoDB)와 유튜브 레시피 링크를 통합하여 사용자에게 제공하고, 모든 활동 기록은 효율적인 하이브리드 DB 구조를 통해 관리됩니다.
* **핵심 목표**: 음식 이미지 분석 및 통합 레시피/영양소 정보 제공 시스템 구축

---

## 2. 프로젝트 핵심 프레임워크 및 선정 이유

| 분류 | 프레임워크/DB | 선정 이유 | 역할 |
| --- | --- | --- | --- |
| **프론트엔드** | Flutter | **크로스 플랫폼 개발**: 단일 코드베이스로 Android/iOS 동시 지원. 뛰어난 UI 성능. | 사용자 UI 구현, 백엔드 API 호출, 이미지 캡처 및 전송. |
| **API 서버** | Spring Boot (Java) | **견고한 아키텍처**: 복잡한 하이브리드 DB 조인 로직 및 인증/보안 처리에 강함. | 인증, DB 조인 로직 구현, Flask 통신 제어, API 엔드포인트 제공. |
| **AI 마이크로 서비스** | Flask (Python) | **AI/딥러닝 특화**: Python 기반 라이브러리(TensorFlow 등)와의 연동 용이성. | 이미지 분석 모델 실행, 분석 결과 도출. |
| **RDBMS** | MariaDB | **데이터 무결성 및 정규화**: 사용자 계정, 비밀번호 해시, 활동 기록의 메타데이터 (SearchSessions) 무결성 보장. | Spring Boot의 JPA를 통해 접근. |
| **NoSQL** | MongoDB | **대용량 파일 저장 및 유연성**: 이미지 원본(GridFS), AI 분석 결과, 영양소 정보 등 유연한 데이터 처리. | Spring Data MongoDB를 통해 접근. |

---

## 3. 최종 데이터베이스 구조 및 관계 분석 (MariaDB + MongoDB)

### 3.1. MariaDB (RDBMS): 무결성, 인증, 조인의 중심

| 테이블 | 필드명 (데이터 타입) | 역할 및 관계 | 제약 조건 |
| --- | --- | --- | --- |
| **Users** | `user_id` (BIGINT/UUID) | Primary Key. 시스템 전체의 고유 식별자. | PK, Not Null |
| | `email` (VARCHAR(255)) | 로그인 ID. | Unique, Not Null |
| | `password_hash` (VARCHAR(255)) | 암호화된 비밀번호. | Not Null |
| | `profile_image_id` (ObjectID Ref.) | MongoDB `ProfileImages` 컬렉션을 참조하는 키. | Index, Nullable (FK to MongoDB) |
| **SearchSessions** | `session_id` (BIGINT) | Primary Key. 검색 기록의 고유 ID. | PK, Auto Inc. |
| | `user_id` (BIGINT/UUID) | `Users` 테이블을 참조하는 외래 키. | FK, Not Null |
| | `analysis_result_id` (ObjectID Ref.) | MongoDB `AnalysisResults` 문서를 참조하는 키. | Index, Not Null (FK to MongoDB) |
| | `search_timestamp` (DATETIME) | 기록의 시간 정보. (최근 검색 정렬 기준) | Not Null |

### 3.2. MongoDB (NoSQL): 대용량 저장 및 유연한 데이터

| 컬렉션 | 필드명 (데이터 타입) | 역할 및 관계 | 비고 |
| --- | --- | --- | --- |
| **ProfileImages** | `_id` (ObjectID) | MariaDB `Users.profile_image_id`와 맵핑됨. | PK in Mongo |
| | `image_data` (Binary/GridFS) | 프로필 이미지 원본 파일 데이터. | 16MB 초과 시 GridFS 권장 |
| **AnalysisResults** | `_id` (ObjectID) | MariaDB `SearchSessions.analysis_result_id`와 맵핑됨. | PK in Mongo |
| | `raw_image_data` (Binary/GridFS) | 사용자 요청 이미지 원본 (딥러닝 활용). | 대용량 파일 저장 |
| | `ai_analysis_result` (Object) | AI 모델의 분석 결과 (food + accuracy). | 유연한 구조 |
| | `youtube_recipes` (Array of Objects) | 구글 API를 통해 검색된 레시피 링크 배열. | 유연한 데이터 구조 |
| | `food_reference_id` (ObjectID Ref.) | `FoodReference` 컬렉션을 참조하는 키. | FK to MongoDB |
| **FoodReference** | `_id` (ObjectID) | `AnalysisResults.food_reference_id`가 참조함. | PK in Mongo |
| | `nutrition_data` (Object) | 직접 입력되는 영양소 및 칼로리 정보. | 정적 데이터 |

---

## 4. 백엔드 아키텍처 및 구현 계획

### 4.1. 백엔드 폴더/패키지 상세 구조 (Spring Boot 기반)

# 🍲 음밥해 (Eumbabae)

**AI 기반 이미지 분석 및 통합 레시피/영양소 제공 시스템**

<br>

## ❓ 문제 인식: 배달 음식의 건강한 대안

잦은 배달 음식 섭취로 인한 건강 불균형과 소비 부담이 증가하고 있습니다. 현대인들은 편리함을 선택하지만, 영양 불균형과 경제적 부담이라는 대가를 치르고 있습니다.

## 💡 우리의 솔루션

집에서 직접 요리할 수 있는 건강하고 맛있는 가정식 레시피를 AI 기반으로 추천합니다. 사용자가 촬영한 음식 사진을 분석하여 영양 정보와 관련 레시피를 제공합니다.

---

## ✨ 핵심 기능

* 📸 **AI 음식 분석**: 촬영한 음식 사진을 **EfficientNet** 모델로 분석하여 Top-3 예측 결과와 정확한 영양 정보(칼로리 등)를 제공합니다.
* 🍳 **레시피 추천**: 분석된 음식과 관련된 **YouTube 레시피 영상**을 검색하고 정렬하여 추천합니다. 사용자 맞춤형 요리 가이드를 제공합니다.
* 🗺️ **맛집 정보**: **Google Maps API**를 연동하여 주변 관련 맛집 정보를 실시간으로 제공합니다. 집에서 요리가 어려울 때 대안을 제시합니다.

---

## 💻 기술 스택 (Tech Stack)

### 📱 Tier 1: 클라이언트 (Flutter)
* **Core**: Flutter, Dart
* **HTTP**: `http`, `dio`
* **Auth**: `app_links`, `flutter_secure_storage`
* **UI/Media**: `lottie`, `image_picker`, `video_player`
* **Maps**: `Maps_flutter`, `geolocator`

### ⚙️ Tier 2: 백엔드 (Spring Boot)
* **Core**: Java, Spring Boot
* **Data**: JPA (MariaDB), MongoDB
* **Security**: Spring Security, JWT, OAuth2
* **API Client**: OkHttp3, WebClient, Google API
* **Utils**: Lombok, ModelMapper

### 🤖 Tier 3: 서비스 계층

#### 1. Flask AI 서버
* **Core**: Python, Flask
* **AI/ML**: PyTorch, Torchvision
* **Models**: EfficientNet (분류), BiRefNet (배경 제거)
* **Image**: Pillow (PIL)

#### 2. 하이브리드 데이터베이스
* **MariaDB (RDBMS)**: User, OAuth2Account, SearchSessions - 정형 데이터 및 트랜잭션 관리
* **MongoDB (NoSQL)**: ProfileImage, AnalysisHistory, FoodReference - 비정형/대용량 데이터 및 유연한 스키마

#### 3. 외부 API 연동
* **Google Places & Maps API**: 주변 맛집 검색 및 지도 표시
* **YouTube Data API**: 레시피 영상 검색 및 추천
* **OAuth 2.0**: Google/Naver 소셜 로그인

---

## 📐 시스템 아키텍처

### 1. 3-Tier 시스템 아키텍처
* **Tier 1: 클라이언트 (Flutter)**
    * Flutter 기반 크로스플랫폼 앱. 사용자 인터페이스와 상호작용을 담당하며, 오직 백엔드와만 통신합니다.
* **Tier 2: 백엔드 (Spring Boot)**
    * Spring Boot 중앙 관제소. 모든 요청을 조율하고, 인증/인가, 비즈니스 로직, 데이터 통합을 처리합니다.
* **Tier 3: 서비스 계층 (Flask/DB/APIs)**
    * Flask AI 서버, 하이브리드 DB(MariaDB + MongoDB), 외부 APIs(Google, YouTube)로 구성된 서비스 레이어입니다.

### 2. 하이브리드 DB 전략

* **Why? (전략적 선택)**
    * **성능 최적화**: `User` 테이블의 성능 저하를 방지하기 위해 무거운 이미지(Binary)와 JSON 데이터를 MongoDB로 분리했습니다.
    * **유연한 확장**: `AnalysisHistory` 같은 로그성 데이터는 스키마 변경이 자유로운 NoSQL이 유리합니다.

* **What? (데이터 분리 구조)**
    * **MariaDB (정형 데이터)**:
        * `Users`: 계정 정보 및 인증
        * `OAuth2Account`: 소셜 로그인 연동
        * `SearchSessions`: 검색 기록 추적
    * **MongoDB (비정형 데이터)**:
        * `ProfileImage`: 프로필 이미지 원본
        * `AnalysisHistory`: AI 분석 내역
        * `FoodReference`: 음식별 영양 정보
    * **Key Link**: MariaDB `User` 엔티티가 MongoDB `ProfileImage`의 Document ID를 `profileImageId(String)` 필드로 참조하여 두 DB를 연결합니다.

### 3. ERD 다이어그램
*(ERD 상세 내용)*

---

## 🔄 핵심 동작 플로우

**Flow 1: 이미지 분석 및 결과 확인**

1.  **이미지 선택 및 전송**
    * Flutter `capture_page`에서 이미지 선택 → Spring Boot `/api/analysis` 호출 (이미지 + JWT 헤더)
2.  **인증 및 AI 요청**
    * `AnalysisController`가 JWT에서 `userId` 추출 → `AIAnalysisService`가 Flask `/analyze` 호출
3.  **AI 모델 실행**
    * Flask에서 `EfficientNet` 모델 실행 → Top-3 예측 결과 JSON 응답
4.  **영양 정보 조회**
    * Spring Boot가 MongoDB `FoodReference`에서 영양 정보 조회
5.  **분석 내역 저장**
    * MongoDB `AnalysisHistory`에 분석 내역(Top-3, 영양소, `userId`) 저장 → `historyId` 생성
6.  **결과 표시**
    * `FoodAnalysisResultDTO`로 Flutter에 응답 → `result_page`에 Top-3, 영양소 정보 표시
7.  **추가 정보 검색**
    * YouTube 레시피 검색 (`/api/youtube/search`) 및 맛집 검색 (`/api/map/search`) 실행

---

## 🤖 AI 모델: EfficientNet

### 1. 초기 모델 검토: CNN & ResNet
저희는 초기 모델로 CNN과 ResNet을 검토했습니다. 이 모델들은 이미지 분석에서 전반적으로 낮은 정확도를 보였으며, 음식 종류를 부정확하게 예측하는 문제점이 있었습니다. 특히 복잡한 음식 이미지에서 미묘한 차이를 구분하는 데 어려움을 겪어, 신뢰할 수 있는 예측 결과를 제공하기 어려웠습니다.

### 2. EfficientNet 선택 이유: 압도적 효율성
1.  **압도적인 효율성 및 성능**
    * CNN이나 ResNet에 비해 훨씬 적은 파라미터(FLOPs)만으로도 더 높은 이미지 인식 정확도(Accuracy)를 달성합니다.
2.  **체계적인 모델 최적화**
    * 깊이, 너비, 해상도를 동시에 조정하는 복합 스케일링(Compound Scaling) 기법을 사용하여, ResNet처럼 한 차원만 임의로 키우는 방식보다 훨씬 효율적인 최적 모델을 찾을 수 있습니다.
3.  **리소스 절약 및 범용성**
    * 적은 계산량 덕분에 모델 학습 및 추론 시간을 대폭 단축하여 자원을 절약할 수 있으며, 모바일 환경 등 리소스가 제한적인 곳에서도 CNN/ResNet보다 훨씬 유용하게 활용 가능합니다.

### 3. EfficientNet AI 모델 학습 과정
1.  **데이터 전처리**
    * 이미지 데이터를 EfficientNet 모델 학습에 최적화하기 위해 크기 조정, 정규화, 증강 등의 전처리 과정을 거칩니다. 이는 모델의 성능을 향상시키고 과적합을 방지하는 데 필수적입니다.
2.  **데이터 추가학습 (Fine-tuning)**
    * 사전 학습된 EfficientNet 모델을 기반으로, 특정 음식 이미지 데이터셋을 추가로 학습시켜 모델이 음식의 다양한 특징을 정확하게 인식하도록 미세 조정(Fine-tuning)합니다. 이를 통해 음식 이미지 분석에 특화된 모델을 구축합니다.
3.  **학습 완료 및 배포**
    * 학습 및 미세 조정이 완료된 EfficientNet 모델을 실제 서비스 환경에 배포하여 음식 이미지 분석 기능을 제공합니다. 이제 모델은 새로운 음식 이미지를 입력받아 종류, 재료, 특징 등을 성공적으로 식별하고 분류할 수 있습니다.

---

## 📱 애플리케이션 주요 화면

### 1. 음식 사진 업로드 & 영양소 상세 정보
* **[Flutter의 음식 사진 업로드 인터페이스]**
    * `image_picker` 패키지를 활용하여 카메라 촬영 또는 갤러리에서 이미지를 선택할 수 있는 직관적인 UI를 제공합니다.
* **[업로드된 음식 사진 미리보기 화면]**
    * 선택된 이미지를 최종 확인하고 분석을 시작하기 전 단계입니다.
* **[영양소 상세 페이지 화면]**
    * 음식의 상세 영양 성분 정보(칼로리, 탄수화물, 단백질, 지방 등)를 표시합니다. 영양소별 수치와 비율을 시각적으로 표현하여 사용자가 섭취한 음식의 영양 정보를 한눈에 파악할 수 있습니다.

### 2. 음식 사진 분석 & YouTube 레시피 추천
* **[음식 분석 결과 화면]**
    * AI가 분석한 음식의 예측 결과와 정확도를 표시하고, 분석된 음식의 기본 정보를 제공합니다.
* **[음식 상세 정보]**
    * AI 분석 결과를 바탕으로 음식 사진의 상세정보를 표시하며, 사용자가 촬영한 음식 사진과 분석 결과가 통합된 화면을 제공합니다.
* **[YouTube 레시피 추천]**
    * 분석된 음식과 관련된 YouTube 요리 영상들을 썸네일과 함께 리스트 형태로 추천합니다. 레시피 영상의 제목, 채널명, 조회수 등 상세 정보를 제공하며, 직관적인 인터페이스로 바로 영상을 시청할 수 있습니다.

### 3. Google Maps API 맛집 검색 & 주변 맛집 탐색
* **[맛집 검색 결과 리스트]**
    * Google Places API를 활용하여 사용자 주변의 맛집 정보를 표시하며, 맛집의 이름, 평점, 거리, 영업시간 등 상세 정보를 제공합니다.
* **[Google Maps를 활용한 지도]**
    * 실시간 지도 상에 맛집 위치가 마커로 표시되어, 사용자 현재 위치와 맛집들의 지리적 관계를 시각적으로 파악할 수 있습니다.

### 4. 마이페이지: 개인화된 음식 분석 히스토리
마이페이지는 사용자의 음식 분석 과정을 한눈에 볼 수 있는 개인화된 대시보드 역할을 합니다.

* **사용자 로그인 정보 표시**: 프로필 이미지와 사용자명이 상단에 명확히 표시됩니다.
* **음식 분석 히스토리**: 이전에 분석했던 음식 사진들과 그 결과들이 시간순으로 정리됩니다.
* **분석 결과 상세 정보**: 예측 결과(Top 3), 정확도, 분석 날짜 및 시간 등 상세 정보가 제공됩니다.
* **YouTube 레시피 추천 목록**: 분석된 음식과 관련된 YouTube 레시피 영상들이 카드 형태로 추천됩니다.

---

## 🗓️ 3주 압축 프로젝트 일정 (10/17 ~ 11/14)

* **1주차 (10/20 ~ 10/24) : 기획 및 기반 구축**
    * 3-Tier 아키텍처 설계 완료
    * 하이브리드 DB 및 API 명세서 작성
    * AI 모델 선정: EfficientNet 채택
* **2주차 (10/27 ~ 10/31) : 핵심 기능 구현 및 연동**
    * Spring ↔ Flask AI 연동 완료
    * YouTube/Google Maps API 분리
    * ngrok 테스트 환경 구축
    * Flutter 인증 연동 및 핵심 기능 구현
    * 1차 통합 테스트 수행
* **3주차 (11/03 ~ 11/14) : 고도화 및 발표**
    * JWT 토큰 기반 API 인증 적용
    * Spring Security JWT/OAuth2 구현
    * Flutter 기본 UI 구현 및 마이페이지 하이브리드 DB 연동
    * 버그 수정 및 UI 정리
    * 11/12 리허설, 11/13 본 발표, 11/14 프로젝트 회고

---

## 👥 팀원 및 역할 분담

* **이승엽 (조장)**: 시스템 아키텍처 (전체 3-Tier 구조 설계), GitHub 관리, 팀원 서포트 및 프로젝트 총괄
* **유영준 (부조장)**: 외부 API 연동 및 통합 로직
    * YouTube Data API v3 연동 (검색 쿼리, 정렬, 필터링, 예외 처리) 및 `YoutubeApiService` 구현
    * 이미지 분석 통합 로직 (Flask AI 연동, 영양정보 조회, 히스토리 저장, 썸네일 생성)
    * JWT 기반 사용자별 히스토리 조회 API 구현
* **추교문**: DB 아키텍처 및 백엔드 개발
    * MariaDB 및 MongoDB 설계 및 구축 (Users, SearchSessions 스키마 포함)
    * `SearchSessionService` 구현, 회원관리 API 개발, 비즈니스 로직 및 서비스 연동
    * Flutter UI 개선 지원
* **김무진**: AI 연동 및 클라이언트 개발
    * MongoDB 관리, Google Maps API 연동, `AnalysisController`/`AIAnalysisService` 구현
    * 플러터 지도 화면 연동, 프론트엔드 기본 UI 구축
* **류성원**: 인증 및 보안
    * 소셜 로그인(Google/Naver) 및 자체 로그인 설정, `SocialController` 및 보안 서비스 구현
    * DTO 설계, 프로필 이미지 저장 방식 구축, 보안 관련 작업 전반

---

## 🚀 향후 발전 계획

* **클라이언트 고도화**
    * 일일 섭취 칼로리 대시보드 구현
    * Refresh Token 자동 재발급 로직
    * 마이페이지 페이징 구현
* **AI 모델 개선**
    * EfficientNet 카테고리 5종 → 50종 확장
    * "음식 아님" 클래스 추가
    * BiRefNet 배경 제거 성능 개선
* **백엔드 최적화**
    * Redis 캐싱으로 API 호출 최소화
    * Refresh Token 인증 고도화
    * 회원 탈퇴 시 MongoDB 연관 데이터 처리
* **DevOps 구축**
    * AWS/GCP 클라우드 배포
    * GitHub Actions CI/CD 파이프라인
    * 자동화된 테스트 및 배포

### 🎯 최종 비전
AI 모델 고도화와 사용자 데이터 기반 분석을 통해, 개개인에게 정교하고 개인화된 영양 관리 서비스를 제공하는 **종합 식단 관리 및 헬스케어 앱**으로 발전시키는 것이 우리의 목표입니다.

---

### 주요 코드 리뷰

알겠습니다. 1번 방법인 `<table>` 태그를 사용해서 중구난방이던 이미지들을 깔끔한 그리드(표)로 정렬했습니다.

코드를 보시기 편하도록 이미지 가로 폭은 `400px`로 통일했습니다. 150px은 너무 작아서 코드 내용을 알아보기 어려울 수 있습니다.

아래의 전체 코드를 복사해서 리드미 파일에 붙여넣기 하시면 됩니다.

-----

## 👨‍💻 주요 코드 리뷰

아, 정말 죄송합니다\! 제가 코드를 잘못 드렸습니다.

제가 드린 코드의 `src` 부분에 마크다운 링크 형식(`[...](...)`)이 통째로 잘못 들어가 있었네요. 그리고 복사하기 편하게 하려다 넣은 `\` 기호 때문에 더 헷갈리게 해 드렸습니다.

모든 문제를 수정한, `README.md` 파일에 **그대로 붙여넣으면 되는** 깨끗한 코드를 다시 드립니다.

이 코드를 `README.md` 파일에 저장하신 후 \*\*'미리보기'\*\*로 보시면, 의도한 대로 이미지들이 표 안에 깔끔하게 정렬되어 보일 겁니다.

-----

## 👨‍💻 주요 코드 리뷰

### 1\. 유영준 (AI/Data/API)

  * **크롤링 이미지 수집**: 문서 전체를 검색하여 모든 이미지 태그 URL 추출 및 다단계 필터링
    \<table\>
    \<tr\>
    \<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/f3aace39f4ac4f36a0390015109c3972/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/f3aace39f4ac4f36a0390015109c3972/original/image.png)" alt="크롤링 코드 1" width="400"\>\</td\>
    \<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/829266b81d2c4c208b902500768cff1d/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/829266b81d2c4c208b902500768cff1d/original/image.png)" alt="크롤링 코드 2" width="400"\>\</td\>
    \<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/cb57966896464a33bbd3aa836cdb357c/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/cb57966896464a33bbd3aa836cdb357c/original/image.png)" alt="크롤링 코드 3" width="400"\>\</td\>
    \</tr\>
    \</table\>
  * **전처리 (배경 제거)**: BiRefNet 딥러닝 모델을 사용한 배경 제거 (누끼 따기)
    \<table\>
    \<tr\>
    \<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/c54dfe5886e145e19b87811652a8bd59/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/c54dfe5886e145e19b87811652a8bd59/original/image.png)" alt="배경 제거 코드 1" width="400"\>\</td\>
    \<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/ba263258bc034a7aabc9da15753e242e/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/ba263258bc034a7aabc9da15753e242e/original/image.png)" alt="배경 제거 코드 2" width="400"\>\</td\>
    \</tr\>
    \<tr\>
    \<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/3d2dd48de7a244a8b2d8f2287cfd6589/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/3d2dd48de7a244a8b2d8f2287cfd6589/original/image.png)" alt="배경 제거 코드 3" width="400"\>\</td\>
    \<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/564edb314ddf4aa39c1c50870fed56dc/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/564edb314ddf4aa39c1c50870fed56dc/original/image.png)" alt="배경 제거 코드 4" width="400"\>\</td\>
    \</tr\>
    \</table\>
  * **이미지 딥러닝 (훈련)**: 클래스별 데이터 불균형 해소를 위한 가중치 적용 샘플링
    \<table\>
    \<tr\>
    \<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/1261c35862d946b98e9c01e29db44f55/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/1261c35862d946b98e9c01e29db44f55/original/image.png)" alt="딥러닝 훈련 코드 1" width="400"\>\</td\>
    \<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/58a06a0845274f2c9b8b6a500ce44610/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/58a06a0845274f2c9b8b6a500ce44610/original/image.png)" alt="딥러닝 훈련 코드 2" width="400"\>\</td\>
    \</tr\>
    \</table\>
  * **이미지 딥러닝 (추론)**: 학습된 모델을 통해 음식 예측
    \<table\>
    \<tr\>
    \<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/33422a141abf4901b78696dd300cea19/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/33422a141abf4901b78696dd300cea19/original/image.png)" alt="딥러닝 추론 코드 1" width="400"\>\</td\>
    \<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/36d2dbbac2c24737804a491dd967594a/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/36d2dbbac2c24737804a491dd967594a/original/image.png)" alt="딥러닝 추론 코드 2" width="400"\>\</td\>
    \</tr\>
    \<tr\>
    \<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/a0f6ab6f85d344a080667e47c7993920/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/a0f6ab6f85d344a080667e47c7993920/original/image.png)" alt="딥러닝 추론 코드 3" width="400"\>\</td\>
    \<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/2734b896cf934003adacaf940d52dc8b/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/2734b896cf934003adacaf940d52dc8b/original/image.png)" alt="딥러닝 추론 코드 4" width="400"\>\</td\>
    \</tr\>
    \</table\>
  * **Flask**: Flask와 PyTorch를 사용해 이미지 분석 결과를 JSON으로 반환하는 API 서버 구축
    \<table\>
    \<tr\>
    \<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/e1319db6b94748cbad0ef948c5522d6a/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/e1319db6b94748cbad0ef948c5522d6a/original/image.png)" alt="Flask 코드 1" width="400"\>\</td\>
    \<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/66de7f681f7344d9aab0389f13e15ad6/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/66de7f681f7344d9aab0389f13e15ad6/original/image.png)" alt="Flask 코드 2" width="400"\>\</td\>
    \</tr\>
    \</table\>
  * **Flutter**: JSON 데이터를 `AnalysisResult` 객체로 변환
    \<table\>
    \<tr\>
    \<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/b4f0c499421e4df4b963a014926acc3f/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/b4f0c499421e4df4b963a014926acc3f/original/image.png)" alt="Flutter 코드 1" width="400"\>\</td\>
    \<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/cf89162d92af4e19825903e91ffc521b/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/cf89162d92af4e19825903e91ffc521b/original/image.png)" alt="Flutter 코드 2" width="400"\>\</td\>
    \</tr\>
    \</table\>

### 2\. 김무진 (Maps API/Client)

  * **이미지 크롤링**:
    \<table\>
    \<tr\>
    \<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/8a7f061996474fbfae20653d89235fc9/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/8a7f061996474fbfae20653d89235fc9/original/image.png)" alt="김무진 크롤링 1" width="400"\>\</td\>
    \<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/ed4f76b5bc444370a5d57443b0c058aa/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/ed4f76b5bc444370a5d57443b0c058aa/original/image.png)" alt="김무진 크롤링 2" width="400"\>\</td\>
    \</tr\>
    \</table\>
  * **이미지 전처리**:
    \<table\>
    \<tr\>
    \<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/55ce1d6b05444d708c5f5bda9b44cff4/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/55ce1d6b05444d708c5f5bda9b44cff4/original/image.png)" alt="김무진 전처리 1" width="400"\>\</td\>
    \<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/b1c8c412048c4ed2a339d5098af12ed5/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/b1c8c412048c4ed2a339d5098af12ed5/original/image.png)" alt="김무진 전처리 2" width="400"\>\</td\>
    \<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/6f829e0756b24a719226915db57c9bdf/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/6f829e0756b24a719226915db57c9bdf/original/image.png)" alt="김무진 전처리 3" width="400"\>\</td\>
    \</tr\>
    \</table\>
  * **Backend Controller**: `(foodName), (latitude), (longitude)` 매개변수를 추출하여 Service로 전달
    <br>
    \<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/918fb885c4884ba3b5eb01ad0cf788f7/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/918fb885c4884ba3b5eb01ad0cf788f7/original/image.png)" alt="Backend Controller" width="400"\>
  * **Backend Service**: `@Value`로 API 키를 불러와 외부 API 통신 후 Controller에 결과 반환
    <br>
    \<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/35d4a901c71848e3b5e35b3094ffd830/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/35d4a901c71848e3b5e35b3094ffd830/original/image.png)" alt="Backend Service" width="400"\>
  * **분석 결과 식당 지도**: 분석 완료 후 해당 음식 이름으로 주변 식당 자동 검색 및 지도 표시 (내 위치: 파란색, 맛집: 빨간색 마커)
    \<table\>
    \<tr\>
    \<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/c8f87db1dfa9451fb067694e1cf7a257/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/c8f87db1dfa9451fb067694e1cf7a257/original/image.png)" alt="식당 지도 1" width="400"\>\</td\>
    \<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/1126ebceb7c24111b69aa2cb42fba4e3/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/1126ebceb7c24111b69aa2cb42fba4e3/original/image.png)" alt="식당 지도 2" width="400"\>\</td\>
    \</tr\>
    \</table\>
  * **직접 검색 입력 지도**: 사용자가 입력한 키워드 기반으로 백엔드 API 호출
    \<table\>
    \<tr\>
    \<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/a705ddaf1d8242bc96cb6ae250b98d27/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/a705ddaf1d8242bc96cb6ae250b98d27/original/image.png)" alt="검색 지도 1" width="400"\>\</td\>
    \<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/ebc5dd9b385147f2ae172e6bd64e4386/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/ebc5dd9b385147f2ae172e6bd64e4386/original/image.png)" alt="검색 지도 2" width="400"\>\</td\>
    \</tr\>
    \</table\>
  * **Google API 설정**: API 키 제한 설정 (제한 안함 X, 필요 API 직접 설정 O)
    <br>
    \<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/806898d2ce0d456fac3acd7510977ad2/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/806898d2ce0d456fac3acd7510977ad2/original/image.png)" alt="Google API 설정" width="400"\>
  * **에뮬레이터 좌표 설정**: 에뮬레이터에서 특정 위치 좌표 설정 방법
    \<table\>
    \<tr\>
    \<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/ab572df560184abfa43ac9712e635e2e/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/ab572df560184abfa43ac9712e635e2e/original/image.png)" alt="에뮬레이터 설정 1" width="400"\>\</td\>
    \<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/21bfa361e4424ed18c5114e3b543712c/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/21bfa361e4424ed18c5114e3b543712c/original/image.png)" alt="에뮬레이터 설정 2" width="400"\>\</td\>
    \</tr\>
    \</table\>

### 3\. 추교문 (DB/Backend)

\<table\>
\<tr\>
\<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/79bf672aae3845ac8380246ea81c44b7/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/79bf672aae3845ac8380246ea81c44b7/original/image.png)" alt="추교문 코드 1" width="400"\>\</td\>
\<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/7707d761333f45ad861aeb5ad05cfa7a/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/7707d761333f45ad861aeb5ad05cfa7a/original/image.png)" alt="추교문 코드 2" width="400"\>\</td\>
\<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/4f93a5da03184601aa635e1837fc10b0/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/4f93a5da03184601aa635e1837fc10b0/original/image.png)" alt="추교문 코드 3" width="400"\>\</td\>
\</tr\>
\<tr\>
\<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/858bf284db724dad82e05b5bc087258d/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/858bf284db724dad82e05b5bc087258d/original/image.png)" alt="추교문 코드 4" width="400"\>\</td\>
\<td\>\<img src="httpss://cdn.gamma.app/a86k0eon9iurvq4/dce0ed322d6742b28563ffba846b96e6/original/image.png" alt="추교문 코드 5" width="400"\>\</td\>
\<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/352fce89e0404406b3de3d1807f0bd42/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/352fce89e0404406b3de3d1807f0bd42/original/image.png)" alt="추교문 코드 6" width="400"\>\</td\>
\</tr\>
\<tr\>
\<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/14ecb346b9f2475d8f79f4b114d4da47/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/14ecb346b9f2475d8f79f4b114d4da47/original/image.png)" alt="추교문 코드 7" width="400"\>\</td\>
\<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/92d9f7c9130345b19be1c8bc366b1140/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/92d9f7c9130345b19be1c8bc366b1140/original/image.png)" alt="추교문 코드 8" width="400"\>\</td\>
\<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/b6acf3dd86584abab369112922ec8258/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/b6acf3dd86584abab369112922ec8258/original/image.png)" alt="추교문 코드 9" width="400"\>\</td\>
\</tr\>
\<tr\>
\<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/55c51a60daf14a27b5e84a1e50cf5e48/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/55c51a60daf14a27b5e84a1e50cf5e48/original/image.png)" alt="추교문 코드 10" width="400"\>\</td\>
\<td\>\</td\>
\<td\>\</td\>
\</tr\>
\</table\>

### 4\. 류성원 (Security/Auth)

\<table\>
\<tr\>
\<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/3f35a44a807d41e2b30ee4a1c960f901/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/3f35a44a807d41e2b30ee4a1c960f901/original/image.png)" alt="류성원 코드 1" width="400"\>\</td\>
\<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/b7f234f78f104547a422f873fa95b011/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/b7f234f78f104547a422f873fa95b011/original/image.png)" alt="류성원 코드 2" width="400"\>\</td\>
\</tr\>
\<tr\>
\<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/48aae4e351094ce0ab45b8d8ebbcfaba/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/48aae4e351094ce0ab45b8d8ebbcfaba/original/image.png)" alt="류성원 코드 3" width="400"\>\</td\>
\<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/044b6563e94d4963b5490df0fd99e2cd/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/044b6563e94d4963b5490df0fd99e2cd/original/image.png)" alt="류성원 코드 4" width="400"\>\</td\>
\</tr\>
\</table\>

  * **SecurityConfig.java**: 진입, 연결, 필터 배치
    <br>
    \<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/543ec8f0fe0f48068f2cd3a4a85209a9/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/543ec8f0fe0f48068f2cd3a4a85209a9/original/image.png)" alt="SecurityConfig" width="400"\>
  * **공급자 응답 표준화**: DB 연동 및 `nameKey` 처리
    \<table\>
    \<tr\>
    \<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/2ac3ea940e684a88a636ec4549d21358/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/2ac3ea940e684a88a636ec4549d21358/original/image.png)" alt="응답 표준화 1" width="400"\>\</td\>
    \<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/582c474bb9cb42eea51d68c65fac5f9b/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/582c474bb9cb42eea51d68c65fac5f9b/original/image.png)" alt="응답 표준화 2" width="400"\>\</td\>
    \</tr\>
    \</table\>
  * **링크 존재 시 즉시 반환 / 신규 생성**: 신규/기존 소셜 로그인 사용자 처리
    \<table\>
    \<tr\>
    \<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/0763be5fa74f4fdc801ab355a46607bc/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/0763be5fa74f4fdc801ab355a46607bc/original/image.png)" alt="신규/기존 사용자 처리 1" width="400"\>\</td\>
    \<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/8721c443ade340e0a0621460bfd1f02b/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/8721c443ade340e0a0621460bfd1f02b/original/image.png)" alt="신규/기존 사용자 처리 2" width="400"\>\</td\>
    \</tr\>
    \</table\>
  * **OAuth2LoginSuccessHandler.java**: JWT 발급 및 리다이렉트
    \<table\>
    \<tr\>
    \<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/f28bd7f26fca45d2bbab97ae2a9e0b7b/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/f28bd7f26fca45d2bbab97ae2a9e0b7b/original/image.png)" alt="OAuth2 Handler 1" width="400"\>\</td\>
    \<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/b08eb6fec89a467296c528249fc4f9f0/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/b08eb6fec89a467296c528249fc4f9f0/original/image.png)" alt="OAuth2 Handler 2" width="400"\>\</td\>
    \</tr\>
    \</table\>
  * **JwtAuthenticationFilter.java**: 토큰 파싱, 검증, 주입
    \<table\>
    \<tr\>
    \<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/676998b7539e421d92de7d2dc5a31a26/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/676998b7539e421d92de7d2dc5a31a26/original/image.png)" alt="JWT Filter 1" width="400"\>\</td\>
    \<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/57c6c08413a84516b1a10a49e9c5e4c3/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/57c6c08413a84516b1a10a49e9c5e4c3/original/image.png)" alt="JWT Filter 2" width="400"\>\</td\>
    \</tr\>
    \</table\>
  * **리다이렉트 문제 해결**
    \<table\>
    \<tr\>
    \<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/8aa8bbec04be4dc79ebb0bde30e217ef/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/8aa8bbec04be4dc79ebb0bde30e217ef/original/image.png)" alt="리다이렉트 해결 1" width="400"\>\</td\>
    \<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/2e2e9cf98066445493d8b2216e996802/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/2e2e9cf98066445493d8b2216e996802/original/image.png)" alt="리다이렉트 해결 2" width="400"\>\</td\>
    \</tr\>
    \</table\>
  * **소셜 로그인 프로필 이미지 업데이트 해결**
    \<table\>
    \<tr\>
    \<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/96b52ce3c1c94e2c8ef733ac7b709fe1/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/96b52ce3c1c94e2c8ef733ac7b709fe1/original/image.png)" alt="프로필 이미지 해결 1" width="400"\>\</td\>
    \<td\>\<img src="[https://cdn.gamma.app/a86k0eon9iurvq4/6a36e2e99e0e4447aba6cb706e72e220/original/image.png](https://cdn.gamma.app/a86k0eon9iurvq4/6a36e2e99e0e4447aba6cb706e72e220/original/image.png)" alt="프로필 이미지 해결 2" width="400"\>\</td\>
    \</tr\>
    \</table\>
