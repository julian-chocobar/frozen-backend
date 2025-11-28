package com.enigcode.frozen_backend.product_phases.service;

import com.enigcode.frozen_backend.product_phases.model.Phase;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Servicio que centraliza la lógica de orden de fases según si el producto es alcohólico o no.
 * 
 * Orden para productos alcohólicos:
 * 1. MOLIENDA
 * 2. MACERACION
 * 3. FILTRACION
 * 4. COCCION
 * 5. FERMENTACION
 * 6. MADURACION
 * 7. GASIFICACION
 * 8. ENVASADO
 * 
 * Orden para productos no alcohólicos (agrega DESALCOHOLIZACION antes de ENVASADO):
 * 1. MOLIENDA
 * 2. MACERACION
 * 3. FILTRACION
 * 4. COCCION
 * 5. FERMENTACION
 * 6. MADURACION
 * 7. GASIFICACION
 * 8. DESALCOHOLIZACION
 * 9. ENVASADO
 */
@Service
public class PhaseOrderService {
    
    /**
     * Retorna las fases en el orden correcto según si el producto es alcohólico
     * 
     * @param isAlcoholic true si el producto es alcohólico, false en caso contrario
     * @return Lista de fases ordenadas
     */
    public List<Phase> getOrderedPhases(boolean isAlcoholic) {
        List<Phase> basePhases = List.of(
            Phase.MOLIENDA,      // 1
            Phase.MACERACION,    // 2
            Phase.FILTRACION,    // 3
            Phase.COCCION,       // 4
            Phase.FERMENTACION,  // 5
            Phase.MADURACION,    // 6
            Phase.GASIFICACION   // 7
        );
        
        List<Phase> phases = new ArrayList<>(basePhases);
        
        if (!isAlcoholic) {
            // Agregar DESALCOHOLIZACION antes de ENVASADO
            phases.add(Phase.DESALCOHOLIZACION); // 8
        }
        
        phases.add(Phase.ENVASADO); // 8 (alcohólico) o 9 (no alcohólico)
        
        return phases;
    }
    
    /**
     * Obtiene la siguiente fase en el orden según si el producto es alcohólico
     * 
     * @param currentPhase Fase actual
     * @param isAlcoholic true si el producto es alcohólico, false en caso contrario
     * @return Optional con la siguiente fase, o empty si no hay siguiente fase
     */
    public Optional<Phase> getNextPhase(Phase currentPhase, boolean isAlcoholic) {
        List<Phase> orderedPhases = getOrderedPhases(isAlcoholic);
        int currentIndex = orderedPhases.indexOf(currentPhase);
        if (currentIndex >= 0 && currentIndex < orderedPhases.size() - 1) {
            return Optional.of(orderedPhases.get(currentIndex + 1));
        }
        return Optional.empty();
    }
    
    /**
     * Obtiene la fase anterior en el orden según si el producto es alcohólico
     * 
     * @param currentPhase Fase actual
     * @param isAlcoholic true si el producto es alcohólico, false en caso contrario
     * @return Optional con la fase anterior, o empty si no hay fase anterior
     */
    public Optional<Phase> getPreviousPhase(Phase currentPhase, boolean isAlcoholic) {
        List<Phase> orderedPhases = getOrderedPhases(isAlcoholic);
        int currentIndex = orderedPhases.indexOf(currentPhase);
        if (currentIndex > 0) {
            return Optional.of(orderedPhases.get(currentIndex - 1));
        }
        return Optional.empty();
    }
    
    /**
     * Verifica si una fase viene antes que otra en el orden
     * 
     * @param phase1 Primera fase
     * @param phase2 Segunda fase
     * @param isAlcoholic true si el producto es alcohólico, false en caso contrario
     * @return true si phase1 viene antes que phase2
     */
    public boolean comesBefore(Phase phase1, Phase phase2, boolean isAlcoholic) {
        List<Phase> orderedPhases = getOrderedPhases(isAlcoholic);
        int index1 = orderedPhases.indexOf(phase1);
        int index2 = orderedPhases.indexOf(phase2);
        return index1 >= 0 && index2 >= 0 && index1 < index2;
    }
}

