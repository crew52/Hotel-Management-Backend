package codegym.c10.hotel.service.impl;

import codegym.c10.hotel.entity.Room;
import codegym.c10.hotel.repository.IRoomCategoryRepository;
import codegym.c10.hotel.repository.IRoomRepository;
import codegym.c10.hotel.service.IRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RoomService implements IRoomService {
    @Autowired
    private IRoomRepository roomRepository;

    @Override
    public Room update(Room room) {
        return null;
    }

    @Override
    public Page<Room> findAllByDeletedFalse(Pageable pageable) {
        return roomRepository.findAllByDeletedFalse(pageable);
    }

    @Override
    public Iterable<Room> findAll() {
        return null;
    }

    @Override
    public Room save(Room T) {
        return null;
    }

    @Override
    public Optional<Room> findById(Long id) {
        return roomRepository.findByIdAndDeletedFalse(id);
    }

    @Override
    public void remove(Long id) {

    }
}
