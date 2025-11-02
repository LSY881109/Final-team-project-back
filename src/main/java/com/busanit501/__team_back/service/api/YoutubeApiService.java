package com.busanit501.__team_back.service.api;

import com.busanit501.__team_back.dto.analysis.YoutubeRecipeDTO;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class YoutubeApiService {

    /**
     * 음식 이름으로 YouTube 레시피를 검색합니다.
     * (현재는 뼈대만 있으며, 실제 API 호출 로직은 추후 구현합니다.)
     * @param foodName 검색할 음식 이름
     * @return YoutubeRecipeDTO 리스트 (현재는 빈 리스트)
     */
    public List<YoutubeRecipeDTO> searchRecipes(String foodName) {
        // TODO: YouTube Data API 의존성 및 API 키 추가 후 실제 API 호출 로직 구현 필요
        
        // 임시로 비어있는 리스트를 반환
        return Collections.emptyList();
    }
}
