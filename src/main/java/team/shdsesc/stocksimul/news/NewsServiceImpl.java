package team.shdsesc.stocksimul.news;

import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import team.shdsesc.stocksimul.dto.PageRequestDTO;
import team.shdsesc.stocksimul.dto.PageResultDTO;
@Service
@Log4j2
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {
    private final NewsRepository newsRepository;

    @Override
    public PageResultDTO<NewsDTO,NewsEntity> getList(PageRequestDTO requestDTO){
        Pageable pageable = requestDTO.getPageable(Sort.by("id").descending());

        Page<NewsEntity> result = newsRepository.findAll(pageable);

        log.info("조회된 뉴스 개수 : {}", result.getTotalElements());

        return new PageResultDTO<>(result, this::entitiesToDTO);
    }

    @Override
    public long getTotalCount() {
        return newsRepository.count();
    }
}
