package be.kdg.airfreight;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

import static org.assertj.core.api.Assertions.assertThat;

class ModularityTests {

    @Test
    void respectsModuleBoundaries() {
        ApplicationModules.of(AirFreightApplication.class).verify();
    }

    @Test
    void discoversAllModulesInTheSharedSkeleton() {
        ApplicationModules modules = ApplicationModules.of(AirFreightApplication.class);

        assertThat(modules)
                .extracting(module -> module.getIdentifier().toString())
                .containsExactlyInAnyOrder(
                        "common",
                        "config",
                        "flightoperations",
                        "loadplanning",
                        "operationscoordination",
                        "partnermessaging");
    }
}
