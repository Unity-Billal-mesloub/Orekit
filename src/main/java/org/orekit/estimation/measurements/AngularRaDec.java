/* Copyright 2002-2026 CS GROUP
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

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.hipparchus.analysis.differentiation.Gradient;
import org.hipparchus.analysis.differentiation.GradientField;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.util.MathUtils;
import org.orekit.estimation.measurements.model.RaDecMeasurementModel;
import org.orekit.frames.Frame;
import org.orekit.propagation.SpacecraftState;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.FieldAbsoluteDate;
import org.orekit.time.clocks.ClockOffset;
import org.orekit.time.clocks.FieldClockOffset;
import org.orekit.time.clocks.QuadraticFieldClockModel;
import org.orekit.utils.FieldPVCoordinatesProvider;
import org.orekit.utils.PVCoordinatesProvider;
import org.orekit.utils.ParameterDriver;
import org.orekit.utils.TimeSpanMap.Span;
import org.orekit.utils.TimeStampedFieldPVCoordinates;
import org.orekit.utils.TimeStampedPVCoordinates;

/** Class modeling a Right Ascension - Declination measurement from a ground point (station, telescope).
 * The angles are given in an inertial reference frame.
 * The motion of the spacecraft during the signal flight time is taken into
 * account. The date of the measurement corresponds to the reception on
 * ground of the reflected signal.
 *
 * @author Thierry Ceolin
 * @author Maxime Journot
 * @since 9.0
 */
public class AngularRaDec extends AbstractMeasurement<AngularRaDec> {

    /** Type of the measurement. */
    public static final String MEASUREMENT_TYPE = "AngularRaDec";

    /** Reference frame in which the right ascension - declination angles are given. */
    private final Frame referenceFrame;

    /** Ground station that receives signal from satellite. */
    private final GroundStation station;

    /** Perfect measurement model. */
    private final RaDecMeasurementModel measurementModel;

    /** Simple constructor.
     * @param station ground station from which measurement is performed
     * @param referenceFrame Reference frame in which the right ascension - declination angles are given
     * @param date date of the measurement
     * @param angular observed value
     * @param sigma theoretical standard deviation
     * @param baseWeight base weight
     * @param satellite satellite related to this measurement
     * @since 9.3
     */
    public AngularRaDec(final GroundStation station, final Frame referenceFrame, final AbsoluteDate date,
                        final double[] angular, final double[] sigma, final double[] baseWeight,
                        final ObservableSatellite satellite) {
        super(date, false, angular, sigma, baseWeight, Collections.singletonList(satellite));
        this.referenceFrame = referenceFrame;
        this.measurementModel = new RaDecMeasurementModel(referenceFrame, getSignalTravelTimeModel());

        addParametersDrivers(station.getParametersDrivers());

        this.station = station;
    }

    /** Get the reference frame in which the right ascension - declination angles are given.
     * @return reference frame in which the right ascension - declination angles are given
     */
    public Frame getReferenceFrame() {
        return referenceFrame;
    }

    /** Get the ground station that receives the signal.
     * @return ground station
     */
    public final GroundStation getStation() {
        return station;
    }

    /** {@inheritDoc} */
    @Override
    protected EstimatedMeasurementBase<AngularRaDec> theoreticalEvaluationWithoutDerivatives(final int iteration,
                                                                                             final int evaluation,
                                                                                             final SpacecraftState[] states) {
        // Compute actual reception time
        final SpacecraftState state = states[0];
        final ClockOffset localClock = station.getQuadraticClockModel().getOffset(state.getDate());
        final AbsoluteDate receptionDate = getDate().shiftedBy(-localClock.getOffset());

        // Compute signal travel time
        final PVCoordinatesProvider emitter = MeasurementObject.extractPVCoordinatesProvider(state, state.getPVCoordinates());
        final PVCoordinatesProvider receiver = station.getPVCoordinatesProvider();
        final double signalTravelTime = getSignalTravelTimeModel().getAdjustableEmitterComputer(emitter)
                .computeDelay(state.getDate(), receiver.getPosition(receptionDate, referenceFrame), receptionDate, referenceFrame);
        final AbsoluteDate emissionDate = receptionDate.shiftedBy(-signalTravelTime);

        // Evaluate angular measurement model
        final Frame frame = state.getFrame();
        final double[] raDec = measurementModel.value(frame, receiver.getPosition(receptionDate, frame), receptionDate,
                emitter, emissionDate);

        // Reset right ascension in correct interval
        final double baseRightAscension = raDec[0];
        final double twoPiWrap          = MathUtils.normalizeAngle(baseRightAscension, getObservedValue()[0]) - baseRightAscension;
        final double rightAscension     = baseRightAscension + twoPiWrap;

        // Prepare the estimation
        final double shift = state.getDate().durationFrom(emissionDate);
        final EstimatedMeasurementBase<AngularRaDec> estimated =
                        new EstimatedMeasurementBase<>(this, iteration, evaluation,
                                                       new SpacecraftState[] {
                                                           state.shiftedBy(shift),
                                                       }, new TimeStampedPVCoordinates[] {
                                                           emitter.getPVCoordinates(emissionDate, frame),
                                                           receiver.getPVCoordinates(receptionDate, frame)
                                                       });

        // RA - Dec values
        estimated.setEstimatedValue(rightAscension, raDec[1]);

        return estimated;

    }

    /** {@inheritDoc} */
    @Override
    protected EstimatedMeasurement<AngularRaDec> theoreticalEvaluation(final int iteration, final int evaluation,
                                                                       final SpacecraftState[] states) {

        final SpacecraftState state = states[0];

        // Right Ascension/declination (in reference frame )derivatives are computed with respect to spacecraft state in inertial frame
        // and station parameters
        // ----------------------
        //
        // Parameters:
        //  - 0..2 - Position of the spacecraft in inertial frame
        //  - 3..5 - Velocity of the spacecraft in inertial frame
        //  - 6..n - station parameters (clock offset, station offsets, pole, prime meridian...)

        // Create the parameter indices map
        final Frame                frame        = states[0].getFrame();
        final Map<String, Integer> paramIndices = station.getParamaterIndices(states, getParametersDrivers());
        final int                  nbParams     = 6 * states.length + paramIndices.size();
        final TimeStampedFieldPVCoordinates<Gradient> pva = AbstractMeasurement.getCoordinates(state, 0, nbParams);

        // Compute actual reception time
        final QuadraticFieldClockModel<Gradient> quadraticClockModel = station.getQuadraticFieldClock(nbParams, getDate(), paramIndices);
        final FieldAbsoluteDate<Gradient> fieldDate = new FieldAbsoluteDate<>(GradientField.getField(nbParams), getDate());
        final FieldClockOffset<Gradient> localClock = quadraticClockModel.getOffset(fieldDate);
        final FieldAbsoluteDate<Gradient> receptionDate = fieldDate.shiftedBy(localClock.getOffset().negate());

        // Compute signal travel time
        final FieldPVCoordinatesProvider<Gradient> receiver = station.getFieldPVCoordinatesProvider(nbParams, paramIndices);
        final FieldPVCoordinatesProvider<Gradient> emitter = MeasurementObject.extractFieldPVCoordinatesProvider(state, pva);
        final Gradient signalTravelTime = getSignalTravelTimeModel().getAdjustableEmitterComputer(emitter)
                .computeDelay(receptionDate, receiver.getPosition(receptionDate, referenceFrame), receptionDate, referenceFrame);

        // Evaluate angular measurement model
        final FieldAbsoluteDate<Gradient> emissionDate = receptionDate.shiftedBy(signalTravelTime.negate());
        final Gradient[] raDec = measurementModel.value(frame, receiver.getPosition(receptionDate, frame), receptionDate,
                emitter, emissionDate);

        // Reset right ascension in correct interval
        final Gradient baseRightAscension = raDec[0];
        final double   twoPiWrap          = MathUtils.normalizeAngle(baseRightAscension.getReal(),
                getObservedValue()[0]) - baseRightAscension.getReal();
        final Gradient rightAscension     = baseRightAscension.add(twoPiWrap);

        // Prepare the estimation
        final double shift = state.getDate().durationFrom(emissionDate.toAbsoluteDate());
        final EstimatedMeasurement<AngularRaDec> estimated =
                        new EstimatedMeasurement<>(this, iteration, evaluation,
                                                   new SpacecraftState[] {
                                                       state.shiftedBy(shift)
                                                   }, new TimeStampedPVCoordinates[] {
                                                       emitter.getPVCoordinates(emissionDate, frame).toTimeStampedPVCoordinates(),
                                                       receiver.getPVCoordinates(receptionDate, frame).toTimeStampedPVCoordinates()
                                                   });

        // RA-Dec values
        final Gradient declination = raDec[1];
        estimated.setEstimatedValue(rightAscension.getValue(), declination.getValue());

        // Partial derivatives of right ascension/declination in reference frame with respect to state
        final double[] raDerivatives  = rightAscension.getGradient();
        final double[] decDerivatives = declination.getGradient();
        estimated.setStateDerivatives(0,
                                      Arrays.copyOfRange(raDerivatives, 0, 6), Arrays.copyOfRange(decDerivatives, 0, 6));

        // Partial derivatives with respect to parameters
        for (final ParameterDriver driver : getParametersDrivers()) {
            for (Span<String> span = driver.getNamesSpanMap().getFirstSpan(); span != null; span = span.next()) {
                final Integer index = paramIndices.get(span.getData());
                if (index != null) {
                    estimated.setParameterDerivatives(driver, span.getStart(), raDerivatives[index], decDerivatives[index]);
                }
            }
        }

        return estimated;

    }

    /** Calculate the Line Of Sight of the given measurement.
     * @param outputFrame output frame of the line of sight vector
     * @return Vector3D the line of Sight of the measurement
     * @since 12.0
     */
    public Vector3D getObservedLineOfSight(final Frame outputFrame) {
        return referenceFrame.getStaticTransformTo(outputFrame, getDate())
            .transformVector(new Vector3D(getObservedValue()[0], getObservedValue()[1]));
    }
}
