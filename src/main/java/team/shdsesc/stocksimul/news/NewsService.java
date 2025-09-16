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
                .newsId(newsDTO.getNewsId())
                .stockId(newsDTO.getStockId())
                .source(newsDTO.getSource())
                .timePublished(newsDTO.getTimePublished())
                .title(newsDTO.getTitle())
                .url(newsDTO.getUrl())
                .image(newsDTO.getImage())
                .build();
        entityMap.put("news", news);
        return entityMap;
    }

    default NewsDTO entitiesToDTO(NewsEntity newsEntity){
        NewsDTO newsDTO = NewsDTO.builder()
                .newsId(newsEntity.getNewsId())
                .stockId(newsEntity.getStockId())
                .source(newsEntity.getSource())
                .timePublished(newsEntity.getTimePublished())
                .title(newsEntity.getTitle())
                .url(newsEntity.getUrl())
                .image(newsEntity.getImage())
                .build();
        return newsDTO;

    }
}
