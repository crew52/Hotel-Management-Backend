package codegym.c10.hotel.service.uploadFile;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileSystemStorageService implements StorageService {

    private final Path rootLocation;

    /**
     * Constructor that initializes the root location for file storage.
     * The root location is set to: "src/main/java/codegym/c10/hotel/images/".
     */
    public FileSystemStorageService() {
        this.rootLocation = Paths.get("src/main/java/codegym/c10/hotel/images/");
    }

    /**
     * Initializes the storage location by creating the necessary directories.
     * If the directories already exist, this method does nothing.
     *
     * @throws RuntimeException if the directories cannot be created.
     */
    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    /**
     * Stores the uploaded file in the root storage directory.
     * The filename is sanitized to prevent path traversal attacks.
     *
     * @param file the MultipartFile to store.
     * @return the filename of the stored file.
     * @throws RuntimeException if the file cannot be stored or if the file path is outside the allowed storage directory.
     */
    @Override
    public String store(MultipartFile file) {
        String filename = Path.of(file.getOriginalFilename()).getFileName().toString(); // Prevent path traversal
        Path destinationFile = this.rootLocation.resolve(filename).normalize().toAbsolutePath();

        // Ensure the file is stored within the root location
        if (!destinationFile.startsWith(rootLocation.toAbsolutePath())) {
            throw new RuntimeException("Cannot store file outside of the storage directory.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }

        return filename;
    }

    /**
     * Stores the uploaded file in a subfolder within the root storage directory.
     * The filename is prefixed with a UUID to ensure uniqueness.
     *
     * @param file the MultipartFile to store.
     * @param subFolder the name of the subfolder where the file will be stored.
     * @return the relative path to the file (including subfolder).
     * @throws RuntimeException if the file cannot be stored or if the subfolder cannot be created.
     */
    @Override
    public String storeWithUUID(MultipartFile file, String subFolder) {
        String originalFilename = Path.of(file.getOriginalFilename()).getFileName().toString();
        String uniqueFilename = UUID.randomUUID() + "_" + originalFilename;

        // Create subfolder within the root location if it doesn't exist
        Path subFolderPath = this.rootLocation.resolve(subFolder).normalize();
        try {
            Files.createDirectories(subFolderPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not create sub-directory for storage", e);
        }

        Path destinationFile = subFolderPath.resolve(uniqueFilename).normalize().toAbsolutePath();

        // Ensure the file is stored within the root location
        if (!destinationFile.startsWith(rootLocation.toAbsolutePath())) {
            throw new RuntimeException("Cannot store file outside of the storage directory.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }

        // Return the relative path to the file, including the subfolder
        return subFolder + "/" + uniqueFilename;
    }

    /**
     * Deletes a file from the storage system if it exists.
     *
     * @param fileName the name of the file to delete.
     * @throws RuntimeException if the file cannot be deleted or if an error occurs during the deletion process.
     */
    @Override
    public void deleteFile(String fileName) {
        try {
            Path file = rootLocation.resolve(fileName);
            Files.deleteIfExists(file);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + fileName, e);
        }
    }

    /**
     * Checks if a file exists in the storage system at the specified path.
     *
     * @param path the relative path to the file.
     * @return true if the file exists, otherwise false.
     */
    @Override
    public boolean exists(String path) {
        try {
            Path file = rootLocation.resolve(path); // rootLocation is the root directory for file storage
            return Files.exists(file);
        } catch (Exception e) {
            return false; // Return false in case of any exceptions to prevent failure
        }
    }
}
