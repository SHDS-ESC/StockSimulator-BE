package team.shdsesc.stocksimul.news;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.Expressions;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import team.shdsesc.stocksimul.dto.PageRequestDTO;
import team.shdsesc.stocksimul.dto.PageResultDTO;
import team.shdsesc.stocksimul.userprofile.UserProfileDTO;
import team.shdsesc.stocksimul.userprofile.UserProfileService;

import java.time.LocalDate;

@Service
@Log4j2
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {
    private final NewsRepository newsRepository;
    private final UserProfileService userProfileService;

    private final QNewsEntity qNews = QNewsEntity.newsEntity;

    @Override
    public PageResultDTO<NewsDTO, NewsEntity> getNewsByUser(Long userProfileId, PageRequestDTO pageRequestDTO) {
       //1. 사용자의 현재 프로필 조회
        UserProfileDTO currentProfile = userProfileService.getCurrentUserProfile(userProfileId);
        //2. 프로필에서 시뮬레이션 날짜 추출
        LocalDate processDate = currentProfile.getProcessDate();
        

        //3. 페이징 정보 설정
        Pageable pageable = pageRequestDTO.getPageable(Sort.by("timePublished").descending());

        //4. QueryDSL 조건 생성(날짜만 비교)
        BooleanBuilder builder = new BooleanBuilder();
        LocalDate targetDate = processDate;
        //Date()함수로 날짜만 비교
        builder.and(Expressions.dateTemplate(LocalDate.class, "DATE({0})", qNews.timePublished).eq(targetDate));

        //5. 조건에 맞는 뉴스 조회
        Page<NewsEntity> result = newsRepository.findAll(builder, pageable);

        //6.로그 출력
        log.info("UserProfielId {}의 시뮬레이션 날짜 {}에 맞는 뉴스 개수 : {}", userProfileId, targetDate, result.getTotalElements());

        //7. 결과 반환
        return new PageResultDTO<>(result,this::entitiesToDTO);

    }

    @Override
    public PageResultDTO<NewsDTO, NewsEntity> getNewsByDate(String processDate, PageRequestDTO requestDTO) {
        // 1. 날짜 파싱
        LocalDate targetDate = LocalDate.parse(processDate);
        
        // 2. 페이징 정보 설정
        Pageable pageable = requestDTO.getPageable(Sort.by("timePublished").descending());
        
        // 3. QueryDSL 조건 생성
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(Expressions.dateTemplate(LocalDate.class, "DATE({0})", qNews.timePublished).eq(targetDate));
        
        // 4. 조건에 맞는 뉴스 조회
        Page<NewsEntity> result = newsRepository.findAll(builder, pageable);
        
        log.info("날짜 {}에 맞는 뉴스 개수: {}", targetDate, result.getTotalElements());
        
        return new PageResultDTO<>(result, this::entitiesToDTO);
    }

    @Override
    public PageResultDTO<NewsDTO,NewsEntity> getList(PageRequestDTO requestDTO){
        Pageable pageable = requestDTO.getPageable(Sort.by("newsId").descending());

        Page<NewsEntity> result = newsRepository.findAll(pageable);

        log.info("조회된 뉴스 개수 : {}", result.getTotalElements());

        return new PageResultDTO<>(result, this::entitiesToDTO);
    }

    @Override
    public long getTotalCount() {
        return newsRepository.count();
    }
}
