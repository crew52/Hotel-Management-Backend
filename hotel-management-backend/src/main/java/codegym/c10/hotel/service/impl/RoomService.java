package codegym.c10.hotel.service.impl;

import codegym.c10.hotel.annotation.LogActivity;
import codegym.c10.hotel.eNum.RoomStatus;
import codegym.c10.hotel.entity.Room;
import codegym.c10.hotel.entity.RoomCategory;
import codegym.c10.hotel.repository.IRoomCategoryRepository;
import codegym.c10.hotel.repository.IRoomRepository;
import codegym.c10.hotel.service.IRoomService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RoomService implements IRoomService {
    @Autowired
    private IRoomRepository roomRepository;

    @Autowired
    private IRoomCategoryRepository roomCategoryRepository;

    @Override
    @LogActivity(action = "UPDATE_ROOM", description = "Cập nhật thông tin phòng")
    public Room update(Room room) {
        Long roomId = room.getId();
        Room existingRoom = roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("Room not found with id: " + roomId));
        
        // Cập nhật các trường thông tin
        if (room.getRoomCategory() != null && room.getRoomCategory().getId() != null) {
            Long categoryId = room.getRoomCategory().getId();
            RoomCategory category = roomCategoryRepository.findById(categoryId)
                    .orElseThrow(() -> new EntityNotFoundException("RoomCategory not found with id: " + categoryId));
            existingRoom.setRoomCategory(category);
        }
        
        if (room.getFloor() != null) {
            existingRoom.setFloor(room.getFloor());
        }
        
        if (room.getStartDate() != null) {
            existingRoom.setStartDate(room.getStartDate());
        }
        
        if (room.getStatus() != null) {
            existingRoom.setStatus(room.getStatus());
        }
        
        if (room.getNote() != null) {
            existingRoom.setNote(room.getNote());
        }
        
        if (room.getIsClean() != null) {
            existingRoom.setIsClean(room.getIsClean());
        }
        
        if (room.getCheckInDuration() != null) {
            existingRoom.setCheckInDuration(room.getCheckInDuration());
        }
        
        if (room.getImg1() != null) {
            existingRoom.setImg1(room.getImg1());
        }
        
        if (room.getImg2() != null) {
            existingRoom.setImg2(room.getImg2());
        }
        
        if (room.getImg3() != null) {
            existingRoom.setImg3(room.getImg3());
        }
        
        if (room.getImg4() != null) {
            existingRoom.setImg4(room.getImg4());
        }
        
        return roomRepository.save(existingRoom);
    }

    @Override
    public Page<Room> findAllByDeletedFalse(Pageable pageable) {
        return roomRepository.findAllByDeletedFalse(pageable);
    }

    @Override
    public Page<Room> advancedSearch(String keyword, RoomStatus status, Integer floor, Pageable pageable) {
        return roomRepository.advancedSearch(
                keyword != null ? keyword.toLowerCase() : null,
                status,
                floor,
                pageable
        );
    }

    @Override
    public Iterable<Room> findAll() {
        return null;
        //TODO
    }

    @Override
    public Room save(Room room) {
        Long categoryId = room.getRoomCategory().getId();

        RoomCategory category = roomCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("RoomCategory not found with id: " + categoryId));

        room.setRoomCategory(category);

        return roomRepository.save(room);
    }

    @Override
    public Optional<Room> findById(Long id) {
        return roomRepository.findByIdAndDeletedFalse(id);
    }

    @Override
    public void remove(Long id) {
        Optional<Room> room = roomRepository.findById(id);

        if (room.isPresent()) {
            Room room1 = room.get();
            room1.setDeleted(true);
            roomRepository.save(room1);
        } else {
            throw new EntityNotFoundException("Room not found with id: " + id);
        }
    }
}
