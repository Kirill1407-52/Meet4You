package com.kirill.meetyou.service;

import com.kirill.meetyou.model.Photo;
import com.kirill.meetyou.repository.PhotoRepository;
import com.kirill.meetyou.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PhotoServiceTest {

    @Mock
    private PhotoRepository photoRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PhotoService photoService;

    private User testUser;
    private Photo testPhoto;
    private Photo mainPhoto;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setName("Test User");

        testPhoto = new Photo();
        testPhoto.setId(1L);
        testPhoto.setPhotoUrl("http://example.com/photo1.jpg");
        testPhoto.setUploadDate(LocalDate.now());
        testPhoto.setUser(testUser);
        testPhoto.setIsMainString("false");

        mainPhoto = new Photo();
        mainPhoto.setId(2L);
        mainPhoto.setPhotoUrl("http://example.com/main.jpg");
        mainPhoto.setUploadDate(LocalDate.now());
        mainPhoto.setUser(testUser);
        mainPhoto.setIsMainString("true");
    }

    @Test
    void addPhoto_ShouldSuccessfullyAddPhoto() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(photoRepository.save(testPhoto)).thenReturn(testPhoto);

        // Act
        Photo result = photoService.addPhoto(1L, testPhoto);

        // Assert
        assertNotNull(result);
        assertEquals(testPhoto, result);
        verify(photoRepository).save(testPhoto);
    }

    @Test
    void addPhoto_ShouldClearMainPhotosWhenAddingNewMainPhoto() {
        // Arrange
        testPhoto.setIsMainString("true");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(photoRepository.save(testPhoto)).thenReturn(testPhoto);

        // Act
        Photo result = photoService.addPhoto(1L, testPhoto);

        // Assert
        assertNotNull(result);
        verify(photoRepository).clearMainPhotos(1L);
    }

    @Test
    void addPhoto_ShouldThrowExceptionForInvalidUserId() {
        // Act & Assert - separate into two tests
        assertThrows(ResponseStatusException.class, () -> {
            Photo photo = testPhoto;
            photoService.addPhoto(null, photo);
        });

        assertThrows(ResponseStatusException.class, () -> {
            Photo photo = testPhoto;
            photoService.addPhoto(0L, photo);
        });
    }

    @Test
    void addPhoto_ShouldThrowExceptionForInvalidPhoto() {
        // Act & Assert - separate null test
        assertThrows(ResponseStatusException.class, () -> {
            Photo photo = null;
            photoService.addPhoto(1L, photo);
        });
    }

    @Test
    void addPhoto_ShouldThrowExceptionForEmptyPhotoUrl() {
        // Act & Assert - separate empty URL test
        Photo invalidPhoto = new Photo();
        invalidPhoto.setPhotoUrl("");
        assertThrows(ResponseStatusException.class, () -> photoService.addPhoto(1L, invalidPhoto));
    }

    @Test
    void getAllUserPhotos_ShouldReturnPhotosList() {
        // Arrange
        when(photoRepository.findByUserId(1L)).thenReturn(List.of(testPhoto, mainPhoto));

        // Act
        List<Photo> result = photoService.getAllUserPhotos(1L);

        // Assert
        assertEquals(2, result.size());
    }

    @Test
    void getAllUserPhotos_ShouldThrowExceptionForDatabaseError() {
        // Arrange
        when(photoRepository.findByUserId(1L)).thenThrow(new RuntimeException("DB error"));

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> photoService.getAllUserPhotos(1L));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
    }

    @Test
    void getPhotoById_ShouldReturnPhoto() {
        // Arrange
        when(photoRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testPhoto));

        // Act
        Photo result = photoService.getPhotoById(1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(testPhoto, result);
    }

    @Test
    void getPhotoById_ShouldThrowExceptionWhenPhotoNotFound() {
        // Arrange
        when(photoRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> photoService.getPhotoById(1L, 1L));
    }

    @Test
    void updatePhoto_ShouldUpdatePhotoDetails() {
        // Arrange
        Photo updatedPhoto = new Photo();
        updatedPhoto.setPhotoUrl("http://example.com/new.jpg");
        updatedPhoto.setIsMainString("true");

        when(photoRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testPhoto));
        when(photoRepository.save(any())).thenReturn(testPhoto);

        // Act
        Photo result = photoService.updatePhoto(1L, 1L, updatedPhoto);

        // Assert
        assertEquals("http://example.com/new.jpg", result.getPhotoUrl());
        assertEquals("true", result.getIsMainString());
        verify(photoRepository).clearMainPhotos(1L);
    }

    @Test
    void updatePhoto_ShouldNotClearMainPhotosWhenNotChangingToMain() {
        // Arrange
        Photo updatedPhoto = new Photo();
        updatedPhoto.setPhotoUrl("http://example.com/new.jpg");

        when(photoRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testPhoto));
        when(photoRepository.save(any())).thenReturn(testPhoto);

        // Act
        photoService.updatePhoto(1L, 1L, updatedPhoto);

        // Assert
        verify(photoRepository, never()).clearMainPhotos(any());
    }

    @Test
    void deletePhoto_ShouldDeletePhotoAndSetNewMain() {
        // Arrange
        when(photoRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(mainPhoto));

        // Act
        photoService.deletePhoto(1L, 1L);

        // Assert
        verify(photoRepository).delete(mainPhoto);
        verify(photoRepository).setNewestPhotoAsMain(1L);
    }

    @Test
    void deletePhoto_ShouldNotSetNewMainWhenDeletingNonMainPhoto() {
        // Arrange
        when(photoRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testPhoto));

        // Act
        photoService.deletePhoto(1L, 1L);

        // Assert
        verify(photoRepository, never()).setNewestPhotoAsMain(any());
    }

    @Test
    void addMultiplePhotos_ShouldHandleEmptyList() {
        // Act
        List<Photo> result = photoService.addMultiplePhotos(1L, Collections.emptyList());

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void addMultiplePhotos_ShouldThrowExceptionForNullList() {
        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> photoService.addMultiplePhotos(1L, null));
    }

    @Test
    void addMultiplePhotos_ShouldClearMainPhotosWhenAddingNewMain() {
        // Arrange
        mainPhoto.setId(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(photoRepository.saveAll(any())).thenReturn(List.of(mainPhoto));

        // Act
        photoService.addMultiplePhotos(1L, List.of(mainPhoto));

        // Assert
        verify(photoRepository).clearMainPhotos(1L);
    }

    @Test
    void setPhotoAsMain_ShouldUpdatePhotoAndClearOthers() {
        // Arrange
        when(photoRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testPhoto));
        when(photoRepository.save(testPhoto)).thenReturn(testPhoto);

        // Act
        Photo result = photoService.setPhotoAsMain(1L, 1L);

        // Assert
        assertEquals("true", result.getIsMainString());
        verify(photoRepository).clearMainPhotos(1L);
    }

    @Test
    void updatePhoto_ShouldNotChangeUploadDateWhenNotProvided() {
        // Arrange
        LocalDate originalDate = testPhoto.getUploadDate();
        Photo updatedPhoto = new Photo();
        updatedPhoto.setPhotoUrl("http://example.com/new.jpg");

        when(photoRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testPhoto));
        when(photoRepository.save(any())).thenReturn(testPhoto);

        // Act
        Photo result = photoService.updatePhoto(1L, 1L, updatedPhoto);

        // Assert
        assertEquals(originalDate, result.getUploadDate());
    }

    @Test
    void updatePhoto_ShouldUpdateUploadDateWhenProvided() {
        // Arrange
        LocalDate newDate = LocalDate.now().minusDays(1);
        Photo updatedPhoto = new Photo();
        updatedPhoto.setUploadDate(newDate);
        updatedPhoto.setPhotoUrl("http://example.com/photo.jpg"); // Добавляем обязательное поле

        when(photoRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testPhoto));
        when(photoRepository.save(any())).thenReturn(testPhoto);

        // Act
        Photo result = photoService.updatePhoto(1L, 1L, updatedPhoto);

        // Assert
        assertEquals(newDate, result.getUploadDate());
    }

    @Test
    void addPhoto_ShouldSetMainPhotoCorrectly() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(photoRepository.save(mainPhoto)).thenReturn(mainPhoto);

        // Act
        Photo result = photoService.addPhoto(1L, mainPhoto);

        // Assert
        assertEquals("true", result.getIsMainString());
        verify(photoRepository).clearMainPhotos(1L);
    }

    @Test
    void addPhoto_ShouldNotClearMainPhotosForNonMainPhoto() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(photoRepository.save(testPhoto)).thenReturn(testPhoto);

        // Act
        Photo result = photoService.addPhoto(1L, testPhoto);

        // Assert
        assertEquals("false", result.getIsMainString());
        verify(photoRepository, never()).clearMainPhotos(any());
    }

    @Test
    void updatePhoto_ShouldUpdateOnlyIsMainField() {
        // Arrange
        Photo updatedPhoto = new Photo();
        updatedPhoto.setIsMainString("true");
        updatedPhoto.setPhotoUrl(testPhoto.getPhotoUrl()); // сохраняем оригинальный URL

        when(photoRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(testPhoto));
        when(photoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Photo result = photoService.updatePhoto(1L, 1L, updatedPhoto);

        // Assert
        assertEquals("true", result.getIsMainString());
        assertEquals(testPhoto.getPhotoUrl(), result.getPhotoUrl());
        verify(photoRepository).clearMainPhotos(1L);
    }

    @Test
    void validatePhotoId_ShouldThrowExceptionForInvalidId() {
        // Act & Assert
        assertThrows(ResponseStatusException.class,
                () -> photoService.getPhotoById(1L, null));
        assertThrows(ResponseStatusException.class,
                () -> photoService.getPhotoById(1L, 0L));
    }

    @Test
    void addMultiplePhotos_ShouldThrowExceptionWhenDatabaseErrorOccurs() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(photoRepository.saveAll(any())).thenThrow(new RuntimeException("DB error"));

        // Act & Assert
        assertThrows(ResponseStatusException.class, () -> {
            List<Photo> photos = List.of(testPhoto);
            photoService.addMultiplePhotos(1L, photos);
        });
    }
}