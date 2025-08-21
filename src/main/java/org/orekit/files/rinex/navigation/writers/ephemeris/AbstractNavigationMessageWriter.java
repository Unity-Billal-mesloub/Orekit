/* Copyright 2022-2025 Thales Alenia Space
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
package org.orekit.files.rinex.navigation.writers.ephemeris;

import org.orekit.files.rinex.navigation.RecordType;
import org.orekit.files.rinex.navigation.RinexNavigationHeader;
import org.orekit.files.rinex.navigation.RinexNavigationParser;
import org.orekit.files.rinex.navigation.RinexNavigationWriter;
import org.orekit.files.rinex.navigation.writers.NavigationMessageWriter;
import org.orekit.propagation.analytical.gnss.data.AbstractNavigationMessage;
import org.orekit.utils.units.Unit;

import java.io.IOException;

/** Base writer for abstract navigation messages.
 * @param <T> type of the navigation messages this writer handles
 * @author Luc Maisonobe
 * @since 14.0
 */
public abstract class AbstractNavigationMessageWriter<T extends AbstractNavigationMessage<T>>
    extends NavigationMessageWriter<T> {

    /** {@inheritDoc} */
    @Override
    public void writeMessage(final String identifier, final T message,
                             final RinexNavigationHeader header, final RinexNavigationWriter writer)
        throws IOException {

        // TYPE / SV / MSG
        writeTypeSvMsg(RecordType.EPH, identifier, message, header, writer);

        // EPH MESSAGE LINE - 0
        writeEphLine0(message, writer);

        // EPH MESSAGE LINE - 1
        writeEphLine1(message, writer);

        // EPH MESSAGE LINE - 2
        writeEphLine2(message, writer);

        // EPH MESSAGE LINE - 3
        writeEphLine3(message, writer);

        // EPH MESSAGE LINE - 4
        writeEphLine4(message, writer);

        // EPH MESSAGE LINE - 5
        writeEphLine5(message, writer);

        // EPH MESSAGE LINE - 6
        writeEphLine6(message, writer);

        // EPH MESSAGE LINE - 7
        writeEphLine7(message, writer);

    }

    /** Write the EPH MESSAGE LINE - 0.
     * @param message navigation message to write
     * @param writer global file writer
     * @throws IOException if an I/O error occurs.
     */
    protected void writeEphLine0(final AbstractNavigationMessage<?> message, final RinexNavigationWriter writer)
        throws IOException {
        writer.startLine();
        writer.writeDate(message.getEpochToc(), message.getSystem());
        writer.writeDouble(message.getAf0(), Unit.SECOND);
        writer.writeDouble(message.getAf1(), RinexNavigationParser.S_PER_S);
        writer.writeDouble(message.getAf2(), RinexNavigationParser.S_PER_S2);
        writer.finishLine();
    }

    /** Write the EPH MESSAGE LINE - 1.
     * @param message navigation message to write
     * @param writer global file writer
     * @throws IOException if an I/O error occurs.
     */
    protected void writeEphLine1(final T message, final RinexNavigationWriter writer)
        throws IOException {
        writer.startLine();
        writeField1Line1(message, writer);
        writer.writeDouble(message.getCrs(), Unit.METRE);
        writer.writeDouble(message.getDeltaN0(), RinexNavigationParser.RAD_PER_S);
        writer.writeDouble(message.getM0(), Unit.RADIAN);
        writer.finishLine();
    }

    /** Write field 1 in line 1.
     * @param message navigation message to write
     * @param writer global file writer
     * @throws IOException if an I/O error occurs.
     */
    protected abstract void writeField1Line1(T message, RinexNavigationWriter writer)
        throws IOException;

    /** Write the EPH MESSAGE LINE - 2.
     * @param message navigation message to write
     * @param writer global file writer
     * @throws IOException if an I/O error occurs.
     */
    protected void writeEphLine2(T message, final RinexNavigationWriter writer)
        throws IOException {
        writer.startLine();
        writer.writeDouble(message.getCuc(), Unit.RADIAN);
        writer.writeDouble(message.getE(), Unit.NONE);
        writer.writeDouble(message.getCus(), Unit.RADIAN);
        writer.writeDouble(message.getSqrtA(), RinexNavigationParser.SQRT_M);
        writer.finishLine();
    }

    /** Write the EPH MESSAGE LINE - 3.
     * @param message navigation message to write
     * @param writer global file writer
     * @throws IOException if an I/O error occurs.
     */
    protected void writeEphLine3(T message, final RinexNavigationWriter writer)
        throws IOException {
        writer.startLine();
        writer.writeDouble(message.getTime(), Unit.SECOND);
        writer.writeDouble(message.getCic(), Unit.RADIAN);
        writer.writeDouble(message.getOmega0(), Unit.RADIAN);
        writer.writeDouble(message.getCis(), Unit.RADIAN);
        writer.finishLine();
    }

    /** Write the EPH MESSAGE LINE - 4.
     * @param message navigation message to write
     * @param writer global file writer
     * @throws IOException if an I/O error occurs.
     */
    protected void writeEphLine4(T message, RinexNavigationWriter writer)
        throws IOException {
        writer.startLine();
        writer.writeDouble(message.getI0(), Unit.RADIAN);
        writer.writeDouble(message.getCrc(), Unit.METRE);
        writer.writeDouble(message.getPa(), Unit.RADIAN);
        writer.writeDouble(message.getOmegaDot(), RinexNavigationParser.RAD_PER_S);
        writer.finishLine();
    }

    /** Write the EPH MESSAGE LINE - 5.
     * @param message navigation message to write
     * @param writer global file writer
     * @throws IOException if an I/O error occurs.
     */
    protected abstract void writeEphLine5(T message, RinexNavigationWriter writer)
        throws IOException;

    /** Write the EPH MESSAGE LINE - 6.
     * @param message navigation message to write
     * @param writer global file writer
     * @throws IOException if an I/O error occurs.
     */
    protected abstract void writeEphLine6(T message, RinexNavigationWriter writer)
        throws IOException;

    /** Write the EPH MESSAGE LINE - 7.
     * @param message navigation message to write
     * @param writer global file writer
     * @throws IOException if an I/O error occurs.
     */
    protected abstract void writeEphLine7(T message, RinexNavigationWriter writer)
        throws IOException;

}
