package com.busanit501.__team_back.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

//작업 순서7
@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class APIUser {

    @Id // Primary Key 설정
    private String mid; // 사용자 ID

    private String mpw; // 사용자 비밀번호

    // 몽고디비 추가
    // 몽고디비에 저장된 프로필 이미지의 File ID
    private String profileImg;

    // 비밀번호 변경 메서드
    public void changePw(String mpw) {
        this.mpw = mpw;
    }
    public void changeProfileImg(String profileImg) {
        this.profileImg = profileImg;
    }
}