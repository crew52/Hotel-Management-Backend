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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin("*")
public class RoomController {

    @Autowired
    private IRoomService roomService;

    @GetMapping()
    @PreAuthorize("hasPermission('VIEW_ROOM')")
    public ResponseEntity<Page<Room>> getRooms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Room> rooms = roomService.findAllByDeletedFalse(pageable);
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission('VIEW_ROOM')")
    public ResponseEntity<Room> getRoomById(@PathVariable Long id) {
        return roomService.findById(id)
                .map(room -> new ResponseEntity<>(room, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/search")
    @PreAuthorize("hasPermission('VIEW_ROOM')")
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
    @PreAuthorize("hasPermission('DELETE_ROOM')")
    public ResponseEntity<Void> removeRoom(@PathVariable Long id) {
        try {
            roomService.remove(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    @PreAuthorize("hasPermission('CREATE_ROOM')")
    public ResponseEntity<?> createRoom(@Valid @RequestBody Room room) {
        Room savedRoom = roomService.save(room);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRoom);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasPermission('UPDATE_ROOM')")
    public ResponseEntity<?> updateRoom(@PathVariable Long id, @Valid @RequestBody Room room) {
        try {
            room.setId(id);
            Room updatedRoom = roomService.update(room);
            return ResponseEntity.ok(updatedRoom);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating room: " + e.getMessage());
        }
    }
}
