package com.busanit501.__team_back.dto.analysis;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class YoutubeRecipeDTO {

    private String title; // 영상 제목
    private String thumbnailUrl; // 썸네일 이미지 URL
    private String videoId; // 영상 ID
    private String description; // 영상 설명
}
