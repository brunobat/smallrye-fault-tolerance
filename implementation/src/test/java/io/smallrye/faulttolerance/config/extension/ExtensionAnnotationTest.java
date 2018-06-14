/*
 * Copyright 2017 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.smallrye.faulttolerance.config.extension;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.temporal.ChronoUnit;

import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;

import io.smallrye.faulttolerance.FaultToleranceOperations;
import io.smallrye.faulttolerance.TestArchive;
import io.smallrye.faulttolerance.config.FaultToleranceOperation;
import io.smallrye.faulttolerance.config.RetryConfig;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
/**
 *
 * @author Martin Kouba
 */
@RunWith(Arquillian.class)
public class ExtensionAnnotationTest {

    @Deployment
    public static JavaArchive createTestArchive() {
        return TestArchive.createBase(ExtensionAnnotationTest.class)
                .addPackage(ExtensionAnnotationTest.class.getPackage())
                .addClass(FaultToleranceOperations.class)
                .addAsServiceProvider(Extension.class, CustomExtension.class);
    }

    @Inject
    FaultToleranceOperations ops;

    @Inject
    UnconfiguredService service;

    @Test
    public void testAnnotationAddedByExtension() throws NoSuchMethodException, SecurityException {
        FaultToleranceOperation ping = ops.get(UnconfiguredService.class.getMethod("ping").toGenericString());
        assertNotNull(ping);
        assertTrue(ping.hasRetry());
        RetryConfig fooRetry = ping.getRetry();
        // Method-level
        assertEquals(fooRetry.get(RetryConfig.MAX_RETRIES, Integer.class), Integer.valueOf(2));
        // Default value
        assertEquals(fooRetry.get(RetryConfig.DELAY_UNIT, ChronoUnit.class), ChronoUnit.MILLIS);

        UnconfiguredService.COUNTER.set(0);
        try {
            service.ping();
            fail();
        } catch (IllegalStateException expected) {
        }
        assertEquals(UnconfiguredService.COUNTER.get(), 3);

    }
}
