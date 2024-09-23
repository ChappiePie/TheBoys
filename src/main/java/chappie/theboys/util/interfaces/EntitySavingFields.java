package chappie.theboys.util.interfaces;

import java.util.Map;

public interface EntitySavingFields {
    void setup(Map<String, Object> map);

    void reset();

    Map<String, Object> map();
}
