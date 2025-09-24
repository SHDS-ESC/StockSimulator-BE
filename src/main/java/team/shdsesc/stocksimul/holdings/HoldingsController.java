package team.shdsesc.stocksimul.holdings;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/holdings")
@RequiredArgsConstructor
public class HoldingsController {
    private final HoldingsService holdingsService;

    @GetMapping("/stocks/{usersProfileId}/{setCurrentDate}")
    ResponseEntity<HoldingsResponseDTO> getHoldingsStockList(@PathVariable Long usersProfileId, @PathVariable String setCurrentDate) {
        return ResponseEntity
                .ok()
                .body(holdingsService.getHoldingsList(usersProfileId, setCurrentDate));
    }
}
