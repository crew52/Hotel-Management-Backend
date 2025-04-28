package codegym.c10.hotel.service.impl;

import codegym.c10.hotel.eNum.RoomCategoryStatus;
import codegym.c10.hotel.annotation.LogActivity;
import codegym.c10.hotel.eNum.ExtraFeeType;
import codegym.c10.hotel.entity.RoomCategory;
import codegym.c10.hotel.repository.IRoomCategoryRepository;
import codegym.c10.hotel.repository.IRoomRepository;
import codegym.c10.hotel.service.IRoomCategoryService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Service implementation for managing RoomCategory entities.
 */
@Service
public class RoomCategoryServiceImpl implements IRoomCategoryService {
    @Autowired
    private IRoomCategoryRepository roomCategoryRepository;

    @Autowired
    private IRoomRepository roomRepository;

    /**
     * Retrieve all non-deleted room categories.
     *
     * @return Iterable list of RoomCategory entities.
     */
    @Override
    public Iterable<RoomCategory> findAll() {
        return roomCategoryRepository.findByDeletedFalse();
    }

    /**
     * Save a new or existing RoomCategory.
     *
     * @param roomCategory the RoomCategory entity to save.
     * @return The saved RoomCategory entity.
     */
    @Override
    public RoomCategory save(RoomCategory roomCategory) {
        return roomCategoryRepository.save(roomCategory);
    }

    /**
     * Find a non-deleted RoomCategory by its ID.
     *
     * @param id the ID of the RoomCategory.
     * @return Optional containing the found RoomCategory, or empty if not found.
     */
    @Override
    public Optional<RoomCategory> findById(Long id) {
        return roomCategoryRepository.findByIdAndDeletedFalse(id);
    }

    /**
     * Soft delete a RoomCategory by setting its 'deleted' flag to true.
     * Throws exception if RoomCategory does not exist or if rooms are linked to it.
     *
     * @param id the ID of the RoomCategory to remove.
     * @throws EntityNotFoundException if the RoomCategory is not found.
     * @throws IllegalStateException if rooms are still linked to the RoomCategory.
     */
    @Override
    @Transactional
    public void remove(Long id) {
        Optional<RoomCategory> optionalRoomCategory = roomCategoryRepository.findById(id);
        if (optionalRoomCategory.isPresent()) {
            RoomCategory category = optionalRoomCategory.get();

            // Kiểm tra xem còn room nào đang dùng roomCategory này không
            boolean hasRooms = roomRepository.existsByRoomCategory(category);
            if (hasRooms) {
                throw new IllegalStateException("Cannot delete RoomCategory because there are rooms linked to it.");
            }

            // Nếu không có room liên kết, thực hiện xóa mềm
            category.setDeleted(true);
            roomCategoryRepository.save(category);
        } else {
            throw new EntityNotFoundException("Room category not found with id: " + id);
        }
    }

    /**
     * Check if a RoomCategory exists by its code.
     *
     * @param code the code to check.
     * @return true if exists, false otherwise.
     */
    @Override
    public boolean existsByCode(String code) {
        return roomCategoryRepository.existsByCode(code);
    }

    /**
     * Check if a RoomCategory exists by its code, excluding a specific ID.
     *
     * @param code the code to check.
     * @param id the ID to exclude.
     * @return true if another RoomCategory with the same code exists, false otherwise.
     */
    @Override
    public boolean existsByCodeAndIdNot(String code, Long id) {
        return roomCategoryRepository.existsByCodeAndIdNot(code, id);
    }

    /**
     * Update an existing RoomCategory with new details.
     * Also logs the activity via @LogActivity annotation.
     *
     * @param roomCategory the RoomCategory entity with updated fields.
     * @return The updated RoomCategory entity.
     * @throws IllegalArgumentException if code already exists or entity not found.
     */
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

    /**
     * Perform advanced search on RoomCategories based on keyword, status, and price ranges.
     *
     * @param keyword search keyword (case insensitive).
     * @param status RoomCategory status.
     * @param minHourlyPrice minimum hourly price.
     * @param maxHourlyPrice maximum hourly price.
     * @param minDailyPrice minimum daily price.
     * @param maxDailyPrice maximum daily price.
     * @param minOvernightPrice minimum overnight price.
     * @param maxOvernightPrice maximum overnight price.
     * @param pageable paging and sorting information.
     * @return A page of RoomCategory matching search criteria.
     */
    @Override
    public Page<RoomCategory> advancedSearch(
            String keyword,
            RoomCategoryStatus status,
            Double minHourlyPrice,
            Double maxHourlyPrice,
            Double minDailyPrice,
            Double maxDailyPrice,
            Double minOvernightPrice,
            Double maxOvernightPrice,
            Pageable pageable) {
        return roomCategoryRepository.advancedSearch(
                keyword != null ? keyword.toLowerCase() : null,
                status,
                minHourlyPrice,
                maxHourlyPrice,
                minDailyPrice,
                maxDailyPrice,
                minOvernightPrice,
                maxOvernightPrice,
                pageable);
    }

    /**
     * Check if a RoomCategory exists by its ID.
     *
     * @param id the ID to check.
     * @return true if exists, false otherwise.
     */
    public boolean existsById(Long id) {
        return roomCategoryRepository.existsById(id);
    }
}
