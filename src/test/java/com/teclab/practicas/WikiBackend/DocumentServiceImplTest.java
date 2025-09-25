package com.teclab.practicas.WikiBackend;

import com.teclab.practicas.WikiBackend.converter.document.DocumentConverter;
import com.teclab.practicas.WikiBackend.dto.documents.DocumentDetailResponseDto;
import com.teclab.practicas.WikiBackend.dto.documents.DocumentRequestDto;
import com.teclab.practicas.WikiBackend.entity.Document;
import com.teclab.practicas.WikiBackend.repository.DocumentRepository;
import com.teclab.practicas.WikiBackend.service.DocumentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class) // Habilita Mockito para JUnit
class DocumentServiceImplTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentConverter documentConverter;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private DocumentServiceImpl documentService;

    @Test
    void whenFindDocumentById_thenReturnsDocumentDetailResponseDto() {
        // Caso de éxito: El documento existe
        Long documentId = 1L;
        Document document = new Document();
        DocumentDetailResponseDto responseDto = new DocumentDetailResponseDto();

        given(documentRepository.findById(documentId)).willReturn(Optional.of(document));
        given(documentConverter.toDetailResponse(document)).willReturn(responseDto);

        DocumentDetailResponseDto result = documentService.getDocumentById(documentId);

        assertNotNull(result);
        verify(documentRepository, times(1)).findById(documentId);
        verify(documentConverter, times(1)).toDetailResponse(document);
    }

    @Test
    void whenFindNonExistingDocument_thenThrowsRuntimeException() {
        // Caso de error: El documento no existe
        Long nonExistentId = 99L;
        given(documentRepository.findById(nonExistentId)).willReturn(Optional.empty());

        // Validamos que se lance la excepción correcta con el mensaje esperado
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            documentService.getDocumentById(nonExistentId);
        });

        assertTrue(exception.getMessage().contains("Documento no encontrado con ID: " + nonExistentId));
        verify(documentRepository, times(1)).findById(nonExistentId);
        verify(documentConverter, never()).toDetailResponse(any());
    }

//    @Test
//    void whenUserIsSuperUser_thenReturnsAllDocuments() {
//        // Simulación del contexto de seguridad para el rol SUPER_USER
//        Authentication authentication = mock(Authentication.class);
//        SecurityContext securityContext = mock(SecurityContext.class);
//        given(securityContext.getAuthentication()).willReturn(authentication);
//        SecurityContextHolder.setContext(securityContext);
//        given(authentication.isAuthenticated()).willReturn(true);
//
//        // Crear una lista de GrantedAuthority para el rol de SUPER_USER
//        Collection<GrantedAuthority> authorities =
//                Collections.singletonList(new SimpleGrantedAuthority("ROLE_SUPER_USER"));
//
//        // Mockear el comportamiento de getAuthorities()
//        given(authentication.getAuthorities()).willReturn(authorities);
//
//        // Mockear el repositorio para devolver una lista completa de documentos
//        List<Document> allDocs = Arrays.asList(new Document(), new Document());
//        given(documentRepository.findByTypeAndFolder(any(), any())).willReturn(allDocs);
//        given(documentConverter.toSummaryResponse(any())).willReturn(new DocumentDetailResponseDto());
//
//        // Ejecutar
//        List<DocumentDetailResponseDto> result = documentService.getAllDocuments(null, null);
//
//        // Verificaciones
//        assertFalse(result.isEmpty());
//        assertEquals(2, result.size());
//        verify(documentRepository, times(1)).findByTypeAndFolder(null, null);
//        verify(documentRepository, never()).findDocumentsByRoleAndTypeAndFolder(any(), any(), any());
//    }
//
//    @Test
//    void whenUserIsCollaborator_thenReturnsFilteredDocuments() {
//        // Simular un usuario con rol COLABORADOR
//        given(securityContext.getAuthentication()).willReturn(authentication);
//        SecurityContextHolder.setContext(securityContext);
//        given(authentication.isAuthenticated()).willReturn(true);
//        given(authentication.getAuthorities()).willReturn(Collections.singleton(new SimpleGrantedAuthority("ROLE_COLABORADOR")));
//
//        // Mockear el repositorio para devolver documentos filtrados por rol
//        List<Document> filteredDocs = Collections.singletonList(new Document());
//
//        String typeRequest = "type";
//        String folderRequest = "folder";
//
//        given(documentRepository.findDocumentsByRoleAndTypeAndFolder(
//                any(Set.class),
//                eq(typeRequest),
//                eq(folderRequest)
//        )).willReturn(filteredDocs);
//
//        given(documentConverter.toSummaryResponse(any(Document.class)))
//                .willReturn(new DocumentDetailResponseDto());
//
//        // Ejecutar
//        List<DocumentDetailResponseDto> result = documentService.getAllDocuments(typeRequest, folderRequest);
//
//        // Verificaciones
//        assertFalse(result.isEmpty());
//        assertEquals(1, result.size());
//
//        // CORRECCIÓN: Eliminar los nombres de los parámetros
//        verify(documentRepository, times(1)).findDocumentsByRoleAndTypeAndFolder(
//                any(),
//                eq(typeRequest),
//                eq(folderRequest)
//        );
//    }

    @Test
    void whenUpdateDocumentWithValidData_thenSavesAndReturnsUpdatedDocument() {
        // Caso de éxito
        Long documentId = 1L;
        Document existingDocument = new Document();
        DocumentRequestDto updateRequest = new DocumentRequestDto();
        updateRequest.setName("Nuevo Nombre");
        updateRequest.setContent("Nuevo Contenido");

        Document updatedDocument = new Document();
        updatedDocument.setName("Nuevo Nombre");
        updatedDocument.setContent("Nuevo Contenido");

        DocumentDetailResponseDto responseDto = new DocumentDetailResponseDto();
        // Crear mocks de las clases necesarias
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        given(securityContext.getAuthentication()).willReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        given(authentication.getName()).willReturn("Correo@hotmail.com");

        given(documentRepository.findById(documentId)).willReturn(Optional.of(existingDocument));
        given(documentRepository.save(any(Document.class))).willReturn(updatedDocument);
        given(documentConverter.toDetailResponse(updatedDocument)).willReturn(responseDto);

        DocumentDetailResponseDto result = documentService.updateDocument(documentId, updateRequest);

        assertNotNull(result);
        verify(documentRepository, times(1)).findById(documentId);
        verify(documentRepository, times(1)).save(any(Document.class));
    }

    @Test
    void whenUpdateNonExistingDocument_thenThrowsRuntimeException() {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        given(securityContext.getAuthentication()).willReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        given(authentication.getName()).willReturn("Correo@hotmail.com");

        // Caso de error: el documento no existe en el repositorio
        Long nonExistentId = 99L;
        DocumentRequestDto requestDto = new DocumentRequestDto();
        requestDto.setName("Nombre de prueba");

        // Mockear el repositorio para que devuelva un Optional vacío
        given(documentRepository.findById(nonExistentId)).willReturn(Optional.empty());

        // Verificamos que se lanza la excepción y que el mensaje es el correcto
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            documentService.updateDocument(nonExistentId, requestDto);
        });

        // Aseguramos que el mensaje de la excepción es el esperado
        assertTrue(exception.getMessage().contains("Documento no encontrado con ID: " + nonExistentId));

        // Verificamos que no se realizó ninguna operación de guardado
        verify(documentRepository, times(1)).findById(nonExistentId);
        verify(documentRepository, never()).save(any());
    }
}
