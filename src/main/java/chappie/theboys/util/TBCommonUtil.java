package chappie.theboys.util;

import chappie.modulus.util.data.DataAccessor;

import java.awt.*;

public class TBCommonUtil {
    public static final DataAccessor<Color> COLOR = new DataAccessor<>("color", DataAccessor.DataSerializer.COLOR);
    public static final DataAccessor<Integer> DISTANCE = new DataAccessor<>("distance", DataAccessor.DataSerializer.INT);
}
