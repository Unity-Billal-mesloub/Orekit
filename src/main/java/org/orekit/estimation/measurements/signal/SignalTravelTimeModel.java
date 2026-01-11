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
import org.hipparchus.optim.ConvergenceChecker;
import org.orekit.utils.FieldPVCoordinatesProvider;
import org.orekit.utils.PVCoordinatesProvider;

/**
 * Full model for signal travel time (adjustable receiver/emitter with fixed emission/reception),
 * compatible with automatic differentiation.
 * @since 14.0
 * @author Romain Serra
 */
public class SignalTravelTimeModel {

    /** Convergence checker for standard values. */
    private final ConvergenceChecker<Double> convergenceChecker;

    /** Convergence checker for automatic differentiation. */
    private final ConvergenceChecker<Gradient> gradientConvergenceChecker;

    /**
     * Constructor.
     * @param convergenceChecker convergence settings for standard values
     * @param gradientConvergenceChecker convergence settings for automatic differentiation
     */
    public SignalTravelTimeModel(final ConvergenceChecker<Double> convergenceChecker,
                                 final ConvergenceChecker<Gradient> gradientConvergenceChecker) {
        this.convergenceChecker = convergenceChecker;
        this.gradientConvergenceChecker = gradientConvergenceChecker;
    }

    /**
     * Constructor.
     * @param convergenceChecker convergence settings for standard values
     */
    public SignalTravelTimeModel(final ConvergenceChecker<Double> convergenceChecker) {
        this(convergenceChecker, FieldAbstractSignalTravelTime.getDefaultConvergenceChecker());
    }

    /**
     * Constructor.
     */
    public SignalTravelTimeModel() {
        this(AbstractSignalTravelTime.getDefaultConvergenceChecker());
    }

    /**
     * Method constructing a delay computer with input emitter.
     * @param emitter signal emitter
     * @return (positive) time delay
     */
    public SignalTravelTimeAdjustableEmitter getAdjustableEmitterComputer(final PVCoordinatesProvider emitter) {
        return new SignalTravelTimeAdjustableEmitter(emitter, convergenceChecker);
    }

    /**
     * Method constructing a delay computer with input receiver.
     * @param receiver signal emitter
     * @return (positive) time delay
     */
    public SignalTravelTimeAdjustableReceiver getAdjustableReceiverComputer(final PVCoordinatesProvider receiver) {
        return new SignalTravelTimeAdjustableReceiver(receiver, convergenceChecker);
    }

    /**
     * Method constructing a delay computer with input emitter.
     * @param emitter signal emitter
     * @return (positive) time delay
     */
    public FieldSignalTravelTimeAdjustableEmitter<Gradient> getAdjustableEmitterComputer(final FieldPVCoordinatesProvider<Gradient> emitter) {
        return new FieldSignalTravelTimeAdjustableEmitter<>(emitter, gradientConvergenceChecker);
    }

    /**
     * Method constructing a delay computer with input receiver.
     * @param receiver signal receiver
     * @return (positive) time delay
     */
    public FieldSignalTravelTimeAdjustableReceiver<Gradient> getFieldAdjustableReceiverComputer(final FieldPVCoordinatesProvider<Gradient> receiver) {
        return new FieldSignalTravelTimeAdjustableReceiver<>(receiver, gradientConvergenceChecker);
    }
}
