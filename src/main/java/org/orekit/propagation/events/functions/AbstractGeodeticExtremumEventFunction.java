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
package org.orekit.propagation.events.functions;

import org.hipparchus.CalculusFieldElement;
import org.hipparchus.analysis.differentiation.FieldUnivariateDerivative2;
import org.hipparchus.analysis.differentiation.UnivariateDerivative2;
import org.hipparchus.analysis.differentiation.UnivariateDerivative2Field;
import org.orekit.bodies.BodyShape;
import org.orekit.bodies.FieldGeodeticPoint;
import org.orekit.propagation.FieldSpacecraftState;
import org.orekit.propagation.SpacecraftState;
import org.orekit.time.FieldAbsoluteDate;
import org.orekit.utils.FieldPVCoordinates;

/** Abstract class for geodetic coordinates extremum event function..
 * @author Romain Serra
 * @since 14.0
 */
public abstract class AbstractGeodeticExtremumEventFunction implements EventFunction {

    /** Body shape. */
    private final BodyShape bodyShape;

    /** Constructor.
     * @param body body
     */
    protected AbstractGeodeticExtremumEventFunction(final BodyShape body) {
        this.bodyShape = body;
    }

    /**
     * Getter for body shape.
     * @return body
     */
    public BodyShape getBodyShape() {
        return bodyShape;
    }

    /** Compute the geodetic coordinates with automatic differentiation.
     * @param s the current state information: date, kinematics, attitude
     * @return geodetic point in Taylor Differential Algebra
     */
    protected FieldGeodeticPoint<UnivariateDerivative2> transformToFieldGeodeticPoint(final SpacecraftState s) {
        final FieldPVCoordinates<UnivariateDerivative2> pv = s.getPVCoordinates().toUnivariateDerivative2PV();
        final UnivariateDerivative2Field field = UnivariateDerivative2Field.getInstance();
        final UnivariateDerivative2 dt = new UnivariateDerivative2(0, 1, 0);
        final FieldAbsoluteDate<UnivariateDerivative2> fieldDate = new FieldAbsoluteDate<>(field, s.getDate()).shiftedBy(dt);
        return getBodyShape().transform(pv.getPosition(), s.getFrame(), fieldDate);
    }

    /** Compute the geodetic coordinates with automatic differentiation.
     * @param s the current state information: date, kinematics, attitude
     * @param <T> field type
     * @return geodetic point in Taylor Differential Algebra
     */
    protected <T extends CalculusFieldElement<T>> FieldGeodeticPoint<FieldUnivariateDerivative2<T>> transformToFieldGeodeticPoint(final FieldSpacecraftState<T> s) {        // convert state to geodetic coordinates
        final FieldAbsoluteDate<FieldUnivariateDerivative2<T>> fud2Date = s.getDate().toFUD2Field();
        final FieldPVCoordinates<FieldUnivariateDerivative2<T>> pv = s.getPVCoordinates().toUnivariateDerivative2PV();
        return getBodyShape().transform(pv.getPosition(), s.getFrame(), fud2Date);
    }
}
