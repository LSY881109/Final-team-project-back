package com.busanit501.__team_back.service;

import com.busanit501.__team_back.entity.MongoDB.ProfileImage;
import com.busanit501.__team_back.repository.mongo.ProfileImageRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.Binary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProfileImageService {

    private final ProfileImageRepository profileImageRepository;

    /** 자체 로그인: 업로드 파일 저장 후 Mongo 문서 ID 반환 */
    public String saveFromUpload(MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) return null;
            ProfileImage doc = new ProfileImage();
            doc.setImageData(new Binary(file.getBytes()));
            doc.setContentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
            doc.setImageUrl(null);
            doc = profileImageRepository.save(doc);
            return doc.getId();
        } catch (Exception e) {
            throw new RuntimeException("프로필 이미지 저장 실패", e);
        }
    }

    /** 소셜: URL로부터 다운로드 저장(동일 URL 있으면 재사용) */
    public ProfileImage saveFromRemoteUrl(String imageUrl) {
        try {
            Optional<ProfileImage> existed = profileImageRepository.findTopByImageUrl(imageUrl);
            if (existed.isPresent()) return existed.get();
            DownloadResult dl = download(imageUrl);
            ProfileImage doc = new ProfileImage();
            doc.setImageData(new Binary(dl.bytes()));
            doc.setContentType(dl.contentType() != null ? dl.contentType() : "application/octet-stream");
            doc.setImageUrl(imageUrl);
            return profileImageRepository.save(doc);
        } catch (Exception e) {
            throw new RuntimeException("원격 프로필 이미지 저장 실패: " + imageUrl, e);
        }
    }

    /** 기존 문서 덮어쓰기 또는 신규 생성(문서 id 유지해야 할 때) */
    public ProfileImage overwriteOrCreateWithUrl(String existingIdOrNull, String newImageUrl) {
        DownloadResult dl = download(newImageUrl);
        ProfileImage doc = (existingIdOrNull != null)
                ? profileImageRepository.findById(existingIdOrNull).orElse(new ProfileImage())
                : new ProfileImage();
        doc.setImageData(new Binary(dl.bytes()));
        doc.setContentType(dl.contentType() != null ? dl.contentType() : "application/octet-stream");
        doc.setImageUrl(newImageUrl);
        return profileImageRepository.save(doc);
    }

    public Optional<ProfileImage> findById(String id) {
        return profileImageRepository.findById(id);
    }

    private record DownloadResult(byte[] bytes, String contentType) {}

    private DownloadResult download(String url) {
        try {
            HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
            conn.setConnectTimeout((int) Duration.ofSeconds(8).toMillis());
            conn.setReadTimeout((int) Duration.ofSeconds(12).toMillis());
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.connect();
            String contentType = conn.getContentType();
            try (InputStream is = conn.getInputStream(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                byte[] buf = new byte[8192];
                int r;
                while ((r = is.read(buf)) != -1) bos.write(buf, 0, r);
                return new DownloadResult(bos.toByteArray(), contentType);
            } finally {
                conn.disconnect();
            }
        } catch (Exception e) {
            throw new RuntimeException("이미지 다운로드 실패: " + url, e);
        }
    }
}

