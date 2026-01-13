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
import org.hipparchus.geometry.euclidean.threed.FieldVector3D;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.estimation.measurements.signal.SignalTravelTimeModel;
import org.orekit.frames.Frame;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.FieldAbsoluteDate;
import org.orekit.utils.FieldPVCoordinatesProvider;
import org.orekit.utils.PVCoordinatesProvider;

/**
 * Perfect measurement model for right ascension and declination.
 * It is passive in the sense that the sensor did not generate the signal in the first place, it is only collecting it.
 * @since 14.0
 * @author Romain Serra
 */
public class RaDecMeasurementModel {

    /** Reference frame defining axis used with right ascension and declination. */
    private final Frame referenceFrame;

    /** Signal travel time model. */
    private final SignalTravelTimeModel signalTravelTimeModel;

    /**
     * Constructor.
     * @param referenceFrame reference frame for RA-Dec
     * @param signalTravelTimeModel time delay computer
     */
    public RaDecMeasurementModel(final Frame referenceFrame, final SignalTravelTimeModel signalTravelTimeModel) {
        this.referenceFrame = referenceFrame;
        this.signalTravelTimeModel = signalTravelTimeModel;
    }

    /**
     * Compute theoretical measurement.
     * @param frame frame where receiver position is given
     * @param receiverPosition receiver position in input frame at reception time
     * @param receptionDate signal reception date
     * @param emitter signal emitter coordinates provider
     * @param approxEmissionDate guess for the emission date (shall be adjusted by signal travel time computer)
     * @return RA-Dec (radians)
     */
    public double[] value(final Frame frame, final Vector3D receiverPosition, final AbsoluteDate receptionDate,
                          final PVCoordinatesProvider emitter, final AbsoluteDate approxEmissionDate) {
        // Refine time delay
        final double signalTravelTime = signalTravelTimeModel.getAdjustableEmitterComputer(emitter)
                .computeDelay(approxEmissionDate, receiverPosition, receptionDate, frame);
        final AbsoluteDate emissionDate = receptionDate.shiftedBy(-signalTravelTime);

        // Compute line of sight in reference frame
        final Vector3D observedPosition = emitter.getPosition(emissionDate, frame);
        final Vector3D apparentLineOfSightInInputFrame = observedPosition.subtract(receiverPosition).normalize();
        final Vector3D apparentLineOfSight = frame.getStaticTransformTo(referenceFrame, receptionDate)
                .transformVector(apparentLineOfSightInInputFrame);

        // Compute right ascension and declination
        final double rightAscension = apparentLineOfSight.getAlpha();
        final double declination = apparentLineOfSight.getDelta();
        return new double[] { rightAscension, declination };
    }

    /**
     * Compute theoretical measurement in Taylor Differential Algebra.
     * @param frame frame where receiver position is given
     * @param receiverPosition receiver position in input frame at reception time
     * @param receptionDate signal reception date
     * @param emitter signal emitter coordinates provider
     * @param approxEmissionDate guess for the emission date (shall be adjusted by signal travel time computer)
     * @return RA-Dec (radians)
     */
    public Gradient[] value(final Frame frame, final FieldVector3D<Gradient> receiverPosition,
                            final FieldAbsoluteDate<Gradient> receptionDate,
                            final FieldPVCoordinatesProvider<Gradient> emitter,
                            final FieldAbsoluteDate<Gradient> approxEmissionDate) {
        // Refine time delay
        final Gradient signalTravelTime = signalTravelTimeModel.getAdjustableEmitterComputer(emitter)
                .computeDelay(approxEmissionDate, receiverPosition, receptionDate, frame);
        final FieldAbsoluteDate<Gradient> emissionDate = receptionDate.shiftedBy(signalTravelTime.negate());

        // Compute line of sight in reference frame
        final FieldVector3D<Gradient> observedPosition = emitter.getPosition(emissionDate, frame);
        final FieldVector3D<Gradient> apparentLineOfSightInInputFrame = observedPosition.subtract(receiverPosition);
        final FieldVector3D<Gradient> apparentLineOfSight = frame.getStaticTransformTo(referenceFrame, receptionDate)
                .transformVector(apparentLineOfSightInInputFrame);

        // Compute right ascension and declination
        final Gradient rightAscension = apparentLineOfSight.getAlpha();
        final Gradient declination = apparentLineOfSight.getDelta();
        return new Gradient[] { rightAscension, declination };
    }
}
