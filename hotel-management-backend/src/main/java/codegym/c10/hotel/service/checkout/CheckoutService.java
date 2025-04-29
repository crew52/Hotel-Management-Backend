package codegym.c10.hotel.service.checkout;

import codegym.c10.hotel.dto.CheckoutRequestDTO;
import codegym.c10.hotel.dto.FeeResponseDTO;
import codegym.c10.hotel.dto.InvoiceResponseDTO;
import codegym.c10.hotel.dto.RoomInvoiceDTO;
import codegym.c10.hotel.eNum.BookingDetailStatus;
import codegym.c10.hotel.eNum.BookingStatus;
import codegym.c10.hotel.eNum.RoomStatus;
import codegym.c10.hotel.entity.Booking;
import codegym.c10.hotel.entity.BookingDetail;
import codegym.c10.hotel.entity.Room;
import codegym.c10.hotel.repository.IBookingDetailsRepository;
import codegym.c10.hotel.repository.IBookingRepository;
import codegym.c10.hotel.repository.IRoomRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CheckoutService {
        @Autowired
        private IRoomRepository roomRepository;

        @Autowired
        private IBookingRepository bookingRepository;

        @Autowired
        private IBookingDetailsRepository bookingDetailsRepository;

    @Transactional
    public String processCheckout(CheckoutRequestDTO requestDTO) {
        try {
            // 1. Xử lý Booking
            Booking booking = bookingRepository.findById(requestDTO.getBookingId())
                    .orElseThrow(() -> new RuntimeException("Booking không tồn tại"));

            booking.setBookingStatus(BookingStatus.CHECKED_OUT);
            bookingRepository.save(booking);

            // 2. Xử lý Room
            Room room = roomRepository.findById(requestDTO.getRoomId())
                    .orElseThrow(() -> new RuntimeException("Room không tồn tại"));

            room.setIsClean(requestDTO.getIsClean());
            room.setStatus(RoomStatus.AVAILABLE);
            roomRepository.save(room);

            // 3. Xử lý BookingDetail
            BookingDetail bookingDetail = booking.getBookingDetails().stream()
                    .filter(detail -> detail.getRoom().getId().equals(requestDTO.getRoomId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết đặt phòng"));

            // Lưu trạng thái cũ để kiểm tra
            BookingDetailStatus oldStatus = bookingDetail.getStatus();

            // Cập nhật trạng thái mới
            bookingDetail.setStatus(BookingDetailStatus.COMPLETED);
            BookingDetail savedBookingDetail = bookingDetailsRepository.save(bookingDetail);

            // Kiểm tra xem việc cập nhật có thành công không
            if (savedBookingDetail.getStatus() != BookingDetailStatus.COMPLETED) {
                throw new RuntimeException("Không thể cập nhật trạng thái BookingDetail");
            }

            // 4. Tạo thông báo chi tiết
            return String.format(
                    "Checkout thành công!\n" +
                            "- Phòng %s đã được trả\n" +
                            "- Trạng thái dọn dẹp: %s\n" +
                            "- Booking Detail: %s → %s",
                    room.getNote(),
                    requestDTO.getIsClean() ? "Đã dọn dẹp" : "Chưa dọn dẹp",
                    oldStatus,
                    savedBookingDetail.getStatus()
            );

        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi checkout: " + e.getMessage());
        }
    }



    public List<FeeResponseDTO> calculateFee(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại"));

        List<FeeResponseDTO> feeList = new ArrayList<>();
        for (BookingDetail detail : booking.getBookingDetails()) {
            String rentType = detail.getRentType().name();
            int duration = detail.getDuration();
            java.math.BigDecimal unitPrice = detail.getPrice();
            java.math.BigDecimal totalFee = unitPrice.multiply(java.math.BigDecimal.valueOf(duration));

            feeList.add(new FeeResponseDTO(
                    detail.getId(),
                    rentType,
                    duration,
                    unitPrice,
                    totalFee
            ));
        }
        return feeList;
    }


    public InvoiceResponseDTO getInvoice(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại"));

        List<RoomInvoiceDTO> roomList = new ArrayList<>();
        BigDecimal roomFee = BigDecimal.ZERO;

        for (BookingDetail detail : booking.getBookingDetails()) {
            RoomInvoiceDTO roomDTO = new RoomInvoiceDTO();
            roomDTO.setRoomId(detail.getRoom().getId());
            roomDTO.setRoomName(detail.getRoom().getNote());
            roomDTO.setRentType(detail.getRentType().name());
            roomDTO.setDuration(detail.getDuration());
            roomDTO.setUnitPrice(detail.getPrice());
            BigDecimal total = detail.getPrice().multiply(BigDecimal.valueOf(detail.getDuration()));
            roomDTO.setTotalFee(total);

            roomFee = roomFee.add(total);
            roomList.add(roomDTO);
        }

        BigDecimal serviceFee = BigDecimal.ZERO;
        BigDecimal surcharge = BigDecimal.ZERO;
        BigDecimal totalFee = roomFee.add(serviceFee).add(surcharge);

        InvoiceResponseDTO invoice = new InvoiceResponseDTO();
        invoice.setBookingId(bookingId);
        invoice.setRooms(roomList);
        invoice.setRoomFee(roomFee);
        invoice.setServiceFee(serviceFee);
        invoice.setSurcharge(surcharge);
        invoice.setTotalFee(totalFee);

        return invoice;
    }
}