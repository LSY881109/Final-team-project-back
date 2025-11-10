package com.busanit501.__team_back.controller;

import com.busanit501.__team_back.dto.analysis.AnalysisHistoryDTO;
import com.busanit501.__team_back.dto.analysis.FoodAnalysisResultDTO;
import com.busanit501.__team_back.entity.MariaDB.User;
import com.busanit501.__team_back.entity.MongoDB.AnalysisHistory;
import com.busanit501.__team_back.repository.maria.UserRepository;
import com.busanit501.__team_back.repository.mongo.AnalysisHistoryRepository;
import com.busanit501.__team_back.service.user.AnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
@Log4j2
@CrossOrigin(origins = "*") // CORS 허용 (개발 환경)
public class AnalysisController {

    private final AnalysisService analysisService;
    private final AnalysisHistoryRepository analysisHistoryRepository;
    private final UserRepository userRepository;
    
    /**
     * SecurityContext에서 현재 로그인한 사용자의 userId (String)를 추출하고,
     * 이를 User.id (Long)로 변환합니다.
     * @return User.id (Long), 인증되지 않은 경우 null
     */
    private Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                log.warn("인증되지 않은 사용자입니다.");
                return null;
            }
            
            // Authentication의 principal에서 userId (String) 추출
            Object principal = authentication.getPrincipal();
            String userIdString = null;
            
            if (principal instanceof UserDetails) {
                userIdString = ((UserDetails) principal).getUsername();
            } else if (principal instanceof String) {
                userIdString = (String) principal;
            }
            
            if (userIdString == null || userIdString.isEmpty()) {
                log.warn("userId를 추출할 수 없습니다.");
                return null;
            }
            
            // userId (String)로 User를 찾아서 User.id (Long) 반환
            Optional<User> userOpt = userRepository.findByUserId(userIdString);
            if (userOpt.isEmpty()) {
                log.warn("사용자를 찾을 수 없습니다. userId: {}", userIdString);
                return null;
            }
            
            Long userId = userOpt.get().getId();
            log.info("현재 로그인한 사용자 ID: {} (userId: {})", userId, userIdString);
            return userId;
        } catch (Exception e) {
            log.error("사용자 ID 추출 중 오류 발생", e);
            return null;
        }
    }

    // 이미지 분석 요청 처리
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FoodAnalysisResultDTO> analyzeImage(
            // TODO: Security 적용 후 @AuthenticationPrincipal 로 실제 로그인 사용자 ID를 가져와야 합니다.
            // 우선 테스트를 위해 userId를 요청 파라미터로 받습니다.
            @RequestParam("userId") Long userId,
            @RequestParam("image") MultipartFile imageFile,
            @RequestParam(value = "youtubeKeyword", required = false) String youtubeKeyword,
            @RequestParam(value = "youtubeOrder", required = false, defaultValue = "relevance") String youtubeOrder) {

        log.info("이미지 분석 요청 수신 - 사용자 ID: {}, 파일명: {}, YouTube 키워드: {}, 정렬: {}", 
                userId, imageFile.getOriginalFilename(), youtubeKeyword, youtubeOrder);

        // 파일 유효성 검증
        if (imageFile.isEmpty()) {
            return ResponseEntity.badRequest().body(
                FoodAnalysisResultDTO.builder()
                    .message("이미지 파일이 비어있습니다.")
                    .build()
            );
        }

        // 파일 타입 검증 (이미지 파일만 허용)
        String contentType = imageFile.getContentType();
        String originalFilename = imageFile.getOriginalFilename();
        
        // Content-Type과 파일 확장자 모두 확인
        boolean isImageByContentType = contentType != null && contentType.startsWith("image/");
        boolean isImageByExtension = originalFilename != null && 
            (originalFilename.toLowerCase().endsWith(".jpg") || 
             originalFilename.toLowerCase().endsWith(".jpeg") ||
             originalFilename.toLowerCase().endsWith(".png") ||
             originalFilename.toLowerCase().endsWith(".gif") ||
             originalFilename.toLowerCase().endsWith(".webp"));
        
        if (!isImageByContentType && !isImageByExtension) {
            return ResponseEntity.badRequest().body(
                FoodAnalysisResultDTO.builder()
                    .message("이미지 파일만 업로드 가능합니다. 현재 파일 타입: " + contentType + ", 파일명: " + originalFilename)
                    .build()
            );
        }

        // 파일 크기 검증 (10MB 제한)
        long maxSize = 10 * 1024 * 1024; // 10MB
        if (imageFile.getSize() > maxSize) {
            return ResponseEntity.badRequest().body(
                FoodAnalysisResultDTO.builder()
                    .message("파일 크기가 너무 큽니다. 최대 10MB까지 업로드 가능합니다.")
                    .build()
            );
        }

        try {
            FoodAnalysisResultDTO result;
            if (youtubeKeyword != null && !youtubeKeyword.trim().isEmpty()) {
                // YouTube 옵션이 있는 경우
                result = analysisService.analyzeImage(userId, imageFile, youtubeKeyword, youtubeOrder);
            } else {
                // YouTube 옵션이 없는 경우 기본 메서드 호출
                result = analysisService.analyzeImage(userId, imageFile);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("이미지 분석 중 서버 내부 오류 발생", e);
            return ResponseEntity.internalServerError().body(
                FoodAnalysisResultDTO.builder()
                    .message("이미지 분석 중 오류가 발생했습니다: " + e.getMessage())
                    .build()
            );
        }
    }

    /**
     * 사용자의 분석 히스토리 조회
     * JWT 토큰에서 현재 로그인한 사용자 ID를 자동으로 추출합니다.
     * @param page 페이지 번호 (기본값: 0)
     * @param size 페이지 크기 (기본값: 10)
     * @return 분석 히스토리 목록
     */
    @GetMapping("/history")
    public ResponseEntity<List<AnalysisHistoryDTO>> getAnalysisHistory(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        
        // JWT 토큰에서 현재 로그인한 사용자 ID 추출
        Long userId = getCurrentUserId();
        if (userId == null) {
            log.warn("인증되지 않은 사용자의 히스토리 조회 요청");
            return ResponseEntity.status(401).build();
        }
        
        log.info("분석 히스토리 조회 요청 - 사용자 ID: {}, 페이지: {}, 크기: {}", userId, page, size);
        
        try {
            List<AnalysisHistoryDTO> history = analysisService.getAnalysisHistory(userId, page, size);
            log.info("분석 히스토리 조회 완료 - {}개", history.size());
            
            // 각 히스토리의 YouTube 레시피 개수 확인
            for (AnalysisHistoryDTO dto : history) {
                log.info("📦 컨트롤러 응답 - 히스토리 ID: {}, 음식: {}, 레시피 개수: {}", 
                    dto.getHistoryId(), 
                    dto.getRecognizedFoodName(),
                    dto.getYoutubeRecipes() != null ? dto.getYoutubeRecipes().size() : 0);
                if (dto.getYoutubeRecipes() != null && !dto.getYoutubeRecipes().isEmpty()) {
                    dto.getYoutubeRecipes().forEach(recipe -> {
                        log.info("   레시피: {} - {}", recipe.getTitle(), recipe.getUrl());
                    });
                }
            }
            
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("분석 히스토리 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * YouTube 레시피 클릭 시 저장
     * JWT 토큰에서 현재 로그인한 사용자 ID를 자동으로 추출합니다.
     * @param historyId 분석 이력 ID
     * @param title YouTube 영상 제목
     * @param url YouTube 영상 URL
     * @return 성공 여부
     */
    @PostMapping("/youtube-recipe/click")
    public ResponseEntity<Void> saveClickedYouTubeRecipe(
            @RequestParam("historyId") String historyId,
            @RequestParam("title") String title,
            @RequestParam("url") String url) {
        
        // JWT 토큰에서 현재 로그인한 사용자 ID 추출
        Long userId = getCurrentUserId();
        if (userId == null) {
            log.warn("인증되지 않은 사용자의 YouTube 레시피 저장 요청");
            return ResponseEntity.status(401).build();
        }
        
        log.info("YouTube 레시피 클릭 저장 요청 - 사용자 ID: {}, 히스토리 ID: {}, 제목: {}", userId, historyId, title);
        
        try {
            analysisService.saveClickedYouTubeRecipe(userId, historyId, title, url);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("YouTube 레시피 저장 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 썸네일 이미지 조회
     * @param historyId 분석 히스토리 ID
     * @return 썸네일 이미지 바이너리
     */
    @GetMapping(value = "/thumbnail/{historyId}", produces = {MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE, MediaType.IMAGE_GIF_VALUE, "image/webp"})
    @CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.OPTIONS})
    public ResponseEntity<byte[]> getThumbnail(@PathVariable("historyId") String historyId) {
        log.info("썸네일 이미지 조회 요청 - History ID: {}", historyId);
        
        try {
            Optional<AnalysisHistory> history = analysisHistoryRepository.findById(historyId);
            
            if (history.isEmpty()) {
                log.warn("히스토리를 찾을 수 없음 - History ID: {}", historyId);
                return ResponseEntity.notFound().build();
            }
            
            AnalysisHistory historyEntity = history.get();
            
            if (historyEntity.getThumbnailImageData() == null) {
                log.warn("썸네일 이미지 데이터가 없음 - History ID: {}", historyId);
                return ResponseEntity.notFound().build();
            }
            
            // Binary 객체에서 데이터 추출 시 예외 처리
            byte[] imageData;
            try {
                imageData = historyEntity.getThumbnailImageData().getData();
            } catch (Exception e) {
                log.error("Binary 데이터 추출 실패 - History ID: {}, 에러: {}", historyId, e.getMessage(), e);
                return ResponseEntity.notFound().build();
            }
            
            if (imageData == null || imageData.length == 0) {
                log.warn("썸네일 이미지 데이터가 비어있음 - History ID: {}", historyId);
                return ResponseEntity.notFound().build();
            }
            
            // 이미지 데이터 유효성 검증 (JPEG, PNG 시그니처 확인)
            String detectedContentType = detectImageType(imageData);
            
            // 이미지 타입이 감지되지 않으면 데이터가 손상되었을 가능성
            // 하지만 detectedContentType은 항상 기본값을 반환하므로 null 체크는 불필요
            if (detectedContentType.equals("image/jpeg") && !isValidJpeg(imageData)) {
                log.error("썸네일 이미지 데이터가 유효하지 않음 (JPEG 시그니처 불일치) - History ID: {}, 크기: {} bytes, 첫 바이트: {}", 
                    historyId, imageData.length, 
                    imageData.length > 0 ? String.format("%02X %02X %02X %02X", 
                        imageData[0] & 0xFF, 
                        imageData.length > 1 ? imageData[1] & 0xFF : 0,
                        imageData.length > 2 ? imageData[2] & 0xFF : 0,
                        imageData.length > 3 ? imageData[3] & 0xFF : 0) : "N/A");
                return ResponseEntity.notFound().build();
            }
            
            String contentType = historyEntity.getThumbnailContentType() != null 
                ? historyEntity.getThumbnailContentType() 
                : detectedContentType;
            
            // Content-Type이 감지되지 않으면 기본값 사용
            if (contentType == null || contentType.isEmpty()) {
                contentType = detectedContentType;
            }
            
            log.info("썸네일 이미지 반환 성공 - History ID: {}, 크기: {} bytes, Content-Type: {}, 감지된 타입: {}", 
                historyId, imageData.length, contentType, detectedContentType);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Methods", "GET, OPTIONS")
                    .header("Access-Control-Allow-Headers", "*")
                    .header("Cache-Control", "public, max-age=3600")
                    .body(imageData);
        } catch (IllegalArgumentException e) {
            log.error("잘못된 파라미터 - History ID: {}, 에러: {}", historyId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("썸네일 이미지 조회 중 예상치 못한 오류 발생 - History ID: {}", historyId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 이미지 바이트 배열의 첫 몇 바이트를 확인하여 이미지 타입을 감지
     * @param imageData 이미지 바이트 배열
     * @return Content-Type (예: "image/jpeg", "image/png")
     */
    private String detectImageType(byte[] imageData) {
        if (imageData == null || imageData.length < 4) {
            return "image/jpeg"; // 기본값
        }
        
        // JPEG 시그니처: FF D8 FF
        if (imageData.length >= 3 && 
            (imageData[0] & 0xFF) == 0xFF && 
            (imageData[1] & 0xFF) == 0xD8 && 
            (imageData[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        
        // PNG 시그니처: 89 50 4E 47
        if (imageData.length >= 4 && 
            (imageData[0] & 0xFF) == 0x89 && 
            (imageData[1] & 0xFF) == 0x50 && 
            (imageData[2] & 0xFF) == 0x4E && 
            (imageData[3] & 0xFF) == 0x47) {
            return "image/png";
        }
        
        // GIF 시그니처: 47 49 46 38
        if (imageData.length >= 4 && 
            imageData[0] == 0x47 && 
            imageData[1] == 0x49 && 
            imageData[2] == 0x46 && 
            imageData[3] == 0x38) {
            return "image/gif";
        }
        
        // WebP 시그니처: RIFF ... WEBP
        if (imageData.length >= 12 && 
            imageData[0] == 0x52 && 
            imageData[1] == 0x49 && 
            imageData[2] == 0x46 && 
            imageData[3] == 0x46 &&
            imageData[8] == 0x57 && 
            imageData[9] == 0x45 && 
            imageData[10] == 0x42 && 
            imageData[11] == 0x50) {
            return "image/webp";
        }
        
        // 기본값: JPEG로 가정
        return "image/jpeg";
    }
    
    /**
     * JPEG 이미지 유효성 검증
     * @param imageData 이미지 바이트 배열
     * @return 유효한 JPEG인지 여부
     */
    private boolean isValidJpeg(byte[] imageData) {
        if (imageData == null || imageData.length < 3) {
            return false;
        }
        // JPEG 시그니처: FF D8 FF
        return (imageData[0] & 0xFF) == 0xFF && 
               (imageData[1] & 0xFF) == 0xD8 && 
               (imageData[2] & 0xFF) == 0xFF;
    }
}

//    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<FoodAnalysisResultDTO> uploadAndAnalyzeImage(
//            @RequestPart("imageFile") MultipartFile imageFile,
//            Principal principal // JWT를 통해 인증된 사용자 정보 (username)가 담겨 있음
//    ) {
//        // 1. JWT 인증 확인 및 로그
//        String username = principal.getName();
//        log.info("인증된 사용자({})가 이미지 분석 요청을 시작했습니다. 파일명: {}", username, imageFile.getOriginalFilename());
//
//        // 2. 파일 유효성 검사 (간단한 예시)
//        if (imageFile.isEmpty()) {
//            return ResponseEntity.badRequest().body(
//                    FoodAnalysisResultDTO.builder()
//                            .message("업로드된 이미지 파일이 비어 있습니다.")
//                            .build()
//            );
//        }
//
//        try {
//            // 3. AI 분석 서비스 호출 (Flask 서버 통신)
//            FoodAnalysisResultDTO resultDTO = aiAnalysisService.analyzeImage(imageFile);
//
//            // 4. (선택적) MongoDB 등 DB에 분석 요청/결과 기록 로직 추가 가능
//
//            log.info("분석 완료. 인식된 음식: {}", resultDTO.getRecognizedFoodName());
//
//            // 5. 성공 응답 반환 (HTTP 200 OK)
//            return ResponseEntity.ok(resultDTO);
//
//        } catch (Exception e) {
//            log.error("이미지 분석 및 Flask 통신 중 예외 발생", e);
//
//            // 6. 예외 응답 반환 (HTTP 500 Internal Server Error)
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
//                    FoodAnalysisResultDTO.builder()
//                            .message("이미지 분석 처리 중 서버 오류가 발생했습니다: " + e.getMessage())
//                            .recognizedFoodName("N/A")
//                            .build()
//            );
//        }
//    }
//}