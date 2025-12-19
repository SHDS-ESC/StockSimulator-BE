package team.shdsesc.stocksimul.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * SimpleVectorStore 기반의 간단한 RAG 구성.
 * resources/rag 폴더 아래의 텍스트 파일(.txt)을 모두 로드해서 벡터 스토어에 적재합니다.
 * (PDF 등은 별도 파서가 필요하므로 여기서는 예시로만 두고 스킵합니다.)
 */
@Configuration
@Log4j2
public class RagConfig {

    @Bean
    public VectorStore simpleVectorStore(EmbeddingModel embeddingModel) {
        SimpleVectorStore store = SimpleVectorStore.builder(embeddingModel).build();

        List<Document> documents = new ArrayList<>();
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        try {
            // 예: src/main/resources/rag/*.txt
            Resource[] resources = resolver.getResources("classpath*:rag/*");
            for (Resource resource : resources) {
                String filename = resource.getFilename();
                if (filename == null) {
                    continue;
                }

                try {
                    if (filename.toLowerCase().endsWith(".txt")) {
                        String content = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
                        documents.add(new Document(content));
                        log.info("RAG 문서 로드 완료: {}", filename);
                    } else {
                        // PDF 등은 여기서는 스킵 (추후 PdfDocumentReader 등으로 확장 가능)
                        log.info("지원하지 않는 RAG 문서 형식, 스킵: {}", filename);
                    }
                } catch (IOException e) {
                    log.warn("RAG 문서 로드 실패: {}", filename, e);
                }
            }
        } catch (IOException e) {
            log.warn("RAG 리소스 검색 실패: {}", e.getMessage(), e);
        }

        if (!documents.isEmpty()) {
            store.add(documents);
            log.info("RAG VectorStore 초기화 완료. 문서 수: {}", documents.size());
        } else {
            log.warn("RAG VectorStore에 적재할 문서가 없습니다. resources/rag/*.txt 를 추가해주세요.");
        }

        return store;
    }
}

