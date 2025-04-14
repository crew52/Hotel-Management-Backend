package codegym.c10.hotel.service;

import codegym.c10.hotel.entity.RoomCategory;

public interface IRoomCategoryService extends IGenerateService<RoomCategory>{
    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, Long id);  // Kiểm tra code duy nhất khi ID khác
    RoomCategory update(RoomCategory roomCategory);
}
