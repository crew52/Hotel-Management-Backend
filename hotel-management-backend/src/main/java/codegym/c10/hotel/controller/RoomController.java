package codegym.c10.hotel.controller;

import codegym.c10.hotel.eNum.RoomStatus;
import codegym.c10.hotel.entity.Room;
import codegym.c10.hotel.entity.RoomCategory;
import codegym.c10.hotel.service.IRoomService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin("*")
public class RoomController {

    @Autowired
    private IRoomService roomService;

    @GetMapping()
    public ResponseEntity<Page<Room>> getRooms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Room> rooms = roomService.findAllByDeletedFalse(pageable);
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> getRoomById(@PathVariable Long id) {
        return roomService.findById(id)
                .map(room -> new ResponseEntity<>(room, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Room>> searchRooms(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) RoomStatus status,
            @RequestParam(required = false) Integer floor,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Room> rooms = roomService.advancedSearch(keyword, status, floor, pageable);
        return ResponseEntity.ok(rooms);
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<Void> removeRoom(@PathVariable Long id) {
        try {
            roomService.remove(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    public ResponseEntity<?> createRoom(@Valid @RequestBody Room room) {
        Room savedRoom = roomService.save(room);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRoom);
    }

}
