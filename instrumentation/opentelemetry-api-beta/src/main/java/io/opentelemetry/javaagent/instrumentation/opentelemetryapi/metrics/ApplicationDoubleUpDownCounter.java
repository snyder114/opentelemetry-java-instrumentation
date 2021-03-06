/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.javaagent.instrumentation.opentelemetryapi.metrics;

import application.io.opentelemetry.api.common.Labels;
import application.io.opentelemetry.api.metrics.DoubleUpDownCounter;
import io.opentelemetry.javaagent.instrumentation.opentelemetryapi.LabelBridging;

class ApplicationDoubleUpDownCounter implements DoubleUpDownCounter {

  private final io.opentelemetry.api.metrics.DoubleUpDownCounter agentDoubleUpDownCounter;

  ApplicationDoubleUpDownCounter(
      io.opentelemetry.api.metrics.DoubleUpDownCounter agentDoubleUpDownCounter) {
    this.agentDoubleUpDownCounter = agentDoubleUpDownCounter;
  }

  io.opentelemetry.api.metrics.DoubleUpDownCounter getAgentDoubleUpDownCounter() {
    return agentDoubleUpDownCounter;
  }

  @Override
  public void add(double delta, Labels labels) {
    agentDoubleUpDownCounter.add(delta, LabelBridging.toAgent(labels));
  }

  @Override
  public void add(double v) {
    agentDoubleUpDownCounter.add(v);
  }

  @Override
  public BoundDoubleUpDownCounter bind(Labels labels) {
    return new BoundInstrument(agentDoubleUpDownCounter.bind(LabelBridging.toAgent(labels)));
  }

  static class BoundInstrument implements BoundDoubleUpDownCounter {

    private final io.opentelemetry.api.metrics.DoubleUpDownCounter.BoundDoubleUpDownCounter
        agentBoundDoubleUpDownCounter;

    BoundInstrument(
        io.opentelemetry.api.metrics.DoubleUpDownCounter.BoundDoubleUpDownCounter
            agentBoundDoubleUpDownCounter) {
      this.agentBoundDoubleUpDownCounter = agentBoundDoubleUpDownCounter;
    }

    @Override
    public void add(double delta) {
      agentBoundDoubleUpDownCounter.add(delta);
    }

    @Override
    public void unbind() {
      agentBoundDoubleUpDownCounter.unbind();
    }
  }

  static class Builder implements DoubleUpDownCounter.Builder {

    private final io.opentelemetry.api.metrics.DoubleUpDownCounter.Builder agentBuilder;

    Builder(io.opentelemetry.api.metrics.DoubleUpDownCounter.Builder agentBuilder) {
      this.agentBuilder = agentBuilder;
    }

    @Override
    public DoubleUpDownCounter.Builder setDescription(String description) {
      agentBuilder.setDescription(description);
      return this;
    }

    @Override
    public DoubleUpDownCounter.Builder setUnit(String unit) {
      agentBuilder.setUnit(unit);
      return this;
    }

    @Override
    public DoubleUpDownCounter build() {
      return new ApplicationDoubleUpDownCounter(agentBuilder.build());
    }
  }
}
