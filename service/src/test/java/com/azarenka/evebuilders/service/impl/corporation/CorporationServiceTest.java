package com.azarenka.evebuilders.service.impl.corporation;

import com.azarenka.evebuilders.domain.db.Corporation;
import com.azarenka.evebuilders.domain.db.ManagedCorporation;
import com.azarenka.evebuilders.domain.exeptions.ValidationException;
import com.azarenka.evebuilders.repository.database.IManagedCorporationRepository;
import com.azarenka.evebuilders.service.api.IEveCharacterService;
import com.azarenka.evebuilders.service.api.IEveCorporationService;
import com.azarenka.evebuilders.service.api.IUserService;
import com.azarenka.evebuilders.service.impl.auth.eve.SecurityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CorporationServiceTest {

    @Mock
    private IManagedCorporationRepository managedCorporationRepository;
    @Mock
    private IEveCorporationService eveCorporationService;
    @Mock
    private IUserService userService;
    @Mock
    private IEveCharacterService eveCharacterService;
    @InjectMocks
    private CorporationService corporationService;

    @Test
    void addCorporationSavesEntityWhenInputIsValid() {
        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
            utilities.when(SecurityUtils::getUserName).thenReturn("builder-user");
            when(eveCorporationService.findCorporationIdByName("Corp One")).thenReturn(1001L);
            when(managedCorporationRepository.existsByOwnerUsernameAndEveCorporationId("builder-user", 1001L))
                .thenReturn(false);
            Corporation corporation = new Corporation();
            corporation.setName("Corp One");
            corporation.setTicker("CONE");
            corporation.setCeoId(2002);
            when(eveCorporationService.getCorporation("1001")).thenReturn(corporation);
            when(userService.getCharacterId()).thenReturn("2002");
            when(userService.getUserToken()).thenReturn("token");
            when(eveCharacterService.getCharacterInfo("token", "2002"))
                .thenReturn("{\"corporation_id\":1001}");
            when(managedCorporationRepository.save(any(ManagedCorporation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            ManagedCorporation result = corporationService.addCorporation("Corp One");

            ArgumentCaptor<ManagedCorporation> captor = ArgumentCaptor.forClass(ManagedCorporation.class);
            verify(managedCorporationRepository).save(captor.capture());
            ManagedCorporation saved = captor.getValue();
            assertEquals("builder-user", saved.getOwnerUsername());
            assertEquals(Long.valueOf(1001L), saved.getEveCorporationId());
            assertEquals("Corp One", saved.getCorporationName());
            assertEquals("CONE", saved.getCorporationTicker());
            assertEquals(saved.getId(), result.getId());
        }
    }

    @Test
    void addCorporationThrowsWhenDuplicateExists() {
        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
            utilities.when(SecurityUtils::getUserName).thenReturn("builder-user");
            when(eveCorporationService.findCorporationIdByName("Corp One")).thenReturn(1001L);
            when(managedCorporationRepository.existsByOwnerUsernameAndEveCorporationId("builder-user", 1001L))
                .thenReturn(true);

            assertThrows(ValidationException.class,
                () -> corporationService.addCorporation("Corp One"));
        }
    }

    @Test
    void addCorporationThrowsWhenEsiReturnsNull() {
        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
            utilities.when(SecurityUtils::getUserName).thenReturn("builder-user");
            when(eveCorporationService.findCorporationIdByName("Corp One")).thenReturn(1001L);
            when(managedCorporationRepository.existsByOwnerUsernameAndEveCorporationId("builder-user", 1001L))
                .thenReturn(false);
            when(eveCorporationService.getCorporation("1001")).thenReturn(null);

            assertThrows(ValidationException.class,
                () -> corporationService.addCorporation("Corp One"));
        }
    }

    @Test
    void addCorporationThrowsWhenUserIsNotCeo() {
        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
            utilities.when(SecurityUtils::getUserName).thenReturn("builder-user");
            when(eveCorporationService.findCorporationIdByName("Corp One")).thenReturn(1001L);
            when(managedCorporationRepository.existsByOwnerUsernameAndEveCorporationId("builder-user", 1001L))
                .thenReturn(false);
            Corporation corporation = new Corporation();
            corporation.setName("Corp One");
            corporation.setTicker("CONE");
            corporation.setCeoId(2002);
            when(eveCorporationService.getCorporation("1001")).thenReturn(corporation);
            when(userService.getCharacterId()).thenReturn("3003");
            when(userService.getUserToken()).thenReturn("token");
            when(eveCharacterService.getCharacterInfo("token", "3003"))
                .thenReturn("{\"corporation_id\":1001}");

            assertThrows(ValidationException.class,
                () -> corporationService.addCorporation("Corp One"));
        }
    }

    @Test
    void getMyCorporationsReturnsCurrentUserRows() {
        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
            utilities.when(SecurityUtils::getUserName).thenReturn("builder-user");
            when(managedCorporationRepository.findAllByOwnerUsernameOrderByCreatedDateDesc("builder-user"))
                .thenReturn(List.of(new ManagedCorporation()));

            var result = corporationService.getMyCorporations();

            assertEquals(1, result.size());
            verify(managedCorporationRepository).findAllByOwnerUsernameOrderByCreatedDateDesc("builder-user");
        }
    }

    @Test
    void getAllCorporationsReturnsRepositoryRows() {
        when(managedCorporationRepository.findAllByOrderByCreatedDateDesc())
            .thenReturn(List.of(new ManagedCorporation(), new ManagedCorporation()));

        var result = corporationService.getAllCorporations();

        assertEquals(2, result.size());
        verify(managedCorporationRepository).findAllByOrderByCreatedDateDesc();
    }
}
