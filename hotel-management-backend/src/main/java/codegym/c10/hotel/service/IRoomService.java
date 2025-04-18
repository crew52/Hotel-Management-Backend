package codegym.c10.hotel.service;

import codegym.c10.hotel.eNum.RoomStatus;
import codegym.c10.hotel.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IRoomService extends IGenerateService<Room>{
    Room update(Room room);
    Page<Room> findAllByDeletedFalse(Pageable pageable);
    Page<Room> advancedSearch(String keyword, RoomStatus status, Integer floor, Pageable pageable);
}
