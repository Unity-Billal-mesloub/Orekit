/* Copyright 2002-2025 Brianna Aubin
 * Licensed to Hawkeye 360 (HE360) under one or more
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

import org.orekit.time.clocks.QuadraticClockModel;
import org.orekit.utils.PVCoordinatesProvider;

/** Class that accepts a PVCoordinatesProvider for a space-
 * based measurement receiver.
 *
 * @author Brianna Aubin
 * @since 14.0
 */
public class ObserverSatellite extends MeasurementObject implements Observer {

    /** Provides satellite trajectory. */
    private final PVCoordinatesProvider pvCoordsProvider;

    /** Simple constructor.
     * @param name name of receiver
     * @param pvCoordsProvider position/velocity coordinates provider for receiver
     * @since 14.0
     */
    public ObserverSatellite(final String name, final PVCoordinatesProvider pvCoordsProvider) {
        super(name);
        this.pvCoordsProvider = pvCoordsProvider;
    }

    /** Simple constructor.
     * @param name name of receiver
     * @param pvCoordsProvider position/velocity coordinates provider for receiver
     * @param quadraticClock clock model for receiver
     * @since 14.0
     */
    public ObserverSatellite(final String name, final PVCoordinatesProvider pvCoordsProvider, final QuadraticClockModel quadraticClock) {
        super(name, quadraticClock);
        this.pvCoordsProvider = pvCoordsProvider;
    }

    /** {@inheritDoc} */
    @Override
    public final ObserverType getObserverType() {
        return ObserverType.SATELLITE;
    }

    /** {@inheritDoc} */
    @Override
    public final PVCoordinatesProvider getPVCoordinatesProvider() {
        return pvCoordsProvider;
    }

}
