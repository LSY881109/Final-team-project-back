package com.busanit501.__team_back.service.user;

import com.busanit501.__team_back.entity.MongoDB.ProfileImage;
import com.busanit501.__team_back.repository.mongo.ProfileImageRepository;
import com.busanit501.__team_back.service.FileService;
import lombok.RequiredArgsConstructor;
import org.bson.types.Binary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final ProfileImageRepository profileImageRepository;

    @Override
    public String storeImage(MultipartFile file) throws IOException {
        ProfileImage profileImage = new ProfileImage();
        profileImage.setImageData(new Binary(file.getBytes()));
        profileImage.setContentType(file.getContentType());

        ProfileImage savedImage = profileImageRepository.save(profileImage);
        return savedImage.getId();
    }

    @Override
    public byte[] getImage(String id) {
        return profileImageRepository.findById(id)
                .map(ProfileImage::getImageData)
                .map(Binary::getData)
                .orElse(null);
    }
}