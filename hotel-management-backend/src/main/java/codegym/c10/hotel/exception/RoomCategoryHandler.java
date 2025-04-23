package codegym.c10.hotel.exception;

import codegym.c10.hotel.entity.RoomCategory;
import codegym.c10.hotel.service.IRoomCategoryService;
import codegym.c10.hotel.service.uploadFile.StorageService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
public class RoomCategoryHandler {

    @Autowired
    private IRoomCategoryService roomCategoryService;

    @Autowired
    private StorageService storageService;

    @Autowired
    private Validator validator;

    /**
     * Creates a new RoomCategory and stores it in the database.
     * @param roomCategory The RoomCategory object to be created.
     * @param bindingResult The result of validating the RoomCategory object.
     * @param img The image file associated with the RoomCategory.
     * @return ResponseEntity containing the created RoomCategory or an error response if validation fails.
     */
    public ResponseEntity<?> createRoomCategory(RoomCategory roomCategory, BindingResult bindingResult, MultipartFile img) {
        // Validate RoomCategory object for required fields
        Map<String, String> errors = validate(roomCategory, bindingResult, true);

        // Validate based on annotation-based constraints (e.g., @NotNull, @Size)
        validateAnnotations(roomCategory, errors);

        if (!errors.isEmpty()) {
            // If validation errors exist, return a bad request response
            return ResponseEntity.badRequest().body(new ErrorResponse("Validation failed", errors));
        }

        // Handle image upload if a file is present
        if (img != null && !img.isEmpty()) {
            String fileName = storageService.storeWithUUID(img, "room-category");
            roomCategory.setImgUrl(fileName);
        }

        // Save the new RoomCategory and return a response with the saved category
        RoomCategory savedCategory = roomCategoryService.save(roomCategory);
        return new ResponseEntity<>(savedCategory, HttpStatus.CREATED); // HTTP status 201 (Created)
    }

    /**
     * Updates an existing RoomCategory based on the given ID.
     * @param id The ID of the RoomCategory to be updated.
     * @param roomCategory The updated RoomCategory object.
     * @param bindingResult The result of validating the updated RoomCategory object.
     * @param img The new image file associated with the RoomCategory (if any).
     * @return ResponseEntity containing the updated RoomCategory or an error response if validation fails.
     */
    public ResponseEntity<?> updateRoomCategory(Long id, RoomCategory roomCategory, BindingResult bindingResult, MultipartFile img) {
        // Validate the updated RoomCategory object
        Map<String, String> errors = validate(roomCategory, bindingResult, false, id);

        // Validate based on annotation-based constraints (e.g., @NotNull, @Size)
        validateAnnotations(roomCategory, errors);

        if (!errors.isEmpty()) {
            // If validation errors exist, return a bad request response
            return ResponseEntity.badRequest().body(new ErrorResponse("Validation failed", errors));
        }

        // Handle RoomCategory update, including image handling
        return handleRoomCategoryUpdate(id, roomCategory, img, errors);
    }

    /**
     * Validates the RoomCategory object based on BindingResult and creation status.
     * @param roomCategory The RoomCategory object to be validated.
     * @param bindingResult The result of validating the RoomCategory.
     * @param isCreate True if the operation is creating a new RoomCategory, false if updating.
     * @return A map containing validation errors (if any).
     */
    private Map<String, String> validate(RoomCategory roomCategory, BindingResult bindingResult, boolean isCreate) {
        return validate(roomCategory, bindingResult, isCreate, null);
    }

    /**
     * Validates the RoomCategory object based on BindingResult, creation status, and optionally an ID (for update).
     * @param roomCategory The RoomCategory object to be validated.
     * @param bindingResult The result of validating the RoomCategory.
     * @param isCreate True if the operation is creating a new RoomCategory, false if updating.
     * @param id The ID of the RoomCategory (only used when updating).
     * @return A map containing validation errors (if any).
     */
    private Map<String, String> validate(RoomCategory roomCategory, BindingResult bindingResult, boolean isCreate, Long id) {
        Map<String, String> errors = new HashMap<>();

        if (bindingResult.hasErrors()) {
            // Collect any field validation errors
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage()));
        }

        // Check if a RoomCategory code already exists (for creation or update)
        boolean isDuplicate = isCreate ?
                roomCategoryService.existsByCode(roomCategory.getCode()) :
                roomCategoryService.existsByCodeAndIdNot(roomCategory.getCode(), id);

        if (isDuplicate) {
            errors.put("code", "Room category code already exists");
        }

        return errors;
    }

    /**
     * Validates the RoomCategory object based on annotations like @NotNull, @Size, etc.
     * @param roomCategory The RoomCategory object to be validated.
     * @param errors The map to store any validation errors found during the annotation-based validation.
     */
    private void validateAnnotations(RoomCategory roomCategory, Map<String, String> errors) {
        Set<ConstraintViolation<RoomCategory>> violations = validator.validate(roomCategory);
        for (ConstraintViolation<RoomCategory> violation : violations) {
            errors.put(violation.getPropertyPath().toString(), violation.getMessage());
        }
    }

    /**
     * Handles the update of an existing RoomCategory, including image upload if necessary.
     * @param id The ID of the RoomCategory to be updated.
     * @param roomCategory The updated RoomCategory object.
     * @param img The new image file (if any).
     * @param errors A map to store any validation errors found during the update process.
     * @return ResponseEntity containing the updated RoomCategory or an error response if update fails.
     */
    private ResponseEntity<?> handleRoomCategoryUpdate(Long id, RoomCategory roomCategory, MultipartFile img, Map<String, String> errors) {
        try {
            // Retrieve the existing RoomCategory from the database by ID
            RoomCategory existingCategory = roomCategoryService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Room category not found"));

            roomCategory.setId(id);

            // Handle image upload if a new image is provided
            if (img != null && !img.isEmpty()) {
                handleImageUpload(existingCategory, img, roomCategory);
            } else {
                // If no new image is uploaded, keep the existing image URL
                roomCategory.setImgUrl(existingCategory.getImgUrl());
            }

            // Save the updated RoomCategory and return the response
            RoomCategory updatedCategory = roomCategoryService.update(roomCategory);
            return ResponseEntity.ok(updatedCategory);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            // Log the error for debugging purposes
            e.printStackTrace(); // You can replace this with proper logging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Update failed: " + e.getMessage()));
        }
    }

    /**
     * Handles the image upload for a RoomCategory. Deletes the old image (if any) and stores the new image.
     * @param existingCategory The existing RoomCategory that is being updated.
     * @param img The new image file to be uploaded.
     * @param roomCategory The RoomCategory object being updated.
     */
    private void handleImageUpload(RoomCategory existingCategory, MultipartFile img, RoomCategory roomCategory) {
        // Delete the old image if it exists
        String oldImgUrl = existingCategory.getImgUrl();
        if (oldImgUrl != null && storageService.exists(oldImgUrl)) {
            storageService.deleteFile(oldImgUrl);
        }

        // Upload the new image and set the new image URL
        String fileName = storageService.storeWithUUID(img, "room-category");
        roomCategory.setImgUrl(fileName);
    }
}
