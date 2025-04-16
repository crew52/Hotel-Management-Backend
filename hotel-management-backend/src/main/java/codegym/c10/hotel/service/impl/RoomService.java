package codegym.c10.hotel.service.impl;

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
    public Room update(Room room) {
        return null;
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
