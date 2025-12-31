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

import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.AbsolutePVCoordinates;
import org.orekit.utils.Constants;
import org.orekit.utils.PVCoordinates;

import static org.junit.jupiter.api.Assertions.*;

class SignalTravelTimeAdjustableEmitterTest {

    @Test
    void testComputeVersusReverse() {
        // GIVEN
        final Frame frame = FramesFactory.getGCRF();
        final AbsoluteDate receptionDate = AbsoluteDate.ARBITRARY_EPOCH;
        final Vector3D emitterPosition = Vector3D.MINUS_I.scalarMultiply(1e5);
        final AbsolutePVCoordinates absolutePVCoordinates = new AbsolutePVCoordinates(frame, receptionDate, new PVCoordinates(emitterPosition));
        final Vector3D receiverPosition = new Vector3D(1e2, 1e3, 1e4);
        final SignalTravelTimeAdjustableEmitter signalTimeOfFlight = new SignalTravelTimeAdjustableEmitter(absolutePVCoordinates);
        // WHEN
        final double actual = signalTimeOfFlight.compute(receiverPosition, receptionDate, frame);
        // THEN
        final AbsolutePVCoordinates reversed = new AbsolutePVCoordinates(frame, receptionDate, new PVCoordinates(receiverPosition));
        final SignalTravelTimeAdjustableReceiver signalTravelTimeAdjustableReceiver = new SignalTravelTimeAdjustableReceiver(reversed);
        final AbsoluteDate emissionDate = receptionDate.shiftedBy(actual);
        final double expected = signalTravelTimeAdjustableReceiver.compute(emitterPosition, emissionDate, frame);
        assertEquals(expected, actual);
    }

    @Test
    void testComputeStatic() {
        // GIVEN
        final Frame frame = FramesFactory.getGCRF();
        final AbsoluteDate receptionDate = AbsoluteDate.ARBITRARY_EPOCH;
        final AbsolutePVCoordinates absolutePVCoordinates = new AbsolutePVCoordinates(frame, receptionDate, new PVCoordinates());
        final Vector3D receiverPosition = new Vector3D(1e2, 1e3, 1e4);
        final SignalTravelTimeAdjustableEmitter signalTimeOfFlight = new SignalTravelTimeAdjustableEmitter(absolutePVCoordinates);
        // WHEN
        final double actual = signalTimeOfFlight.compute(receiverPosition, receptionDate, frame);
        // THEN
        final double expected = receiverPosition.getNorm() / Constants.SPEED_OF_LIGHT;
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1e3, -1e1, 0., 1e1, 1e2, 1e4, 1e4})
    void testCompute(final double speedFactor) {
        // GIVEN
        final Frame frame = FramesFactory.getGCRF();
        final AbsoluteDate receptionDate = AbsoluteDate.ARBITRARY_EPOCH;
        final PVCoordinates pvCoordinates = new PVCoordinates(Vector3D.MINUS_I, new Vector3D(1, -2, 3).scalarMultiply(speedFactor));
        final AbsolutePVCoordinates absolutePVCoordinates = new AbsolutePVCoordinates(frame, receptionDate, pvCoordinates);
        final Vector3D receiverPosition = new Vector3D(1e2, 1e3, 1e4);
        final SignalTravelTimeAdjustableEmitter signalTimeOfFlight = new SignalTravelTimeAdjustableEmitter(absolutePVCoordinates);
        // WHEN
        final double actual = signalTimeOfFlight.compute(receiverPosition, receptionDate, frame);
        // THEN
        final AbsoluteDate emittingDate = receptionDate.shiftedBy(-actual);
        final double expected = signalTimeOfFlight.compute(emittingDate, receiverPosition, receptionDate, frame);
        assertEquals(expected, actual);
    }
}
