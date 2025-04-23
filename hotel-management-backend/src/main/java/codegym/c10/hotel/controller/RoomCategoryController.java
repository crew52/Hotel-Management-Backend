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
     * Fetch a list of all room categories.
     *
     * @return ResponseEntity<Iterable<RoomCategory>>: A response containing the list of room categories.
     *          - 200 OK: Returns the list of room categories if available.
     *          - 204 No Content: If no room categories exist.
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
     * Search for room categories based on multiple filters such as keyword, status, and price range.
     *
     * @param keyword Optional query parameter to search by name of the room category.
     * @param status Optional query parameter to filter by room category status.
     * @param minHourlyPrice Optional query parameter to filter by minimum hourly price.
     * @param maxHourlyPrice Optional query parameter to filter by maximum hourly price.
     * @param minDailyPrice Optional query parameter to filter by minimum daily price.
     * @param maxDailyPrice Optional query parameter to filter by maximum daily price.
     * @param minOvernightPrice Optional query parameter to filter by minimum overnight price.
     * @param maxOvernightPrice Optional query parameter to filter by maximum overnight price.
     * @param page Page number for pagination.
     * @param size Number of results per page.
     * @return ResponseEntity<Page<RoomCategory>>: A paginated list of room categories matching the search criteria.
     *          - 200 OK: Returns a page of room categories matching the search filters.
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
     * Delete a specific room category by its ID.
     *
     * @param id The ID of the room category to be deleted.
     * @return ResponseEntity<Void>:
     *          - 204 No Content: If the room category was successfully deleted.
     *          - 404 Not Found: If no room category with the specified ID exists.
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
     * Fetch a room category by its ID.
     *
     * @param id The ID of the room category to retrieve.
     * @return ResponseEntity<RoomCategory>:
     *          - 200 OK: Returns the room category if found.
     *          - 404 Not Found: If no room category with the specified ID exists.
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
     * Create a new room category with optional image upload.
     *
     * @param roomCategoryJson JSON representation of the room category.
     * @param bindingResult Validation errors, if any.
     * @param img Optional image for the room category.
     * @return ResponseEntity<?>:
     *          - 201 Created: Room category successfully created.
     *          - 400 Bad Request: Invalid JSON format or missing required fields.
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
     * Update an existing room category by its ID with optional image upload.
     *
     * @param id The ID of the room category to update.
     * @param roomCategoryJson JSON representation of the room category to update.
     * @param bindingResult Validation errors, if any.
     * @param img Optional image for the room category.
     * @return ResponseEntity<?>:
     *          - 200 OK: Room category successfully updated.
     *          - 400 Bad Request: Invalid JSON format or missing required fields.
     *          - 404 Not Found: If no room category with the specified ID exists.
     */
    @PutMapping(value = "/{id}/edit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@securityService.hasPermission('UPDATE_ROOM_CATEGORY')")
    public ResponseEntity<?> updateRoomCategory(@PathVariable Long id,
                                                @RequestPart("roomCategory") String roomCategoryJson,
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