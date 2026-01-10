/* Copyright 2022-2026 Romain Serra
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
package org.orekit.estimation.measurements;

import org.hipparchus.geometry.euclidean.threed.FieldVector3D;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.optim.ConvergenceChecker;
import org.hipparchus.util.Binary64;
import org.hipparchus.util.Binary64Field;
import org.junit.jupiter.api.Test;
import org.orekit.TestUtils;
import org.orekit.orbits.KeplerianExtendedPositionProvider;
import org.orekit.orbits.Orbit;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.FieldAbsoluteDate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FieldSignalTravelTimeAdjustableReceiverTest {

    @Test
    void testConstructorDefaultConvergenceChecker() {
        // GIVEN
        final FieldSignalTravelTimeAdjustableReceiver<Binary64> adjustableReceiver = new FieldSignalTravelTimeAdjustableReceiver<>(null);
        // WHEN
        final ConvergenceChecker<Binary64> convergenceChecker = adjustableReceiver.getConvergenceChecker();
        final Binary64 convergedValue = Binary64.ZERO;
        // THEN
        assertFalse(convergenceChecker.converged(0, convergedValue, convergedValue));  // enforces at least one iteration
        assertTrue(convergenceChecker.converged(1, convergedValue, convergedValue));
    }

    @Test
    void testCompute() {
        // GIVEN
        final Orbit orbit = TestUtils.getDefaultOrbit(AbsoluteDate.ARBITRARY_EPOCH);
        final KeplerianExtendedPositionProvider positionProvider = new KeplerianExtendedPositionProvider(orbit);
        final Binary64Field field = Binary64Field.getInstance();
        final FieldSignalTravelTimeAdjustableReceiver<Binary64> fieldComputer = new FieldSignalTravelTimeAdjustableReceiver<>(positionProvider.toFieldPVCoordinatesProvider(field));
        final FieldAbsoluteDate<Binary64> fieldDate = FieldAbsoluteDate.getArbitraryEpoch(field);
        final Vector3D receiver = new Vector3D(-1e3, 2e2, 1e4);
        final FieldVector3D<Binary64> fieldReceiver = new FieldVector3D<>(field, receiver);
        // WHEN
        final Binary64 actual = fieldComputer.compute(fieldReceiver, fieldDate, orbit.getFrame());
        // THEN
        final double expected = new SignalTravelTimeAdjustableReceiver(positionProvider).compute(receiver,
                fieldDate.toAbsoluteDate(), orbit.getFrame());
        assertEquals(expected, actual.getReal());
    }

    @Test
    void testComputeWithGuess() {
        // GIVEN
        final Orbit orbit = TestUtils.getDefaultOrbit(AbsoluteDate.ARBITRARY_EPOCH);
        final KeplerianExtendedPositionProvider positionProvider = new KeplerianExtendedPositionProvider(orbit);
        final Binary64Field field = Binary64Field.getInstance();
        final FieldSignalTravelTimeAdjustableReceiver<Binary64> fieldComputer = new FieldSignalTravelTimeAdjustableReceiver<>(positionProvider.toFieldPVCoordinatesProvider(field));
        final FieldAbsoluteDate<Binary64> fieldDate = FieldAbsoluteDate.getArbitraryEpoch(field);
        final FieldAbsoluteDate<Binary64> guessDate = fieldDate.shiftedBy(1);
        final Vector3D receiver = new Vector3D(1e3, 2e4, 0);
        final FieldVector3D<Binary64> fieldReceiver = new FieldVector3D<>(field, receiver);
        // WHEN
        final Binary64 actual = fieldComputer.compute(fieldReceiver, fieldDate, guessDate, orbit.getFrame());
        // THEN
        final double expected = new SignalTravelTimeAdjustableReceiver(positionProvider).compute(receiver,
                fieldDate.toAbsoluteDate(), guessDate.toAbsoluteDate(), orbit.getFrame());
        assertEquals(expected, actual.getReal());
    }


}
