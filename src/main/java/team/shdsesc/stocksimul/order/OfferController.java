package team.shdsesc.stocksimul.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/history/{usersProfileId}")
    public ResponseEntity<List<OfferResponseDTO>> getOfferHistory(@PathVariable Long usersProfileId) {
        try {
            List<OfferResponseDTO> result = offerService.getOfferHistory(usersProfileId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("주문 내역 조회 실패: usersProfileId={}, error={}", usersProfileId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

//    @PostMapping("/today")
//    public ResponseEntity<?> tradeTodayStockOffer(@RequestBody TodayOfferRequestDTO todayOfferRequestDTO) {
//        try{
//            return ResponseEntity.ok().body(offerService.tradeTodayOffer(todayOfferRequestDTO));
//        }catch (Exception e){
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }
}
