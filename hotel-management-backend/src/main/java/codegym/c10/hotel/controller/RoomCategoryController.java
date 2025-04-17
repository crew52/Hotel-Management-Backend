package codegym.c10.hotel.controller;

import codegym.c10.hotel.eNum.RoomCategoryStatus;
import codegym.c10.hotel.entity.RoomCategory;
import codegym.c10.hotel.exception.ErrorResponse;
import codegym.c10.hotel.exception.RoomCategoryHandler;
import codegym.c10.hotel.service.IRoomCategoryService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    private final RoomCategoryHandler roomCategoryHandler;

    public RoomCategoryController(RoomCategoryHandler roomCategoryFacade) {
        this.roomCategoryHandler = roomCategoryFacade;
    }

    @GetMapping()
    public ResponseEntity<Iterable<RoomCategory>> findAllRoomCategories() {
        List<RoomCategory> roomCategories = (List<RoomCategory>) roomCategoryService.findAll();
        if (roomCategories.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(roomCategories, HttpStatus.OK);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<RoomCategory>> searchRoomCategories(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) RoomCategoryStatus status,
            @RequestParam(required = false) Double minHourlyPrice,
            @RequestParam(required = false) Double maxHourlyPrice,
            @RequestParam(required = false) Double minDailyPrice,
            @RequestParam(required = false) Double maxDailyPrice,
            @RequestParam(required = false) Double minOvernightPrice,
            @RequestParam(required = false) Double maxOvernightPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<RoomCategory> categories = roomCategoryService.advancedSearch(
                keyword, status, minHourlyPrice, maxHourlyPrice,
                minDailyPrice, maxDailyPrice, minOvernightPrice, maxOvernightPrice, pageable);
        return ResponseEntity.ok(categories);
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<Void> removeRoomCategory(@PathVariable Long id) {
        try {
            roomCategoryService.remove(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomCategory> getRoomCategoryById(@PathVariable Long id) {
        return roomCategoryService.findById(id)
                .map(roomCategory -> new ResponseEntity<>(roomCategory, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<?> createRoomCategory(@Valid @RequestBody RoomCategory roomCategory,
                                                BindingResult bindingResult) {
        return roomCategoryHandler.createRoomCategory(roomCategory, bindingResult);
    }

    @PutMapping("/{id}/edit")
    public ResponseEntity<?> updateRoomCategory(@PathVariable Long id,
                                                @Valid @RequestBody RoomCategory roomCategory,
                                                BindingResult bindingResult) {
        return roomCategoryHandler.updateRoomCategory(id, roomCategory, bindingResult);
    }
}