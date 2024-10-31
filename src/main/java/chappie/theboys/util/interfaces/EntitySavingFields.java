package chappie.theboys.util.interfaces;

import java.util.Map;

public interface EntitySavingFields {
    void theBoys$setup(Map<String, Object> map);

    void theBoys$reset();

    Map<String, Object> theBoys$map();
}
