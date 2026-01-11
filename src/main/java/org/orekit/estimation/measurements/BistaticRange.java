/* Copyright 2002-2026 Mark Rutten
 * Licensed to CS GROUP (CS) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * Mark Rutten licenses this file to You under the Apache License, Version 2.0
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

import java.util.Arrays;
import java.util.Collections;

import org.hipparchus.analysis.differentiation.Gradient;
import org.orekit.estimation.measurements.signal.FieldSignalTravelTimeAdjustableEmitter;
import org.orekit.estimation.measurements.signal.SignalTravelTimeAdjustableEmitter;
import org.orekit.propagation.SpacecraftState;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.FieldAbsoluteDate;
import org.orekit.utils.Constants;
import org.orekit.utils.FieldPVCoordinatesProvider;
import org.orekit.utils.PVCoordinatesProvider;
import org.orekit.utils.ParameterDriver;
import org.orekit.utils.TimeSpanMap.Span;
import org.orekit.utils.TimeStampedFieldPVCoordinates;
import org.orekit.utils.TimeStampedPVCoordinates;

/**
 * Class modeling a bistatic range measurement using
 * an emitter ground station and a receiver ground station.
 * <p>
 * The measurement is considered to be a signal:
 * <ul>
 * <li>Emitted from the emitter ground station</li>
 * <li>Reflected on the spacecraft</li>
 * <li>Received on the receiver ground station</li>
 * </ul>
 * The date of the measurement corresponds to the reception on ground of the reflected signal.
 * <p>
 * The motion of the stations and the spacecraft during the signal flight time are taken into account.
 * </p>
 *
 * @author Mark Rutten
 * @since 11.2
 */
public class BistaticRange extends AbstractMeasurement<BistaticRange> {

    /** Type of the measurement. */
    public static final String MEASUREMENT_TYPE = "BistaticRange";

    /**
     * Ground station from which transmission is made.
     */
    private final GroundStation emitter;

    /**
     * Ground station that performs measurement.
     */
    private final GroundStation receiver;

    /**
     * Simple constructor.
     *
     * @param emitter     ground station from which transmission is performed
     * @param receiver    ground station from which measurement is performed
     * @param date        date of the measurement
     * @param range       observed value
     * @param sigma       theoretical standard deviation
     * @param baseWeight  base weight
     * @param satellite   satellite related to this measurement
     * @since 11.2
     */
    public BistaticRange(final GroundStation emitter, final GroundStation receiver, final AbsoluteDate date,
                         final double range, final double sigma, final double baseWeight,
                         final ObservableSatellite satellite) {
        super(date, true, range, sigma, baseWeight, Collections.singletonList(satellite));

        // Add the parameters for the emitter and receiver
        addParametersDrivers(receiver.getParametersDrivers());
        addParametersDrivers(emitter.getParametersDrivers());

        // Set emitter and receiver values
        this.receiver = receiver;
        this.emitter  = emitter;

    }

    /** Get the emitter ground station.
     * @return emitter ground station
     */
    public GroundStation getEmitterStation() {
        return emitter;
    }

    /** Get the receiver ground station.
     * @return receiver ground station
     */
    public GroundStation getReceiverStation() {
        return receiver;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected EstimatedMeasurementBase<BistaticRange> theoreticalEvaluationWithoutDerivatives(final int iteration,
                                                                                              final int evaluation,
                                                                                              final SpacecraftState[] states) {

        final CommonParametersWithoutDerivatives common = getReceiverStation().
            computeRemoteParametersWithout(states, getSatellites().get(0), getDate(), false);

        final TimeStampedPVCoordinates transitPV   = common.getTransitPV();
        final AbsoluteDate             transitDate = transitPV.getDate();

        // Uplink time of flight from emitter station to transit state
        final PVCoordinatesProvider emitterPVCoordinatesProvider = getEmitterStation().getPVCoordinatesProvider();
        final SignalTravelTimeAdjustableEmitter signalTimeOfFlight = getSignalTravelTimeModel().getAdjustableEmitterComputer(emitterPVCoordinatesProvider);
        final double tauU = signalTimeOfFlight.computeDelay(transitPV.getPosition(), transitDate, states[0].getFrame());

        // Secondary station PV in inertial frame at rebound date on secondary station
        final TimeStampedPVCoordinates emitterPV = emitterPVCoordinatesProvider.getPVCoordinates(transitDate.shiftedBy(-tauU), states[0].getFrame());

        // Prepare the evaluation
        final EstimatedMeasurementBase<BistaticRange> estimated =
                        new EstimatedMeasurementBase<>(this,
                                                       iteration, evaluation,
                                                       new SpacecraftState[] {
                                                           common.getTransitState()
                                                       },
                                                       new TimeStampedPVCoordinates[] {
                                                           common.getRemotePV(),
                                                           transitPV,
                                                           emitterPV
                                                       });

        // Clock offsets
        final double dte = getEmitterStation().getClockOffsetDriver().getValue(common.getState().getDate());
        final double dtr = getReceiverStation().getClockOffsetDriver().getValue(common.getState().getDate());

        // Range value
        final double tau = common.getTauD() + tauU + dtr - dte;
        final double range = tau * Constants.SPEED_OF_LIGHT;

        estimated.setEstimatedValue(range);

        return estimated;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected EstimatedMeasurement<BistaticRange> theoreticalEvaluation(final int iteration,
                                                                        final int evaluation,
                                                                        final SpacecraftState[] states) {
        final SpacecraftState state = states[0];

        // Bistatic range derivatives are computed with respect to spacecraft state in inertial frame
        // and station parameters
        // ----------------------
        //
        // Parameters:
        //  - 0..2 - Position of the spacecraft in inertial frame
        //  - 3..5 - Velocity of the spacecraft in inertial frame
        //  - 6..n - measurements parameters (clock offset, station offsets, pole, prime meridian, sat clock offset...)
        final CommonParametersWithDerivatives common = getReceiverStation().
            computeRemoteParametersWith(states, getSatellites().get(0), getDate(), false, getParametersDrivers());

        final int                                     nbParams    = common.getTauD().getFreeParameters();
        final TimeStampedFieldPVCoordinates<Gradient> transitPV   = common.getTransitPV();
        final FieldAbsoluteDate<Gradient>             transitDate = transitPV.getDate();

        // Uplink time of flight from emitter to transit state
        // states[0].getPVCoordinates(states[0].getFrame()).shiftedBy(transitDate.durationFrom(states[0].getDate()).getValue());
        // does not QUITE equal transitPV
        // transitPV gradients do NOT equal pvaDownlink gradient once the movement to the proper time is made
        final FieldPVCoordinatesProvider<Gradient> emitterPVCoordsProvider = getEmitterStation().getFieldPVCoordinatesProvider(nbParams, common.getIndices());
        final FieldSignalTravelTimeAdjustableEmitter<Gradient> fieldComputer = getSignalTravelTimeModel().getAdjustableEmitterComputer(emitterPVCoordsProvider);
        final Gradient tauU = fieldComputer.computeDelay(transitPV.getPosition(), transitPV.getDate(), state.getFrame());

        // Emitter coordinates at transmit time
        final TimeStampedFieldPVCoordinates<Gradient> emitterPV
                 = emitterPVCoordsProvider.getPVCoordinates(transitDate, states[0].getFrame()).shiftedBy(tauU.negate());

        // Prepare the evaluation
        final EstimatedMeasurement<BistaticRange> estimated = new EstimatedMeasurement<>(this,
                iteration, evaluation,
                new SpacecraftState[] {
                    common.getTransitState()
                },
                new TimeStampedPVCoordinates[] {
                    common.getRemotePV().toTimeStampedPVCoordinates(),
                    common.getTransitPV().toTimeStampedPVCoordinates(),
                    emitterPV.toTimeStampedPVCoordinates()
                });

        // Clock offsets
        final Gradient dte = getEmitterStation().getClockOffsetDriver().getValue(nbParams, common.getIndices(), state.getDate());
        final Gradient dtr = getReceiverStation().getClockOffsetDriver().getValue(nbParams, common.getIndices(), state.getDate());

        // Range value
        final Gradient tau   = common.getTauD().add(tauU).add(dtr).subtract(dte);
        final Gradient range = tau.multiply(Constants.SPEED_OF_LIGHT);

        estimated.setEstimatedValue(range.getValue());

        // Range first order derivatives with respect to state
        final double[] derivatives = range.getGradient();
        estimated.setStateDerivatives(0, Arrays.copyOfRange(derivatives, 0, 6));

        // Set first order derivatives with respect to parameters
        for (final ParameterDriver driver : getParametersDrivers()) {
            for (Span<String> span = driver.getNamesSpanMap().getFirstSpan(); span != null; span = span.next()) {
                final Integer index = common.getIndices().get(span.getData());
                if (index != null) {
                    estimated.setParameterDerivatives(driver, span.getStart(), derivatives[index]);
                }
            }
        }

        return estimated;
    }

}
