package team.shdsesc.stocksimul.auth.util;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass // 상속한 엔티티에서 컬럼으로 인식
@EntityListeners(AuditingEntityListener.class) // JPA Auditing 기능 활성화
public abstract class BaseEntity {

    @CreatedDate
    @Column(updatable = false) // insert 이후 수정 불가
    private LocalDateTime regDate;

    @LastModifiedDate
    private LocalDateTime modDate;
}
