package com.teclab.practicas.WikiBackend.controller;

import com.teclab.practicas.WikiBackend.dto.documents.DocumentDetailResponseDto;
import com.teclab.practicas.WikiBackend.dto.documents.DocumentFileRequestDto;
import com.teclab.practicas.WikiBackend.dto.documents.DocumentRequestDto;
import com.teclab.practicas.WikiBackend.service.document.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Documentos", description = "Gestión de contenido de capacitación (Textos y URLs)")
@RestController
@RequestMapping("/documents")
@Validated
@SecurityRequirement(name = "Bearer Authentication")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    //---------------------------------------------------------------------------------------
    //-                               DOCUMENT FILE                                         -
    //---------------------------------------------------------------------------------------
    @Operation(
            summary = "Crear documento de tipo FILE (PDF)",
            description = "Recibe multipart/form-data con metadatos y el archivo PDF, y crea el documento. Retorna el detalle del documento creado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Documento creado con éxito",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DocumentDetailResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida (validación de campos)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "No autorizado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentDetailResponseDto> createFileDocument(@Valid @ModelAttribute DocumentFileRequestDto request) {
        try {
            DocumentDetailResponseDto savedDocument = documentService.createFileDocument(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedDocument);
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }

    @Operation(
            summary = "Actualizar documento de tipo FILE (PDF)",
            description = "Actualiza metadatos y/o reemplaza el archivo PDF del documento indicado por su ID.",
            parameters = {
                    @Parameter(name = "id", description = "ID del documento FILE a actualizar", required = true, example = "24")
            }
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Documento actualizado con éxito",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DocumentDetailResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida (validación de campos)",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "401", description = "No autorizado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Prohibido",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Documento no encontrado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PutMapping("/file/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_SUPER_USER')")
    public ResponseEntity<DocumentDetailResponseDto> updateDocument(
            @PathVariable Long id,
            @Valid @ModelAttribute DocumentFileRequestDto request
    ) {
        DocumentDetailResponseDto updatedDocument = documentService.updateFileDocument(id, request);
        return ResponseEntity.ok(updatedDocument);
    }

    @Operation(
            summary = "Descargar/visualizar documento de tipo FILE (PDF)",
            description = "Retorna el PDF almacenado para el documento indicado. Requiere autenticación."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Archivo recuperado con éxito"),
            @ApiResponse(responseCode = "401", description = "No autorizado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Documento o archivo no encontrado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping(value = "/file/{id}")
    public ResponseEntity<Resource> getFileDocument(@PathVariable Long id) {
        try {
            Resource resource = documentService.getFileResourceByDocumentId(id);
            // Recuperamos el nombre para cabecera (sin exponer lógica de validación en controller)
            DocumentDetailResponseDto doc = documentService.getDocumentById(id);
            String filename = (doc.getName() != null && !doc.getName().isBlank()) ? doc.getName() + ".pdf" : "document.pdf";
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header("Content-Disposition", "inline; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }

    @Operation(
            summary = "Eliminar documento de tipo FILE (PDF)",
            description = "Elimina el archivo físico y el registro del documento. Solo SUPER_USER o ADMIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Documento eliminado con éxito"),
            @ApiResponse(responseCode = "401", description = "No autorizado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "403", description = "Prohibido",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
            @ApiResponse(responseCode = "404", description = "Documento no encontrado",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PreAuthorize("hasAnyRole('ROLE_SUPER_USER', 'ROLE_ADMIN')")
    @DeleteMapping("/file/{id}")
    public ResponseEntity<?> deleteFileDocument(@PathVariable Long id) {
        try {
            documentService.deleteFileDocument(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }

    //---------------------------------------------------------------------------------------
    //-                             DOCUMENT TEXT y URL                                     -
    //---------------------------------------------------------------------------------------
    @Operation(
            summary = "Obtener un documento por ID",
            description = "Permite a cualquier usuario autenticado obtener los detalles de un documento (texto o URL). Aplica filtrado de contenido según los roles del usuario.",
            parameters = {
                    @Parameter(name = "id", description = "ID único del documento a buscar", required = true, example = "24")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Detalles del documento obtenidos con éxito",
                            content = @Content(schema = @Schema(implementation = DocumentDetailResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = "No autorizado (Token JWT inválido o ausente)",
                            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                    @ApiResponse(responseCode = "403", description = "Prohibido (Usuario autenticado pero sin permisos suficientes para el contenido)",
                            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                    @ApiResponse(responseCode = "422", description = "Documento no encontrado",
                            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
            }
    )
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<DocumentDetailResponseDto> getDocument(@PathVariable Long id) {
        try {
            DocumentDetailResponseDto documentContent = documentService.getDocumentById(id);
            return ResponseEntity.ok(documentContent);
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }

    @Operation(
            summary = "Obtener listado de documentos",
            description = "Recupera una lista de documentos detallados. Permite filtrar los resultados por 'type' o 'folder'. Requiere autenticación (JWT).",
            security = @SecurityRequirement(name = "bearerAuth") // Hace referencia a la configuración de JWT
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de documentos recuperado con éxito.",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = DocumentDetailResponseDto.class)))),
            @ApiResponse(responseCode = "401", description = "No Autorizado (Unauthorized). El token JWT es inválido o no se proporcionó.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class))), // Usamos ProblemDetail para coherencia),
            @ApiResponse(responseCode = "422", description = "No se envio un parametro correcto.",
                    content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<List<DocumentDetailResponseDto>> getAllDocument(@RequestParam(required = false) String type, @RequestParam(required = false) String folder) {
        try {
            List<DocumentDetailResponseDto> documents = documentService.getAllDocuments(type, folder);
            return ResponseEntity.ok(documents);
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }

    @Operation(
            summary = "Crear un nuevo documento de URL o TEXT",
            description = "Crea un nuevo documento de tipo URL/Link o TEXT. Solo accesible para SUPER_USER o ADMIN.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Documento creado con éxito"),
                    @ApiResponse(responseCode = "403", description = "Prohibido (Usuario autenticado sin rol Admin/SuperUser)")
            }
    )
    @PreAuthorize("hasAnyRole('ROLE_SUPER_USER', 'ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<DocumentDetailResponseDto> createDocument(@Valid @RequestBody DocumentRequestDto request) {
        try {
            DocumentDetailResponseDto documentId = documentService.createDocument(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(documentId);
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }

    @Operation(
            summary = "Actualizar un documento existente",
            description = "Permite a SUPER_USER o ADMIN modificar el contenido de un documento (Texto o URL) por su ID.",
            responses = {
                    @ApiResponse(responseCode = "202", description = "Documento actualizado correctamente"),
                    @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos",
                            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                    @ApiResponse(responseCode = "401", description = "No autorizado"),
                    @ApiResponse(responseCode = "403", description = "Prohibido (Usuario autenticado sin rol Admin/SuperUser)")
            }
    )
    @PreAuthorize("hasAnyRole('ROLE_SUPER_USER', 'ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<DocumentDetailResponseDto> editDocument(
            @PathVariable Long id,
            @RequestBody DocumentRequestDto request
    ) {
        try {
            DocumentDetailResponseDto documentId = documentService.updateDocument(id, request);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(documentId);
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }

    @Operation(
            summary = "Eliminar un documento",
            description = "Elimina permanentemente un documento por su ID. Solo accesible para SUPER_USER o ADMIN.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Documento eliminado con éxito (Sin contenido de respuesta)"),
                    @ApiResponse(responseCode = "401", description = "No autorizado"),
                    @ApiResponse(responseCode = "403", description = "Prohibido (Usuario autenticado sin rol Admin/SuperUser)"),
                    @ApiResponse(responseCode = "404", description = "Documento no encontrado")
            }
    )
    @PreAuthorize("hasAnyRole('ROLE_SUPER_USER', 'ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDocument(
            @PathVariable Long id
    ) {
        try {
            documentService.deleteDocument(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }
}