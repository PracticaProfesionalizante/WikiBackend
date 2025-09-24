package com.teclab.practicas.WikiBackend.controller;

import com.teclab.practicas.WikiBackend.dto.documents.DocumentDetailResponseDto;
import com.teclab.practicas.WikiBackend.dto.documents.DocumentRequestDto;
import com.teclab.practicas.WikiBackend.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

    // --- Endpoints Comunes (GET, PUT, DELETE) ----------------------------
    @Operation(
            summary = "Obtener un documento por ID",
            description = "Permite a cualquier usuario autenticado obtener los detalles de un documento (texto o URL). Aplica filtrado de contenido según los roles del usuario.",
            parameters = {
                    @Parameter(name = "id", description = "ID único del documento a buscar", required = true, example = "101")
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Detalles del documento obtenidos con éxito"),
                    @ApiResponse(responseCode = "401", description = "No autorizado (Token JWT inválido o ausente)"),
                    @ApiResponse(responseCode = "403", description = "Prohibido (Usuario autenticado pero sin permisos suficientes para el contenido)"),
                    @ApiResponse(responseCode = "404", description = "Documento no encontrado",
                            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
            }
    )
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public ResponseEntity<DocumentDetailResponseDto> getDocument(@PathVariable Long id) {
        try {
            DocumentDetailResponseDto documentContent = documentService.getDocumentByRoles(id);
            return ResponseEntity.ok(documentContent);
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
    public ResponseEntity<DocumentDetailResponseDto> editUrlDocument(
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
    public ResponseEntity<?> deleteUrlDocument(
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
    //-------------------------------------------------------------------------

    //--------------------- REQUEST PARA TEXT ----------------------------
    @Operation(
            summary = "Crear un nuevo documento de Texto",
            description = "Crea un nuevo documento de tipo TEXT. Requiere autenticación y validación de los campos de `DocumentRequestDto`.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Documento de texto creado con éxito"),
                    @ApiResponse(responseCode = "422", description = "Fallo en la validación de campos del DTO",
                            content = @Content(schema = @Schema(implementation = ProblemDetail.class))),
                    @ApiResponse(responseCode = "401", description = "No autorizado")
            }
    )
    @PreAuthorize("hasAnyRole('ROLE_SUPER_USER', 'ROLE_ADMIN')")
    @PostMapping("/text")
    public ResponseEntity<DocumentDetailResponseDto> createDocument(@Valid @RequestBody DocumentRequestDto request) {
        try {
            DocumentDetailResponseDto newDocument = documentService.createText(request);
            return new ResponseEntity<>(newDocument, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }

    @Operation(
            summary = "Obtener todos los documentos de Texto",
            description = "Devuelve una lista de todos los documentos de tipo TEXT. Requiere autenticación."
    )
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/texts")
    public ResponseEntity<List<DocumentDetailResponseDto>> getAllTextDocuments() {
        try {
            List<DocumentDetailResponseDto> documents = documentService.getAllTextDocuments();
            return ResponseEntity.ok(documents);
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }
    //-------------------------------------------------------------------------

    //--------------------- REQUEST PARA URL ----------------------------
    @Operation(
            summary = "Crear un nuevo documento de URL",
            description = "Crea un nuevo documento de tipo URL/Link. Solo accesible para SUPER_USER o ADMIN.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Documento de URL creado con éxito"),
                    @ApiResponse(responseCode = "403", description = "Prohibido (Usuario autenticado sin rol Admin/SuperUser)")
            }
    )
    @PreAuthorize("hasAnyRole('ROLE_SUPER_USER', 'ROLE_ADMIN')")
    @PostMapping("/url")
    public ResponseEntity<DocumentDetailResponseDto> createUrlDocument(@Valid @RequestBody DocumentRequestDto request) {
        try {
            DocumentDetailResponseDto documentId = documentService.createDocument(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(documentId);
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }

    @Operation(
            summary = "Obtener todos los documentos de URL",
            description = "Devuelve una lista de todos los documentos de tipo URL/Link. Requiere autenticación."
    )
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/urls")
    public ResponseEntity<List<DocumentDetailResponseDto>> getAllUrlDocument() {
        try {
            List<DocumentDetailResponseDto> documents = documentService.getAllUrlDocuments();
            return ResponseEntity.ok(documents);
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }
    //-------------------------------------------------------------------------
}