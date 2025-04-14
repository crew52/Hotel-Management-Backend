package codegym.c10.hotel.controller;

import codegym.c10.hotel.entity.RoomCategory;
import codegym.c10.hotel.service.IRoomCategoryService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @GetMapping("/{id}")
    public ResponseEntity<RoomCategory> getRoomCategoryById(@PathVariable Long id) {
        return roomCategoryService.findById(id)
                .map(roomCategory -> new ResponseEntity<>(roomCategory, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

//    @PostMapping
//    public ResponseEntity<RoomCategory> createRoomCategory(@RequestBody @Valid RoomCategory roomCategory) {
//        RoomCategory savedCategory = roomCategoryService.save(roomCategory);
//        return new ResponseEntity<>(savedCategory, HttpStatus.CREATED);
//    }

    // CREATE
    @PostMapping
    public ResponseEntity<?> createRoomCategory(@Valid @RequestBody RoomCategory roomCategory, BindingResult bindingResult) {
        Map<String, String> errors = new HashMap<>();

        // Validation errors
        if (bindingResult.hasErrors()) {
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
        }

        // Check duplicate code
        if (roomCategoryService.existsByCode(roomCategory.getCode())) {
            errors.put("code", "Room category code already exists");
        }

        if (!errors.isEmpty()) {
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }

        RoomCategory savedCategory = roomCategoryService.save(roomCategory);
        return new ResponseEntity<>(savedCategory, HttpStatus.CREATED);
    }

}