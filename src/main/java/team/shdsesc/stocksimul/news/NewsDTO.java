package team.shdsesc.stocksimul.news;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsDTO {
    private Long id;
    private String ticker;
    private LocalDateTime timePublished;
    private String source; //출판사
    private String title;
    private String url;
    private String bannerImage;
    private String summary;
    private String topicPrimary;
    private String topicSecondary;


}
