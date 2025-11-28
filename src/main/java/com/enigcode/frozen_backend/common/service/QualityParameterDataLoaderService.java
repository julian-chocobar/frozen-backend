package com.enigcode.frozen_backend.common.service;

import com.enigcode.frozen_backend.product_phases.model.Phase;
import com.enigcode.frozen_backend.quality_parameters.DTO.QualityParameterCreateDTO;
import com.enigcode.frozen_backend.quality_parameters.repository.QualityParameterRepository;
import com.enigcode.frozen_backend.quality_parameters.service.QualityParameterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class QualityParameterDataLoaderService {

    private final QualityParameterRepository qualityParameterRepository;
    private final QualityParameterService qualityParameterService;

    public void loadQualityParameters() {
        if (qualityParameterRepository.count() > 0)
            return;

        log.info("Generando parámetros de calidad por fase...");
        List<QualityParameterCreateDTO> parameters = List.of(
                // Molienda
                QualityParameterCreateDTO.builder()
                        .phase(Phase.MOLIENDA)
                        .name("Granulometria")
                        .description("Molienda homogénea, base para extracción eficiente.")
                        .unit("μm")
                        .information("Valor válido: '350 μm' (granulometría media), cualquier valor mayor a 500 μm sería rechazado")
                        .isCritical(true)
                        .build(),
                QualityParameterCreateDTO.builder()
                        .phase(Phase.MOLIENDA)
                        .name("Humedad Malta")
                        .description("Control de humedad post molienda para evitar compactación.")
                        .unit("%")
                        .information("Valor válido: '4.5 %', por encima de 6 % se considera húmedo en exceso")
                        .isCritical(false)
                        .build(),
                // Maceración
                QualityParameterCreateDTO.builder()
                        .phase(Phase.MACERACION)
                        .name("Temp Macerac")
                        .description("Temperatura de maceración en rango objetivo.")
                        .unit("°C")
                        .information("Valor válido: '66 °C', menos de 60 °C afecta conversión enzimática")
                        .isCritical(true)
                        .build(),
                QualityParameterCreateDTO.builder()
                        .phase(Phase.MACERACION)
                        .name("pH Macerac")
                        .description("pH del mosto entre 5.2 - 5.6.")
                        .unit("pH")
                        .information("Valor válido: '5.4 pH', si supera 5.8 pH se considera fuera de rango")
                        .isCritical(true)
                        .build(),
                // Filtración
                QualityParameterCreateDTO.builder()
                        .phase(Phase.FILTRACION)
                        .name("Claridad")
                        .description("Nivel de turbidez permisible del mosto filtrado.")
                        .unit("NTU")
                        .information("Valor válido: '15 NTU', mayor a 50 NTU indicaría turbidez elevada")
                        .isCritical(false)
                        .build(),
                QualityParameterCreateDTO.builder()
                        .phase(Phase.FILTRACION)
                        .name("Temp Filtrado")
                        .description("Temperatura de salida tras filtrado.")
                        .unit("°C")
                        .information("Valor válido: '78 °C', menos de 70 °C podría afectar separación")
                        .isCritical(false)
                        .build(),
                // Cocción
                QualityParameterCreateDTO.builder()
                        .phase(Phase.COCCION)
                        .name("Plato Final")
                        .description("Grados plato finales tras evaporación.")
                        .unit("°P")
                        .information("Valor válido: '12.5 °P', por debajo de 10 °P indica falta de evaporación")
                        .isCritical(true)
                        .build(),
                QualityParameterCreateDTO.builder()
                        .phase(Phase.COCCION)
                        .name("Tiempo Lupulo")
                        .description("Tiempo exacto de adición de lúpulo aromático.")
                        .unit("min")
                        .information("Valor válido: '10 min', menos de 5 min reduce el aporte aromático")
                        .isCritical(false)
                        .build(),
                // Fermentación
                QualityParameterCreateDTO.builder()
                        .phase(Phase.FERMENTACION)
                        .name("Temp Ferm")
                        .description("Temperatura controlada según perfil de levadura.")
                        .unit("°C")
                        .information("Valor válido: '19 °C', más de 24 °C para ales se considera fuera de rango")
                        .isCritical(true)
                        .build(),
                QualityParameterCreateDTO.builder()
                        .phase(Phase.FERMENTACION)
                        .name("Densidad Ferm")
                        .description("Densidad específica diaria para seguimiento de atenuación.")
                        .unit("SG")
                        .information("Valor válido: '1.010 SG', si queda por encima de 1.020 SG indica fermentación incompleta")
                        .isCritical(true)
                        .build(),
                // Desalcoholización
                QualityParameterCreateDTO.builder()
                        .phase(Phase.DESALCOHOLIZACION)
                        .name("Alcohol Final")
                        .description("Porcentaje de alcohol residual en cerveza sin alcohol.")
                        .unit("% ABV")
                        .information("Valor válido: '0.4 % ABV', por encima de 0.5 % ABV no cumple sin alcohol")
                        .isCritical(true)
                        .build(),
                QualityParameterCreateDTO.builder()
                        .phase(Phase.DESALCOHOLIZACION)
                        .name("Temp Columna")
                        .description("Temperatura de columna de adsorción.")
                        .unit("°C")
                        .information("Valor válido: '65 °C', más de 75 °C afecta aromas")
                        .isCritical(false)
                        .build(),
                // Maduración
                QualityParameterCreateDTO.builder()
                        .phase(Phase.MADURACION)
                        .name("Diacetilo")
                        .description("Nivel de diacetilo por debajo del umbral sensorial.")
                        .unit("ppm")
                        .information("Valor válido: '0.08 ppm', superar 0.15 ppm genera sabores mantecosos")
                        .isCritical(true)
                        .build(),
                QualityParameterCreateDTO.builder()
                        .phase(Phase.MADURACION)
                        .name("Turbidez")
                        .description("Control visual de sedimentos previos a gasificación.")
                        .unit("EBC")
                        .information("Valor válido: '5 EBC', mayor a 20 EBC sugiere sedimentos en suspensión")
                        .isCritical(false)
                        .build(),
                // Gasificación
                QualityParameterCreateDTO.builder()
                        .phase(Phase.GASIFICACION)
                        .name("CO2 Volumen")
                        .description("Volumen final de CO2 disuelto.")
                        .unit("vol CO2")
                        .information("Valor válido: '2.4 vol CO2', menos de 1.8 vol produce carbonatación insuficiente")
                        .isCritical(true)
                        .build(),
                QualityParameterCreateDTO.builder()
                        .phase(Phase.GASIFICACION)
                        .name("Presion Tank")
                        .description("Presión alcanzada en tanques de gasificación.")
                        .unit("psi")
                        .information("Valor válido: '18 psi', pasar de 25 psi puede comprometer válvulas")
                        .isCritical(false)
                        .build(),
                // Envasado
                QualityParameterCreateDTO.builder()
                        .phase(Phase.ENVASADO)
                        .name("Sellado")
                        .description("Integridad del cierre en botellas o barriles.")
                        .unit("Estado")
                        .information("Valor válido: 'Hermético', cualquier anotación diferente implica fallo de sellado")
                        .isCritical(true)
                        .build(),
                QualityParameterCreateDTO.builder()
                        .phase(Phase.ENVASADO)
                        .name("Etiquetado")
                        .description("Revisión visual de etiquetado y codificación.")
                        .unit("Estado")
                        .information("Valor válido: 'OK', valores como 'Desalineada' o 'Sin lote' marcan rechazo visual")
                        .isCritical(false)
                        .build());

        parameters.forEach(qualityParameterService::createQualityParameter);
    }
}

