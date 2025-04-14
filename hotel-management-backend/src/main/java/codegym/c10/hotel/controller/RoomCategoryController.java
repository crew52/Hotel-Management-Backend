package codegym.c10.hotel.controller;

import codegym.c10.hotel.entity.RoomCategory;
import codegym.c10.hotel.service.IRoomCategoryService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/room-categories")
@CrossOrigin("*")
public class RoomCategoryController {

    @Autowired
    private IRoomCategoryService roomCategoryService;

    @GetMapping()
    public ResponseEntity<Iterable<RoomCategory>> findAllRoomCategories() {
        List<RoomCategory> roomCategories = (List<RoomCategory>) roomCategoryService.findAll();
        if (roomCategories.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(roomCategories, HttpStatus.OK);
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<Void> removeRoomCategory(@PathVariable Long id) {
        try {
            roomCategoryService.remove(id);  // Gọi service để cập nhật trạng thái deleted
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);  // Trả về trạng thái "No Content" nếu thành công
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);  // Trả về lỗi "Not Found" nếu không tìm thấy bản ghi
        }
    }
}