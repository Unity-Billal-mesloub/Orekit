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
package org.orekit.estimation.measurements.signal;

import org.hipparchus.analysis.differentiation.Gradient;
import org.hipparchus.analysis.differentiation.GradientField;
import org.hipparchus.optim.ConvergenceChecker;
import org.junit.jupiter.api.Test;
import org.orekit.frames.FramesFactory;
import org.orekit.time.FieldAbsoluteDate;
import org.orekit.utils.FieldAbsolutePVCoordinates;
import org.orekit.utils.FieldPVCoordinates;
import org.orekit.utils.PVCoordinates;
import org.orekit.utils.PVCoordinatesProvider;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class SignalTravelTimeModelTest {

    @Test
    void testGetAdjustableReceiverComputer() {
        // GIVEN
        final ConvergenceChecker<Double> convergenceChecker = (iteration, previous, current) -> true;
        final SignalTravelTimeModel signalTravelTimeModel = new SignalTravelTimeModel(convergenceChecker);
        // WHEN
        final SignalTravelTimeAdjustableReceiver adjustableReceiver = signalTravelTimeModel.getAdjustableReceiverComputer(mock(PVCoordinatesProvider.class));
        // THEN
        assertEquals(convergenceChecker, adjustableReceiver.getConvergenceChecker());
    }

    @Test
    void testGetAdjustableEmitterComputer() {
        // GIVEN
        final ConvergenceChecker<Double> convergenceChecker = (iteration, previous, current) -> true;
        final SignalTravelTimeModel signalTravelTimeModel = new SignalTravelTimeModel(convergenceChecker);
        // WHEN
        final SignalTravelTimeAdjustableEmitter adjustableEmitter = signalTravelTimeModel.getAdjustableEmitterComputer(mock(PVCoordinatesProvider.class));
        // THEN
        assertEquals(convergenceChecker, adjustableEmitter.getConvergenceChecker());
    }

    @Test
    void testGetFieldAdjustableReceiverComputer() {
        // GIVEN
        final ConvergenceChecker<Gradient> convergenceChecker = (iteration, previous, current) -> true;
        final SignalTravelTimeModel signalTravelTimeModel = new SignalTravelTimeModel(null, convergenceChecker);
        final GradientField gradientField = GradientField.getField(0);
        final FieldPVCoordinates<Gradient> fieldPVCoordinates = new FieldPVCoordinates<>(gradientField, new PVCoordinates());
        final FieldAbsolutePVCoordinates<Gradient> fieldAbsolutePVCoordinates = new FieldAbsolutePVCoordinates<>(FramesFactory.getGCRF(),
                FieldAbsoluteDate.getArbitraryEpoch(gradientField), fieldPVCoordinates);
        // WHEN
        final FieldSignalTravelTimeAdjustableReceiver<Gradient> adjustableReceiver = signalTravelTimeModel.getFieldAdjustableReceiverComputer(fieldAbsolutePVCoordinates);
        // THEN
        assertEquals(convergenceChecker, adjustableReceiver.getConvergenceChecker());
    }

    @Test
    void testGetFieldAdjustableEmitterComputer() {
        // GIVEN
        final ConvergenceChecker<Gradient> convergenceChecker = (iteration, previous, current) -> true;
        final SignalTravelTimeModel signalTravelTimeModel = new SignalTravelTimeModel(null, convergenceChecker);
        final GradientField gradientField = GradientField.getField(0);
        final FieldPVCoordinates<Gradient> fieldPVCoordinates = new FieldPVCoordinates<>(gradientField, new PVCoordinates());
        final FieldAbsolutePVCoordinates<Gradient> fieldAbsolutePVCoordinates = new FieldAbsolutePVCoordinates<>(FramesFactory.getGCRF(),
                FieldAbsoluteDate.getArbitraryEpoch(gradientField), fieldPVCoordinates);
        // WHEN
        final FieldSignalTravelTimeAdjustableEmitter<Gradient> adjustableEmitter = signalTravelTimeModel.getAdjustableEmitterComputer(fieldAbsolutePVCoordinates);
        // THEN
        assertEquals(convergenceChecker, adjustableEmitter.getConvergenceChecker());
    }
}
