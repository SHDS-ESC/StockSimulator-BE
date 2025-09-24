package team.shdsesc.stocksimul.news;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import team.shdsesc.stocksimul.dto.PageRequestDTO;
import team.shdsesc.stocksimul.dto.PageResultDTO;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
@Log4j2
@Tag(name = "News", description = "뉴스 관련 API")
public class NewsController {
    private final NewsService newsService;
    private final NewsCrawlService newsCrawlService;

    @GetMapping
    @Operation(summary = "뉴스 목록 조회", description = "페이징과 검색 조건을 통한 뉴스 목록을 조회합니다.")
    public ResponseEntity<PageResultDTO<NewsDTO, NewsEntity>> getNewsList(
            @Parameter(description = "페이지 번호 (기본값: 1)")
            @RequestParam(defaultValue = "1") int page,

            @Parameter(description = "페이지 크기 (기본값: 10)")
            @RequestParam(defaultValue = "10") int size) {

        log.info("뉴스 목록 조회 요청 - page: {}, size: {}", page, size);

        PageRequestDTO requestDTO = PageRequestDTO.builder()
                .page(page)
                .size(size)
                .build();

        PageResultDTO<NewsDTO, NewsEntity> result = newsService.getList(requestDTO);

        log.info("뉴스 목록 조회 완료 - 총 개수: {}, 전체 페이지: {}",
                result.getDtoList().size(), result.getTotalPage());

        if (result.getDtoList().isEmpty()) {
            log.warn("조회된 뉴스가 없습니다. 데이터베이스를 확인해주세요.");
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/date")
    @Operation(summary = "특정 날짜의 뉴스 조회", description = "지정된 날짜의 뉴스를 조회합니다.")
    public ResponseEntity<PageResultDTO<NewsDTO, NewsEntity>> getNewsByDate(
            @Parameter(description = "조회할 날짜 (YYYY-MM-DD)")
            @RequestParam String processDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("날짜별 뉴스 조회 요청 - processDate: {}, page: {}, size: {}", processDate, page, size);

        PageRequestDTO requestDTO = PageRequestDTO.builder()
                .page(page)
                .size(size)
                .build();

        PageResultDTO<NewsDTO, NewsEntity> result = newsService.getNewsByDate(processDate, requestDTO);

        log.info("날짜별 뉴스 조회 완료 - 총 개수: {}, 전체 페이지: {}",
                result.getDtoList().size(), result.getTotalPage());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/newscrawl")
    public String newsCrawl(Model model) throws Exception{
        List<NewsDTO> newsList = newsCrawlService.getNewsDatas();
        model.addAttribute("news", newsList);

        return "news";
    }

}