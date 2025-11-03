package com.enigcode.frozen_backend.product_phases.model;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class PhaseTest {

    @Test
    void allPhases_haveUniqueOrders() {
        // Given: Todas las fases
        Phase[] phases = Phase.values();
        
        // When/Then: Verificar que cada fase tiene un orden único del 1 al 9
        assertThat(phases).hasSize(9);
        
        for (int i = 1; i <= 9; i++) {
            final int order = i;
            assertThat(phases)
                .filteredOn(p -> p.getOrder().equals(order))
                .hasSize(1);
        }
    }

    @Test
    void activePhases_areCorrectlyIdentified() {
        // Given/When/Then: Fases activas (requieren trabajo continuo)
        assertThat(Phase.MOLIENDA.getIsTimeActive()).isTrue();
        assertThat(Phase.MACERACION.getIsTimeActive()).isTrue();
        assertThat(Phase.FILTRACION.getIsTimeActive()).isTrue();
        assertThat(Phase.COCCION.getIsTimeActive()).isTrue();
        assertThat(Phase.GASIFICACION.getIsTimeActive()).isTrue();
        assertThat(Phase.DESALCOHOLIZACION.getIsTimeActive()).isTrue();
        assertThat(Phase.ENVASADO.getIsTimeActive()).isTrue();
    }

    @Test
    void passivePhases_areCorrectlyIdentified() {
        // Given/When/Then: Fases pasivas (tiempo de espera)
        assertThat(Phase.FERMENTACION.getIsTimeActive()).isFalse();
        assertThat(Phase.MADURACION.getIsTimeActive()).isFalse();
    }

    @Test
    void phaseOrder_isCorrect() {
        // Given/When/Then: Verificar orden de cada fase
        assertThat(Phase.MOLIENDA.getOrder()).isEqualTo(1);
        assertThat(Phase.MACERACION.getOrder()).isEqualTo(2);
        assertThat(Phase.FILTRACION.getOrder()).isEqualTo(3);
        assertThat(Phase.COCCION.getOrder()).isEqualTo(4);
        assertThat(Phase.FERMENTACION.getOrder()).isEqualTo(5);
        assertThat(Phase.MADURACION.getOrder()).isEqualTo(6);
        assertThat(Phase.GASIFICACION.getOrder()).isEqualTo(7);
        assertThat(Phase.DESALCOHOLIZACION.getOrder()).isEqualTo(8);
        assertThat(Phase.ENVASADO.getOrder()).isEqualTo(9);
    }

    @Test
    void comesBefore_returnsTrue_whenPhaseIsEarlier() {
        // Given
        Phase earlier = Phase.MOLIENDA;
        Phase later = Phase.ENVASADO;
        
        // When/Then
        assertThat(earlier.comesBefore(later)).isTrue();
        assertThat(Phase.COCCION.comesBefore(Phase.FERMENTACION)).isTrue();
        assertThat(Phase.FERMENTACION.comesBefore(Phase.MADURACION)).isTrue();
    }

    @Test
    void comesBefore_returnsFalse_whenPhaseIsLater() {
        // Given
        Phase later = Phase.ENVASADO;
        Phase earlier = Phase.MOLIENDA;
        
        // When/Then
        assertThat(later.comesBefore(earlier)).isFalse();
        assertThat(Phase.MADURACION.comesBefore(Phase.COCCION)).isFalse();
    }

    @Test
    void comesBefore_returnsFalse_whenPhasesAreSame() {
        // Given
        Phase phase = Phase.MOLIENDA;
        
        // When/Then
        assertThat(phase.comesBefore(phase)).isFalse();
    }

    @Test
    void next_returnsNextPhase_whenExists() {
        // Given/When/Then
        assertThat(Phase.MOLIENDA.next()).isPresent().contains(Phase.MACERACION);
        assertThat(Phase.MACERACION.next()).isPresent().contains(Phase.FILTRACION);
        assertThat(Phase.FILTRACION.next()).isPresent().contains(Phase.COCCION);
        assertThat(Phase.COCCION.next()).isPresent().contains(Phase.FERMENTACION);
        assertThat(Phase.FERMENTACION.next()).isPresent().contains(Phase.MADURACION);
        assertThat(Phase.MADURACION.next()).isPresent().contains(Phase.GASIFICACION);
        assertThat(Phase.GASIFICACION.next()).isPresent().contains(Phase.DESALCOHOLIZACION);
        assertThat(Phase.DESALCOHOLIZACION.next()).isPresent().contains(Phase.ENVASADO);
    }

    @Test
    void next_returnsEmpty_forLastPhase() {
        // Given
        Phase lastPhase = Phase.ENVASADO;
        
        // When
        Optional<Phase> next = lastPhase.next();
        
        // Then
        assertThat(next).isEmpty();
    }

    @Test
    void next_chainingMultipleTimes() {
        // Given
        Phase start = Phase.MOLIENDA;
        
        // When: Navegar a través de varias fases
        Optional<Phase> phase1 = start.next();
        Optional<Phase> phase2 = phase1.flatMap(Phase::next);
        Optional<Phase> phase3 = phase2.flatMap(Phase::next);
        
        // Then
        assertThat(phase1).contains(Phase.MACERACION);
        assertThat(phase2).contains(Phase.FILTRACION);
        assertThat(phase3).contains(Phase.COCCION);
    }

    @Test
    void phaseSequence_isLogical() {
        // Given/When/Then: Verificar que la secuencia tiene sentido para producción de cerveza
        Phase[] expectedSequence = {
            Phase.MOLIENDA,           // 1. Triturar granos
            Phase.MACERACION,         // 2. Maceración (extraer azúcares)
            Phase.FILTRACION,         // 3. Filtrar líquido
            Phase.COCCION,            // 4. Cocción con lúpulo
            Phase.FERMENTACION,       // 5. Fermentación (levadura convierte azúcar en alcohol)
            Phase.MADURACION,         // 6. Maduración (desarrollo de sabor)
            Phase.GASIFICACION,       // 7. Carbonatación
            Phase.DESALCOHOLIZACION,  // 8. Remover alcohol (opcional)
            Phase.ENVASADO            // 9. Embotellado final
        };
        
        assertThat(Phase.values()).containsExactly(expectedSequence);
    }

    @Test
    void activeVsPassivePhases_distribution() {
        // Given
        long activeCount = 0;
        long passiveCount = 0;
        
        for (Phase phase : Phase.values()) {
            if (phase.getIsTimeActive()) {
                activeCount++;
            } else {
                passiveCount++;
            }
        }
        
        // When/Then: 7 activas, 2 pasivas (FERMENTACION y MADURACION)
        assertThat(activeCount).isEqualTo(7);
        assertThat(passiveCount).isEqualTo(2);
    }

    @Test
    void getOrder_isImmutable() {
        // Given
        Phase phase = Phase.MOLIENDA;
        Integer originalOrder = phase.getOrder();
        
        // When: Intentar obtener el orden múltiples veces
        Integer order1 = phase.getOrder();
        Integer order2 = phase.getOrder();
        
        // Then: Siempre devuelve el mismo valor
        assertThat(order1).isEqualTo(originalOrder).isEqualTo(1);
        assertThat(order2).isEqualTo(originalOrder).isEqualTo(1);
    }

    @Test
    void getIsTimeActive_isImmutable() {
        // Given
        Phase activePhase = Phase.MOLIENDA;
        Phase passivePhase = Phase.FERMENTACION;
        
        // When/Then: Los valores no cambian
        assertThat(activePhase.getIsTimeActive()).isTrue();
        assertThat(activePhase.getIsTimeActive()).isTrue(); // Segunda llamada
        
        assertThat(passivePhase.getIsTimeActive()).isFalse();
        assertThat(passivePhase.getIsTimeActive()).isFalse(); // Segunda llamada
    }
}
