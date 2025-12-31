/* Copyright 2002-2026 Brianna Aubin
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

import org.orekit.utils.PVCoordinatesProvider;

/** Abstract interface that contains those methods necessary
 *  for both space and ground-based satellite observers.
 *
 * @author Brianna Aubin
 * @since 14.0
 */
interface Observer  {

    enum ObserverType {
        /** Indicates a ground-based observation station. */
        GROUNDSTATION,

        /** Indicates a space-based observer. */
        SATELLITE;
    }

    /** Get the type of object being used in measurement observations.
     * @return string value
     * @since 14.0
     */
    ObserverType getObserverType();

    /** Return the PVCoordinatesProvider.
     * @return pos/vel coordinates provider
     * @since 14.0
     */
    PVCoordinatesProvider getPVCoordinatesProvider();
}
