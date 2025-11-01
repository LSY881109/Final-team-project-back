"# Final-team-project-back" 

최종 프로젝트 핵심 기획서 (하이브리드 DB 및 백엔드 아키텍처)
1. 프로젝트 개요 및 목표
팀 프로젝트명: (현재 미정)
프로젝트 개요 및 목표:
음식 이미지를 모바일 앱(Flutter)으로 캡처하여 백엔드(Spring Boot, Flask)로 전송하고, AI(Flask) 분석을 통해 음식 이름과 정확도를 도출합니다. 이 분석 결과를 기반으로 영양소/칼로리 정보(MongoDB)와 유튜브 레시피 링크를 통합하여 사용자에게 제공하고, 모든 활동 기록은 효율적인 하이브리드 DB 구조를 통해 관리됩니다. 핵심 목표는 음식 이미지 분석 및 통합 레시피/영양소 정보 제공 시스템 구축입니다.
2. 프로젝트 핵심 프레임워크 및 선정 이유
프로젝트의 요구사항(하이브리드 DB, AI 분석, 크로스 플랫폼)을 충족하기 위해 아래의 기술 스택을 확정하고, 각 선택에 대한 구체적인 이유를 제시합니다.
분류
프레임워크/DB
선정 이유
역할
프론트엔드
Flutter
크로스 플랫폼 개발: 단일 코드베이스로 Android/iOS 동시 지원. 뛰어난 UI 성능.
사용자 UI 구현, 백엔드 API 호출, 이미지 캡처 및 전송.
API 서버
Spring Boot (Java)
견고한 아키텍처: 복잡한 하이브리드 DB 조인 로직 및 인증/보안 처리에 강함.
인증, DB 조인 로직 구현, Flask 통신 제어, API 엔드포인트 제공.
AI 마이크로 서비스
Flask (Python)
AI/딥러닝 특화: Python 기반 라이브러리(TensorFlow 등)와의 연동 용이성.
이미지 분석 모델 실행, 분석 결과 도출.
RDBMS
MariaDB
데이터 무결성 및 정규화: 사용자 계정, 비밀번호 해시, 활동 기록의 메타데이터 (SearchSessions) 무결성 보장.
Spring Boot의 JPA를 통해 접근.
NoSQL
MongoDB
대용량 파일 저장 및 유연성: 이미지 원본(GridFS), AI 분석 결과, 영양소 정보 등 유연한 데이터 처리.
Spring Data MongoDB를 통해 접근.

3. 최종 데이터베이스 구조 및 관계 분석 (MariaDB + MongoDB)
3.1. MariaDB (RDBMS) 역할: 무결성, 인증, 조인의 중심
테이블
필드명 (데이터 타입)
역할 및 관계
제약 조건
Users
user_id (BIGINT/UUID)
Primary Key. 시스템 전체의 고유 식별자.
PK, Not Null


email (VARCHAR(255))
로그인 ID.
Unique, Not Null


password_hash (VARCHAR(255))
암호화된 비밀번호.
Not Null


profile_image_id (ObjectID Ref.)
MongoDB ProfileImages 컬렉션을 참조하는 키.
Index, Nullable (FK to MongoDB)
SearchSessions
session_id (BIGINT)
Primary Key. 검색 기록의 고유 ID.
PK, Auto Inc.


user_id (BIGINT/UUID)
Users 테이블을 참조하는 외래 키.
FK, Not Null


analysis_result_id (ObjectID Ref.)
MongoDB AnalysisResults 문서를 참조하는 키.
Index, Not Null (FK to MongoDB)


search_timestamp (DATETIME)
기록의 시간 정보. (최근 검색 정렬 기준)
Not Null

3.2. MongoDB (NoSQL) 역할: 대용량 저장 및 유연한 데이터
컬렉션
필드명 (데이터 타입)
역할 및 관계
비고
ProfileImages
_id (ObjectID)
MariaDB Users.profile_image_id와 맵핑됨.
PK in Mongo


image_data (Binary/GridFS)
프로필 이미지 원본 파일 데이터.
16MB 초과 시 GridFS 권장
AnalysisResults
_id (ObjectID)
MariaDB SearchSessions.analysis_result_id와 맵핑됨.
PK in Mongo


raw_image_data (Binary/GridFS)
사용자 요청 이미지 원본 (딥러닝 활용).
대용량 파일 저장


ai_analysis_result (Object)
AI 모델의 분석 결과 (food + accuracy).
유연한 구조


youtube_recipes (Array of Objects)
구글 API를 통해 검색된 레시피 링크 배열.
유연한 데이터 구조


food_reference_id (ObjectID Ref.)
FoodReference 컬렉션을 참조하는 키.
FK to MongoDB
FoodReference
_id (ObjectID)
AnalysisResults.food_reference_id가 참조함.
PK in Mongo


nutrition_data (Object)
직접 입력되는 영양소 및 칼로리 정보.
정적 데이터

4. 백엔드 아키텍처 및 구현 계획
4.1. 백엔드 폴더/패키지 상세 구조 (Spring Boot 기반)
패키지/파일
계층
상세 파일 및 역할
com.busanit501.__team_back
Root
프로젝트 최상위 패키지
├── config
설정
OkHttpConfig.java, SecurityConfig.java, SocialSecurityConfig.java
├── controller
웹 진입점
AuthController.java, AnalysisController.java, MypageController.java
├── domain
DB 모델
maria (Users, SearchSessions), mongo (AnalysisResults, ProfileImages, FoodReference)
├── dto
DTO
analysis DTO, user DTO
├── security
보안
CustomOAuth2UserService.java (소셜 로그인 사용자 처리)
└── service
비즈니스 로직
ai, auth, social, user, search (SearchSessionService.java - 핵심 조인)

4.2. 핵심 데이터 플로우: '최근 검색 기록 조회' (3단계 하이브리드 조인)
service/search/SearchSessionService.java에서 하이브리드 조인 로직을 구현합니다.
MariaDB 1차 쿼리: Users.user_id를 사용하여 SearchSessions에서 최신 analysis_result_id 목록 5개를 확보.
MongoDB 2차 쿼리: 확보한 ID 목록으로 AnalysisResults 컬렉션에서 AI 결과, 유튜브 링크, food_reference_id를 확보.
MongoDB 3차 쿼리: 확보한 food_reference_id로 FoodReference에서 최종 영양소/칼로리 정보를 확보하여 통합 후 클라이언트에 응답.
5. 4인 팀 프로젝트 역할 분담 계획 

담당 팀원
주요 책임 영역
담당 DB 엔티티
핵심 백엔드 파일 책임
추교문
데이터베이스 기본 설계 및 관리
MariaDB: Users, SearchSessions (DB 구현 중심)
domain/maria/* (DB 모델링 및 리포지토리), SearchSessionService 구현 지원
유영준
AI 연동 및 대용량 이미지 관리
MongoDB: ProfileImages, AnalysisResults
AnalysisController.java, AIAnalysisService.java, domain/mongo/* 모델/리포지토리
김무진
정적 데이터 및 외부 API 연동
MongoDB: FoodReference
YoutubeApiService.java, FoodReference.java 모델/리포지토리, dto/analysis
류성원
소셜 인증 지원 및 DTO/보안 보조
MariaDB: Users (인증 흐름 보조)
SocialController.java, security/service/*, 모든 dto/* 관리
(선행 작업후 
  후반 작업)
핵심 통합 및 인증/보안 총괄
-
추후 최종 통합 및 코드 리뷰, 보안 검토 담당


6. 결론 및 향후 확장 계획
이 아키텍처는 데이터의 무결성(MariaDB)과 유연한 처리 능력(MongoDB)을 동시에 확보하여 프로젝트의 핵심 목표를 달성할 수 있는 최적의 기반을 제공합니다. 향후, 영양소 데이터의 자동 크롤링 모듈 추가나 추천 알고리즘 강화 등의 기능을 Flask 또는 별도의 마이크로 서비스로 쉽게 확장할 수 있습니다.
