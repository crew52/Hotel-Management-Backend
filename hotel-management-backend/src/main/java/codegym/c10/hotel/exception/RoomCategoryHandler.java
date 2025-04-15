package codegym.c10.hotel.exception;

import codegym.c10.hotel.entity.RoomCategory;
import codegym.c10.hotel.service.IRoomCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import java.util.HashMap;
import java.util.Map;

@Component
public class RoomCategoryHandler {

    @Autowired
    private IRoomCategoryService roomCategoryService;

    public ResponseEntity<?> createRoomCategory(RoomCategory roomCategory, BindingResult bindingResult) {
        Map<String, String> errors = validate(roomCategory, bindingResult, true);

        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Validation failed", errors));
        }

        RoomCategory savedCategory = roomCategoryService.save(roomCategory);
        return new ResponseEntity<>(savedCategory, HttpStatus.CREATED);
    }

    public ResponseEntity<?> updateRoomCategory(Long id, RoomCategory roomCategory, BindingResult bindingResult) {
        Map<String, String> errors = validate(roomCategory, bindingResult, false, id);

        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Validation failed", errors));
        }

        try {
            roomCategory.setId(id);
            RoomCategory updated = roomCategoryService.update(roomCategory);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("An error occurred while updating the room category"));
        }
    }

    private Map<String, String> validate(RoomCategory roomCategory, BindingResult bindingResult, boolean isCreate) {
        return validate(roomCategory, bindingResult, isCreate, null);
    }

    private Map<String, String> validate(RoomCategory roomCategory, BindingResult bindingResult, boolean isCreate, Long id) {
        Map<String, String> errors = new HashMap<>();

        if (bindingResult.hasErrors()) {
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage()));
        }

        // Check duplicate code
        boolean isDuplicate = isCreate ?
                roomCategoryService.existsByCode(roomCategory.getCode()) :
                roomCategoryService.existsByCodeAndIdNot(roomCategory.getCode(), id);

        if (isDuplicate) {
            errors.put("code", "Room category code already exists");
        }

        return errors;
    }
}
