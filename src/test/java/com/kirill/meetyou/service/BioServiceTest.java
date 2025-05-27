package com.kirill.meetyou.service;

import com.kirill.meetyou.dto.BioDto;
import com.kirill.meetyou.exception.ResourceAlreadyExistsException;
import com.kirill.meetyou.exception.ResourceNotFoundException;
import com.kirill.meetyou.model.Bio;
import com.kirill.meetyou.repository.BioRepository;
import com.kirill.meetyou.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BioServiceTest {

    @Mock
    private BioRepository bioRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BioService bioService;

    private Long userId;
    private String bioText;
    private String interestFact;
    private User testUser;
    private Bio testBio;

    @BeforeEach
    void setUp() {
        userId = 1L;
        bioText = "Test bio text";
        interestFact = "Test interest fact";

        testUser = new User();
        testUser.setId(userId);
        testUser.setName("Test User");

        testBio = new Bio();
        testBio.setId(1L);
        testBio.setUserBio(bioText);
        testBio.setInterestFact(interestFact);
        testBio.setUser(testUser);
    }

    @Test
    void createUserBio_Success() {
        BioDto.CreateRequest createRequest = BioDto.CreateRequest.builder()
                .bio(bioText)
                .interestFact(interestFact)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(bioRepository.existsByUserId(userId)).thenReturn(false);
        when(bioRepository.save(any(Bio.class))).thenReturn(testBio);

        BioDto.Response response = bioService.createUserBio(userId, createRequest);

        assertNotNull(response);
        assertEquals(bioText, response.getBio());
        assertEquals(interestFact, response.getInterestFact());
        verify(bioRepository).save(any(Bio.class));
    }

    @Test
    void createUserBio_UserNotFound_ThrowsException() {
        BioDto.CreateRequest request = BioDto.CreateRequest.builder()
                .bio(bioText)
                .interestFact(interestFact)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                bioService.createUserBio(userId, request));

        verify(userRepository).findById(userId);
        verifyNoInteractions(bioRepository);
    }

    @Test
    void createUserBio_BioAlreadyExists_ThrowsException() {
        BioDto.CreateRequest request = BioDto.CreateRequest.builder()
                .bio(bioText)
                .interestFact(interestFact)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(bioRepository.existsByUserId(userId)).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () ->
                bioService.createUserBio(userId, request));

        verify(bioRepository, never()).save(any());
    }

    @Test
    void createUserBio_EmptyBioText_ThrowsException() {
        BioDto.CreateRequest request = BioDto.CreateRequest.builder()
                .bio("")
                .interestFact(interestFact)
                .build();

        assertThrows(IllegalArgumentException.class, () ->
                bioService.createUserBio(userId, request));

        verifyNoInteractions(userRepository);
        verifyNoInteractions(bioRepository);
    }

    @Test
    void createUserBio_NullBioText_ThrowsException() {
        BioDto.CreateRequest request = BioDto.CreateRequest.builder()
                .bio(null)
                .interestFact(interestFact)
                .build();

        assertThrows(IllegalArgumentException.class, () ->
                bioService.createUserBio(userId, request));

        verifyNoInteractions(userRepository);
        verifyNoInteractions(bioRepository);
    }

    @Test
    void getBioByUserId_Success() {
        when(bioRepository.findByUserId(userId)).thenReturn(Optional.of(testBio));

        String result = bioService.getBioByUserId(userId);

        assertEquals(bioText, result);
        verify(bioRepository).findByUserId(userId);
    }

    @Test
    void getBioByUserId_BioNotFound_ThrowsException() {
        when(bioRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                bioService.getBioByUserId(userId));

        verify(bioRepository).findByUserId(userId);
    }

    @Test
    void getBioByUserId_EmptyBioText_ThrowsException() {
        testBio.setUserBio("");
        when(bioRepository.findByUserId(userId)).thenReturn(Optional.of(testBio));

        assertThrows(ResourceNotFoundException.class, () ->
                bioService.getBioByUserId(userId));

        verify(bioRepository).findByUserId(userId);
    }

    @Test
    void getInterestFactByUserId_Success() {
        when(bioRepository.findByUserId(userId)).thenReturn(Optional.of(testBio));

        String result = bioService.getInterestFactByUserId(userId);

        assertEquals(interestFact, result);
        verify(bioRepository).findByUserId(userId);
    }

    @Test
    void getInterestFactByUserId_BioNotFound_ThrowsException() {
        when(bioRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                bioService.getInterestFactByUserId(userId));

        verify(bioRepository).findByUserId(userId);
    }

    @Test
    void getInterestFactByUserId_EmptyInterestFact_ThrowsException() {
        testBio.setInterestFact("");
        when(bioRepository.findByUserId(userId)).thenReturn(Optional.of(testBio));

        assertThrows(ResourceNotFoundException.class, () ->
                bioService.getInterestFactByUserId(userId));

        verify(bioRepository).findByUserId(userId);
    }

    @Test
    void getFullBioByUserId_Success() {
        when(bioRepository.findByUserId(userId)).thenReturn(Optional.of(testBio));

        BioDto.Response response = bioService.getFullBioByUserId(userId);

        assertNotNull(response);
        assertEquals(bioText, response.getBio());
        assertEquals(interestFact, response.getInterestFact());
        verify(bioRepository).findByUserId(userId);
    }

    @Test
    void getFullBioByUserId_BioNotFound_ThrowsException() {
        when(bioRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                bioService.getFullBioByUserId(userId));

        verify(bioRepository).findByUserId(userId);
    }

    @Test
    void updateBio_Success() {
        BioDto.UpdateBioRequest request = BioDto.UpdateBioRequest.builder()
                .bio("Updated bio text")
                .build();

        when(bioRepository.findByUserId(userId)).thenReturn(Optional.of(testBio));
        when(bioRepository.save(any(Bio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BioDto.Response response = bioService.updateBio(userId, request);

        assertNotNull(response);
        assertEquals("Updated bio text", response.getBio());
        assertEquals(interestFact, response.getInterestFact());
        verify(bioRepository).save(testBio);
    }

    @Test
    void updateBio_BioNotFound_ThrowsException() {
        BioDto.UpdateBioRequest request = BioDto.UpdateBioRequest.builder()
                .bio("New bio")
                .build();
        when(bioRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                bioService.updateBio(userId, request));

        verify(bioRepository).findByUserId(userId);
        verify(bioRepository, never()).save(any());
    }

    @Test
    void updateBio_EmptyBioText_ThrowsException() {
        BioDto.UpdateBioRequest request = BioDto.UpdateBioRequest.builder()
                .bio("")
                .build();

        assertThrows(IllegalArgumentException.class, () ->
                bioService.updateBio(userId, request));

        verifyNoInteractions(bioRepository);
    }

    @Test
    void updateBio_NullBioText_ThrowsException() {
        BioDto.UpdateBioRequest request = BioDto.UpdateBioRequest.builder()
                .bio(null)
                .build();

        assertThrows(IllegalArgumentException.class, () ->
                bioService.updateBio(userId, request));

        verifyNoInteractions(bioRepository);
    }

    @Test
    void updateInterestFact_Success() {
        BioDto.UpdateInterestFactRequest request = BioDto.UpdateInterestFactRequest.builder()
                .interestFact("Updated interest fact")
                .build();

        when(bioRepository.findByUserId(userId)).thenReturn(Optional.of(testBio));
        when(bioRepository.save(any(Bio.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BioDto.Response response = bioService.updateInterestFact(userId, request);

        assertNotNull(response);
        assertEquals(bioText, response.getBio());
        assertEquals("Updated interest fact", response.getInterestFact());
        verify(bioRepository).save(testBio);
    }

    @Test
    void updateInterestFact_BioNotFound_ThrowsException() {
        BioDto.UpdateInterestFactRequest request = BioDto.UpdateInterestFactRequest.builder()
                .interestFact("New fact")
                .build();
        when(bioRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                bioService.updateInterestFact(userId, request));

        verify(bioRepository).findByUserId(userId);
        verify(bioRepository, never()).save(any());
    }

    @Test
    void deleteUserBio_Success() {
        when(bioRepository.findByUserId(userId)).thenReturn(Optional.of(testBio));

        bioService.deleteUserBio(userId);

        verify(bioRepository).delete(testBio);
    }

    @Test
    void deleteUserBio_BioNotFound_ThrowsException() {
        when(bioRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                bioService.deleteUserBio(userId));

        verify(bioRepository, never()).delete(any());
    }

    @Test
    void deleteInterestFact_Success() {
        when(bioRepository.findByUserId(userId)).thenReturn(Optional.of(testBio));

        bioService.deleteInterestFact(userId);

        assertNull(testBio.getInterestFact());
        verify(bioRepository).save(testBio);
    }

    @Test
    void deleteInterestFact_BioNotFound_ThrowsException() {
        when(bioRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                bioService.deleteInterestFact(userId));

        verify(bioRepository, never()).save(any());
    }
}