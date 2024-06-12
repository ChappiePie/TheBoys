package chappie.theboys.util;

import java.util.Map;

public interface EntitySavingFields {
    void setup(Map<String, Object> map);
    void reset();
    Map<String, Object> map();
}
