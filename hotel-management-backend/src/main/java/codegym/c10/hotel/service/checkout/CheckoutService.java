package codegym.c10.hotel.service.checkout;

import codegym.c10.hotel.dto.CheckoutRequestDTO;
import codegym.c10.hotel.dto.FeeResponseDTO;
import codegym.c10.hotel.dto.InvoiceResponseDTO;
import codegym.c10.hotel.dto.RoomInvoiceDTO;
import codegym.c10.hotel.eNum.BookingDetailStatus;
import codegym.c10.hotel.eNum.BookingStatus;
import codegym.c10.hotel.eNum.RoomStatus;
import codegym.c10.hotel.entity.*;
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
            FeeResponseDTO feeDTO = new FeeResponseDTO();

            // 1. Set thông tin BookingDetail
            feeDTO.setBookingDetailId(detail.getId());
            feeDTO.setRentType(detail.getRentType().name());
            feeDTO.setDuration(detail.getDuration());
            feeDTO.setUnitPrice(detail.getPrice());
            feeDTO.setTotalFee(detail.getPrice().multiply(BigDecimal.valueOf(detail.getDuration())));
            feeDTO.setCheckinTime(detail.getCheckinTime());
            feeDTO.setCheckoutTime(detail.getCheckoutTime());
            feeDTO.setStatus(detail.getStatus());
            feeDTO.setAdultCount(detail.getAdultCount());
            feeDTO.setChildCount(detail.getChildCount());

            // 2. Set thông tin Room
            Room room = detail.getRoom();
            feeDTO.setRoomId(room.getId());
            feeDTO.setRoomName(room.getNote());
            feeDTO.setFloor(room.getFloor());

            // 3. Set thông tin RoomCategory
            RoomCategory category = room.getRoomCategory();
            feeDTO.setRoomCategoryName(category.getName());
            feeDTO.setHourlyPrice(category.getHourlyPrice());
            feeDTO.setDailyPrice(category.getDailyPrice());
            feeDTO.setOvernightPrice(category.getOvernightPrice());

            // 4. Set thông tin Customer
            Customer customer = booking.getCustomer();
            feeDTO.setCustomerId(customer.getId());
            feeDTO.setCustomerName(customer.getFullName());
            feeDTO.setCustomerPhone(customer.getPhone());
            feeDTO.setCustomerEmail(customer.getEmail());

            // 5. Set thông tin Booking
            feeDTO.setBookingId(booking.getId());
            feeDTO.setBookingTime(booking.getBookingTime());
            feeDTO.setTotalAmount(booking.getTotalAmount());
            feeDTO.setPaidAmount(booking.getPaidAmount());
            feeDTO.setBookingStatus(booking.getBookingStatus().name());

            feeList.add(feeDTO);
        }

        return feeList;
    }


    public InvoiceResponseDTO getInvoice(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking không tồn tại"));

        InvoiceResponseDTO invoice = new InvoiceResponseDTO();

        // 1. Set thông tin booking
        invoice.setBookingId(booking.getId());
        invoice.setBookingTime(booking.getBookingTime());
        invoice.setBookingStatus(booking.getBookingStatus());
        invoice.setNote(booking.getNote());

        // 2. Set thông tin khách hàng
        Customer customer = booking.getCustomer();
        invoice.setCustomerId(customer.getId());
        invoice.setCustomerName(customer.getFullName());
        invoice.setCustomerPhone(customer.getPhone());
        invoice.setCustomerEmail(customer.getEmail());
        invoice.setCustomerAddress(customer.getAddress());

        // 3. Set thông tin thanh toán
        invoice.setTotalAmount(booking.getTotalAmount());
        invoice.setPaidAmount(booking.getPaidAmount());
        invoice.setRemainingAmount(booking.getTotalAmount().subtract(booking.getPaidAmount()));

        // 4. Set thông tin người tạo
        User createdBy = booking.getCreatedBy();
        invoice.setCreatedById(createdBy.getId());
        invoice.setCreatedByName(createdBy.getUsername());
        invoice.setCreatedAt(booking.getCreatedAt());

        // 5. Xử lý chi tiết các phòng
        List<RoomInvoiceDTO> roomList = new ArrayList<>();
        BigDecimal roomFee = BigDecimal.ZERO;

        for (BookingDetail detail : booking.getBookingDetails()) {
            RoomInvoiceDTO roomDTO = new RoomInvoiceDTO();

            // Thông tin phòng
            Room room = detail.getRoom();
            roomDTO.setRoomId(room.getId());
            roomDTO.setRoomName(room.getNote());
            roomDTO.setFloor(room.getFloor());
            roomDTO.setRoomCategoryName(room.getRoomCategory().getName());

            // Thông tin thuê phòng
            roomDTO.setRentType(detail.getRentType().name());
            roomDTO.setDuration(detail.getDuration());
            roomDTO.setUnitPrice(detail.getPrice());
            roomDTO.setCheckinTime(detail.getCheckinTime());
            roomDTO.setCheckoutTime(detail.getCheckoutTime());

            // Tính tiền phòng
            BigDecimal total = detail.getPrice().multiply(BigDecimal.valueOf(detail.getDuration()));
            roomDTO.setTotalFee(total);
            roomFee = roomFee.add(total);

            // Thông tin khách trong phòng
            roomDTO.setAdultCount(detail.getAdultCount());
            roomDTO.setChildCount(detail.getChildCount());

            roomList.add(roomDTO);
        }

        // 6. Set tổng phí
        invoice.setRooms(roomList);
        invoice.setRoomFee(roomFee);
        invoice.setServiceFee(BigDecimal.ZERO);   // Có thể tính thêm nếu có
        invoice.setSurcharge(BigDecimal.ZERO);   // Có thể tính thêm nếu có
        invoice.setTotalFee(roomFee);             // Tổng = phí phòng + phí dịch vụ + phụ phí

        return invoice;
    }
}