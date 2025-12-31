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
package org.orekit.orbits;

import org.hipparchus.CalculusFieldElement;
import org.hipparchus.Field;
import org.hipparchus.geometry.euclidean.threed.FieldVector3D;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.frames.Frame;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.FieldAbsoluteDate;
import org.orekit.utils.ExtendedPositionProvider;
import org.orekit.utils.PVCoordinates;
import org.orekit.utils.TimeStampedFieldPVCoordinates;
import org.orekit.utils.TimeStampedPVCoordinates;

import java.util.HashMap;
import java.util.Map;

/**
 * Position provider assuming pure Keplerian motion.
 * Propagation is computed with the same orbital parameters used to define the reference.
 *
 * @author Romain Serra
 * @see org.orekit.utils.ExtendedPositionProvider
 * @see Orbit
 * @see FieldOrbit
 *
 * @since 14.0
 */
public class KeplerianExtendedPositionProvider implements ExtendedPositionProvider {

    /** Reference orbit. */
    private final Orbit referenceOrbit;

    /** Cache for already encountered Field. */
    private final Map<Field<? extends CalculusFieldElement<?>>, FieldOrbit<?>> fieldCache;

    /**
     * Constructor.
     * @param referenceOrbit reference orbit (non-Keplerian terms will be ignored if any)
     */
    public KeplerianExtendedPositionProvider(final Orbit referenceOrbit) {
        // Remove non-Keplerian rates if any
        final PVCoordinates keplerianPV = new PVCoordinates(referenceOrbit.getPosition(), referenceOrbit.getVelocity());
        final CartesianOrbit cartesianOrbit = new CartesianOrbit(keplerianPV, referenceOrbit.getFrame(),
                referenceOrbit.getDate(), referenceOrbit.getMu());
        this.referenceOrbit = referenceOrbit.getType().convertType(cartesianOrbit);
        this.fieldCache = new HashMap<>();
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D getPosition(final AbsoluteDate date, final Frame frame) {
        return referenceOrbit.getPosition(date, frame);
    }

    /** {@inheritDoc} */
    @Override
    public Vector3D getVelocity(final AbsoluteDate date, final Frame frame) {
        return referenceOrbit.getVelocity(date, frame);
    }

    /** {@inheritDoc} */
    @Override
    public TimeStampedPVCoordinates getPVCoordinates(final AbsoluteDate date, final Frame frame) {
        return referenceOrbit.getPVCoordinates(date, frame);
    }

    /** {@inheritDoc} */
    @Override
    public <T extends CalculusFieldElement<T>> FieldVector3D<T> getPosition(final FieldAbsoluteDate<T> date,
                                                                            final Frame frame) {
        return buildFieldOrbit(date.getField()).getPosition(date, frame);
    }

    /** {@inheritDoc} */
    @Override
    public <T extends CalculusFieldElement<T>> FieldVector3D<T> getVelocity(final FieldAbsoluteDate<T> date, final Frame frame) {
        return buildFieldOrbit(date.getField()).getVelocity(date, frame);
    }

    /** {@inheritDoc} */
    @Override
    public <T extends CalculusFieldElement<T>> TimeStampedFieldPVCoordinates<T> getPVCoordinates(final FieldAbsoluteDate<T> date,
                                                                                                 final Frame frame) {
        return buildFieldOrbit(date.getField()).getPVCoordinates(date, frame);
    }

    /**
     * Convert reference orbit to Field.
     * @param field field type
     * @return FieldOrbit
     * @param <T> field
     */
    @SuppressWarnings("unchecked")
    private <T extends CalculusFieldElement<T>> FieldOrbit<T> buildFieldOrbit(final Field<T> field) {
        final OrbitType orbitType = referenceOrbit.getType();
        return (FieldOrbit<T>) fieldCache.computeIfAbsent(field, ignored -> orbitType.convertToFieldOrbit(field, referenceOrbit));
    }
}
