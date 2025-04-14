package codegym.c10.hotel.service.impl;

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
}
