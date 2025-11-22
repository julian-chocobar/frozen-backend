package com.enigcode.frozen_backend.production_phases;

import static org.assertj.core.api.Assertions.assertThat;

import com.enigcode.frozen_backend.production_phases.model.ProductionPhase;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

public class ProductionPhaseModelTest {

    // Helper to invoke lifecycle methods by name (PrePersist / PreUpdate are regular methods)
    private void invokeLifecycle(ProductionPhase phase, String methodName) throws Exception {
        Method m = ProductionPhase.class.getDeclaredMethod(methodName);
        m.setAccessible(true);
        m.invoke(phase);
    }

    @Test
    public void prePersist_setsPhaseOrder_whenNull() throws Exception {
        ProductionPhase p = new ProductionPhase();
        p.setPhaseOrder(null);
        p.setPhase(com.enigcode.frozen_backend.product_phases.model.Phase.MOLIENDA);
        invokeLifecycle(p, "setPhaseOrder");

        assertThat(p.getPhaseOrder()).isNotNull();
    }

    @Test
    public void preUpdate_calculatesProductWaste_positiveCase() throws Exception {
        ProductionPhase p = new ProductionPhase();
        p.setStandardInput(100.0);
        p.setStandardOutput(90.0);
        p.setInput(110.0);
        p.setOutput(95.0);
        p.setProductWaste(0.0);
        p.setMovementWaste(0.0);

        invokeLifecycle(p, "calculateProductWaste");

        // Calculation from entity: expectedOutput = standardOutput * (input / standardInput)
        double expectedOutput = 90.0 * (110.0 / 100.0); // 99
        double expected = expectedOutput - 95.0; // 99 - 95 = 4
        assertThat(p.getProductWaste()).isEqualTo(expected);
        assertThat(p.getMovementWaste()).isGreaterThanOrEqualTo(0.0);
    }

    @Test
    public void preUpdate_calculatesProductWaste_nonNegative() throws Exception {
        ProductionPhase p = new ProductionPhase();
        p.setStandardInput(100.0);
        p.setStandardOutput(90.0);
        p.setInput(95.0);
        p.setOutput(92.0);

        invokeLifecycle(p, "calculateProductWaste");

        // expected negative blown down to zero
        assertThat(p.getProductWaste()).isGreaterThanOrEqualTo(0.0);
    }

    @Test
    public void preUpdate_handlesZeroStandardInput() throws Exception {
        ProductionPhase p = new ProductionPhase();
        p.setStandardInput(0.0);
        p.setStandardOutput(0.0);
        p.setInput(50.0);
        p.setOutput(48.0);

        invokeLifecycle(p, "calculateProductWaste");

        assertThat(p.getProductWaste()).isGreaterThanOrEqualTo(0.0);
    }
}
