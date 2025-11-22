package com.enigcode.frozen_backend.system_configurations.repository;

import com.enigcode.frozen_backend.system_configurations.model.SystemConfiguration;
import com.enigcode.frozen_backend.system_configurations.model.WorkingDay;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class SystemConfigurationRepositoryTest {

    @Autowired
    private SystemConfigurationRepository repository;

    @Test
    void findFirstByIsActiveTrueOrderByIdDesc_returnsLatestActiveWithWorkingDays() {
        SystemConfiguration old = new SystemConfiguration();
        old.setIsActive(false);
        repository.save(old);

        SystemConfiguration active = new SystemConfiguration();
        active.setIsActive(true);

        WorkingDay wd1 = new WorkingDay();
        wd1.setDayOfWeek(DayOfWeek.MONDAY);
        wd1.setSystemConfiguration(active);

        WorkingDay wd2 = new WorkingDay();
        wd2.setDayOfWeek(DayOfWeek.TUESDAY);
        wd2.setSystemConfiguration(active);

        active.setWorkingDays(List.of(wd1, wd2));

        repository.save(active);

        Optional<SystemConfiguration> found = repository.findFirstByIsActiveTrueOrderByIdDesc();

        assertThat(found).isPresent();
        SystemConfiguration sc = found.get();
        assertThat(sc.getIsActive()).isTrue();
        assertThat(sc.getWorkingDays()).isNotNull();
        assertThat(sc.getWorkingDays()).hasSize(2);
    }
}
