package team.shdsesc.stocksimul.news;


import team.shdsesc.stocksimul.dto.PageRequestDTO;
import team.shdsesc.stocksimul.dto.PageResultDTO;

import java.util.HashMap;
import java.util.Map;

public interface NewsService {
    PageResultDTO<NewsDTO, NewsEntity> getList(PageRequestDTO requestDTO);
    long getTotalCount();

    default Map<String, Object> dtoToEntity(NewsDTO newsDTO){
        Map<String, Object> entityMap = new HashMap<>();
        NewsEntity news = NewsEntity.builder()
                .id(newsDTO.getId())
                .ticker(newsDTO.getTicker())
                .source(newsDTO.getSource())
                .timePublished(newsDTO.getTimePublished())
                .title(newsDTO.getTitle())
                .url(newsDTO.getUrl())
                .bannerImage(newsDTO.getBannerImage())
                .build();
        entityMap.put("news", news);
        return entityMap;
    }

    default NewsDTO entitiesToDTO(NewsEntity newsEntity){
        NewsDTO newsDTO = NewsDTO.builder()
                .id(newsEntity.getId())
                .ticker(newsEntity.getTicker())
                .source(newsEntity.getSource())
                .timePublished(newsEntity.getTimePublished())
                .title(newsEntity.getTitle())
                .url(newsEntity.getUrl())
                .bannerImage(newsEntity.getBannerImage())
                .build();
        return newsDTO;

    }
}
