package codegym.c10.hotel.controller;

import codegym.c10.hotel.dto.CheckoutDueSoonDTO;
import codegym.c10.hotel.dto.CheckoutRequestDTO;
import codegym.c10.hotel.dto.FeeResponseDTO;
import codegym.c10.hotel.dto.InvoiceResponseDTO;
import codegym.c10.hotel.dto.auth.UserPrinciple;
import codegym.c10.hotel.service.checkout.CheckoutService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/checkouts")
public class CheckoutController {

    @Autowired
    private CheckoutService checkoutService;

    @PostMapping
    public ResponseEntity<?> checkout(@RequestBody CheckoutRequestDTO requestDTO) {
        try {
            // Lấy thông tin người dùng hiện tại đang đăng nhập từ SecurityContextHolder
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserPrinciple) {
                UserPrinciple userPrinciple = (UserPrinciple) authentication.getPrincipal();
                Long userId = userPrinciple.getId();

                requestDTO.setUserId(userId);
                requestDTO.setUserName(userPrinciple.getUsername());
            }

            String result = checkoutService.processCheckout(requestDTO);
            return ResponseEntity.ok(new HashMap<String, Object>() {{
                put("status", "success");
                put("message", result);
                put("data", new HashMap<String, Object>() {{
                    put("bookingId", requestDTO.getBookingId());
                    put("roomId", requestDTO.getRoomId());
                    put("isClean", requestDTO.getIsClean());
                    put("userId", requestDTO.getUserId());
                    put("userName", requestDTO.getUserName());
                    put("bookingDetailStatus", "COMPLETED");
                }});
            }});
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new HashMap<String, Object>() {{
                put("status", "error");
                put("message", e.getMessage());
                put("data", null);
            }});
        }
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