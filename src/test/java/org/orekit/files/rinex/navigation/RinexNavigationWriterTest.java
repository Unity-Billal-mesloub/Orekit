/* Copyright 2022-2025 Luc Maisonobe
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
package org.orekit.files.rinex.navigation;

import org.hipparchus.util.FastMath;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.orekit.Utils;
import org.orekit.annotation.DefaultDataContext;
import org.orekit.data.DataContext;
import org.orekit.data.DataSource;
import org.orekit.errors.OrekitException;
import org.orekit.errors.OrekitMessages;
import org.orekit.files.rinex.section.RinexComment;
import org.orekit.propagation.analytical.gnss.data.AbstractAlmanac;
import org.orekit.propagation.analytical.gnss.data.AbstractEphemerisMessage;
import org.orekit.propagation.analytical.gnss.data.AbstractNavigationMessage;
import org.orekit.propagation.analytical.gnss.data.BeidouCivilianNavigationMessage;
import org.orekit.propagation.analytical.gnss.data.BeidouLegacyNavigationMessage;
import org.orekit.propagation.analytical.gnss.data.CivilianNavigationMessage;
import org.orekit.propagation.analytical.gnss.data.CommonGnssData;
import org.orekit.propagation.analytical.gnss.data.GLONASSFdmaNavigationMessage;
import org.orekit.propagation.analytical.gnss.data.GNSSOrbitalElements;
import org.orekit.propagation.analytical.gnss.data.GNSSOrbitalElementsDriversProvider;
import org.orekit.propagation.analytical.gnss.data.GPSCivilianNavigationMessage;
import org.orekit.propagation.analytical.gnss.data.GPSLegacyNavigationMessage;
import org.orekit.propagation.analytical.gnss.data.GalileoNavigationMessage;
import org.orekit.propagation.analytical.gnss.data.LegacyNavigationMessage;
import org.orekit.propagation.analytical.gnss.data.NavICL1NvNavigationMessage;
import org.orekit.propagation.analytical.gnss.data.NavICLegacyNavigationMessage;
import org.orekit.propagation.analytical.gnss.data.NavigationMessage;
import org.orekit.propagation.analytical.gnss.data.QZSSCivilianNavigationMessage;
import org.orekit.propagation.analytical.gnss.data.QZSSLegacyNavigationMessage;
import org.orekit.propagation.analytical.gnss.data.SBASNavigationMessage;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.ParameterDriver;

import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class RinexNavigationWriterTest {

    @BeforeEach
    public void setUp() {
        Utils.setDataRoot("regular-data");
    }

    @DefaultDataContext
    @Test
    public void testWriteHeaderTwice() throws IOException {
        final RinexNavigation rnav = load("gnss/navigation/Example_table_A41_Rinex402.n");
        final CharArrayWriter  caw  = new CharArrayWriter();
        try (RinexNavigationWriter writer = new RinexNavigationWriter(caw, "dummy")) {
            writer.prepareComments(rnav.getComments());
            writer.writeHeader(rnav.getHeader());
            writer.writeHeader(rnav.getHeader());
            Assertions.fail("an exception should have been thrown");
        } catch (OrekitException oe) {
            Assertions.assertEquals(OrekitMessages.HEADER_ALREADY_WRITTEN, oe.getSpecifier());
            Assertions.assertEquals("dummy", oe.getParts()[0]);
        }
    }

    @DefaultDataContext
    @Test
    public void testOutputClosed() throws IOException {
        final RinexNavigation rnav = load("gnss/navigation/Example_table_A41_Rinex402.n");
        final CharArrayWriter  caw  = new CharArrayWriter();
        try (RinexNavigationWriter writer = new RinexNavigationWriter(caw, "dummy")) {
            writer.prepareComments(rnav.getComments());
            writer.close();
            writer.writeHeader(rnav.getHeader());
            Assertions.fail("an exception should have been thrown");
        } catch (OrekitException oe) {
            Assertions.assertEquals(OrekitMessages.OUTPUT_ALREADY_CLOSED, oe.getSpecifier());
            Assertions.assertEquals("dummy", oe.getParts()[0]);
        }
    }

    @DefaultDataContext
    @Test
    public void testRoundTripBRDC013022G() throws IOException {
        doTestRoundTrip("gnss/navigation/brdc0130.22g");
    }

    @DefaultDataContext
    @Test
    public void testRoundTripBRDC013022N() throws IOException {
        doTestRoundTrip("gnss/navigation/brdc0130.22n");
    }

    @DefaultDataContext
    @Test
    public void testRoundTripExampleBeidouRinex302() throws IOException {
        doTestRoundTrip("gnss/navigation/Example_Beidou_Rinex302.n");
    }

    @DefaultDataContext
    @Test
    public void testRoundTripExampleBeidouRinex304() throws IOException {
        doTestRoundTrip("gnss/navigation/Example_Beidou_Rinex304.n");
    }

    @DefaultDataContext
    @Test
    public void testRoundTripBeidouRinex400() throws IOException {
        doTestRoundTrip("gnss/navigation/Example_Beidou_Rinex400.n");
    }

    @DefaultDataContext
    @Test
    public void testRoundTripTableEopRinex400() throws IOException {
        doTestRoundTrip("gnss/navigation/Example_Eop_Rinex400.n");
    }

    @DefaultDataContext
    @Test
    public void testRoundTripTableGalileoRinex302() throws IOException {
        doTestRoundTrip("gnss/navigation/Example_Galileo_Rinex302.n");
    }

    @DefaultDataContext
    @Test
    public void testRoundTripGalileoRinex304() throws IOException {
        doTestRoundTrip("gnss/navigation/Example_Galileo_Rinex304.n");
    }

    @DefaultDataContext
    @Test
    public void testRoundTripGalileoRinex400() throws IOException {
        doTestRoundTrip("gnss/navigation/Example_Galileo_Rinex400.n");
    }

    @DefaultDataContext
    @Test
    public void testRoundTripGlonassRinex303() throws IOException {
        doTestRoundTrip("gnss/navigation/Example_Glonass_Rinex303.n");
    }

    @DefaultDataContext
    @Test
    public void testRoundTripGlonassRinex400() throws IOException {
        doTestRoundTrip("gnss/navigation/Example_Glonass_Rinex400.n");
    }

    @DefaultDataContext
    @Test
    public void testRoundTripGPSRinex301() throws IOException {
        doTestRoundTrip("gnss/navigation/Example_GPS_Rinex301.n");
    }

    @DefaultDataContext
    @Test
    public void testRoundTripGPSRinex304() throws IOException {
        doTestRoundTrip("gnss/navigation/Example_GPS_Rinex304.n");
    }

    @DefaultDataContext
    @Test
    public void testRoundTripGPSRinex400() throws IOException {
        doTestRoundTrip("gnss/navigation/Example_GPS_Rinex400.n");
    }

    @DefaultDataContext
    @Test
    public void testRoundTripIonRinex400() throws IOException {
        doTestRoundTrip("gnss/navigation/Example_Ion_Rinex400.n");
    }

    @DefaultDataContext
    @Test
    public void testRoundTripMixedRinex304() throws IOException {
        doTestRoundTrip("gnss/navigation/Example_Mixed_Rinex304.n");
    }

    @DefaultDataContext
    @Test
    public void testRoundTripMixedRinex305() throws IOException {
        doTestRoundTrip("gnss/navigation/Example_Mixed_Rinex305.n");
    }

    @DefaultDataContext
    @Test
    public void testRoundTripNavICRinex303() throws IOException {
        doTestRoundTrip("gnss/navigation/Example_NavIC_Rinex303.n");
    }

    @DefaultDataContext
    @Test
    public void testRoundTripNavICRinex304() throws IOException {
        doTestRoundTrip("gnss/navigation/Example_NavIC_Rinex304.n");
    }

    @DefaultDataContext
    @Test
    public void testRoundTripNavICRinex400() throws IOException {
        doTestRoundTrip("gnss/navigation/Example_NavIC_Rinex400.n");
    }

    @DefaultDataContext
    @Test
    public void testRoundTripNavICRinex402() throws IOException {
        doTestRoundTrip("gnss/navigation/Example_NavIC_Rinex402.n");
    }

    @DefaultDataContext
    @Test
    public void testRoundTripQZSSRinex302() throws IOException {
        doTestRoundTrip("gnss/navigation/Example_QZSS_Rinex302.n");
    }

    @DefaultDataContext
    @Test
    public void testRoundTripQZSSRinex304() throws IOException {
        doTestRoundTrip("gnss/navigation/Example_QZSS_Rinex304.n");
    }

    @DefaultDataContext
    @Test
    public void testRoundTripQZSSRinex400() throws IOException {
        doTestRoundTrip("gnss/navigation/Example_QZSS_Rinex400.n");
    }

    @DefaultDataContext
    @Test
    public void testRoundTripSBASRinex301() throws IOException {
        doTestRoundTrip("gnss/navigation/Example_SBAS_Rinex301.n");
    }

    @DefaultDataContext
    @Test
    public void testRoundTripSBASRinex304() throws IOException {
        doTestRoundTrip("gnss/navigation/Example_SBAS_Rinex304.n");
    }

    @DefaultDataContext
    @Test
    public void testRoundTripSBASRinex400() throws IOException {
        doTestRoundTrip("gnss/navigation/Example_SBAS_Rinex400.n");
    }

    @DefaultDataContext
    @Test
    public void testRoundTripStoRinex400() throws IOException {
        doTestRoundTrip("gnss/navigation/Example_Sto_Rinex400.n");
    }

    @DefaultDataContext
    @Test
    public void testRoundTripTableA41Rinex402() throws IOException {
        doTestRoundTrip("gnss/navigation/Example_table_A41_Rinex402.n");
    }

    private RinexNavigation load(final String name) throws IOException {
        final DataSource dataSource =
            new DataSource(name, () -> Utils.class.getClassLoader().getResourceAsStream(name));
        return new RinexNavigationParser(DataContext.getDefault().getTimeScales()).parse(dataSource);
    }

    @DefaultDataContext
    private void doTestRoundTrip(final String resourceName) throws IOException {

        final RinexNavigation rnav = load(resourceName);
        final CharArrayWriter  caw  = new CharArrayWriter();
        try (RinexNavigationWriter writer =
                 new RinexNavigationWriter(caw, "dummy",
                                           (system, timeScales) -> system.getObservationTimeScale() == null ?
                                                                   null :
                                                                   system.getObservationTimeScale().getTimeScale(timeScales),
                                           DataContext.getDefault().getTimeScales())) {
            RinexNavigation loaded = load(resourceName);
            writer.writeCompleteFile(loaded);
        }

        // reparse the written file
        final byte[]          bytes   = caw.toString().getBytes(StandardCharsets.UTF_8);
        final DataSource      source  = new DataSource("", () -> new ByteArrayInputStream(bytes));
        final RinexNavigation rebuilt = new RinexNavigationParser(DataContext.getDefault().getTimeScales()).
                                        parse(source);

        // check that the original and the reparsed files have the same content
        checkRinexFile(rnav, rebuilt);

    }

    private void checkRinexFile(final RinexNavigation first, final RinexNavigation second) {

        // header
        checkRinexHeader(first.getHeader(), second.getHeader());

        // comments
        Assertions.assertEquals(first.getComments().size(), second.getComments().size());
        for (int i = 0; i < second.getComments().size(); ++i) {
            checkRinexComments(first.getComments().get(i), second.getComments().get(i));
        }

        // navigation messages
        checkMessages(first.getGPSLegacyNavigationMessages(), second.getGPSLegacyNavigationMessages(),
                      this::checkGPSLegacy);
        checkMessages(first.getGPSCivilianNavigationMessages(), second.getGPSCivilianNavigationMessages(),
                      this::checkGPSCivilian);
        checkMessages(first.getGalileoNavigationMessages(), second.getGalileoNavigationMessages(),
                      this::checkGalileo);
        checkMessages(first.getBeidouLegacyNavigationMessages(), second.getBeidouLegacyNavigationMessages(),
                      this::checkBeidouLegacy);
        checkMessages(first.getBeidouCivilianNavigationMessages(), second.getBeidouCivilianNavigationMessages(),
                      this::checkBeidouCivilian);
        checkMessages(first.getQZSSLegacyNavigationMessages(), second.getQZSSLegacyNavigationMessages(),
                      this::checkQZSSLegacy);
        checkMessages(first.getQZSSCivilianNavigationMessages(), second.getQZSSCivilianNavigationMessages(),
                      this::checkQZSSCivilian);
        checkMessages(first.getNavICLegacyNavigationMessages(), second.getNavICLegacyNavigationMessages(),
                      this::checkNavICLegacy);
        checkMessages(first.getNavICL1NVNavigationMessages(), second.getNavICL1NVNavigationMessages(),
                      this::checkNavICL1Nv);
        checkMessages(first.getGlonassNavigationMessages(), second.getGlonassNavigationMessages(),
                      this::checkGLONASSFdma);
        checkMessages(first.getSBASNavigationMessages(), second.getSBASNavigationMessages(),
                      this::checkSBAS);

        // System Time Offset messages
        checkMessages(first.getSystemTimeOffsets(), second.getSystemTimeOffsets(),
                      this::checkTimeOffset);

        // Earth Orientation Parameters messages
        checkMessages(first.getEarthOrientationParameters(), second.getEarthOrientationParameters(),
                      this::checkEOP);

        // ionosphere messages
        checkMessages(first.getKlobucharMessages(), second.getKlobucharMessages(),
                      this::checkKlobuchar);
        checkMessages(first.getNequickGMessages(), second.getNequickGMessages(),
                      this::checkNequickG);
        checkMessages(first.getBDGIMMessages(), second.getBDGIMMessages(),
                      this::checkBDGIM);
        checkMessages(first.getNavICKlobucharMessages(), second.getNavICKlobucharMessages(),
                      this::checkNavICKlobuchar);
        checkMessages(first.getNavICNeQuickNMessages(), second.getNavICNeQuickNMessages(),
                      this::checkNavICNeQuickN);
        checkMessages(first.getGlonassCDMSMessages(), second.getGlonassCDMSMessages(),
                      this::checkGlonassCDMS);

    }

    private void checkRinexHeader(final RinexNavigationHeader first, final RinexNavigationHeader second) {

        // base header
        Assertions.assertEquals(first.getFormatVersion(),          second.getFormatVersion(), 0.001);
        Assertions.assertEquals(first.getFileType(),               second.getFileType());
        Assertions.assertEquals(first.getSatelliteSystem(),        second.getSatelliteSystem());
        Assertions.assertEquals(first.getProgramName(),            second.getProgramName());
        Assertions.assertEquals(first.getRunByName(),              second.getRunByName());
        Assertions.assertEquals(first.getCreationDateComponents(), second.getCreationDateComponents());
        Assertions.assertEquals(first.getCreationTimeZone(),       second.getCreationTimeZone());
        checkDate(first.getCreationDate(), second.getCreationDate());
        Assertions.assertEquals(first.getReceiverNumber(),         second.getReceiverNumber());
        Assertions.assertEquals(first.getReceiverType(),           second.getReceiverType());
        Assertions.assertEquals(first.getReceiverVersion(),        second.getReceiverVersion());
        Assertions.assertEquals(first.getLeapSecondsGNSS(),        second.getLeapSecondsGNSS());
        Assertions.assertEquals(first.getLeapSecondsFuture(),      second.getLeapSecondsFuture());
        Assertions.assertEquals(first.getLeapSecondsWeekNum(),     second.getLeapSecondsWeekNum());
        Assertions.assertEquals(first.getLeapSecondsDayNum(),      second.getLeapSecondsDayNum());
        Assertions.assertEquals(first.getDoi(),                    second.getDoi());
        Assertions.assertEquals(first.getLicense(),                second.getLicense());
        Assertions.assertEquals(first.getStationInformation(),     second.getStationInformation());
        Assertions.assertEquals(first.getReceiverNumber(),         second.getReceiverNumber());

        // ionospheric models (for 3.X)
        Assertions.assertEquals(first.getIonosphericCorrections().size(), second.getIonosphericCorrections().size());
        for (int i = 0; i < first.getIonosphericCorrections().size(); ++i) {
            final IonosphericCorrection fIono = first.getIonosphericCorrections().get(i);
            final IonosphericCorrection sIono = second.getIonosphericCorrections().get(i);
            Assertions.assertEquals(fIono.getType(), sIono.getType());
            Assertions.assertEquals(fIono.getTimeMark(), sIono.getTimeMark());
            if (fIono.getType() == IonosphericCorrectionType.GAL) {
                final NeQuickGIonosphericCorrection fNequick = (NeQuickGIonosphericCorrection) fIono;
                final NeQuickGIonosphericCorrection sNequick = (NeQuickGIonosphericCorrection) fIono;
                checkArray(fNequick.getNeQuickAlpha(), sNequick.getNeQuickAlpha());
            } else {
                final KlobucharIonosphericCorrection fKlobuchar = (KlobucharIonosphericCorrection) fIono;
                final KlobucharIonosphericCorrection sKlobuchar = (KlobucharIonosphericCorrection) fIono;
                checkArray(fKlobuchar.getKlobucharAlpha(), sKlobuchar.getKlobucharAlpha());
                checkArray(fKlobuchar.getKlobucharBeta(), sKlobuchar.getKlobucharBeta());
            }
        }

        // time corrections (for 3.X)
        Assertions.assertEquals(first.getTimeSystemCorrections().size(), second.getTimeSystemCorrections().size());
        for (int i = 0; i < first.getTimeSystemCorrections().size(); ++i) {
            checkTimeSystemCorrection(first.getTimeSystemCorrections().get(i), second.getTimeSystemCorrections().get(i));
        }

    }

    private void checkRinexComments(final RinexComment first, final RinexComment second) {
        Assertions.assertEquals(first.getLineNumber(), second.getLineNumber());
        Assertions.assertEquals(first.getText(),       second.getText());
    }

    private void checkTimeSystemCorrection(final TimeSystemCorrection first, final TimeSystemCorrection second) {
        Assertions.assertEquals(first.getTimeSystemCorrectionType(), second.getTimeSystemCorrectionType());
        Assertions.assertEquals(first.getReferenceDate(),            second.getReferenceDate());
        Assertions.assertEquals(first.getTimeSystemCorrectionA0(),
                                second.getTimeSystemCorrectionA0(),
                                FastMath.ulp(first.getTimeSystemCorrectionA0()));
        Assertions.assertEquals(first.getTimeSystemCorrectionA1(),
                                second.getTimeSystemCorrectionA1(),
                                FastMath.ulp(first.getTimeSystemCorrectionA1()));
    }

    private void checkDate(final AbsoluteDate first, final AbsoluteDate second) {
        if (first == null) {
            Assertions.assertNull(second);
        } else if (Double.isInfinite(first.durationFrom(AbsoluteDate.ARBITRARY_EPOCH))) {
            Assertions.assertEquals(first, second);
        } else {
            Assertions.assertEquals(0.0, second.durationFrom(first), 1.0e-6);
        }
    }

    private void checkArray(final double[] first, final double[] second) {
        if (first == null) {
            Assertions.assertNull(second);
        } else {
            Assertions.assertEquals(first.length, second.length);
            for (int i = 0; i < first.length; ++i) {
                Assertions.assertEquals(first[i], second[i], FastMath.ulp(first[i]));
            }
        }
    }

    private <T extends NavigationMessage> void checkMessages(final Map<String, List<T>> first,
                                                             final Map<String, List<T>> second,
                                                             final BiConsumer<T, T> checker) {
        Assertions.assertEquals(first.size(), second.size());
        for (final String key : first.keySet()) {
            Assertions.assertEquals(first.get(key).size(), second.get(key).size());
            for (int i = 0; i < first.get(key).size(); ++i) {
                checker.accept(first.get(key).get(i), second.get(key).get(i));
            }
        }
    }

    private void checkGPSLegacy(final GPSLegacyNavigationMessage first, final GPSLegacyNavigationMessage second) {

        // check data inherited from base class
        checkLegacy(first, second);

        // there are no specific data to check at GPSLegacyNavigationMessage level

    }

    private void checkGPSCivilian(final GPSCivilianNavigationMessage first, final GPSCivilianNavigationMessage second) {

        // check data inherited from base class
        checkCivilian(first, second);

        // there are no specific data to check at GPSCivilianNavigationMessage level

    }

    private void checkGalileo(final GalileoNavigationMessage first, final GalileoNavigationMessage second) {

        // check data inherited from base class
        checkAbstractNavigation(first, second);

        // check data specific to this message
        Assertions.assertEquals(first.getIODNav(), second.getIODNav());
        Assertions.assertEquals(first.getDataSource(), second.getDataSource());
        Assertions.assertEquals(first.getBGDE1E5a(), second.getBGDE1E5a(), FastMath.ulp(first.getBGDE1E5a()));
        Assertions.assertEquals(first.getBGDE5bE1(), second.getBGDE5bE1(), FastMath.ulp(first.getBGDE5bE1()));
        Assertions.assertEquals(first.getSisa(), second.getSisa(), FastMath.ulp(first.getSisa()));
        Assertions.assertEquals(first.getSvHealth(), second.getSvHealth(), FastMath.ulp(first.getSvHealth()));

    }

    private void checkBeidouLegacy(final BeidouLegacyNavigationMessage first, final BeidouLegacyNavigationMessage second) {

        // check data inherited from base class
        checkAbstractNavigation(first, second);

        // check data specific to this message
        Assertions.assertEquals(first.getAODC(), second.getAODC());
        Assertions.assertEquals(first.getAODE(), second.getAODE());
        Assertions.assertEquals(first.getTGD1(), second.getTGD1(), FastMath.ulp(first.getTGD1()));
        Assertions.assertEquals(first.getTGD2(), second.getTGD2(), FastMath.ulp(first.getTGD2()));
        Assertions.assertEquals(first.getSvAccuracy(), second.getSvAccuracy(), FastMath.ulp(first.getSvAccuracy()));
        Assertions.assertEquals(first.getSatH1(), second.getSatH1());

    }

    private void checkBeidouCivilian(final BeidouCivilianNavigationMessage first, final BeidouCivilianNavigationMessage second) {

        // check data inherited from base class
        checkAbstractNavigation(first, second);

        // check data specific to this message
        Assertions.assertEquals(first.getRadioWave().getFrequency(), second.getRadioWave().getFrequency(),
                                FastMath.ulp(first.getRadioWave().getFrequency()));
        Assertions.assertEquals(first.getADot(), second.getADot(), FastMath.ulp(first.getADot()));
        Assertions.assertEquals(first.getDeltaN0Dot(), second.getDeltaN0Dot(), FastMath.ulp(first.getDeltaN0Dot()));
        Assertions.assertEquals(first.getIODE(), second.getIODE());
        Assertions.assertEquals(first.getIODC(), second.getIODC());
        Assertions.assertEquals(first.getIscB1CD(), second.getIscB1CD(), FastMath.ulp(first.getIscB1CD()));
        Assertions.assertEquals(first.getIscB2AD(), second.getIscB2AD(), FastMath.ulp(first.getIscB2AD()));
        Assertions.assertEquals(first.getIscB1CP(), second.getIscB1CP(), FastMath.ulp(first.getIscB1CP()));
        Assertions.assertEquals(first.getSisaiOe(), second.getSisaiOe());
        Assertions.assertEquals(first.getSisaiOcb(), second.getSisaiOcb());
        Assertions.assertEquals(first.getSisaiOc1(), second.getSisaiOc1());
        Assertions.assertEquals(first.getSisaiOc2(), second.getSisaiOc2());
        Assertions.assertEquals(first.getSismai(), second.getSismai());
        Assertions.assertEquals(first.getHealth(), second.getHealth());
        Assertions.assertEquals(first.getIntegrityFlags(), second.getIntegrityFlags());
        Assertions.assertEquals(first.getTgdB1Cp(), second.getTgdB1Cp(), FastMath.ulp(first.getTgdB1Cp()));
        Assertions.assertEquals(first.getTgdB1Cp(), second.getTgdB1Cp(), FastMath.ulp(first.getTgdB1Cp()));
        Assertions.assertEquals(first.getTgdB2bI(), second.getTgdB2bI(), FastMath.ulp(first.getTgdB2bI()));
        Assertions.assertEquals(first.getSatelliteType(), second.getSatelliteType());

    }

    private void checkQZSSLegacy(final QZSSLegacyNavigationMessage first, final QZSSLegacyNavigationMessage second) {

        // check data inherited from base class
        checkLegacy(first, second);

        // there are no specific data to check at QZSSLegacyNavigationMessage level

    }

    private void checkQZSSCivilian(final QZSSCivilianNavigationMessage first, final QZSSCivilianNavigationMessage second) {

        // check data inherited from base class
        checkCivilian(first, second);

        // there are no specific data to check at QZSSCivilianNavigationMessage level

    }

    private void checkNavICLegacy(final NavICLegacyNavigationMessage first, final NavICLegacyNavigationMessage second) {

        // check data inherited from base class
        checkLegacy(first, second);

        // there are no specific data to check at NavICLegacyNavigationMessage level

    }

    private void checkNavICL1Nv(final NavICL1NvNavigationMessage first, final NavICL1NvNavigationMessage second) {

        // check data inherited from base class
        checkCivilian(first, second);

        // check data specific to this message
        Assertions.assertEquals(first.getReferenceSignalFlag(), second.getReferenceSignalFlag());
        Assertions.assertEquals(first.getTGDSL5(), second.getTGDSL5(), FastMath.ulp(first.getTGDSL5()));
        Assertions.assertEquals(first.getIscSL1P(), second.getIscSL1P(), FastMath.ulp(first.getIscSL1P()));
        Assertions.assertEquals(first.getIscL1DL1P(), second.getIscL1DL1P(), FastMath.ulp(first.getIscL1DL1P()));
        Assertions.assertEquals(first.getIscL1PS(), second.getIscL1PS(), FastMath.ulp(first.getIscL1PS()));
        Assertions.assertEquals(first.getIscL1DS(), second.getIscL1DS(), FastMath.ulp(first.getIscL1DS()));

    }

    private void checkGLONASSFdma(final GLONASSFdmaNavigationMessage first, final GLONASSFdmaNavigationMessage second) {

        // check data inherited from base class
        checkAbstractEphemeris(first, second);

        // check interface data
        checkNavigationMessage(first, second);

        // check data specific to this message
        Assertions.assertEquals(first.getTN(), second.getTN(), FastMath.ulp(first.getTN()));
        Assertions.assertEquals(first.getGammaN(), second.getGammaN(), FastMath.ulp(first.getGammaN()));
        Assertions.assertEquals(first.getFrequencyNumber(), second.getFrequencyNumber());
        Assertions.assertEquals(first.getTime(), second.getTime(), FastMath.ulp(first.getTime()));
        Assertions.assertEquals(first.getStatusFlags(), second.getStatusFlags());
        Assertions.assertEquals(first.getHealthFlags(), second.getHealthFlags());
        Assertions.assertEquals(first.getGroupDelayDifference(), second.getGroupDelayDifference(), FastMath.ulp(first.getGroupDelayDifference()));
        Assertions.assertEquals(first.getURA(), second.getURA(), FastMath.ulp(first.getURA()));

    }

    private void checkSBAS(final SBASNavigationMessage first, final SBASNavigationMessage second) {

        // check data inherited from base class
        checkAbstractEphemeris(first, second);

        // check interface data
        checkNavigationMessage(first, second);

        // check data specific to this message
        Assertions.assertEquals(first.getWeek(), second.getWeek());
        Assertions.assertEquals(first.getTime(), second.getTime(), FastMath.ulp(first.getTime()));
        Assertions.assertEquals(first.getIODN(), second.getIODN());
        Assertions.assertEquals(first.getAGf0(), second.getAGf0(), FastMath.ulp(first.getAGf0()));
        Assertions.assertEquals(first.getAGf1(), second.getAGf1(), FastMath.ulp(first.getAGf1()));
        Assertions.assertEquals(first.getURA(), second.getURA(), FastMath.ulp(first.getURA()));

    }

    private <T extends LegacyNavigationMessage<T>> void checkLegacy(final LegacyNavigationMessage<T> first,
                                                                    final LegacyNavigationMessage<T> second) {

        // check data inherited from base class
        checkAbstractNavigation(first, second);

        // check data specific to this message
        Assertions.assertEquals(first.getIODE(), second.getIODE());
        Assertions.assertEquals(first.getIODC(), second.getIODC());
        Assertions.assertEquals(first.getSvAccuracy(), second.getSvAccuracy(), FastMath.ulp(first.getSvAccuracy()));
        Assertions.assertEquals(first.getSvHealth(), second.getSvHealth());
        Assertions.assertEquals(first.getFitInterval(), second.getFitInterval());
        Assertions.assertEquals(first.getL2Codes(), second.getL2Codes());
        Assertions.assertEquals(first.getL2PFlags(), second.getL2PFlags());

    }

    private <T extends CivilianNavigationMessage<T>> void checkCivilian(final CivilianNavigationMessage<T> first,
                                                                        final CivilianNavigationMessage<T> second) {

        // check data inherited from base class
        checkAbstractNavigation(first, second);

        // check data specific to this message
        Assertions.assertEquals(first.isCnv2(), second.isCnv2());
        Assertions.assertEquals(first.getADot(), second.getADot(), FastMath.ulp(first.getADot()));
        Assertions.assertEquals(first.getDeltaN0Dot(), second.getDeltaN0Dot(), FastMath.ulp(first.getDeltaN0Dot()));
        Assertions.assertEquals(first.getSvAccuracy(), second.getSvAccuracy(), FastMath.ulp(first.getSvAccuracy()));
        Assertions.assertEquals(first.getSvHealth(), second.getSvHealth());
        Assertions.assertEquals(first.getIscL1CA(), second.getIscL1CA(), FastMath.ulp(first.getIscL1CA()));
        Assertions.assertEquals(first.getIscL1CD(), second.getIscL1CD(), FastMath.ulp(first.getIscL1CD()));
        Assertions.assertEquals(first.getIscL1CP(), second.getIscL1CP(), FastMath.ulp(first.getIscL1CP()));
        Assertions.assertEquals(first.getIscL2C(), second.getIscL2C(), FastMath.ulp(first.getIscL2C()));
        Assertions.assertEquals(first.getIscL5I5(), second.getIscL5I5(), FastMath.ulp(first.getIscL5I5()));
        Assertions.assertEquals(first.getIscL5Q5(), second.getIscL5Q5(), FastMath.ulp(first.getIscL5Q5()));
        Assertions.assertEquals(first.getUraiEd(), second.getUraiEd());
        Assertions.assertEquals(first.getUraiNed0(), second.getUraiNed0());
        Assertions.assertEquals(first.getUraiNed1(), second.getUraiNed1());
        Assertions.assertEquals(first.getUraiNed2(), second.getUraiNed2());
        Assertions.assertEquals(first.getFlags(), second.getFlags());


    }

    private <T extends AbstractNavigationMessage<T>> void checkAbstractNavigation(final AbstractNavigationMessage<T> first,
                                                                                  final AbstractNavigationMessage<T> second) {

        // check data inherited from base class
        checkAbstractAlmanac(first, second);

        // check interface data
        checkNavigationMessage(first, second);

        // check data specific to this message
        Assertions.assertEquals(first.getSqrtA(), second.getSqrtA(), FastMath.ulp(first.getSqrtA()));
        Assertions.assertEquals(first.getDeltaN0(), second.getDeltaN0(), FastMath.ulp(first.getDeltaN0()));
        checkDate(first.getEpochToc(), second.getEpochToc());
        Assertions.assertEquals(first.getTransmissionTime(), second.getTransmissionTime(), FastMath.ulp(first.getTransmissionTime()));

    }

    private void checkNavigationMessage(final NavigationMessage first,
                                        final NavigationMessage second) {
        Assertions.assertEquals(first.getNavigationMessageType(),    second.getNavigationMessageType());
        Assertions.assertEquals(first.getNavigationMessageSubType(), second.getNavigationMessageSubType());
    }

    private <T extends AbstractAlmanac<T>> void checkAbstractAlmanac(final AbstractAlmanac<T> first,
                                                                     final AbstractAlmanac<T> second) {

        // check data inherited from base class
        checkCommonGnssData(first, second);

        // there are no specific data to check at AbstractAlmanac level

    }

    private <T extends CommonGnssData<T>> void checkCommonGnssData(final CommonGnssData<T> first,
                                                                   final CommonGnssData<T> second) {

        // check data inherited from base class
        checkGNSSOrbitalElements(first, second);

        // check data specific to this message
        Assertions.assertEquals(first.getAf0(), second.getAf0(), FastMath.ulp(first.getAf0()));
        Assertions.assertEquals(first.getAf1(), second.getAf1(), FastMath.ulp(first.getAf1()));
        Assertions.assertEquals(first.getAf2(), second.getAf2(), FastMath.ulp(first.getAf2()));
        Assertions.assertEquals(first.getTGD(), second.getTGD(), FastMath.ulp(first.getTGD()));
        Assertions.assertEquals(first.getToc(), second.getToc(), FastMath.ulp(first.getToc()));

    }

    private <T extends GNSSOrbitalElements<T>> void checkGNSSOrbitalElements(final GNSSOrbitalElements<T> first,
                                                                             final GNSSOrbitalElements<T> second) {

        // check data inherited from base class
        checkGNSSOrbitalElementsDriversProvider(first, second);

        // check data specific to this message
        checkDate(first.getDate(), second.getDate());
        Assertions.assertEquals(first.getMu(), second.getMu(), FastMath.ulp(first.getMu()));
        checkParameterDriver(first.getSmaDriver(), second.getSmaDriver());
        Assertions.assertEquals(first.getADot(), second.getADot(), FastMath.ulp(first.getADot()));
        Assertions.assertEquals(first.getMeanMotion0(), second.getMeanMotion0(), FastMath.ulp(first.getMeanMotion0()));
        Assertions.assertEquals(first.getDeltaN0(), second.getDeltaN0(), FastMath.ulp(first.getDeltaN0()));
        Assertions.assertEquals(first.getDeltaN0Dot(), second.getDeltaN0Dot(), FastMath.ulp(first.getDeltaN0Dot()));
        checkParameterDriver(first.getEDriver(), second.getEDriver());
        checkParameterDriver(first.getI0Driver(), second.getI0Driver());
        checkParameterDriver(first.getOmega0Driver(), second.getOmega0Driver());
        checkParameterDriver(first.getPaDriver(), second.getPaDriver());
        checkParameterDriver(first.getM0Driver(), second.getM0Driver());

    }

    private void checkGNSSOrbitalElementsDriversProvider(final GNSSOrbitalElementsDriversProvider first,
                                                         final GNSSOrbitalElementsDriversProvider second) {
        Assertions.assertEquals(first.getSystem(), second.getSystem());
        Assertions.assertSame(first.getTimeScales(), second.getTimeScales());
        Assertions.assertEquals(first.getParametersDrivers().size(), second.getParametersDrivers().size());
        for (int  i = 0; i < first.getParametersDrivers().size(); ++i) {
            checkParameterDriver(first.getParametersDrivers().get(i), second.getParametersDrivers().get(i));
        }
        Assertions.assertEquals(first.getAngularVelocity(), second.getAngularVelocity(),
                                FastMath.ulp(first.getAngularVelocity()));
        Assertions.assertEquals(first.getWeeksInCycle(), second.getWeeksInCycle());
        Assertions.assertEquals(first.getCycleDuration(), second.getCycleDuration(),
                                FastMath.ulp(first.getCycleDuration()));
        Assertions.assertEquals(first.getPRN(), second.getPRN());
        Assertions.assertEquals(first.getWeek(), second.getWeek());

    }

    private  void checkAbstractEphemeris(final AbstractEphemerisMessage first,
                                         final AbstractEphemerisMessage second) {
        checkDate(first.getDate(),    second.getDate());
        checkDate(first.getEpochToc(),    second.getEpochToc());
        Assertions.assertEquals(first.getPRN(), second.getPRN());
        Assertions.assertEquals(first.getX(), second.getX(), FastMath.ulp(first.getX()));
        Assertions.assertEquals(first.getXDot(), second.getXDot(), FastMath.ulp(first.getXDot()));
        Assertions.assertEquals(first.getXDotDot(), second.getXDotDot(), FastMath.ulp(first.getXDotDot()));
        Assertions.assertEquals(first.getY(), second.getY(), FastMath.ulp(first.getY()));
        Assertions.assertEquals(first.getYDot(), second.getYDot(), FastMath.ulp(first.getYDot()));
        Assertions.assertEquals(first.getYDotDot(), second.getYDotDot(), FastMath.ulp(first.getYDotDot()));
        Assertions.assertEquals(first.getZ(), second.getZ(), FastMath.ulp(first.getZ()));
        Assertions.assertEquals(first.getZDot(), second.getZDot(), FastMath.ulp(first.getZDot()));
        Assertions.assertEquals(first.getZDotDot(), second.getZDotDot(), FastMath.ulp(first.getZDotDot()));
        Assertions.assertEquals(first.getHealth(), second.getHealth(), FastMath.ulp(first.getHealth()));
    }

    private void checkParameterDriver(final ParameterDriver first, final ParameterDriver second) {
        Assertions.assertEquals(first.getName(), second.getName());
        checkArray(first.getValues(), second.getValues());
    }

    private <T extends TypeSvMessage> void checkMessages(final List<T> first, final List<T> second,
                                                         final BiConsumer<T, T> checker) {
        Assertions.assertEquals(first.size(), second.size());
        for (int i = 0; i < first.size(); ++i) {
            checker.accept(first.get(i), second.get(i));
        }
    }

    private void checkTimeOffset(final SystemTimeOffsetMessage first, final SystemTimeOffsetMessage second) {
        checkDate(first.getDate(), second.getDate());
        checkDate(first.getReferenceEpoch(), second.getReferenceEpoch());
        Assertions.assertEquals(first.getDefinedTimeSystem().getKey(), second.getDefinedTimeSystem().getKey());
        Assertions.assertEquals(first.getReferenceTimeSystem().getKey(), second.getReferenceTimeSystem().getKey());
        Assertions.assertEquals(first.getSbasId(), second.getSbasId());
        Assertions.assertEquals(first.getUtcId(), second.getUtcId());
        Assertions.assertEquals(first.getA0(), second.getA0(), FastMath.ulp(first.getA0()));
        Assertions.assertEquals(first.getA1(), second.getA1(), FastMath.ulp(first.getA1()));
        Assertions.assertEquals(first.getA2(), second.getA2(), FastMath.ulp(first.getA2()));
        Assertions.assertEquals(first.getTransmissionTime(), second.getTransmissionTime(), FastMath.ulp(first.getTransmissionTime()));
    }

    private void checkEOP(final EarthOrientationParameterMessage first, final EarthOrientationParameterMessage second) {
        checkDate(first.getDate(), second.getDate());
        checkDate(first.getReferenceEpoch(), second.getReferenceEpoch());
        Assertions.assertEquals(first.getXp(), second.getXp(), FastMath.ulp(first.getXp()));
        Assertions.assertEquals(first.getXpDot(), second.getXpDot(), FastMath.ulp(first.getXpDot()));
        Assertions.assertEquals(first.getXpDotDot(), second.getXpDotDot(), FastMath.ulp(first.getXpDotDot()));
        Assertions.assertEquals(first.getYp(), second.getYp(), FastMath.ulp(first.getYp()));
        Assertions.assertEquals(first.getYpDot(), second.getYpDot(), FastMath.ulp(first.getYpDot()));
        Assertions.assertEquals(first.getYpDotDot(), second.getYpDotDot(), FastMath.ulp(first.getYpDotDot()));
        Assertions.assertEquals(first.getDut1(), second.getDut1(), FastMath.ulp(first.getDut1()));
        Assertions.assertEquals(first.getDut1Dot(), second.getDut1Dot(), FastMath.ulp(first.getDut1Dot()));
        Assertions.assertEquals(first.getDut1DotDot(), second.getDut1DotDot(), FastMath.ulp(first.getDut1DotDot()));
        Assertions.assertEquals(first.getTransmissionTime(), second.getTransmissionTime(), FastMath.ulp(first.getTransmissionTime()));
    }

    private void checkKlobuchar(final IonosphereKlobucharMessage first, final IonosphereKlobucharMessage second) {

        // check data inherited from base class
        checkIonosphereBase(first, second);

        // check data specific to this message
        checkArray(first.getAlpha(),  second.getAlpha());
        checkArray(first.getBeta(),  second.getBeta());
        Assertions.assertEquals(first.getRegionCode(), second.getRegionCode());

    }

    private void checkNequickG(final IonosphereNequickGMessage first, final IonosphereNequickGMessage second) {

        // check data inherited from base class
        checkIonosphereBase(first, second);

        // check data specific to this message
        checkIonosphereAij(first.getAij(), second.getAij());
        Assertions.assertEquals(first.getFlags(), second.getFlags());

    }

    private void checkBDGIM(final IonosphereBDGIMMessage first, final IonosphereBDGIMMessage second) {

        // check data inherited from base class
        checkIonosphereBase(first, second);

        // check data specific to this message
        checkArray(first.getAlpha(),  second.getAlpha());

    }

    private void checkNavICKlobuchar(final IonosphereNavICKlobucharMessage first, final IonosphereNavICKlobucharMessage second) {

        // check data inherited from base class
        checkIonosphereBase(first, second);

        // check data specific to this message
        Assertions.assertEquals(first.getIOD(), second.getIOD());
        checkArray(first.getAlpha(), second.getAlpha());
        checkArray(first.getBeta(), second.getBeta());
        Assertions.assertEquals(first.getLonMin(), second.getLonMin(), FastMath.ulp(first.getLonMin()));
        Assertions.assertEquals(first.getLonMax(), second.getLonMax(), FastMath.ulp(first.getLonMax()));
        Assertions.assertEquals(first.getModipMin(), second.getModipMin(), FastMath.ulp(first.getModipMin()));
        Assertions.assertEquals(first.getModipMax(), second.getModipMax(), FastMath.ulp(first.getModipMax()));

    }

    private void checkNavICNeQuickN(final IonosphereNavICNeQuickNMessage first, final IonosphereNavICNeQuickNMessage second) {

        // check data inherited from base class
        checkIonosphereBase(first, second);

        // check data specific to this message
        Assertions.assertEquals(first.getIOD(), second.getIOD());
        checkRegionalAij(first.getRegion1(),  second.getRegion1());
        checkRegionalAij(first.getRegion2(),  second.getRegion2());
        checkRegionalAij(first.getRegion3(),  second.getRegion3());

    }

    private void checkGlonassCDMS(final IonosphereGlonassCdmsMessage first, final IonosphereGlonassCdmsMessage second) {

        // check data inherited from base class
        checkIonosphereBase(first, second);

        // check data specific to this message
        Assertions.assertEquals(first.getCA(), second.getCA(), FastMath.ulp(first.getCA()));
        Assertions.assertEquals(first.getCF107(), second.getCF107(), FastMath.ulp(first.getCF107()));
        Assertions.assertEquals(first.getCAP(), second.getCAP(), FastMath.ulp(first.getCAP()));

    }

    private void checkIonosphereBase(final IonosphereBaseMessage first, final IonosphereBaseMessage second) {
        checkDate(first.getDate(), second.getDate());
        checkDate(first.getTransmitTime(), second.getTransmitTime());
    }

    private void checkRegionalAij(final RegionalAij first, final RegionalAij second) {

        // check data inherited from base class
        checkIonosphereAij(first, second);

        // check data specific to this message
        Assertions.assertEquals(first.getIDF(), second.getIDF(), FastMath.ulp(first.getIDF()));
        Assertions.assertEquals(first.getLonMin(), second.getLonMin(), FastMath.ulp(first.getLonMin()));
        Assertions.assertEquals(first.getLonMax(), second.getLonMax(), FastMath.ulp(first.getLonMax()));
        Assertions.assertEquals(first.getModipMin(), second.getModipMin(), FastMath.ulp(first.getModipMin()));
        Assertions.assertEquals(first.getModipMax(), second.getModipMax(), FastMath.ulp(first.getModipMax()));

    }

    private void checkIonosphereAij(final IonosphereAij first, final IonosphereAij second) {
        Assertions.assertEquals(first.getAi0(), second.getAi0(), FastMath.ulp(first.getAi0()));
        Assertions.assertEquals(first.getAi1(), second.getAi1(), FastMath.ulp(first.getAi1()));
        Assertions.assertEquals(first.getAi2(), second.getAi2(), FastMath.ulp(first.getAi2()));
    }

}
