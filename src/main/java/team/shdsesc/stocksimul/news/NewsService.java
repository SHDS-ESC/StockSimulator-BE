package team.shdsesc.stocksimul.news;


import team.shdsesc.stocksimul.dto.PageRequestDTO;
import team.shdsesc.stocksimul.dto.PageResultDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface NewsService {
    PageResultDTO<NewsDTO, NewsEntity> getList(PageRequestDTO requestDTO);
    long getTotalCount();

    //특정 날짜의 뉴스 조회 메서드
    PageResultDTO<NewsDTO, NewsEntity> getNewsByUser(Long userProfileId, PageRequestDTO requestDTO);
    
    //날짜 파라미터로 직접 뉴스 조회 메서드
    PageResultDTO<NewsDTO, NewsEntity> getNewsByDate(String processDate, PageRequestDTO requestDTO);

    // 크롤링된 뉴스 저장
    void saveCrawledNews();

    // 중복 뉴스 체크
    boolean isDuplicateNews(String title, String url);

    
    default Map<String, Object> dtoToEntity(NewsDTO newsDTO){
        Map<String, Object> entityMap = new HashMap<>();
        NewsEntity news = NewsEntity.builder()
                .newsId(newsDTO.getNewsId())
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
                .source(newsEntity.getSource())
                .timePublished(newsEntity.getTimePublished())
                .title(newsEntity.getTitle())
                .url(newsEntity.getUrl())
                .image(newsEntity.getImage())
                .summary(newsEntity.getSummary())
                .sector(newsEntity.getSector())
                .industry(newsEntity.getIndustry())
                .topic(newsEntity.getTopic())
                .build();
        return newsDTO;

    }
}
