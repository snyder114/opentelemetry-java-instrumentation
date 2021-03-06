/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

import io.opentelemetry.instrumentation.test.AgentTestRunner
import io.opentelemetry.test.annotation.SayTracedHello
import io.opentracing.contrib.dropwizard.Trace
import java.util.concurrent.Callable

class TraceAnnotationsTest extends AgentTestRunner {

  def "test simple case annotations"() {
    setup:
    // Test single span in new trace
    SayTracedHello.sayHello()

    expect:
    assertTraces(1) {
      trace(0, 1) {
        span(0) {
          name "SayTracedHello.sayHello"
          hasNoParent()
          errored false
          attributes {
            "myattr" "test"
          }
        }
      }
    }
  }

  def "test complex case annotations"() {
    when:
    // Test new trace with 2 children spans
    SayTracedHello.sayHELLOsayHA()

    then:
    assertTraces(1) {
      trace(0, 3) {
        span(0) {
          name "SayTracedHello.sayHELLOsayHA"
          hasNoParent()
          errored false
          attributes {
            "myattr" "test2"
          }
        }
        span(1) {
          name "SayTracedHello.sayHello"
          childOf span(0)
          errored false
          attributes {
            "myattr" "test"
          }
        }
        span(2) {
          name "SayTracedHello.sayHello"
          childOf span(0)
          errored false
          attributes {
            "myattr" "test"
          }
        }
      }
    }
  }

  def "test exception exit"() {
    setup:
    Throwable error = null
    try {
      SayTracedHello.sayERROR()
    } catch (final Throwable ex) {
      error = ex
    }

    expect:
    assertTraces(1) {
      trace(0, 1) {
        span(0) {
          name "SayTracedHello.sayERROR"
          errored true
          errorEvent(error.class)
        }
      }
    }
  }

  def "test annonymous class annotations"() {
    setup:
    // Test anonymous classes with package.
    SayTracedHello.fromCallable()

    expect:
    assertTraces(1) {
      trace(0, 1) {
        span(0) {
          name "SayTracedHello\$1.call"
          attributes {
          }
        }
      }
    }

    when:
    // Test anonymous classes with no package.
    new Callable<String>() {
      @Trace
      @Override
      String call() throws Exception {
        return "Howdy!"
      }
    }.call()

    then:
    assertTraces(2) {
      trace(0, 1) {
        span(0) {
          name "SayTracedHello\$1.call"
          attributes {
          }
        }
        trace(1, 1) {
          span(0) {
            name "TraceAnnotationsTest\$1.call"
            attributes {
            }
          }
        }
      }
    }
  }
}
