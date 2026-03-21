package com.intouch.IntouchApps.aspect;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PerformanceTrackerHandler implements ObservationHandler<Observation.Context> {

    @Override
    public void onStart(Observation.Context context) {
        log.info("method execution started {}", context.getName());
        context.put("time", System.currentTimeMillis());//assign custom key to the context
    }

    @Override
    public void onError(Observation.Context context) {
        log.info("Execution error occurred {}", context.getError().getMessage());
    }

    @Override
    public void onEvent(Observation.Event event, Observation.Context context) {
        ObservationHandler.super.onEvent(event, context);
    }

    @Override
    public void onScopeOpened(Observation.Context context) {
        ObservationHandler.super.onScopeOpened(context);
    }

    @Override
    public void onScopeClosed(Observation.Context context) {
        ObservationHandler.super.onScopeClosed(context);
    }

    @Override
    public void onScopeReset(Observation.Context context) {
        ObservationHandler.super.onScopeReset(context);
    }

    @Override
    public void onStop(Observation.Context context) {
        //print execution time from added custom key to context
        log.info("method execution stopped " +
                context.getName() +
                " duration " +
                (System.currentTimeMillis() - context.getOrDefault("time", 0L)));
    }

    @Override
    public boolean supportsContext(Observation.Context context) {
        return true;
    }
}
