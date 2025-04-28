package codegym.c10.hotel.controller;

import codegym.c10.hotel.dto.CheckoutRequestDTO;
import codegym.c10.hotel.dto.FeeResponseDTO;
import codegym.c10.hotel.dto.InvoiceResponseDTO;
import codegym.c10.hotel.service.checkout.CheckoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/{id}/fee")
    public ResponseEntity<List<FeeResponseDTO>> getFee(@PathVariable("id") Long bookingId) {
        List<FeeResponseDTO> feeList = checkoutService.calculateFee(bookingId);
        return ResponseEntity.ok(feeList);
    }


    @GetMapping("/{id}/invoice")
    public ResponseEntity<InvoiceResponseDTO> getInvoice(@PathVariable("id") Long bookingId) {
        InvoiceResponseDTO invoice = checkoutService.getInvoice(bookingId);
        return ResponseEntity.ok(invoice);
    }
}