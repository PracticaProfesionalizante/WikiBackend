package com.teclab.practicas.WikiBackend.service.document;

import com.teclab.practicas.WikiBackend.converter.document.DocumentConverter;
import com.teclab.practicas.WikiBackend.dto.documents.DocumentDetailResponseDto;
import com.teclab.practicas.WikiBackend.dto.documents.DocumentFileRequestDto;
import com.teclab.practicas.WikiBackend.dto.documents.DocumentRequestDto;
import com.teclab.practicas.WikiBackend.entity.Document;
import com.teclab.practicas.WikiBackend.entity.Roles;
import com.teclab.practicas.WikiBackend.exception.FileSizeExceededException;
import com.teclab.practicas.WikiBackend.exception.InvalidFileTypeException;
import com.teclab.practicas.WikiBackend.exception.PathDuplicado;
import com.teclab.practicas.WikiBackend.repository.DocumentRepository;
import com.teclab.practicas.WikiBackend.repository.RolesRepository;
import com.teclab.practicas.WikiBackend.service.file.FileStorageService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
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
@Slf4j
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
            // Validación de request y archivo
            if (request == null) {
                throw new IllegalArgumentException("La solicitud no puede ser nula");
            }

            MultipartFile file = request.getFile();
            validateFile(file);

            // Usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new IllegalStateException("Usuario no autenticado");
            }
            String currentUsername = authentication.getName();

            // Roles
            Set<Roles> roles = setRoles(request.getRoles());

            // Persistir archivo y documento
            String storedFilePath = fileStorageService.storeFile(file);

            validateSlug(request.getSlug());

            Document newDocument = documentConverter.toEntity(
                    request,
                    roles,
                    currentUsername,
                    currentUsername
            );
            newDocument.setContent(storedFilePath);



            Document savedDocument = documentRepository.save(newDocument);
            return documentConverter.toDetailResponse(savedDocument);
        } catch (InvalidFileTypeException | FileSizeExceededException e) {
            log.warn("Validación de archivo fallida: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            log.error("Error al crear documento de archivo", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public DocumentDetailResponseDto updateFileDocument(Long id, DocumentFileRequestDto request) {
        try {
            // Validación de request y archivo
            if (request == null) {
                throw new IllegalArgumentException("La solicitud no puede ser nula");
            }

            MultipartFile file = request.getFile();
            validateFile(file);

            // Usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new IllegalStateException("Usuario no autenticado");
            }
            String currentUsername = authentication.getName();

            // Buscar documento existente
            Document document = documentRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Documento no encontrado con ID: " + id));

            // Actualizar campos opcionales provenientes del request
            if (request.getName() != null && !request.getName().isBlank()) document.setName(request.getName());
            if (request.getIcon() != null && !request.getIcon().isBlank()) document.setIconName(request.getIcon());
            if (request.getSlug() != null && !request.getSlug().isBlank()) document.setSlug(request.getSlug());
            if (request.getType() != null && !request.getType().isBlank()) {
                try {
                    document.setType(Document.TypeName.valueOf(request.getType()));
                } catch (RuntimeException ex) {
                    throw new IllegalArgumentException("Tipo de archivo invalido");
                }
            }
            if (request.getRoles() != null && !request.getRoles().isEmpty()) {
                document.setRoles(setRoles(request.getRoles()));
            }

            // Reemplazar archivo físico
            String oldPath = document.getContent();
            String storedFilePath = fileStorageService.storeFile(file);
            document.setContent(storedFilePath);

            // Borrar el archivo anterior (si existía)
            if (oldPath != null && !oldPath.isBlank()) {
                try {
                    fileStorageService.deleteFile(oldPath);
                } catch (Exception ex) {
                    // No interrumpimos la actualización si falla el borrado del archivo previo
                    log.warn("No se pudo eliminar el archivo anterior en '{}': {}", oldPath, ex.getMessage());
                }
            }

            document.setUpdatedBy(currentUsername);

            Document savedDocument = documentRepository.save(document);
            return documentConverter.toDetailResponse(savedDocument);
        } catch (InvalidFileTypeException | FileSizeExceededException e) {
            log.warn("Validación de archivo fallida: {}", e.getMessage());
            throw e;
        } catch (RuntimeException e) {
            log.error("Error al actualizar documento de archivo", e);
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

            // Usuario autenticado
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new IllegalStateException("Usuario no autenticado");
            }
            List<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            boolean hasMatch = roles.stream()
                    .anyMatch(document.getRoles().stream().map(it -> it.getName().name()).toList()::contains);

            if (!hasMatch) {
                throw new AccessDeniedException("No tienes permiso para ver este documento");
            }
            return documentConverter.toDetailResponse(document);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Resource getFileResourceByDocumentId(Long id) {
        try {
            Document document = documentRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Documento no encontrado con ID: " + id));

            if (document.getType() == null || document.getType() != Document.TypeName.TYPE_PDF) {
                throw new IllegalArgumentException("El documento solicitado no es de tipo FILE/PDF");
            }
            if (document.getContent() == null || document.getContent().isBlank()) {
                throw new IllegalArgumentException("El documento no tiene un archivo asociado");
            }

            return fileStorageService.loadFileAsResource(document.getContent());
        } catch (RuntimeException e) {
            log.error("Error al recuperar archivo de documento", e);
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
            if (typeRequest != null) type = Document.TypeName.valueOf(typeRequest);

            Set<String> userRoles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());

            List<Document> documents;
            if (userRoles.contains("ROLE_SUPER_USER"))
                documents = documentRepository.findByTypeAndFolder(type, folderRequest);
            else documents = documentRepository.findDocumentsByRoleAndTypeAndFolder(userRoles, type, folderRequest);

            return documents.stream()
                    .map(doc -> {
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

            validateSlug(request.getSlug());

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
            if (request.getContent() != null && !request.getContent().isBlank())
                document.setContent(request.getContent());
            if (request.getRoles() != null && !request.getRoles().isEmpty())
                document.setRoles(setRoles(request.getRoles()));
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

    @Override
    @Transactional
    public void deleteFileDocument(Long id) {
        try {
            Document document = documentRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Documento no encontrado con ID: " + id));

            if (document.getType() == null || document.getType() != Document.TypeName.TYPE_PDF) {
                throw new IllegalArgumentException("El documento solicitado no es de tipo FILE/PDF");
            }

            String path = document.getContent();
            if (path != null && !path.isBlank()) {
                try {
                    fileStorageService.deleteFile(path);
                } catch (Exception ex) {
                    log.warn("No se pudo eliminar el archivo físico '{}': {}", path, ex.getMessage());
                }
            }

            documentRepository.deleteById(id);
        } catch (RuntimeException e) {
            log.error("Error al eliminar documento de archivo", e);
            throw e;
        }
    }

    private Set<Roles> setRoles(Set<String> rolesName) {
        if (rolesName == null || rolesName.isEmpty())
            throw new IllegalArgumentException("Debe ingresar al menos un rol");

        return rolesName.stream()
                .map(roleName -> rolesRepository.findByName(Roles.RoleName.valueOf("ROLE_" + roleName))
                        .orElseThrow(() -> new EntityNotFoundException("Role no encontrado con el siguiente nombre: " + roleName)))
                .collect(Collectors.toSet());
    }

    /**
     * Valida que el archivo no sea nulo/ vacío, sea PDF y no exceda el límite configurado.
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo no puede estar vacío");
        }

        // Validación de tipo de contenido: tolera variantes (e.g. application/pdf; charset=binary)
        String contentType = file.getContentType();
        boolean isPdfByContentType = contentType != null && contentType.toLowerCase().startsWith(MediaType.APPLICATION_PDF_VALUE);

        // Validación por firma mágica (%PDF-) para mayor robustez
        boolean isPdfByMagic = false;
        try (java.io.InputStream is = file.getInputStream()) {
            byte[] header = new byte[5];
            int read = is.read(header, 0, 5);
            if (read == 5) {
                isPdfByMagic = header[0] == '%'
                        && header[1] == 'P'
                        && header[2] == 'D'
                        && header[3] == 'F'
                        && header[4] == '-';
            }
        } catch (Exception ignored) {
            // Si falla la lectura del header, nos quedamos con la validación por content-type
        }

        if (!(isPdfByContentType || isPdfByMagic)) {
            throw new InvalidFileTypeException("Solo se permiten archivos PDF.");
        }

        long limitBytes = MAX_FILE_SIZE_BYTES.toBytes();
        if (file.getSize() > limitBytes) {
            throw new FileSizeExceededException("El archivo excede el límite de " + MAX_FILE_SIZE_BYTES.toMegabytes() + "MB.");
        }
    }

    private void validateSlug(String slug){
        if (documentRepository.existsBySlug(slug)) {
            throw new PathDuplicado("El slug " + slug + " ya existe");
        }
    }
}
