package team.shdsesc.stocksimul.util;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class FormatUtil {
    public Double changePriceFormatter(Double price) {
        try {
            BigDecimal value = BigDecimal.valueOf(price).setScale(3, RoundingMode.FLOOR);
            return value.doubleValue();
        } catch (Exception e) {
            return 0.0;
        }
    }

    public LocalDateTime localDateTimeFormatter(String curDate) {
        LocalDate date = LocalDate.parse(curDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return date.atStartOfDay();
    }
}
