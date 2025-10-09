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
package org.orekit.estimation.measurements;

import org.hipparchus.CalculusFieldElement;
import org.hipparchus.geometry.euclidean.threed.FieldVector3D;
import org.hipparchus.util.FastMath;
import org.orekit.frames.Frame;
import org.orekit.time.FieldAbsoluteDate;
import org.orekit.utils.Constants;
import org.orekit.utils.FieldPVCoordinatesProvider;

/**
 * Abstract class for computing signal travel time in vacuum.
 * @since 14.0
 * @author Romain Serra
 * @author Luc Maisonnobe
 */
abstract class FieldAbstractSignalTravelTime<T extends CalculusFieldElement<T>> {

    /** Reciprocal for light speed. */
    protected static final double C_RECIPROCAL = 1.0 / Constants.SPEED_OF_LIGHT;

    /** Maximum number of iterations. */
    private static final int MAX_ITER = 10;

    /** Compute propagation delay on a link leg (typically downlink or uplink).
     * @param pvCoordinatesProvider adjustable emitter/receiver
     * @param initialOffset guess for the time off set
     * @param fixedPosition fixed receiver/emitter position
     * @param guessDate guess for emission/reception date
     * @param frame Inertial frame in which receiver/emitter is defined.
     * @return <em>positive</em> delay between signal emission and signal reception dates
     */
    protected T compute(final FieldPVCoordinatesProvider<T> pvCoordinatesProvider, final T initialOffset,
                        final FieldVector3D<T> fixedPosition, final FieldAbsoluteDate<T> guessDate, final Frame frame) {
        T delay = initialOffset;

        // search signal transit date, computing the signal travel in the frame shared by emitter and receiver
        T delta;
        int count = 0;
        do {
            final T previous           = delay.add(0.0);
            final T shift = computeShift(initialOffset, delay);
            final FieldVector3D<T> position = pvCoordinatesProvider.getPosition(guessDate.shiftedBy(shift), frame);
            delay                           = position.distance(fixedPosition).multiply(C_RECIPROCAL);
            delta                           = FastMath.abs(delay.subtract(previous));
        } while (count++ < MAX_ITER && delta.norm() >= 2 * FastMath.ulp(delay.getReal()));

        return delay;
    }

    /**
     * Computes the time shift.
     * @param offset time offset
     * @param delay time delay
     * @return time shift to use in computation
     */
    protected abstract T computeShift(T offset, T delay);

}
