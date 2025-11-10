package com.busanit501.__team_back.controller;

import com.busanit501.__team_back.service.ProfileImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/images/profile")
public class ProfileImageController {

    private final ProfileImageService profileImageService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getImage(@PathVariable String id) {
        return profileImageService.findById(id)
                .map(doc -> {
                    try {
                        // 1) 이미지 바이너리가 없고, URL만 있는 소셜 문서라면 즉시 채워 넣기
                        boolean noBinary = (doc.getImageData() == null
                                || doc.getImageData().getData() == null
                                || doc.getImageData().getData().length == 0);
                        if (noBinary) {
                            String url = doc.getImageUrl();
                            if (url == null || url.isBlank()) {
                                return ResponseEntity.notFound().build();
                            }
                            // 같은 문서 id로 덮어쓰기(문서 id 유지)
                            doc = profileImageService.overwriteOrCreateWithUrl(id, url);
                        }

                        String contentType = doc.getContentType() != null ? doc.getContentType() : "application/octet-stream";
                        byte[] bytes = doc.getImageData().getData();
                        if (bytes == null || bytes.length == 0) {
                            return ResponseEntity.notFound().build();
                        }
                        return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_TYPE, contentType)
                                .body(bytes);
                    } catch (Exception e) {
                        // URL 다운로드 실패 등 예외 시 404로 처리(내부 정보 노출 방지)
                        return ResponseEntity.notFound().build();
                    }
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
