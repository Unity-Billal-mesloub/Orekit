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
package org.orekit.control.indirect.adjoint.cost;

import org.hipparchus.CalculusFieldElement;
import org.orekit.propagation.events.functions.EventFunction;
import org.orekit.propagation.events.FieldEventDetectionSettings;
import org.orekit.propagation.events.FieldEventDetector;
import org.orekit.propagation.events.functions.EventFunctionModifier;
import org.orekit.propagation.events.handlers.FieldEventHandler;
import org.orekit.propagation.events.handlers.FieldResetDerivativesOnEvent;

/**
 * Abstract event detector for singularities in adjoint dynamics.
 *
 * @author Romain Serra
 * @since 13.0
 */
public abstract class FieldControlSwitchDetector<T extends CalculusFieldElement<T>> implements FieldEventDetector<T> {

    /** Event handler. */
    private final FieldEventHandler<T> handler = new FieldResetDerivativesOnEvent<>();

    /** Event detection settings. */
    private final FieldEventDetectionSettings<T> detectionSettings;

    /** Event function. */
    private final EventFunction eventFunction;

    /**
     * Constructor.
     * @param detectionSettings detection settings
     */
    protected FieldControlSwitchDetector(final FieldEventDetectionSettings<T> detectionSettings) {
        this.detectionSettings = detectionSettings;
        this.eventFunction = new LocalEventFunction(EventFunction.of(detectionSettings.getThreshold().getField(), this::g));
    }

    @Override
    public EventFunction getEventFunction() {
        return eventFunction;
    }

    @Override
    public FieldEventDetectionSettings<T> getDetectionSettings() {
        return detectionSettings;
    }

    @Override
    public FieldEventHandler<T> getHandler() {
        return handler;
    }

    private static class LocalEventFunction implements EventFunctionModifier {
        /** Wrapped event function. */
        private final EventFunction baseFunction;

        LocalEventFunction(final EventFunction baseFunction) {
            this.baseFunction = baseFunction;
        }

        @Override
        public EventFunction getBaseFunction() {
            return baseFunction;
        }

        @Override
        public boolean dependsOnMainVariablesOnly() {
            return false;
        }
    }
}
