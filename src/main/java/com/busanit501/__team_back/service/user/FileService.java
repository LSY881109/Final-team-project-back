package com.busanit501.__team_back.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface FileService {

    // 이미지를 MongoDB에 저장하고, 해당 Document의 ID를 반환
    String storeImage(MultipartFile file) throws IOException;

    // 이미지 ID로 이미지 데이터(바이너리)를 조회
    byte[] getImage(String id);
}