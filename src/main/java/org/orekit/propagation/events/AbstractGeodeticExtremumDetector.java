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
package org.orekit.propagation.events;

import org.hipparchus.analysis.differentiation.UnivariateDerivative2;
import org.hipparchus.analysis.differentiation.UnivariateDerivative2Field;
import org.orekit.bodies.BodyShape;
import org.orekit.bodies.FieldGeodeticPoint;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.events.handlers.EventHandler;
import org.orekit.time.FieldAbsoluteDate;
import org.orekit.utils.FieldPVCoordinates;

/** Abstract class for geodetic coordinates extremum.
 * @author Romain Serra
 * @since 14.0
 */
public abstract class AbstractGeodeticExtremumDetector<T extends AbstractDetector<T>> extends AbstractGeographicalDetector<T> {

    /** Protected constructor with full parameters.
     * <p>
     * This constructor is not public as users are expected to use the builder
     * API with the various {@code withXxx()} methods to set up the instance
     * in a readable manner without using a huge amount of parameters.
     * </p>
     * @param detectionSettings event detection settings
     * @param handler event handler to call at event occurrences
     * @param body body
     * @since 13.0
     */
    protected AbstractGeodeticExtremumDetector(final EventDetectionSettings detectionSettings, final EventHandler handler,
                                               final BodyShape body) {
        super(detectionSettings, handler, body);
    }

    /** Compute the geodetic coordinates with automatic differentiation.
     * @param s the current state information: date, kinematics, attitude
     * @return geodetic point in Taylor Differential Algebra
     */
    public FieldGeodeticPoint<UnivariateDerivative2> transformToFieldGeodeticPoint(final SpacecraftState s) {
        final FieldPVCoordinates<UnivariateDerivative2> pv = s.getPVCoordinates().toUnivariateDerivative2PV();
        final UnivariateDerivative2Field field = UnivariateDerivative2Field.getInstance();
        final UnivariateDerivative2 dt = new UnivariateDerivative2(0, 1, 0);
        final FieldAbsoluteDate<UnivariateDerivative2> fieldDate = new FieldAbsoluteDate<>(field, s.getDate()).shiftedBy(dt);
        return getBodyShape().transform(pv.getPosition(), s.getFrame(), fieldDate);
    }

}
