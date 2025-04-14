package codegym.c10.hotel.service.impl;

import codegym.c10.hotel.entity.RoomCategory;
import codegym.c10.hotel.repository.IRoomCategoryRepository;
import codegym.c10.hotel.service.IRoomCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
    public RoomCategory save(RoomCategory T) {
        return null;
        //TODO
    }

    @Override
    public Optional<RoomCategory> findById(Long id) {
        return Optional.empty();
        //TODO
    }

    @Override
    public void remove(Long id) {
        //TODO
    }
}
