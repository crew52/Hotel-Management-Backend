package codegym.c10.hotel.validator;

import codegym.c10.hotel.entity.RoomCategory;

/**
 * This utility class is used for validating the guest count (adults and children) for a specific room category.
 * It ensures that the number of adults and children do not exceed the allowed capacity of the room category.
 * The class contains only static methods and has a private constructor to prevent instantiation.
 */
public class GuestCountValidator {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private GuestCountValidator() {
        // Private constructor to prevent instantiation (only static methods are used).
    }

    /**
     * Validates the guest count (adults and children) for a given room category.
     *
     * @param roomCategory The room category for which the guest count is being validated.
     * @param adultCount The number of adults.
     * @param childCount The number of children.
     * @throws IllegalArgumentException if any validation fails:
     *      - if the roomCategory is null,
     *      - if adultCount or childCount is less than 0,
     *      - if the number of adults exceeds the allowed capacity of the room,
     *      - if the number of children exceeds the allowed capacity of the room,
     *      - if the total number of guests exceeds the total capacity of the room.
     */
    public static void validate(RoomCategory roomCategory, int adultCount, int childCount) {
        // Check if the room category is null
        if (roomCategory == null) {
            throw new IllegalArgumentException("RoomCategory cannot be null.");
        }

        // Check if adultCount or childCount is less than 0
        if (adultCount < 0 || childCount < 0) {
            throw new IllegalArgumentException("Adult and child counts must be >= 0.");
        }

        // Check if the number of adults exceeds the allowed capacity of the room
        if (adultCount > roomCategory.getMaxAdultCapacity()) {
            throw new IllegalArgumentException("The number of adults exceeds the allowed limit.");
        }

        // Check if the number of children exceeds the allowed capacity of the room
        if (childCount > roomCategory.getMaxChildCapacity()) {
            throw new IllegalArgumentException("The number of children exceeds the allowed limit.");
        }

        // Calculate the total number of guests
        int totalGuests = adultCount + childCount;

        // Calculate the total capacity (adults + children) of the room
        int totalCapacity = roomCategory.getMaxAdultCapacity() + roomCategory.getMaxChildCapacity();

        // Check if the total number of guests exceeds the total capacity of the room
        if (totalGuests > totalCapacity) {
            throw new IllegalArgumentException("Total number of guests exceeds the total room capacity.");
        }
    }
}
