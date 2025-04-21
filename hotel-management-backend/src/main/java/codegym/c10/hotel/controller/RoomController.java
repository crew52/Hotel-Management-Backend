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

/**
 * Controller for managing rooms in the hotel system.
 *
 * This class provides the following endpoints for room management:
 * 1. GET /api/rooms - List all rooms with pagination.
 * 2. GET /api/rooms/{id} - Get a room by its ID.
 * 3. GET /api/rooms/search - Search rooms based on filters like keyword, status, floor.
 * 4. DELETE /api/rooms/{id}/delete - Delete a room by its ID.
 * 5. POST /api/rooms - Create a new room with images.
 * 6. PUT /api/rooms/{id}/edit - Update an existing room with images.
 */
@RestController
@RequestMapping("/api/rooms")
@CrossOrigin("*")
public class RoomController {

    @Autowired
    private IRoomService roomService;

    @Autowired
    private StorageService storageService;

    /**
     * Endpoint to get a paginated list of rooms.
     *
     * @param page Page number for pagination (default: 0)
     * @param size Number of rooms per page (default: 3)
     * @return A paginated list of rooms.
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
     * Endpoint to get a room by its ID.
     *
     * @param id The ID of the room.
     * @return The room if found, or a 404 Not Found response if not found.
     */
    @GetMapping("/{id}")
    @PreAuthorize("@securityService.hasPermission('VIEW_ROOM')")
    public ResponseEntity<Room> getRoomById(@PathVariable Long id) {
        return roomService.findById(id)
                .map(room -> ResponseEntity.ok(room))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Endpoint to search rooms based on provided filters.
     *
     * @param keyword Search keyword for room name or description.
     * @param status Room status (optional).
     * @param floor Floor number of the room (optional).
     * @param page Page number for pagination (default: 0).
     * @param size Number of rooms per page (default: 10).
     * @return A paginated list of rooms matching the search criteria.
     */
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

    /**
     * Endpoint to delete a room by its ID.
     *
     * @param id The ID of the room.
     * @return A 204 No Content response if deletion is successful,
     *         or a 404 Not Found if the room doesn't exist.
     */
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

    /**
     * Endpoint to create a new room with associated images.
     *
     * @param roomJson JSON representation of the room.
     * @param img1, img2, img3, img4 Optional images for the room.
     * @return The created room, or an error response if the room data is invalid.
     */
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

    /**
     * Endpoint to update an existing room by its ID and optionally update its images.
     *
     * @param id The ID of the room to update.
     * @param roomJson JSON representation of the updated room.
     * @param img1, img2, img3, img4 Optional images for the room.
     * @return The updated room, or an error response if any issue occurs.
     */
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

    /**
     * Helper method to parse a room JSON string into a Room object.
     *
     * @param roomJson The JSON string to convert.
     * @return A Room object, or null if JSON parsing fails.
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
     * Helper method to handle room image uploads.
     *
     * @param room The room object to update with image paths.
     * @param images Array of images to upload.
     * @throws NoSuchMethodException If the method to set an image is not found.
     * @throws InvocationTargetException If an error occurs when invoking the setter.
     * @throws IllegalAccessException If the method cannot be accessed.
     */
    private void handleRoomImages(Room room, MultipartFile[] images) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        for (int i = 0; i < images.length; i++) {
            if (images[i] != null && !images[i].isEmpty()) {
                String path = storageService.storeWithUUID(images[i], "rooms");
                room.getClass().getMethod("setImg" + (i + 1), String.class).invoke(room, path);
            }
        }
    }
}