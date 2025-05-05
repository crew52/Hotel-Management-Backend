package codegym.c10.hotel.service.impl;

import codegym.c10.hotel.annotation.LogActivity;
import codegym.c10.hotel.eNum.RoomStatus;
import codegym.c10.hotel.entity.Room;
import codegym.c10.hotel.entity.RoomCategory;
import codegym.c10.hotel.repository.IRoomCategoryRepository;
import codegym.c10.hotel.repository.IRoomRepository;
import codegym.c10.hotel.service.IRoomService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service implementation for managing Room entities.
 */
@Service
public class RoomService implements IRoomService {
    @Autowired
    private IRoomRepository roomRepository;

    @Autowired
    private IRoomCategoryRepository roomCategoryRepository;

    /**
     * Updates an existing room with new information.
     *
     * @param room the room data to update
     * @return the updated Room entity
     * @throws EntityNotFoundException if the room or its category is not found
     */
    @Override
    @LogActivity(action = "UPDATE_ROOM", description = "Cập nhật thông tin phòng")
    public Room update(Room room) {
        // Kiểm tra xem phòng có tồn tại không
        Room existingRoom = roomRepository.findById(room.getId())
                .orElseThrow(() -> new EntityNotFoundException("Room not found with id: " + room.getId()));

        // Kiểm tra và cập nhật room category nếu có thay đổi
        if (room.getRoomCategory() != null && room.getRoomCategory().getId() != null) {
            RoomCategory category = roomCategoryRepository.findById(room.getRoomCategory().getId())
                    .orElseThrow(() -> new EntityNotFoundException("Room category not found with id: " + room.getRoomCategory().getId()));
            existingRoom.setRoomCategory(category);
        }

        // Cập nhật các thông tin khác
        existingRoom.setFloor(room.getFloor());
        existingRoom.setStartDate(room.getStartDate());
        existingRoom.setStatus(room.getStatus());
        existingRoom.setNote(room.getNote());
        existingRoom.setIsClean(room.getIsClean());
        existingRoom.setCheckInDuration(room.getCheckInDuration());
        existingRoom.setImg1(room.getImg1());
        existingRoom.setImg2(room.getImg2());
        existingRoom.setImg3(room.getImg3());
        existingRoom.setImg4(room.getImg4());

        // Lưu và trả về phòng đã cập nhật
        return roomRepository.save(existingRoom);
    }

    /**
     * Retrieves a paginated list of rooms that are not marked as deleted.
     *
     * @param pageable the pagination information
     * @return a page of rooms
     */
    @Override
    public Page<Room> findAllByDeletedFalse(Pageable pageable) {
        return roomRepository.findAllByDeletedFalse(pageable);
    }

    /**
     * Searches for rooms based on keyword, status, and floor with pagination.
     *
     * @param keyword the keyword to search for (can be null)
     * @param status  the room status to filter by (can be null)
     * @param floor   the floor number to filter by (can be null)
     * @param pageable the pagination information
     * @return a page of rooms matching the search criteria
     */
    @Override
    public Page<Room> advancedSearch(String keyword, RoomStatus status, Integer floor, Long categoryId, Pageable pageable) {
        return roomRepository.advancedSearch(
                keyword != null ? keyword.toLowerCase() : null,
                status,
                floor,
                categoryId,
                pageable
        );
    }

    /**
     * Finds an available, clean, and non-deleted room by ID.
     *
     * @param id the ID of the room
     * @return an Optional containing the room if found, or empty otherwise
     */
    @Override
    public Optional<Room> findByIdAndStatusAndIsCleanTrueAndDeletedFalse(Long id) {
        return roomRepository.findByIdAndStatusAndIsCleanTrueAndDeletedFalse(id, RoomStatus.AVAILABLE);
    }

    /**
     * Retrieves all rooms. (Not yet implemented.)
     *
     * @return an Iterable of all rooms
     */
    @Override
    public Iterable<Room> findAll() {
        return null;
        //TODO
    }

    /**
     * Saves a new room after verifying the room category exists.
     *
     * @param room the room to save
     * @return the saved Room entity
     * @throws EntityNotFoundException if the room category does not exist
     */
    @Override
    @LogActivity(action = "CREATE_ROOM", description = "Thêm phòng mới vào hệ thống")
    public Room save(Room room) {
        Long categoryId = room.getRoomCategory().getId();

        RoomCategory category = roomCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("RoomCategory not found with id: " + categoryId));

        room.setRoomCategory(category);

        return roomRepository.save(room);
    }

    /**
     * Finds a room by ID, ensuring it is not marked as deleted.
     *
     * @param id the ID of the room
     * @return an Optional containing the room if found, or empty otherwise
     */
    @Override
    public Optional<Room> findById(Long id) {
        return roomRepository.findByIdAndDeletedFalse(id);
    }

    /**
     * Deletes a room by marking it as deleted if its status allows.
     *
     * @param id the ID of the room to delete
     * @throws EntityNotFoundException if the room does not exist
     * @throws IllegalStateException if the room is not in a deletable status
     */
    @Override
    @Transactional
    @LogActivity(action = "DELETE_ROOM", description = "Xóa phòng khỏi hệ thống")
    public void remove(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Room not found with id: " + id));

        // Only allow deleting if the room is AVAILABLE or under MAINTENANCE
        if (room.getStatus() != RoomStatus.AVAILABLE && room.getStatus() != RoomStatus.MAINTENANCE) {
            throw new IllegalStateException("Cannot delete a room that is currently in use.");
        }

        // Soft delete the room
        room.setDeleted(true);
        roomRepository.save(room);
    }

    /**
     * Updates the status of the room identified by its ID.
     * This method allows you to change the status of a room.
     *
     * @param id the ID of the room to update
     * @param status the new status to assign to the room
     * @return the updated room with the new status
     * @throws EntityNotFoundException if no room is found with the given ID
     */
    @Override
    @Transactional
    @LogActivity(action = "UPDATE_ROOM_STATUS", description = "Cập nhật trạng thái phòng")
    public Room updateRoomStatus(Long id, RoomStatus status) {
        Room room = roomRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new EntityNotFoundException("Room not found with id: " + id));
        room.setStatus(status);
        return roomRepository.save(room);
    }

    /**
     * Toggles the cleaning status of the room identified by its ID.
     * This method switches the room's cleaning status from clean to dirty, or vice versa.
     *
     * @param id the ID of the room to update
     * @return the updated room with the new cleaning status
     * @throws EntityNotFoundException if no room is found with the given ID
     */
    @Override
    @LogActivity(action = "UPDATE_ROOM_CLEANING_STATUS", description = "Cập nhật trạng thái vệ sinh phòng")
    public Room updateRoomCleaningStatus(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Room not found with id: " + id));
        room.setIsClean(!room.getIsClean());
        return roomRepository.save(room);
    }
}
