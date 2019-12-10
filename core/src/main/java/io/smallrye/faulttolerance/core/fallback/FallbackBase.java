package io.smallrye.faulttolerance.core.fallback;

import java.util.concurrent.Callable;

import io.smallrye.faulttolerance.core.FaultToleranceStrategy;
import io.smallrye.faulttolerance.core.InvocationContext;

public abstract class FallbackBase<V, ContextType extends InvocationContext<V>>
        implements FaultToleranceStrategy<V, ContextType> {
    final FaultToleranceStrategy<V, ContextType> delegate;
    final String description;

    final FallbackFunction<V> fallback;
    final MetricsRecorder metricsRecorder;

    FallbackBase(FaultToleranceStrategy<V, ContextType> delegate, String description, FallbackFunction<V> fallback,
            MetricsRecorder metricsRecorder) {
        this.delegate = delegate;
        this.description = description;
        this.fallback = fallback;
        this.metricsRecorder = metricsRecorder == null ? MetricsRecorder.NO_OP : metricsRecorder;
    }

    V doApply(Callable<V> c) throws Exception {
        Throwable failure;
        try {
            return c.call();
        } catch (Exception e) {
            failure = e;
        }

        if (failure instanceof InterruptedException || Thread.interrupted()) {
            throw new InterruptedException();
        }

        metricsRecorder.fallbackCalled();
        return fallback.call(failure);
    }

    public interface MetricsRecorder {
        void fallbackCalled();

        MetricsRecorder NO_OP = () -> {
        };
    }
}