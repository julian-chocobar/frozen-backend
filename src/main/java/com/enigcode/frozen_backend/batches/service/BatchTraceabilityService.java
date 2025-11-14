package com.enigcode.frozen_backend.batches.service;

import com.enigcode.frozen_backend.batches.DTO.BatchTraceabilityDTO;
import com.enigcode.frozen_backend.batches.model.Batch;
import com.enigcode.frozen_backend.batches.repository.BatchRepository;
import com.enigcode.frozen_backend.common.exceptions_configs.exceptions.ResourceNotFoundException;
import com.enigcode.frozen_backend.production_materials.repository.ProductionMaterialRepository;
import com.enigcode.frozen_backend.production_phases.repository.ProductionPhaseRepository;
import com.enigcode.frozen_backend.production_phases.model.ProductionPhaseStatus;
import com.enigcode.frozen_backend.production_phases_qualities.repository.ProductionPhaseQualityRepository;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BatchTraceabilityService {

    private final BatchRepository batchRepository;
    private final ProductionPhaseRepository productionPhaseRepository;
    private final ProductionMaterialRepository productionMaterialRepository;
    private final ProductionPhaseQualityRepository productionPhaseQualityRepository;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Transactional(readOnly = true)
    public byte[] generateTraceabilityPDF(Long batchId) {
        // Obtener toda la información del batch
        BatchTraceabilityDTO traceabilityData = getBatchTraceabilityData(batchId);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);

            // Generar el contenido del PDF
            addHeader(document, traceabilityData);
            addBatchInformation(document, traceabilityData);
            addProductionOrderInformation(document, traceabilityData);
            addUsersInformation(document, traceabilityData);
            addPhasesInformation(document, traceabilityData);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generando PDF de trazabilidad para batch {}: {}", batchId, e.getMessage());
            throw new RuntimeException("Error generando PDF de trazabilidad", e);
        }
    }

    @Transactional(readOnly = true)
    public BatchTraceabilityDTO getBatchTraceabilityData(Long batchId) {
        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch no encontrado con id: " + batchId));

        if (batch.getProductionOrder() == null) {
            throw new ResourceNotFoundException("El batch no tiene orden de producción asociada");
        }

        // Construir DTO con toda la información
        BatchTraceabilityDTO traceability = BatchTraceabilityDTO.builder()
                // Información básica del batch
                .batchId(batch.getId())
                .batchCode(batch.getCode())
                .batchStatus(batch.getStatus())
                .quantity(batch.getQuantity())
                .creationDate(batch.getCreationDate())
                .plannedDate(batch.getPlannedDate())
                .startDate(batch.getStartDate())
                .estimatedCompletedDate(batch.getEstimatedCompletedDate())
                .completedDate(batch.getCompletedDate())

                // Información del producto
                .productName(batch.getProductionOrder().getProduct().getName())
                .productUnitMeasurement(batch.getProductionOrder().getProduct().getUnitMeasurement())
                .standardQuantity(batch.getProductionOrder().getProduct().getStandardQuantity())

                // Información del packaging
                .packagingName(batch.getPackaging().getName())
                .packagingQuantity(batch.getPackaging().getQuantity())
                .packagingUnitMeasurement(batch.getPackaging().getUnitMeasurement())

                // Información de la orden
                .productionOrderId(batch.getProductionOrder().getId())
                .orderStatus(batch.getProductionOrder().getStatus())
                .orderCreationDate(batch.getProductionOrder().getCreationDate())
                .orderValidationDate(batch.getProductionOrder().getValidationDate())

                // Usuarios
                .assignedUserName(batch.getAssignedUser() != null ? batch.getAssignedUser().getName() : "No asignado")
                .createdByUserName(batch.getProductionOrder().getCreatedByUser() != null
                        ? batch.getProductionOrder().getCreatedByUser().getName()
                        : "No registrado")
                .approvedByUserName(batch.getProductionOrder().getApprovedByUser() != null
                        ? batch.getProductionOrder().getApprovedByUser().getName()
                        : "No aprobado")

                // Fases con todos los detalles
                .phases(buildPhasesTraceability(batchId))
                .build();

        return traceability;
    }

    private List<BatchTraceabilityDTO.ProductionPhaseTraceabilityDTO> buildPhasesTraceability(Long batchId) {
        return productionPhaseRepository.findAllByBatchIdOrderByPhaseOrderAsc(batchId).stream()
                .map(phase -> {
                    // Obtener materiales solo si la fase no está PENDIENTE
                    List<BatchTraceabilityDTO.ProductionMaterialTraceabilityDTO> materials = !phase.getStatus()
                            .equals(ProductionPhaseStatus.PENDIENTE)
                                    ? productionMaterialRepository.findAllByProductionPhaseId(phase.getId()).stream()
                                            .map(pm -> BatchTraceabilityDTO.ProductionMaterialTraceabilityDTO.builder()
                                                    .materialName(pm.getMaterial().getName())
                                                    .materialType(pm.getMaterial().getType().toString())
                                                    .quantity(pm.getQuantity())
                                                    .unitMeasurement(pm.getMaterial().getUnitMeasurement())
                                                    .build())
                                            .collect(Collectors.toList())
                                    : List.of(); // Lista vacía para fases PENDIENTE

                    // Obtener parámetros de calidad de la fase
                    List<BatchTraceabilityDTO.QualityParameterTraceabilityDTO> qualityParams = productionPhaseQualityRepository
                            .findAllByProductionPhaseId(phase.getId()).stream()
                            .map(pq -> BatchTraceabilityDTO.QualityParameterTraceabilityDTO.builder()
                                    .parameterName(pq.getQualityParameter().getName())
                                    .value(pq.getValue())
                                    .unit(pq.getQualityParameter().getUnit())
                                    .isApproved(pq.getIsApproved())
                                    .isCritical(pq.getQualityParameter().getIsCritical())
                                    .realizationDate(pq.getRealizationDate())
                                    .build())
                            .collect(Collectors.toList());

                    return BatchTraceabilityDTO.ProductionPhaseTraceabilityDTO.builder()
                            .phaseId(phase.getId())
                            .phase(phase.getPhase())
                            .status(phase.getStatus())
                            .input(phase.getInput())
                            .standardInput(phase.getStandardInput())
                            .output(phase.getOutput())
                            .standardOutput(phase.getStandardOutput())
                            .outputUnit(phase.getOutputUnit())
                            .startDate(phase.getStartDate())
                            .endDate(phase.getEndDate())
                            .sectorName(phase.getSector() != null ? phase.getSector().getName() : "No asignado")
                            .materials(materials)
                            .qualityParameters(qualityParams)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private void addHeader(Document document, BatchTraceabilityDTO data) {
        Paragraph title = new Paragraph("REPORTE DE TRAZABILIDAD DE LOTE")
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(title);

        Paragraph subtitle = new Paragraph("Lote: " + data.getBatchCode())
                .setFontSize(14)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(subtitle);
    }

    private void addBatchInformation(Document document, BatchTraceabilityDTO data) {
        document.add(new Paragraph("INFORMACIÓN DEL LOTE").setBold().setFontSize(14).setMarginTop(10));

        Table table = new Table(UnitValue.createPercentArray(new float[] { 30, 70 }))
                .setWidth(UnitValue.createPercentValue(100));

        addTableRow(table, "ID del Lote:", data.getBatchId().toString());
        addTableRow(table, "Código:", data.getBatchCode());
        addTableRow(table, "Estado:", data.getBatchStatus().toString());
        addTableRow(table, "Cantidad:", data.getQuantity().toString());
        addTableRow(table, "Producto:", data.getProductName());
        addTableRow(table, "Packaging:", data.getPackagingName());
        addTableRow(table, "Fecha Creación:", formatDate(data.getCreationDate()));
        addTableRow(table, "Fecha Planificada:", formatDate(data.getPlannedDate()));
        if (data.getStartDate() != null) {
            addTableRow(table, "Fecha Inicio:", formatDate(data.getStartDate()));
        }
        if (data.getCompletedDate() != null) {
            addTableRow(table, "Fecha Completado:", formatDate(data.getCompletedDate()));
        }

        document.add(table);
    }

    private void addProductionOrderInformation(Document document, BatchTraceabilityDTO data) {
        document.add(new Paragraph("INFORMACIÓN DE LA ORDEN DE PRODUCCIÓN").setBold().setFontSize(14).setMarginTop(20));

        Table table = new Table(UnitValue.createPercentArray(new float[] { 30, 70 }))
                .setWidth(UnitValue.createPercentValue(100));

        addTableRow(table, "ID Orden:", data.getProductionOrderId().toString());
        addTableRow(table, "Estado Orden:", data.getOrderStatus().toString());
        addTableRow(table, "Fecha Creación:", formatDate(data.getOrderCreationDate()));
        if (data.getOrderValidationDate() != null) {
            addTableRow(table, "Fecha Aprobación:", formatDate(data.getOrderValidationDate()));
        }

        document.add(table);
    }

    private void addUsersInformation(Document document, BatchTraceabilityDTO data) {
        document.add(new Paragraph("USUARIOS INVOLUCRADOS").setBold().setFontSize(14).setMarginTop(20));

        Table table = new Table(UnitValue.createPercentArray(new float[] { 30, 70 }))
                .setWidth(UnitValue.createPercentValue(100));

        addTableRow(table, "Usuario Asignado:", data.getAssignedUserName());
        addTableRow(table, "Creado Por:", data.getCreatedByUserName());
        addTableRow(table, "Aprobado Por:", data.getApprovedByUserName());

        document.add(table);
    }

    private void addPhasesInformation(Document document, BatchTraceabilityDTO data) {
        document.add(new Paragraph("FASES DE PRODUCCIÓN").setBold().setFontSize(14).setMarginTop(20));

        for (BatchTraceabilityDTO.ProductionPhaseTraceabilityDTO phase : data.getPhases()) {
            addPhaseDetails(document, phase);
        }
    }

    private void addPhaseDetails(Document document, BatchTraceabilityDTO.ProductionPhaseTraceabilityDTO phase) {
        // Título de la fase
        document.add(new Paragraph("FASE: " + phase.getPhase().toString())
                .setBold()
                .setFontSize(12)
                .setMarginTop(15)
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setPadding(5));

        // Información básica de la fase
        Table phaseTable = new Table(UnitValue.createPercentArray(new float[] { 25, 25, 25, 25 }))
                .setWidth(UnitValue.createPercentValue(100));

        phaseTable.addHeaderCell("Estado");
        phaseTable.addHeaderCell("Sector");
        phaseTable.addHeaderCell("Fecha Inicio");
        phaseTable.addHeaderCell("Fecha Fin");

        phaseTable.addCell(phase.getStatus().toString());
        phaseTable.addCell(phase.getSectorName());
        phaseTable.addCell(formatDate(phase.getStartDate()));
        phaseTable.addCell(formatDate(phase.getEndDate()));

        document.add(phaseTable);

        // Materiales utilizados en la fase
        if (!phase.getMaterials().isEmpty()) {
            document.add(new Paragraph("Materiales Utilizados:").setBold().setMarginTop(10));

            Table materialsTable = new Table(UnitValue.createPercentArray(new float[] { 40, 30, 30 }))
                    .setWidth(UnitValue.createPercentValue(100));

            materialsTable.addHeaderCell("Material");
            materialsTable.addHeaderCell("Tipo");
            materialsTable.addHeaderCell("Cantidad");

            for (BatchTraceabilityDTO.ProductionMaterialTraceabilityDTO material : phase.getMaterials()) {
                materialsTable.addCell(material.getMaterialName());
                materialsTable.addCell(material.getMaterialType());
                materialsTable.addCell(material.getQuantity() + " " + material.getUnitMeasurement());
            }

            document.add(materialsTable);
        }

        // Parámetros de calidad
        if (!phase.getQualityParameters().isEmpty()) {
            document.add(new Paragraph("Parámetros de Calidad:").setBold().setMarginTop(10));

            Table qualityTable = new Table(UnitValue.createPercentArray(new float[] { 25, 25, 15, 15, 20 }))
                    .setWidth(UnitValue.createPercentValue(100));

            qualityTable.addHeaderCell("Parámetro");
            qualityTable.addHeaderCell("Valor");
            qualityTable.addHeaderCell("Aprobado");
            qualityTable.addHeaderCell("Crítico");
            qualityTable.addHeaderCell("Fecha");

            for (BatchTraceabilityDTO.QualityParameterTraceabilityDTO param : phase.getQualityParameters()) {
                qualityTable.addCell(param.getParameterName());
                qualityTable.addCell(param.getValue() != null ? param.getValue() + " " + param.getUnit() : "N/A");
                qualityTable.addCell(param.getIsApproved() ? "Sí" : "No");
                qualityTable.addCell(param.getIsCritical() ? "Sí" : "No");
                qualityTable.addCell(formatDate(param.getRealizationDate()));
            }

            document.add(qualityTable);
        }
    }

    private void addTableRow(Table table, String label, String value) {
        table.addCell(new Cell().add(new Paragraph(label).setBold()));
        table.addCell(value != null ? value : "N/A");
    }

    private String formatDate(java.time.OffsetDateTime date) {
        return date != null ? date.format(DATE_FORMAT) : "N/A";
    }
}