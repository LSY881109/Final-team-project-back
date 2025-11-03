package com.busanit501.__team_back.controller;

import com.busanit501.__team_back.dto.analysis.FoodAnalysisResultDTO;
import com.busanit501.__team_back.service.ai.AIAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.security.Principal; // 현재 인증된 사용자 정보를 가져오기 위해 사용

/**
 * 음식 이미지 분석 및 관련 정보 처리를 위한 API 컨트롤러
 * 기본 경로: /api/v1/analysis
 */
@RestController
@RequestMapping("/api/v1/analysis")
@RequiredArgsConstructor
@Log4j2
public class AnalysisController {

    private final AIAnalysisService aiAnalysisService;

    /**
     * [POST] /api/v1/analysis/upload
     * 클라이언트로부터 음식 이미지를 받아 AI 분석 서버에 전달하고 결과를 반환합니다.
     * * @param imageFile 클라이언트가 업로드한 이미지 파일
     * @param principal JWT 토큰으로 인증된 사용자 정보
     * @return AI 분석 및 추가 정보가 포함된 DTO
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FoodAnalysisResultDTO> uploadAndAnalyzeImage(
            @RequestPart("imageFile") MultipartFile imageFile,
            Principal principal // JWT를 통해 인증된 사용자 정보 (username)가 담겨 있음
    ) {
        // 1. JWT 인증 확인 및 로그
        String username = principal.getName();
        log.info("인증된 사용자({})가 이미지 분석 요청을 시작했습니다. 파일명: {}", username, imageFile.getOriginalFilename());

        // 2. 파일 유효성 검사 (간단한 예시)
        if (imageFile.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    FoodAnalysisResultDTO.builder()
                            .message("업로드된 이미지 파일이 비어 있습니다.")
                            .build()
            );
        }

        try {
            // 3. AI 분석 서비스 호출 (Flask 서버 통신)
            FoodAnalysisResultDTO resultDTO = aiAnalysisService.analyzeImage(imageFile);

            // 4. (선택적) MongoDB 등 DB에 분석 요청/결과 기록 로직 추가 가능

            log.info("분석 완료. 인식된 음식: {}", resultDTO.getFoodName());

            // 5. 성공 응답 반환 (HTTP 200 OK)
            return ResponseEntity.ok(resultDTO);

        } catch (Exception e) {
            log.error("이미지 분석 및 Flask 통신 중 예외 발생", e);

            // 6. 예외 응답 반환 (HTTP 500 Internal Server Error)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    FoodAnalysisResultDTO.builder()
                            .message("이미지 분석 처리 중 서버 오류가 발생했습니다: " + e.getMessage())
                            .foodName("N/A")
                            .build()
            );
        }
    }
}