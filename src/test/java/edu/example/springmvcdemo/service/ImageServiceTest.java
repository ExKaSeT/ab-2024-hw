package edu.example.springmvcdemo.service;

import edu.example.springmvcdemo.annotation.IntegrationTest;
import edu.example.springmvcdemo.dao.ImageRepository;
import edu.example.springmvcdemo.dao.RefreshTokenRepository;
import edu.example.springmvcdemo.dao.StorageRepository;
import edu.example.springmvcdemo.dao.UserRepository;
import edu.example.springmvcdemo.dto.auth.RegisterRequestDto;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@IntegrationTest
@RequiredArgsConstructor
public class ImageServiceTest {

    private final ImageService imageService;
    private final ImageRepository imageRepository;
    private final AuthService authService;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final StorageRepository storageRepository;

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    @BeforeEach
    public void init() {
        authService.register(new RegisterRequestDto(USERNAME, PASSWORD));
    }

    @AfterEach
    public void clear() {
        imageRepository.deleteAll();
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
        storageRepository.getAllObjects().forEach(storageRepository::deleteObject);
    }

    private MockMultipartFile createFile() {
        return new MockMultipartFile("filename", "filename", MediaType.TEXT_PLAIN_VALUE,
                "someText".getBytes());
    }

    @Test
    @Transactional
    public void storeImageChecks() throws IOException {
        var file = createFile();
        var user = userRepository.findByUsername(USERNAME).get();

        var image = imageService.upload(file, user);
        var imageStream = imageService.get(image.getLink());
        var imageMeta = imageService.getMeta(image.getLink());
        var userImages = imageService.getUserImageMetas(user);
        var exist = imageService.exists(image.getLink());

        assertArrayEquals(file.getBytes(), imageStream.readAllBytes());
        assertTrue(exist);
        assertEquals(user, imageMeta.getUser());
        assertEquals(file.getSize(), imageMeta.getSizeBytes());
        assertEquals(1, userImages.size());
        assertEquals(imageMeta, userImages.get(0));

        imageService.delete(image.getLink());
        assertThrows(DataAccessException.class,
                () -> imageService.get(image.getLink()));
    }

    @Test
    public void downloadNonExisting() {
        assertThrows(DataAccessException.class,
                () -> imageService.get(UUID.randomUUID().toString()));
    }
}
