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
import org.orekit.frames.Frame;
import org.orekit.propagation.SpacecraftState;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.AbsolutePVCoordinates;
import org.orekit.utils.PVCoordinatesProvider;

/**
 * Class for computing signal time of flight with an adjustable emitter and a fixed receiver's position and date.
 * @since 14.0
 * @author Romain Serra
 */
public class SignalTravelTimeAdjustableEmitter extends AbstractSignalTravelTime {

    /** Position/velocity provider of emitter. */
    private final PVCoordinatesProvider adjustableEmitterPVProvider;

    /**
     * Constructor.
     * @param adjustableEmitterPVProvider adjustable emitter
     */
    public SignalTravelTimeAdjustableEmitter(final PVCoordinatesProvider adjustableEmitterPVProvider) {
        this.adjustableEmitterPVProvider = adjustableEmitterPVProvider;
    }

    /**
     * Build instance from spacecraft state.
     * @param state spacecraft state
     * @return signal travel time computer
     */
    public static SignalTravelTimeAdjustableEmitter of(final SpacecraftState state) {
        return new SignalTravelTimeAdjustableEmitter(new AbsolutePVCoordinates(state.getFrame(), state.getPVCoordinates()));
    }

    /** Compute propagation delay on a link leg (typically downlink or uplink) without a guess.
     * @param receiverPosition fixed position of receiver at {@code signalArrivalDate}
     * @param signalArrivalDate date at which the signal arrives to receiver
     * @param frame Inertial frame in which receiver is defined.
     * @return <em>positive</em> delay between signal emission and signal reception dates
     */
    public double compute(final Vector3D receiverPosition, final AbsoluteDate signalArrivalDate, final Frame frame) {
        final Vector3D emitterPosition = adjustableEmitterPVProvider.getPosition(signalArrivalDate, frame);
        final double distance = receiverPosition.subtract(emitterPosition).getNorm();
        final AbsoluteDate approxEmissionDate = signalArrivalDate.shiftedBy(-distance * C_RECIPROCAL);
        return compute(approxEmissionDate, receiverPosition, signalArrivalDate, frame);
    }

    /** Compute propagation delay on a link leg (typically downlink or uplink).
     * @param approxEmissionDate approximate emission date
     * @param receiverPosition fixed position of receiver at {@code signalArrivalDate}
     * @param signalArrivalDate date at which the signal arrives to receiver
     * @param frame Inertial frame in which receiver is defined.
     * @return <em>positive</em> delay between signal emission and signal reception dates
     */
    public double compute(final AbsoluteDate approxEmissionDate, final Vector3D receiverPosition,
                          final AbsoluteDate signalArrivalDate, final Frame frame) {

        // initialize emission date search loop assuming the state is already correct
        // this will be true for all but the first orbit determination iteration,
        // and even for the first iteration the loop will converge very fast
        final double offset = signalArrivalDate.durationFrom(approxEmissionDate);

        return compute(adjustableEmitterPVProvider, offset, receiverPosition, approxEmissionDate, frame);
    }

    @Override
    protected double computeShift(final double offset, final double delay) {
        return offset - delay;
    }
}
