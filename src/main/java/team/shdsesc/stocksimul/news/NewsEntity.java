package team.shdsesc.stocksimul.news;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "news")
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NewsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "news_id")
    private Long newsId;

    @Column(name = "stock_id")
    private String stockId;
    private String source;

    @Column(name = "time_published")
    private LocalDateTime timePublished;

    private String title;
    private String url;


    private String image;

    private String summary;
}
