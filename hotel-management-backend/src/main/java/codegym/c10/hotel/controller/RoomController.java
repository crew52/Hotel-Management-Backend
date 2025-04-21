package codegym.c10.hotel.controller;

import codegym.c10.hotel.eNum.RoomStatus;
import codegym.c10.hotel.entity.Room;
import codegym.c10.hotel.entity.RoomCategory;
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

import java.util.HashMap;
import java.util.Map;

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
    public ResponseEntity<Page<Room>> getRooms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Room> rooms = roomService.findAllByDeletedFalse(pageable);
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@securityService.hasPermission('VIEW_ROOM')")
    public ResponseEntity<Room> getRoomById(@PathVariable Long id) {
        return roomService.findById(id)
                .map(room -> new ResponseEntity<>(room, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/search")
    @PreAuthorize("@securityService.hasPermission('VIEW_ROOM')")
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
    @PreAuthorize("@securityService.hasPermission('DELETE_ROOM')")
    public ResponseEntity<Void> removeRoom(@PathVariable Long id) {
        try {
            roomService.remove(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

//    @PostMapping
//    @PreAuthorize("@securityService.hasPermission('CREATE_ROOM')")
//    public ResponseEntity<?> createRoom(@Valid @RequestBody Room room) {
//        Room savedRoom = roomService.save(room);
//        return ResponseEntity.status(HttpStatus.CREATED).body(savedRoom);
//    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@securityService.hasPermission('CREATE_ROOM')")
    public ResponseEntity<?> createRoom(
            @RequestPart(value = "room") String roomJson,
            @RequestPart(value = "img1", required = false) MultipartFile img1,
            @RequestPart(value = "img2", required = false) MultipartFile img2,
            @RequestPart(value = "img3", required = false) MultipartFile img3,
            @RequestPart(value = "img4", required = false) MultipartFile img4
    ) {
        Room room;
        try {
            // convert roomJSON to string: {
            //  "roomCategory": { "id": 1 },
            //  "floor": 2,
            //  "startDate": "2023-12-01",
            //  "status": "AVAILABLE",
            //  "note": "Near elevator",
            //  "isClean": true,
            //  "checkInDuration": 2
//            }
//             roomJson = roomJson.replaceAll("\\s+", "");
//             roomJson = roomJson.replaceAll(":", ": ");
//             roomJson = roomJson.replaceAll(",", ", ");
//             roomJson = roomJson.replaceAll("\\{", "{ ");
//             roomJson = roomJson.replaceAll("\\}", "} ");
//             System.out.println(roomJson);
             ObjectMapper objectMapper = new ObjectMapper();
            // Java 8 date/time type `java.time.LocalDate` not supported by default: add Module "com.fasterxml.jackson.datatype:jackson-datatype-jsr310" to enable handling
             objectMapper.registerModule(new JavaTimeModule());
            room = objectMapper.readValue(roomJson, Room.class);
        } catch (JsonProcessingException e) {
            Map<String, String> error = new HashMap<>();
            error.put("room", "Invalid JSON format or value");
            return ResponseEntity.badRequest().body(new ErrorResponse("Invalid value provided", error));
        }

        // Lưu ảnh
        if (img1 != null && !img1.isEmpty()) {
            String path = storageService.storeWithUUID(img1, "rooms");
            room.setImg1(path);
        }
        if (img2 != null && !img2.isEmpty()) {
            String path = storageService.storeWithUUID(img2, "rooms");
            room.setImg2(path);
        }
        if (img3 != null && !img3.isEmpty()) {
            String path = storageService.storeWithUUID(img3, "rooms");
            room.setImg3(path);
        }
        if (img4 != null && !img4.isEmpty()) {
            String path = storageService.storeWithUUID(img4, "rooms");
            room.setImg4(path);
        }


        Room savedRoom = roomService.save(room);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRoom);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@securityService.hasPermission('UPDATE_ROOM')")
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
