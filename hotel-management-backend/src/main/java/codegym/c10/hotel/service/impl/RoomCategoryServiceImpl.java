package codegym.c10.hotel.service.impl;

import codegym.c10.hotel.annotation.LogActivity;
import codegym.c10.hotel.eNum.ExtraFeeType;
import codegym.c10.hotel.entity.RoomCategory;
import codegym.c10.hotel.repository.IRoomCategoryRepository;
import codegym.c10.hotel.service.IRoomCategoryService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class RoomCategoryServiceImpl implements IRoomCategoryService {
    @Autowired
    private IRoomCategoryRepository roomCategoryRepository;

    @Override
    public Iterable<RoomCategory> findAll() {
        return roomCategoryRepository.findByDeletedFalse();
    }

    @Override
    public RoomCategory save(RoomCategory roomCategory) {
        return roomCategoryRepository.save(roomCategory);
    }


    @Override
    public Optional<RoomCategory> findById(Long id) {
        return roomCategoryRepository.findByIdAndDeletedFalse(id);
    }

    @Override
    public void remove(Long id) {
        Optional<RoomCategory> roomCategory = roomCategoryRepository.findById(id);  // Tìm phòng theo id

        if (roomCategory.isPresent()) {
            RoomCategory category = roomCategory.get();
            category.setDeleted(true);  // Cập nhật trường deleted thành true
            roomCategoryRepository.save(category);  // Lưu thay đổi vào DB
        } else {
            throw new EntityNotFoundException("Room category not found with id: " + id);  // Nếu không tìm thấy bản ghi
        }
    }

    @Override
    public boolean existsByCode(String code) {
        return roomCategoryRepository.existsByCode(code);
    }

    @Override
    public boolean existsByCodeAndIdNot(String code, Long id) {
        return roomCategoryRepository.existsByCodeAndIdNot(code, id);
    }

    @Override
    @LogActivity(action = "UPDATE_ROOM_CATEGORY", description = "Cập nhật loại phòng")
    public RoomCategory update(RoomCategory roomCategory) {
        // Kiểm tra mã 'code' có trùng với mã của các phòng khác không
        if (existsByCodeAndIdNot(roomCategory.getCode(), roomCategory.getId())) {
            throw new IllegalArgumentException("Room category code already exists.");
        }

        // Tìm kiếm đối tượng RoomCategory cần cập nhật
        RoomCategory existingRoomCategory = roomCategoryRepository.findById(roomCategory.getId())
                .orElseThrow(() -> new IllegalArgumentException("Room category not found"));

        // Cập nhật các trường thông tin
        existingRoomCategory.setCode(roomCategory.getCode());
        existingRoomCategory.setName(roomCategory.getName());
        existingRoomCategory.setDescription(roomCategory.getDescription());
        existingRoomCategory.setHourlyPrice(roomCategory.getHourlyPrice());
        existingRoomCategory.setDailyPrice(roomCategory.getDailyPrice());
        existingRoomCategory.setOvernightPrice(roomCategory.getOvernightPrice());
        existingRoomCategory.setEarlyCheckinFee(roomCategory.getEarlyCheckinFee());
        existingRoomCategory.setLateCheckoutFee(roomCategory.getLateCheckoutFee());
        existingRoomCategory.setExtraFeeType(roomCategory.getExtraFeeType());
        existingRoomCategory.setDefaultExtraFee(roomCategory.getDefaultExtraFee());
        existingRoomCategory.setApplyToAllCategories(roomCategory.getApplyToAllCategories());
        existingRoomCategory.setStandardAdultCapacity(roomCategory.getStandardAdultCapacity());
        existingRoomCategory.setStandardChildCapacity(roomCategory.getStandardChildCapacity());
        existingRoomCategory.setMaxAdultCapacity(roomCategory.getMaxAdultCapacity());
        existingRoomCategory.setMaxChildCapacity(roomCategory.getMaxChildCapacity());
        existingRoomCategory.setStatus(roomCategory.getStatus());
        existingRoomCategory.setImgUrl(roomCategory.getImgUrl());

        // Lưu lại đối tượng đã cập nhật
        return roomCategoryRepository.save(existingRoomCategory);
    }
}
