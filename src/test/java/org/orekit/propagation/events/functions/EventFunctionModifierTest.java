/* Copyright 2022-2025 Romain Serra
 * Licensed to CS GROUP (CS) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * CS licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.orekit.propagation.events.functions;

import org.hipparchus.CalculusFieldElement;
import org.hipparchus.util.Binary64;
import org.junit.jupiter.api.Test;
import org.orekit.propagation.FieldSpacecraftState;
import org.orekit.propagation.SpacecraftState;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EventFunctionModifierTest {

    @Test
    void testDependsOnTimeOnly() {
        // GIVEN
        final TestFunction testFunction = new TestFunction();
        final EventFunctionModifier modifier = () -> testFunction;
        // WHEN
        final boolean actual = modifier.dependsOnTimeOnly();
        // THEN
        assertEquals(testFunction.dependsOnTimeOnly(), actual);
    }

    @Test
    void testValue() {
        // GIVEN
        final TestFunction testFunction = new TestFunction();
        final EventFunctionModifier modifier = () -> testFunction;
        final SpacecraftState state = mock();
        // WHEN
        final double value = modifier.value(state);
        // THEN
        assertEquals(testFunction.value(state), value);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testValueField() {
        // GIVEN
        final TestFunction testFunction = new TestFunction();
        final EventFunctionModifier modifier = () -> testFunction;
        final FieldSpacecraftState<Binary64> fieldState = mock();
        when(fieldState.getMass()).thenReturn(Binary64.ONE);
        // WHEN
        final Binary64 value = modifier.value(fieldState);
        // THEN
        assertEquals(testFunction.value(fieldState), value);
    }

    private static class TestFunction implements EventFunction {

        @Override
        public double value(SpacecraftState state) {
            return 1.;
        }

        @Override
        public <T extends CalculusFieldElement<T>> T value(FieldSpacecraftState<T> fieldState) {
            return fieldState.getMass().getField().getOne();
        }

        @Override
        public boolean dependsOnTimeOnly() {
            return true;
        }
    }
}
