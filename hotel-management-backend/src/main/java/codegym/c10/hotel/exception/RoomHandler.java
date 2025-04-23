package codegym.c10.hotel.exception;

import codegym.c10.hotel.entity.Room;
import codegym.c10.hotel.service.IRoomService;
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
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Handler class responsible for validating and managing Room creation and update requests.
 */
@Component
public class RoomHandler {

    @Autowired
    private IRoomService roomService;

    @Autowired
    private StorageService storageService;

    @Autowired
    private Validator validator;

    /**
     * Handles the creation of a new room with validation and image handling.
     *
     * @param room           The room entity to be created.
     * @param bindingResult  The result of Spring's binding validation.
     * @param images         A list of uploaded images.
     * @return ResponseEntity with created Room or validation error response.
     */
    public ResponseEntity<?> createRoom(Room room, BindingResult bindingResult, List<MultipartFile> images) {
        Map<String, String> errors = validate(room, bindingResult, true);
        validateAnnotations(room, errors);

        if (!errors.isEmpty()) {
            // Nếu validation không hợp lệ, xóa những ảnh đã tải lên
            cleanupUploadedImages(images);
            return ResponseEntity.badRequest().body(new ErrorResponse("Validation failed", errors));
        }

        // Thêm ảnh vào trước khi lưu phòng
        setRoomImages(room, images);

        // Lưu phòng
        Room savedRoom = roomService.save(room);
        return new ResponseEntity<>(savedRoom, HttpStatus.CREATED);
    }

    /**
     * Handles the update of an existing room with validation and image handling.
     *
     * @param id             The ID of the room to update.
     * @param room           The new room data.
     * @param bindingResult  The result of Spring's binding validation.
     * @param images         A list of uploaded images.
     * @return ResponseEntity with updated Room or validation error response.
     */
    public ResponseEntity<?> updateRoom(Long id, Room room, BindingResult bindingResult, List<MultipartFile> images) {
        Map<String, String> errors = validate(room, bindingResult, false, id);
        validateAnnotations(room, errors);

        if (!errors.isEmpty()) {
            // Nếu validation không hợp lệ, xóa những ảnh đã tải lên
            cleanupUploadedImages(images);
            return ResponseEntity.badRequest().body(new ErrorResponse("Validation failed", errors));
        }

        return handleRoomUpdate(id, room, images, errors);
    }

    /**
     * Performs validation using Spring's BindingResult.
     *
     * @param room       The room object to validate.
     * @param bindingResult The binding result containing validation results.
     * @param isCreate   Flag indicating if it's a creation operation.
     * @return Map of field names to error messages.
     */
    private Map<String, String> validate(Room room, BindingResult bindingResult, boolean isCreate) {
        return validate(room, bindingResult, isCreate, null);
    }

    /**
     * Performs validation using Spring's BindingResult.
     *
     * @param room       The room object to validate.
     * @param bindingResult The binding result containing validation results.
     * @param isCreate   Flag indicating if it's a creation operation.
     * @param id         The ID of the room (used for update).
     * @return Map of field names to error messages.
     */
    private Map<String, String> validate(Room room, BindingResult bindingResult, boolean isCreate, Long id) {
        Map<String, String> errors = new HashMap<>();

        if (bindingResult.hasErrors()) {
            bindingResult.getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        }

        return errors;
    }

    /**
     * Adds annotation-based validation errors to the provided error map.
     *
     * @param room   The room entity to validate.
     * @param errors The map of errors to populate.
     */
    private void validateAnnotations(Room room, Map<String, String> errors) {
        Set<ConstraintViolation<Room>> violations = validator.validate(room);
        for (ConstraintViolation<Room> violation : violations) {
            errors.put(violation.getPropertyPath().toString(), violation.getMessage());
        }
    }

    /**
     * Handles image upload and sets corresponding fields in the Room entity.
     *
     * @param room   The room entity to update.
     * @param images List of uploaded images.
     */
    private void setRoomImages(Room room, List<MultipartFile> images) {
        if (images == null) return;

        for (int i = 0; i < images.size() && i < 4; i++) {
            MultipartFile img = images.get(i);
            if (img != null && !img.isEmpty()) {
                String fileName = storageService.storeWithUUID(img, "room");
                switch (i) {
                    case 0 -> room.setImg1(fileName);
                    case 1 -> room.setImg2(fileName);
                    case 2 -> room.setImg3(fileName);
                    case 3 -> room.setImg4(fileName);
                }
            }
        }
    }

    /**
     * Handles the full update process of a Room including image updates.
     *
     * @param id      The ID of the room to update.
     * @param room    The new room data.
     * @param images  The list of uploaded images.
     * @param errors  The error map for validation.
     * @return ResponseEntity with the updated Room or error message.
     */
    private ResponseEntity<?> handleRoomUpdate(Long id, Room room, List<MultipartFile> images, Map<String, String> errors) {
        try {
            Room existingRoom = roomService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Room not found"));

            room.setId(id);

            handleImageUpdate(existingRoom, room, images);

            Room updatedRoom = roomService.update(room);
            return ResponseEntity.ok(updatedRoom);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Update failed: " + e.getMessage()));
        }
    }

    /**
     * Updates room images, deleting old ones and storing new ones.
     *
     * @param existingRoom The current room entity from DB.
     * @param room         The new room entity with updated data.
     * @param images       The list of uploaded images.
     */
    private void handleImageUpdate(Room existingRoom, Room room, List<MultipartFile> images) {
        if (images == null) {
            room.setImg1(existingRoom.getImg1());
            room.setImg2(existingRoom.getImg2());
            room.setImg3(existingRoom.getImg3());
            room.setImg4(existingRoom.getImg4());
            return;
        }

        for (int i = 0; i < images.size() && i < 4; i++) {
            MultipartFile img = images.get(i);
            if (img != null && !img.isEmpty()) {
                String oldImg = switch (i) {
                    case 0 -> existingRoom.getImg1();
                    case 1 -> existingRoom.getImg2();
                    case 2 -> existingRoom.getImg3();
                    case 3 -> existingRoom.getImg4();
                    default -> null;
                };

                if (oldImg != null && storageService.exists(oldImg)) {
                    try {
                        storageService.deleteFile(oldImg);
                    } catch (Exception e) {
                        System.err.println("Could not delete old image: " + oldImg + ". Reason: " + e.getMessage());
                    }
                }

                String newFileName = storageService.storeWithUUID(img, "room");
                switch (i) {
                    case 0 -> room.setImg1(newFileName);
                    case 1 -> room.setImg2(newFileName);
                    case 2 -> room.setImg3(newFileName);
                    case 3 -> room.setImg4(newFileName);
                }
            } else {
                // Nếu ảnh mới không có thì giữ ảnh cũ
                switch (i) {
                    case 0 -> room.setImg1(existingRoom.getImg1());
                    case 1 -> room.setImg2(existingRoom.getImg2());
                    case 2 -> room.setImg3(existingRoom.getImg3());
                    case 3 -> room.setImg4(existingRoom.getImg4());
                }
            }
        }
    }

    /**
     * Cleans up the uploaded images if validation fails or creation fails.
     *
     * @param images List of uploaded images to be cleaned up.
     */
    private void cleanupUploadedImages(List<MultipartFile> images) {
        if (images != null) {
            for (MultipartFile img : images) {
                if (img != null && !img.isEmpty()) {
                    try {
                        storageService.deleteFile(img.getOriginalFilename());
                    } catch (Exception e) {
                        System.err.println("Failed to delete image: " + img.getOriginalFilename() + ". Reason: " + e.getMessage());
                    }
                }
            }
        }
    }
}
