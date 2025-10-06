package org.orekit.estimation.measurements;

import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.AbsolutePVCoordinates;
import org.orekit.utils.Constants;
import org.orekit.utils.PVCoordinates;

import static org.junit.jupiter.api.Assertions.*;

class SignalTravelTimeAdjustableReceiverTest {


    @Test
    void testComputeStatic() {
        // GIVEN
        final Frame frame = FramesFactory.getGCRF();
        final AbsoluteDate emissionDate = AbsoluteDate.ARBITRARY_EPOCH;
        final AbsolutePVCoordinates absolutePVCoordinates = new AbsolutePVCoordinates(frame, emissionDate, new PVCoordinates());
        final Vector3D emitterPosition = new Vector3D(1e2, 1e3, 1e4);
        final SignalTravelTimeAdjustableReceiver signalTimeOfFlight = new SignalTravelTimeAdjustableReceiver(absolutePVCoordinates);
        // WHEN
        final double actual = signalTimeOfFlight.compute(emitterPosition, emissionDate, frame);
        // THEN
        final double expected = emitterPosition.getNorm() / Constants.SPEED_OF_LIGHT;
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1e3, -1e1, 0., 1e1, 1e2, 1e4, 1e4})
    void testCompute(final double speedFactor) {
        // GIVEN
        final Frame frame = FramesFactory.getGCRF();
        final AbsoluteDate emissionDate = AbsoluteDate.ARBITRARY_EPOCH;
        final PVCoordinates pvCoordinates = new PVCoordinates(Vector3D.MINUS_I, new Vector3D(1, -2, 3).scalarMultiply(speedFactor));
        final AbsolutePVCoordinates absolutePVCoordinates = new AbsolutePVCoordinates(frame, emissionDate, pvCoordinates);
        final Vector3D emitterPosition = new Vector3D(1e2, 1e3, 1e4);
        final SignalTravelTimeAdjustableReceiver signalTimeOfFlight = new SignalTravelTimeAdjustableReceiver(absolutePVCoordinates);
        // WHEN
        final double actual = signalTimeOfFlight.compute(emitterPosition, emissionDate, frame);
        // THEN
        final AbsoluteDate receptionDate = emissionDate.shiftedBy(actual);
        final double expected = signalTimeOfFlight.compute(emitterPosition, emissionDate, receptionDate, frame);
        assertEquals(expected, actual);
    }
}
