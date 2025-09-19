package team.shdsesc.stocksimul.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/offer")
public class OfferController  {
    private final OfferService offerService;

    @PostMapping("/update")
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> updateStockAmount(@RequestBody OfferRequestDTO offerRequestDTO) {
        try {
            offerService.calcOfferStock(offerRequestDTO);
            return ResponseEntity.ok().build();
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
