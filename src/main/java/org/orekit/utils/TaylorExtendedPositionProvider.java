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
package org.orekit.utils;

import org.hipparchus.CalculusFieldElement;
import org.hipparchus.Field;
import org.hipparchus.geometry.euclidean.threed.FieldVector3D;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.frames.Frame;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.FieldAbsoluteDate;

/**
 * Class providing position, including Field, according to 2nd order Taylor expansion.
 * @author Romain Serra
 * @see AbsolutePVCoordinates
 * @since 14.0
 */
public class TaylorExtendedPositionProvider implements ExtendedPositionProvider {

    /** Absolute Cartesian coordinates. */
    private final AbsolutePVCoordinates absolutePVCoordinates;

    /**
     * Constructor.
     * @param absolutePVCoordinates absolute Cartesian coordinates
     */
    public TaylorExtendedPositionProvider(final AbsolutePVCoordinates absolutePVCoordinates) {
        this.absolutePVCoordinates = absolutePVCoordinates;
    }

    @Override
    public Vector3D getPosition(final AbsoluteDate date, final Frame frame) {
        return absolutePVCoordinates.getPosition(date, frame);
    }

    @Override
    public Vector3D getVelocity(final AbsoluteDate date, final Frame frame) {
        return absolutePVCoordinates.getVelocity(date, frame);
    }

    @Override
    public TimeStampedPVCoordinates getPVCoordinates(final AbsoluteDate date, final Frame frame) {
        return absolutePVCoordinates.getPVCoordinates(date, frame);
    }

    @Override
    public <T extends CalculusFieldElement<T>> FieldVector3D<T> getPosition(final FieldAbsoluteDate<T> date,
                                                                            final  Frame frame) {
        return getFieldAbsolute(date.getField()).getPosition(date, frame);
    }

    @Override
    public <T extends CalculusFieldElement<T>> FieldVector3D<T> getVelocity(final FieldAbsoluteDate<T> date,
                                                                            final Frame frame) {
        return getFieldAbsolute(date.getField()).getVelocity(date, frame);
    }

    @Override
    public <T extends CalculusFieldElement<T>> TimeStampedFieldPVCoordinates<T> getPVCoordinates(final FieldAbsoluteDate<T> date,
                                                                                                 final Frame frame) {
        return getFieldAbsolute(date.getField()).getPVCoordinates(date, frame);
    }

    /**
     * Build a Field version of the absolute coordinates.
     * @param field field
     * @return field absolute coordinates
     * @param <T> field type
     */
    private <T extends CalculusFieldElement<T>> FieldAbsolutePVCoordinates<T> getFieldAbsolute(final Field<T> field) {
        return new FieldAbsolutePVCoordinates<>(field, absolutePVCoordinates);
    }
}
