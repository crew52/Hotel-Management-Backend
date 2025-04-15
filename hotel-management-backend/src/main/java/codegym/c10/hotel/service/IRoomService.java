package codegym.c10.hotel.service;

import codegym.c10.hotel.entity.Room;

public interface IRoomService extends IGenerateService<Room>{
    Room update(Room room);
}
