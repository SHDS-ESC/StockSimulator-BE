package team.shdsesc.stocksimul.news;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "alphavantage_news")
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NewsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ticker;
    private String source;

    @Column(name = "time_published")
    private LocalDateTime timePublished;

    private String title;
    private String url;

    @Column(name = "banner_image")
    private String bannerImage;

    private String summary;

    @Column(name = "topic_primary")
    private String topicPrimary;

    @Column(name = "topic_secondary")
    private String topicSecondary;
}
