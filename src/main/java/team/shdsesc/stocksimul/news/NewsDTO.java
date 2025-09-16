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
    private Long newsId;
    private String stockId;
    private LocalDateTime timePublished;
    private String source; //출판사
    private String title;
    private String url;
    private String  image;
    private String summary;


}
