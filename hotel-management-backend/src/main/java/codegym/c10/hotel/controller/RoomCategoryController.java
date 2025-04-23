package codegym.c10.hotel.controller;

import codegym.c10.hotel.eNum.RoomCategoryStatus;
import codegym.c10.hotel.entity.RoomCategory;
import codegym.c10.hotel.exception.ErrorResponse;
import codegym.c10.hotel.exception.RoomCategoryHandler;
import codegym.c10.hotel.service.IRoomCategoryService;
import codegym.c10.hotel.service.uploadFile.StorageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RoomCategoryController
 *
 * This controller handles HTTP requests related to Room Categories,
 * including operations such as creation, retrieval, update, deletion, and advanced search.
 */

@RestController
@RequestMapping("/api/room-categories")
@CrossOrigin("*")
public class RoomCategoryController {

    @Autowired
    private IRoomCategoryService roomCategoryService;

    private final RoomCategoryHandler roomCategoryHandler;

    public RoomCategoryController(StorageService storageService, RoomCategoryHandler roomCategoryFacade) {
        this.roomCategoryHandler = roomCategoryFacade;
    }

    /**
     * Get All Room Categories
     *
     * Retrieves a list of all available room categories.
     *
     * @return ResponseEntity containing:
     *         - 200 OK: List of room categories.
     *         - 204 No Content: If no room categories are found.
     */
    @GetMapping()
    @PreAuthorize("@securityService.hasPermission('VIEW_ROOM_CATEGORY')")
    public ResponseEntity<Iterable<RoomCategory>> findAllRoomCategories() {
        List<RoomCategory> roomCategories = (List<RoomCategory>) roomCategoryService.findAll();
        if (roomCategories.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(roomCategories, HttpStatus.OK);
    }

    /**
     * Search Room Categories
     *
     * Performs an advanced search for room categories using optional filters:
     * keyword, status, price ranges (hourly, daily, overnight), and pagination.
     *
     * @param keyword Optional keyword to search by name.
     * @param status Optional status filter (e.g., ACTIVE, INACTIVE).
     * @param minHourlyPrice Minimum hourly price filter.
     * @param maxHourlyPrice Maximum hourly price filter.
     * @param minDailyPrice Minimum daily price filter.
     * @param maxDailyPrice Maximum daily price filter.
     * @param minOvernightPrice Minimum overnight price filter.
     * @param maxOvernightPrice Maximum overnight price filter.
     * @param page Page number for pagination (default: 0).
     * @param size Page size for pagination (default: 10).
     * @return Paginated list of matching room categories (200 OK).
     */
    @GetMapping("/search")
    @PreAuthorize("@securityService.hasPermission('VIEW_ROOM_CATEGORY')")
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

    /**
     * Delete Room Category
     *
     * Deletes a room category by its ID.
     *
     * @param id ID of the room category to delete.
     * @return ResponseEntity:
     *         - 204 No Content: Successfully deleted.
     *         - 404 Not Found: If the ID does not exist.
     */
    @DeleteMapping("/{id}/delete")
    @PreAuthorize("@securityService.hasPermission('DELETE_ROOM_CATEGORY')")
    public ResponseEntity<Void> removeRoomCategory(@PathVariable Long id) {
        try {
            roomCategoryService.remove(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Get Room Category by ID
     *
     * Retrieves a room category by its ID.
     *
     * @param id ID of the room category.
     * @return ResponseEntity:
     *         - 200 OK: Room category found.
     *         - 404 Not Found: If not found.
     */
    @GetMapping("/{id}")
    @PreAuthorize("@securityService.hasPermission('VIEW_ROOM_CATEGORY')")
    public ResponseEntity<RoomCategory> getRoomCategoryById(@PathVariable Long id) {
        return roomCategoryService.findById(id)
                .map(roomCategory -> new ResponseEntity<>(roomCategory, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Create Room Category
     *
     * Creates a new room category with optional image upload.
     *
     * @param roomCategoryJson JSON string representing the room category.
     * @param bindingResult Holds validation results.
     * @param img Optional image for the room category.
     * @return ResponseEntity:
     *         - 201 Created: Successfully created.
     *         - 400 Bad Request: Invalid JSON or validation failure.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@securityService.hasPermission('CREATE_ROOM_CATEGORY')")
    public ResponseEntity<?> createRoomCategory(@Valid @RequestPart("roomCategory") String roomCategoryJson,
                                                BindingResult bindingResult,
                                                @RequestPart(value = "img", required = false) MultipartFile img) {
        RoomCategory roomCategory;
        try {
            roomCategory = new ObjectMapper().readValue(roomCategoryJson, RoomCategory.class);
        } catch (JsonProcessingException e) {
            Map<String, String> error = new HashMap<>();
            error.put("roomCategory", "Invalid JSON format or value");
            return ResponseEntity.badRequest().body(new ErrorResponse("Invalid value provided", error));
        }

        return roomCategoryHandler.createRoomCategory(roomCategory, bindingResult, img);
    }

    /**
     * Update Room Category
     *
     * Updates an existing room category by ID with optional image upload.
     *
     * @param id ID of the room category to update.
     * @param roomCategoryJson JSON string representing the updated room category.
     * @param bindingResult Holds validation results.
     * @param img Optional image file.
     * @return ResponseEntity:
     *         - 200 OK: Successfully updated.
     *         - 400 Bad Request: Invalid input or validation failure.
     *         - 404 Not Found: If the category does not exist.
     */
    @PutMapping(value = "/{id}/edit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@securityService.hasPermission('UPDATE_ROOM_CATEGORY')")
    public ResponseEntity<?> updateRoomCategory(@PathVariable Long id,
                                                @Valid @RequestPart("roomCategory") String roomCategoryJson,
                                                BindingResult bindingResult,
                                                @RequestPart(value = "img", required = false) MultipartFile img) {
        RoomCategory roomCategory;
        try {
            roomCategory = new ObjectMapper().readValue(roomCategoryJson, RoomCategory.class);
        } catch (JsonProcessingException e) {
            Map<String, String> error = new HashMap<>();
            error.put("roomCategory", "Invalid JSON format or value");
            return ResponseEntity.badRequest().body(new ErrorResponse("Invalid value provided", error));
        }

        return roomCategoryHandler.updateRoomCategory(id, roomCategory, bindingResult, img);
    }
}