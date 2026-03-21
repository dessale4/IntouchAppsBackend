package com.intouch.IntouchApps.aspect;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ObservationAspectConfig {
    @Bean
    public ObservedAspect observedAspect(ObservationRegistry observationRegistry){
        //attach our performance tracker to the observationRegistry
        observationRegistry.observationConfig().observationHandler(new PerformanceTrackerHandler());
        return new ObservedAspect(observationRegistry);
    }
}
