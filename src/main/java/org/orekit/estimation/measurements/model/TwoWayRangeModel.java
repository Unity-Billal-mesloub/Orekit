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
package org.orekit.estimation.measurements.model;

import org.hipparchus.analysis.differentiation.Gradient;
import org.orekit.annotation.DefaultDataContext;
import org.orekit.errors.OrekitException;
import org.orekit.errors.OrekitMessages;
import org.orekit.estimation.measurements.signal.SignalTravelTimeModel;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.FieldAbsoluteDate;
import org.orekit.utils.Constants;
import org.orekit.utils.FieldPVCoordinatesProvider;
import org.orekit.utils.PVCoordinatesProvider;

/**
 * Perfect measurement model for two-way range model.
 * A signal is emitted, received a first time (relay) and received again a final time. There is no other assumption.
 * The class will not work if the signal travel time model emulates instantaneous transmission.
 * @since 14.0
 * @author Romain Serra
 */
public class TwoWayRangeModel {

    /** Light speed. */
    private static final double LIGHT_SPEED = Constants.SPEED_OF_LIGHT;

    /** Inertial frame needed for intermediate computations. */
    private final Frame inertialFrame;

    /** Signal travel time model. */
    private final SignalTravelTimeModel signalTravelTimeModel;

    /**
     * Constructor with default inertial frame.
     * @param signalTravelTimeModel time delay computer
     */
    @DefaultDataContext
    public TwoWayRangeModel(final SignalTravelTimeModel signalTravelTimeModel) {
        this(FramesFactory.getGCRF(), signalTravelTimeModel);
    }

    /**
     * Constructor.
     * @param inertialFrame inertial frame needed for intermediate computations
     * @param signalTravelTimeModel time delay computer
     */
    public TwoWayRangeModel(final Frame inertialFrame, final SignalTravelTimeModel signalTravelTimeModel) {
        if (!inertialFrame.isPseudoInertial()) {
            throw new OrekitException(OrekitMessages.NON_PSEUDO_INERTIAL_FRAME, inertialFrame.getName());
        }
        this.inertialFrame = inertialFrame;
        this.signalTravelTimeModel = signalTravelTimeModel;
    }

    /**
     * Compute theoretical measurement.
     * @param receiver end receiver coordinates provider
     * @param receptionDate signal end reception date
     * @param relay signal relayer (initial reception) coordinates provider
     * @param approxRelayDate guess for the relay date (shall be adjusted by signal travel time computer)
     * @param emitter signal initial emitter coordinates provider
     * @param approxEmissionDate guess for the emission date (shall be adjusted by signal travel time computer)
     * @return ranges on both legs in chronological order (m)
     */
    public double[] value(final PVCoordinatesProvider receiver, final AbsoluteDate receptionDate,
                          final PVCoordinatesProvider relay, final AbsoluteDate approxRelayDate,
                          final PVCoordinatesProvider emitter, final AbsoluteDate approxEmissionDate) {
        final double secondLegTravelTime = computeTravelTime(receiver, receptionDate, receiver, approxRelayDate);
        final AbsoluteDate relayDate = receptionDate.shiftedBy(-secondLegTravelTime);
        final double firstLegTravelTime = computeTravelTime(relay, relayDate, emitter, approxEmissionDate);
        return new double[] { firstLegTravelTime * LIGHT_SPEED, secondLegTravelTime * LIGHT_SPEED };
    }

    /**
     * Compute theoretical measurement.
     * @param receiver end receiver coordinates provider
     * @param receptionDate signal end reception date
     * @param relay signal relay (initial reception, second emission) coordinates provider
     * @param approxRelayDate guess for the relay date (shall be adjusted by signal travel time computer)
     * @param emitter signal initial emitter coordinates provider
     * @param approxEmissionDate guess for the emission date (shall be adjusted by signal travel time computer)
     * @return ranges on both legs in chronological order (m)
     */
    public Gradient[] value(final FieldPVCoordinatesProvider<Gradient> receiver,
                            final FieldAbsoluteDate<Gradient> receptionDate,
                            final FieldPVCoordinatesProvider<Gradient> relay,
                            final FieldAbsoluteDate<Gradient> approxRelayDate,
                            final FieldPVCoordinatesProvider<Gradient> emitter,
                            final FieldAbsoluteDate<Gradient> approxEmissionDate) {
        final Gradient secondLegTravelTime = computeTravelTime(receiver, receptionDate, receiver, approxRelayDate);
        final FieldAbsoluteDate<Gradient> relayDate = receptionDate.shiftedBy(secondLegTravelTime.negate());
        final Gradient firstLegTravelTime = computeTravelTime(relay, relayDate, emitter, approxEmissionDate);
        return new Gradient[] { firstLegTravelTime.multiply(LIGHT_SPEED), secondLegTravelTime.multiply(LIGHT_SPEED) };
    }

    private double computeTravelTime(final PVCoordinatesProvider receiver, final AbsoluteDate receptionDate,
                                     final PVCoordinatesProvider emitter, final AbsoluteDate approxEmissionDate) {
        return signalTravelTimeModel.getAdjustableEmitterComputer(emitter).computeDelay(approxEmissionDate,
                receiver.getPosition(receptionDate, inertialFrame), receptionDate, inertialFrame);
    }

    private Gradient computeTravelTime(final FieldPVCoordinatesProvider<Gradient> receiver,
                                       final FieldAbsoluteDate<Gradient> receptionDate,
                                       final FieldPVCoordinatesProvider<Gradient> emitter,
                                       final FieldAbsoluteDate<Gradient> approxEmissionDate) {
        return signalTravelTimeModel.getAdjustableEmitterComputer(emitter).computeDelay(approxEmissionDate,
                receiver.getPosition(receptionDate, inertialFrame), receptionDate, inertialFrame);
    }
}
