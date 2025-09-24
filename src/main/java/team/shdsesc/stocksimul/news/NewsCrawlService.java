package team.shdsesc.stocksimul.news;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Log4j2
public class NewsCrawlService {
    private static String News_URL = "https://finance.daum.net/global/news";

    public List<NewsDTO> getNewsDatas() throws IOException {
        log.info("크롤링 시작 - 5페이지까지 크롤링 예정");
        List<NewsDTO> allNewsList = new ArrayList<>();
        WebDriver driver = null;
        
        try {
            // WebDriverManager로 ChromeDriver 자동 설정
            WebDriverManager.chromedriver().setup();
            
            // Selenium WebDriver 설정
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless"); // 브라우저 창을 띄우지 않음
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            
            driver = new ChromeDriver(options);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            
            log.info("Selenium WebDriver 시작");
            
            // 1페이지부터 5페이지까지 크롤링
            for (int page = 1; page <= 5; page++) {
                try {
                    String pageUrl = getPageUrl(page);
                    log.info("페이지 {} 크롤링 시작 - URL: {}", page, pageUrl);
                    
                    // 페이지 로드
                    driver.get(pageUrl);
                    log.info("페이지 {} 로드 완료 - 제목: {}", page, driver.getTitle());
                    
                    // JavaScript 실행 대기 (뉴스 목록이 로드될 때까지)
                    Thread.sleep(3000); // 3초 대기
                    
                    // 페이지 소스 가져오기
                    String pageSource = driver.getPageSource();
                    log.info("페이지 {} 소스 길이: {} 문자", page, pageSource.length());
                    
                    // Jsoup으로 파싱
                    Document doc = Jsoup.parse(pageSource);
                    
                    // 실제 HTML 구조에 맞는 셀렉터 사용
                    Elements contents = doc.select("div.box_contents div.tableB ul.listW li.imgB");
                    log.info("페이지 {} - 실제 구조 셀렉터로 찾은 요소 개수: {}", page, contents.size());
                    
                    // 대안 셀렉터들도 시도
                    if (contents.isEmpty()) {
                        log.warn("페이지 {} - 실제 구조 셀렉터로 요소를 찾지 못함. 대안 셀렉터 시도...", page);
                        
                        String[] selectors = {
                            "ul.listW li.imgB",
                            ".listW li.imgB", 
                            "div.tableB li.imgB",
                            "ul.listW li",
                            ".listW li",
                            "div.tableB li"
                        };
                        
                        for (String selector : selectors) {
                            Elements altContents = doc.select(selector);
                            log.info("페이지 {} - 셀렉터 '{}'로 찾은 요소 개수: {}", page, selector, altContents.size());
                            if (!altContents.isEmpty()) {
                                contents = altContents;
                                break;
                            }
                        }
                    }
                    
                    if (contents.isEmpty()) {
                        log.warn("페이지 {} - 모든 셀렉터로 요소를 찾지 못함. 다음 페이지로 이동", page);
                        continue;
                    }

                    // 현재 페이지의 뉴스 파싱
                    List<NewsDTO> pageNewsList = parseNewsFromElements(contents, page);
                    allNewsList.addAll(pageNewsList);
                    
                    log.info("페이지 {} 크롤링 완료 - {} 개 뉴스 수집", page, pageNewsList.size());
                    
                    // 페이지 간 대기 (서버 부하 방지)
                    if (page < 5) {
                        Thread.sleep(2000); // 2초 대기
                    }
                    
                } catch (Exception e) {
                    log.error("페이지 {} 크롤링 중 오류 발생: {}", page, e.getMessage());
                    // 개별 페이지 오류는 전체 크롤링을 중단하지 않음
                    continue;
                }
            }
            
            log.info("전체 크롤링 완료 - 총 {} 개 뉴스 수집 (5페이지)", allNewsList.size());
            
        } catch (Exception e) {
            log.error("크롤링 중 오류 발생: {}", e.getMessage(), e);
            throw new IOException("크롤링 실패", e);
        } finally {
            if (driver != null) {
                driver.quit();
                log.info("Selenium WebDriver 종료");
            }
        }
        
        return allNewsList;
    }
    
    /**
     * 페이지 번호에 따른 URL 생성
     */
    private String getPageUrl(int page) {
        if (page == 1) {
            return "https://finance.daum.net/global/news";
        } else {
            return "https://finance.daum.net/global/news#?page=" + page;
        }
    }
    
    /**
     * HTML 요소들에서 뉴스 데이터 파싱
     */
    private List<NewsDTO> parseNewsFromElements(Elements contents, int page) {
        List<NewsDTO> pageNewsList = new ArrayList<>();
        
        for(Element content : contents){
            try {
                // 실제 HTML 구조에 맞게 셀렉터 수정
                String title = content.select("span a.tit").text();
                String summary = content.select("span a.txt").text();
                String dateText = content.select("span p.date").text();
                String url = content.select("span a.tit").attr("abs:href");
                String image = content.select("a.thumb img.news_img").attr("abs:src");
                
                log.debug("페이지 {} - 뉴스 파싱 - 제목: {}, 요약: {}, 날짜: {}", page, title, summary, dateText);
                
                if (title.isEmpty()) {
                    log.warn("페이지 {} - 제목이 비어있는 요소 건너뜀", page);
                    continue;
                }
                
                String source = "";
                LocalDateTime timePublished = null;
                
                if (!dateText.isEmpty() && dateText.contains("·")) {
                    String[] dateParts = dateText.split("·");
                    if (dateParts.length >= 2) {
                        source = dateParts[0].trim();
                        String dateStr = dateParts[1].trim();
                        timePublished = parseDateString(dateStr);
                    }
                }
                
                NewsDTO news = NewsDTO.builder()
                        .image(image)
                        .title(title)
                        .summary(summary)
                        .timePublished(timePublished)
                        .url(url)
                        .source(source)
                        .sector(null)
                        .industry(null)
                        .topic(null)
                        .build();
                pageNewsList.add(news);
                
                log.debug("페이지 {} - 뉴스 추가 완료 - 제목: {}, 출처: {}, 날짜: {}", page, title, source, timePublished);
                
            } catch (Exception e) {
                log.error("페이지 {} - 뉴스 파싱 중 오류 발생: {}", page, e.getMessage());
            }
        }
        
        return pageNewsList;
    }
    
    /**
     * 날짜 문자열을 LocalDateTime으로 변환
     * "09.23" -> "2025-09-23T00:00:00"
     */
    private LocalDateTime parseDateString(String dateStr) {
        try {
            // "09.23" 형태의 날짜를 파싱
            if (dateStr.matches("\\d{2}\\.\\d{2}")) {
                String[] parts = dateStr.split("\\.");
                int month = Integer.parseInt(parts[0]);
                int day = Integer.parseInt(parts[1]);
                
                // 현재 연도 사용
                int currentYear = LocalDateTime.now().getYear();
                
                // 현재 시간으로 설정 (크롤링 시간)
                LocalDateTime now = LocalDateTime.now();
                
                return LocalDateTime.of(currentYear, month, day, 
                                      now.getHour(), now.getMinute(), now.getSecond());
            }
            
            // 다른 형태의 날짜가 있다면 여기에 추가
            log.warn("알 수 없는 날짜 형식: {}", dateStr);
            return LocalDateTime.now(); // 기본값으로 현재 시간 사용
            
        } catch (Exception e) {
            log.error("날짜 파싱 오류: {}", e.getMessage());
            return LocalDateTime.now(); // 오류 시 현재 시간 사용
        }
    }
}
