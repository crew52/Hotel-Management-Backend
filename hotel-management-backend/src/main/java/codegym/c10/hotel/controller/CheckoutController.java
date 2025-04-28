package codegym.c10.hotel.controller;

import codegym.c10.hotel.dto.CheckoutRequestDTO;
import codegym.c10.hotel.service.checkout.CheckoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/checkouts")
public class CheckoutController {

    @Autowired
    private CheckoutService checkoutService;

    @PostMapping
    public ResponseEntity<?> checkout(@RequestBody CheckoutRequestDTO requestDTO) {
        checkoutService.processCheckout(requestDTO);
        return ResponseEntity.ok("Trả phòng thành công");
    }
}