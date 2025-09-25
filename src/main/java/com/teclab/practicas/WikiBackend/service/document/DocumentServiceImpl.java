package com.teclab.practicas.WikiBackend.service.document;

import com.teclab.practicas.WikiBackend.converter.document.DocumentConverter;
import com.teclab.practicas.WikiBackend.dto.documents.DocumentDetailResponseDto;
import com.teclab.practicas.WikiBackend.dto.documents.DocumentFileRequestDto;
import com.teclab.practicas.WikiBackend.dto.documents.DocumentRequestDto;
import com.teclab.practicas.WikiBackend.entity.Document;
import com.teclab.practicas.WikiBackend.entity.Roles;
import com.teclab.practicas.WikiBackend.exception.FileSizeExceededException;
import com.teclab.practicas.WikiBackend.exception.InvalidFileTypeException;
import com.teclab.practicas.WikiBackend.repository.DocumentRepository;
import com.teclab.practicas.WikiBackend.repository.RolesRepository;
import com.teclab.practicas.WikiBackend.service.file.FileStorageService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documentRepository;
    private final RolesRepository rolesRepository;
    private final DocumentConverter documentConverter;
    private final FileStorageService fileStorageService;

    @Value("${app.upload.max-file-size-bytes:50MB}")
    private DataSize MAX_FILE_SIZE_BYTES;

    @Autowired
    public DocumentServiceImpl(
            DocumentRepository documentRepository,
            DocumentConverter documentConverter,
            FileStorageService fileStorageService,
            RolesRepository rolesRepository
    ) {
        this.documentRepository = documentRepository;
        this.documentConverter = documentConverter;
        this.fileStorageService = fileStorageService;
        this.rolesRepository = rolesRepository;
    }


//    ---------------------------------------------------------------------------------------
//    -                               DOCUMENT FILE                                         -
//    ---------------------------------------------------------------------------------------
    @Transactional
    public DocumentDetailResponseDto createFileDocument(DocumentFileRequestDto request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();

//            Set<Roles> roles = setRoles(Set.of());
            Set<Roles> roles = setRoles(request.getRoles());

            Document newDocument = documentConverter.toEntity(
                    request,
                    roles,
                    currentUsername,
                    currentUsername
            );

            MultipartFile file = request.getFile();

            if (!"application/pdf".equals(file.getContentType())) {
                throw new InvalidFileTypeException("Solo se permiten archivos PDF.");
            }

            long limitBytes = MAX_FILE_SIZE_BYTES.toBytes();
            if (file.getSize() > limitBytes) {
                throw new FileSizeExceededException("El archivo excede el límite de 10MB.");
            }

            String storedFilePath = fileStorageService.storeFile(file);
            newDocument.setContent(storedFilePath);

            Document savedDocument = documentRepository.save(newDocument);

            return documentConverter.toDetailResponse(savedDocument);
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }




//    ---------------------------------------------------------------------------------------
//    -                             DOCUMENT TEXT y URL                                     -
//    ---------------------------------------------------------------------------------------
    @Override
    public DocumentDetailResponseDto getDocumentById(Long id) {
        try {
            Document document = documentRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Documento no encontrado con ID: " + id));
            return documentConverter.toDetailResponse(document);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentDetailResponseDto> getAllDocuments(String typeRequest, String folderRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return Collections.emptyList();
            }

            Document.TypeName type = null;
            if (typeRequest != null ) type = Document.TypeName.valueOf(typeRequest);

            Set<String> userRoles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());

            List<Document> documents;
            if (userRoles.contains("ROLE_SUPER_USER")) documents = documentRepository.findByTypeAndFolder(type, folderRequest);
            else documents = documentRepository.findDocumentsByRoleAndTypeAndFolder(userRoles, type, folderRequest);

            return documents.stream()
                    .map( doc -> {
                        if (doc.getType() == Document.TypeName.TYPE_URL) return documentConverter.toDetailResponse(doc);
                        else return documentConverter.toSummaryResponse(doc);
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }

    @Override
    public DocumentDetailResponseDto createDocument(DocumentRequestDto request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();

            Set<Roles> roles = setRoles(request.getRoles());

            Document newDocument = documentConverter.toEntity(
                    request,
                    roles,
                    currentUsername,
                    currentUsername
            );

            Document savedDocument = documentRepository.save(newDocument);
            return documentConverter.toDetailResponse(savedDocument);
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }

    @Override
    public DocumentDetailResponseDto updateDocument(Long id, DocumentRequestDto request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();

            Document document = documentRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Documento no encontrado con ID: " + id));

            if (request.getName() != null && !request.getName().isBlank()) document.setName(request.getName());
            if (request.getIcon() != null && !request.getIcon().isBlank()) document.setIconName(request.getIcon());
            if (request.getContent() != null && !request.getContent().isBlank()) document.setContent(request.getContent());
            if (request.getRoles() != null && !request.getRoles().isEmpty()) document.setRoles(setRoles(request.getRoles()));
            document.setUpdatedBy(currentUsername);

            Document savedDocument = documentRepository.save(document);
            return documentConverter.toDetailResponse(savedDocument);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }

    @Override
    public void deleteDocument(Long id) {
        try {
            if (documentRepository.existsById(id)) {
                documentRepository.deleteById(id);
            } else {
                throw new RuntimeException("Documento no encontrado con ID: " + id);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }

    private Set<Roles> setRoles(Set<String> rolesName){
        if (rolesName == null || rolesName.isEmpty()) throw new IllegalArgumentException("Debe ingresar al menos un rol");

        return rolesName.stream()
                .map(roleName -> rolesRepository.findByName(Roles.RoleName.valueOf("ROLE_" + roleName))
                        .orElseThrow(() -> new EntityNotFoundException("Role no encontrado con el siguiente nombre: " + roleName)))
                .collect(Collectors.toSet());
    }
}
