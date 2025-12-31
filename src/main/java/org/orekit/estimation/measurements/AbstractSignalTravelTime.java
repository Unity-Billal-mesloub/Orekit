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
import org.hipparchus.util.FastMath;
import org.orekit.frames.Frame;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.Constants;
import org.orekit.utils.PVCoordinatesProvider;

/**
 * Abstract class for computing signal travel time in vacuum.
 * @since 14.0
 * @author Romain Serra
 * @author Luc Maisonnobe
 */
abstract class AbstractSignalTravelTime {

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
    protected double compute(final PVCoordinatesProvider pvCoordinatesProvider, final double initialOffset,
                             final Vector3D fixedPosition, final AbsoluteDate guessDate, final Frame frame) {
        double delay = initialOffset;

        // search signal transit date, computing the signal travel in inertial frame
        double delta;
        int count = 0;
        do {
            final double previous = delay;
            final double shift = computeShift(initialOffset, delay);
            final Vector3D pos    = pvCoordinatesProvider.getPosition(guessDate.shiftedBy(shift), frame);
            delay                 = fixedPosition.distance(pos) * C_RECIPROCAL;
            delta                 = FastMath.abs(delay - previous);
        } while (count++ < MAX_ITER && delta >= 2 * FastMath.ulp(delay));

        return delay;

    }

    /**
     * Computes the time shift.
     * @param offset time offset
     * @param delay time delay
     * @return time shift to use in computation
     */
    protected abstract double computeShift(double offset, double delay);

}
