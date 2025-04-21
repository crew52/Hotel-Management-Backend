package codegym.c10.hotel.controller;

import codegym.c10.hotel.eNum.RoomStatus;
import codegym.c10.hotel.entity.Room;
import codegym.c10.hotel.exception.ErrorResponse;
import codegym.c10.hotel.service.IRoomService;
import codegym.c10.hotel.service.uploadFile.StorageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin("*")
public class RoomController {

    @Autowired
    private IRoomService roomService;

    @Autowired
    private StorageService storageService;

    @GetMapping()
    @PreAuthorize("@securityService.hasPermission('VIEW_ROOM')")
    public ResponseEntity<Page<Room>> getRooms(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "3") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Room> rooms = roomService.findAllByDeletedFalse(pageable);
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityService.hasPermission('VIEW_ROOM')")
    public ResponseEntity<Room> getRoomById(@PathVariable Long id) {
        return roomService.findById(id)
                .map(room -> ResponseEntity.ok(room))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    @PreAuthorize("@securityService.hasPermission('VIEW_ROOM')")
    public ResponseEntity<Page<Room>> searchRooms(@RequestParam(required = false) String keyword,
                                                  @RequestParam(required = false) RoomStatus status,
                                                  @RequestParam(required = false) Integer floor,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Room> rooms = roomService.advancedSearch(keyword, status, floor, pageable);
        return ResponseEntity.ok(rooms);
    }

    @DeleteMapping("/{id}/delete")
    @PreAuthorize("@securityService.hasPermission('DELETE_ROOM')")
    public ResponseEntity<Void> removeRoom(@PathVariable Long id) {
        try {
            roomService.remove(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@securityService.hasPermission('CREATE_ROOM')")
    public ResponseEntity<?> createRoom(@RequestPart("room") String roomJson,
                                        @RequestPart(value = "img1", required = false) MultipartFile img1,
                                        @RequestPart(value = "img2", required = false) MultipartFile img2,
                                        @RequestPart(value = "img3", required = false) MultipartFile img3,
                                        @RequestPart(value = "img4", required = false) MultipartFile img4) {
        Room room = parseRoomJson(roomJson);
        if (room == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Invalid value provided",
                    Collections.singletonMap("room", "Invalid JSON format or value")));
        }

        try {
            handleRoomImages(room, new MultipartFile[]{img1, img2, img3, img4});
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing images: " + e.getMessage());
        }

        Room savedRoom = roomService.save(room);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRoom);
    }

    @PutMapping(value = "/{id}/edit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@securityService.hasPermission('UPDATE_ROOM')")
    public ResponseEntity<?> updateRoom(@PathVariable Long id,
                                        @RequestPart("room") String roomJson,
                                        @RequestPart(value = "img1", required = false) MultipartFile img1,
                                        @RequestPart(value = "img2", required = false) MultipartFile img2,
                                        @RequestPart(value = "img3", required = false) MultipartFile img3,
                                        @RequestPart(value = "img4", required = false) MultipartFile img4) {
        Room room = parseRoomJson(roomJson);
        if (room == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Invalid value provided",
                    Collections.singletonMap("room", "Invalid JSON format or value")));
        }
        room.setId(id);

        try {
            handleRoomImages(room, new MultipartFile[]{img1, img2, img3, img4});
            Room updatedRoom = roomService.update(room);
            return ResponseEntity.ok(updatedRoom);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating room: " + e.getMessage());
        }
    }

    // Phương thức hỗ trợ chuyển đổi JSON thành Room
    private Room parseRoomJson(String roomJson) {
        try {
            ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
            return mapper.readValue(roomJson, Room.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    // Phương thức xử lý ảnh chung cho cả create và update
    private void handleRoomImages(Room room, MultipartFile[] images) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        for (int i = 0; i < images.length; i++) {
            if (images[i] != null && !images[i].isEmpty()) {
                String path = storageService.storeWithUUID(images[i], "rooms");
                room.getClass().getMethod("setImg" + (i + 1), String.class).invoke(room, path);
            }
        }
    }
}
