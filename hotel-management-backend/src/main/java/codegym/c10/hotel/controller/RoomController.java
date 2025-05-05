package codegym.c10.hotel.controller;

import codegym.c10.hotel.dto.CheckoutDueSoonDTO;
import codegym.c10.hotel.eNum.RoomStatus;
import codegym.c10.hotel.entity.Room;
import codegym.c10.hotel.exception.ErrorResponse;
import codegym.c10.hotel.exception.RoomHandler;
import codegym.c10.hotel.service.IRoomCategoryService;
import codegym.c10.hotel.service.IRoomService;
import codegym.c10.hotel.service.checkout.CheckoutService;
import codegym.c10.hotel.service.uploadFile.StorageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * RoomController is a REST controller responsible for managing room-related operations
 * in the hotel system. It provides endpoints to perform CRUD operations and search functionality
 * for rooms, along with image uploads.
 *
 * Endpoints:
 * - GET /api/rooms: Fetch paginated list of rooms
 * - GET /api/rooms/{id}: Get details of a specific room
 * - GET /api/rooms/search: Search rooms by keyword, status, or floor
 * - DELETE /api/rooms/{id}/delete: Delete a room
 * - POST /api/rooms: Create a new room with optional image uploads
 * - PUT /api/rooms/{id}/edit: Update a room with optional image updates
 */

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin("*")
public class RoomController {

    @Autowired
    private IRoomService roomService;

    @Autowired
    private StorageService storageService;

    @Autowired
    private RoomHandler roomHandler;

    @Autowired
    private IRoomCategoryService roomCategoryService;

    @Autowired
    private CheckoutService checkoutService;

    /**
     * Retrieves a paginated list of all rooms that are not marked as deleted.
     *
     * @param page the page number to retrieve (default is 0)
     * @param size the number of items per page (default is 3)
     * @return a paginated list of Room objects
     */
    @GetMapping()
    @PreAuthorize("@securityService.hasPermission('VIEW_ROOM')")
    public ResponseEntity<Page<Room>> getRooms(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "3") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Room> rooms = roomService.findAllByDeletedFalse(pageable);
        return ResponseEntity.ok(rooms);
    }

    /**
     * Retrieves a room based on its ID.
     *
     * @param id the ID of the room to retrieve
     * @return the Room object if found, or a 404 Not Found if the room does not exist
     */
    @GetMapping("/{id}")
    @PreAuthorize("@securityService.hasPermission('VIEW_ROOM')")
    public ResponseEntity<Room> getRoomById(@PathVariable Long id) {
        return roomService.findById(id)
                .map(room -> ResponseEntity.ok(room))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

//    /**
//     * Searches rooms based on optional filter parameters including keyword, status, floor, and category.
//     *
//     * @param keyword the search keyword (optional)
//     * @param status the status of the room (optional)
//     * @param floor the floor number to filter by (optional)
//     * @param categoryId the category ID to filter by (optional), defaults to null if empty or invalid
//     * @param page the page number for pagination (default is 0)
//     * @param size the number of rooms per page (default is 10)
//     * @return a paginated list of rooms that match the search criteria
//     */
    @GetMapping("/search")
    @PreAuthorize("@securityService.hasPermission('VIEW_ROOM')")
    public ResponseEntity<Page<Room>> searchRooms(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) RoomStatus status,
            @RequestParam(required = false) Integer floor,
            @RequestParam(required = false) String categoryId,
            Pageable pageable) {
        Long categoryIdLong = (categoryId != null && !categoryId.isEmpty()) ? Long.valueOf(categoryId) : null;
        Page<Room> rooms = roomService.advancedSearch(keyword, status, floor, categoryIdLong, pageable);
        return ResponseEntity.ok(rooms);
    }

    /**
     * Deletes a room by marking it as deleted.
     *
     * @param id the ID of the room to delete
     * @return 204 No Content if successfully deleted,
     *         404 Not Found if the room does not exist,
     *         400 Bad Request if the room cannot be deleted due to its status
     */
    @DeleteMapping("/{id}/delete")
    @PreAuthorize("@securityService.hasPermission('DELETE_ROOM')")
    public ResponseEntity<?> removeRoom(@PathVariable Long id) {
        try {
            roomService.remove(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message","Room not found with id: " + id));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Creates a new room using the provided JSON string and optional image files.
     *
     * @param roomJson the JSON representation of the room
     * @param img1 optional image file 1
     * @param img2 optional image file 2
     * @param img3 optional image file 3
     * @param img4 optional image file 4
     * @param bindingResult used for validation (automatically filled by Spring)
     * @return a ResponseEntity containing the created Room or validation errors
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@securityService.hasPermission('CREATE_ROOM')")
    public ResponseEntity<?> createRoom(@RequestPart("room") String roomJson,
                                        @RequestPart(value = "img1", required = false) MultipartFile img1,
                                        @RequestPart(value = "img2", required = false) MultipartFile img2,
                                        @RequestPart(value = "img3", required = false) MultipartFile img3,
                                        @RequestPart(value = "img4", required = false) MultipartFile img4,
                                        BindingResult bindingResult) {
        Room room = parseRoomJson(roomJson);
        if (room == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Invalid value provided",
                    Collections.singletonMap("room", "Invalid JSON format or value")));
        }

        // Kiểm tra xem RoomCategory có tồn tại không
        if (room.getRoomCategory() == null || !roomCategoryService.existsById(room.getRoomCategory().getId())) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Room category not found",
                    Collections.singletonMap("roomCategory", "Room category does not exist")));
        }

        List<MultipartFile> images = List.of(img1, img2, img3, img4);
        return roomHandler.createRoom(room, bindingResult, images);
    }

    /**
     * Updates an existing room with new details and optional image updates.
     *
     * @param id the ID of the room to update
     * @param roomJson the updated room as a JSON string
     * @param img1 optional new image file 1
     * @param img2 optional new image file 2
     * @param img3 optional new image file 3
     * @param img4 optional new image file 4
     * @param bindingResult used for validation (automatically filled by Spring)
     * @return a ResponseEntity containing the updated Room or validation errors
     */
    @PutMapping(value = "/{id}/edit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@securityService.hasPermission('UPDATE_ROOM')")
    public ResponseEntity<?> updateRoom(@PathVariable Long id,
                                        @RequestPart("room") String roomJson,
                                        @RequestPart(value = "img1", required = false) MultipartFile img1,
                                        @RequestPart(value = "img2", required = false) MultipartFile img2,
                                        @RequestPart(value = "img3", required = false) MultipartFile img3,
                                        @RequestPart(value = "img4", required = false) MultipartFile img4,
                                        BindingResult bindingResult) {
        Room room = parseRoomJson(roomJson);
        if (room == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Invalid value provided",
                    Collections.singletonMap("room", "Invalid JSON format or value")));
        }

        // Kiểm tra xem RoomCategory có tồn tại không
        if (room.getRoomCategory() == null || !roomCategoryService.existsById(room.getRoomCategory().getId())) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Room category not found",
                    Collections.singletonMap("roomCategory", "Room category does not exist")));
        }

        List<MultipartFile> images = List.of(img1, img2, img3, img4);
        return roomHandler.updateRoom(id, room, bindingResult, images);
    }

    /**
     * Parses a JSON string into a Room object.
     *
     * @param roomJson the JSON string representing the room
     * @return the Room object if parsing is successful, otherwise null
     */
    private Room parseRoomJson(String roomJson) {
        try {
            ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
            return mapper.readValue(roomJson, Room.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /**
     * PATCH method to update the room's status.
     * @param id - The ID of the room to update.
     * @param status - The new status of the room.
     * @return ResponseEntity containing the updated room or error message.
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Room> updateRoomStatus(@PathVariable Long id, @RequestParam RoomStatus status) {
        try {
            Room updatedRoom = roomService.updateRoomStatus(id, status);
            return ResponseEntity.ok(updatedRoom);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    /**
     * PATCH method to update the room's cleaning status.
     * The cleaning status will be toggled (if it's true, it will become false, and vice versa).
     * @param id - The ID of the room to update.
     * @return ResponseEntity containing the updated room or error message.
     */
    @PatchMapping("/{id}/is_clean")
    public ResponseEntity<Room> updateRoomCleaningStatus(@PathVariable Long id) {
        try {
            Room updatedRoom = roomService.updateRoomCleaningStatus(id);
            return ResponseEntity.ok(updatedRoom);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    /**
     * Định nghĩa endpoint cho việc lấy danh sách phòng sắp checkout
     *
     * @param minutesThreshold
     * - Tham số không bắt buộc (optional) định nghĩa khoảng thời gian (phút) để tìm kiếm
     * - Giá trị mặc định là 60 phút
     * - Ví dụ:
     *   + /api/rooms/checkout-due-soon            -> tìm trong 60 phút tới
     *   + /api/rooms/checkout-due-soon?minutesThreshold=30  -> tìm trong 30 phút tới
     */
    @GetMapping("/checkout-due-soon")
    public ResponseEntity<List<CheckoutDueSoonDTO>> getCheckoutDueSoon(
            @RequestParam(defaultValue = "60") Integer minutesThreshold) {
        List<CheckoutDueSoonDTO> rooms = checkoutService.findRoomsCheckoutDueSoon(minutesThreshold);
        return ResponseEntity.ok(rooms);
    }
}