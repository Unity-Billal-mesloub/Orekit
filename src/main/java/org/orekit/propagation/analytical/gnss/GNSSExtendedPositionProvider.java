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
package org.orekit.propagation.analytical.gnss;

import org.hipparchus.CalculusFieldElement;
import org.hipparchus.Field;
import org.hipparchus.geometry.euclidean.threed.FieldVector3D;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.attitudes.AttitudeProvider;
import org.orekit.frames.Frame;
import org.orekit.propagation.analytical.gnss.data.GNSSOrbitalElements;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.FieldAbsoluteDate;
import org.orekit.utils.ExtendedPositionProvider;
import org.orekit.utils.TimeStampedFieldPVCoordinates;
import org.orekit.utils.TimeStampedPVCoordinates;

/**
 * Class for GNSS extended position provider.
 * @see ExtendedPositionProvider
 * @author Romain Serra
 * @since 14.0
 */
public class GNSSExtendedPositionProvider implements ExtendedPositionProvider {

    /** Internal propagator. */
    private final GNSSPropagator gnssPropagator;

    /**
     * Build a new instance.
     * @param orbitalElements GNSS orbital elements
     * @param eci Earth Centered Inertial frame
     * @param ecef Earth Centered Earth Fixed frame
     * @param provider attitude provider
     * @param mass satellite mass (kg)
     */
    public GNSSExtendedPositionProvider(final GNSSOrbitalElements<?> orbitalElements, final Frame eci,
                                        final Frame ecef, final AttitudeProvider provider, final double mass) {
        this.gnssPropagator = new GNSSPropagator(orbitalElements, eci, ecef, provider, mass);
    }

    @Override
    public Vector3D getPosition(final AbsoluteDate date, final Frame frame) {
        return gnssPropagator.getPosition(date, frame);
    }

    @Override
    public Vector3D getVelocity(final AbsoluteDate date, final Frame frame) {
        return gnssPropagator.getVelocity(date, frame);
    }

    @Override
    public TimeStampedPVCoordinates getPVCoordinates(final AbsoluteDate date, final Frame frame) {
        return gnssPropagator.getPVCoordinates(date, frame);
    }

    @Override
    public <T extends CalculusFieldElement<T>> FieldVector3D<T> getPosition(final FieldAbsoluteDate<T> date,
                                                                            final Frame frame) {
        return getFieldPropagator(date.getField()).getPosition(date, frame);
    }

    @Override
    public <T extends CalculusFieldElement<T>> FieldVector3D<T> getVelocity(final FieldAbsoluteDate<T> date,
                                                                            final Frame frame) {
        return getFieldPropagator(date.getField()).getVelocity(date, frame);
    }

    @Override
    public <T extends CalculusFieldElement<T>> TimeStampedFieldPVCoordinates<T> getPVCoordinates(final FieldAbsoluteDate<T> date,
                                                                                                 final Frame frame) {
        return getFieldPropagator(date.getField()).getPVCoordinates(date, frame);
    }

    /**
     * Build Field propagator.
     * @param field field
     * @return field propagator
     * @param <T> field type
     */
    private <T extends CalculusFieldElement<T>> FieldGnssPropagator<T> getFieldPropagator(final Field<T> field) {
        return new FieldGnssPropagator<>(gnssPropagator.getOrbitalElements().toField(field),
                gnssPropagator.getECI(), gnssPropagator.getECEF(), gnssPropagator.getAttitudeProvider(),
                field.getZero().newInstance(gnssPropagator.getInitialState().getMass()));
    }
}
