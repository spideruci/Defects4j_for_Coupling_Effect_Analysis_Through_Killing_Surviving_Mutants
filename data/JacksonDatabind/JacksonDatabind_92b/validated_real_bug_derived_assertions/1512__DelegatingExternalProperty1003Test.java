// Instrumented at 2025-11-28 09:38:36
package com.fasterxml.jackson.databind.creators;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.*;

public class DelegatingExternalProperty1003Test extends BaseMapTest {

    static class HeroBattle {

        private final Hero hero;

        private HeroBattle(Hero hero) {
            if (hero == null)
                throw new Error();
            this.hero = hero;
        }

        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "heroType")
        public Hero getHero() {
            return hero;
        }

        @JsonCreator
        static HeroBattle fromJson(Delegate json) {
            return new HeroBattle(json.hero);
        }
    }

    static class Delegate {

        @JsonProperty
        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "heroType")
        public Hero hero;
    }

    public interface Hero {
    }

    static class Superman implements Hero {

        String name = "superman";

        public String getName() {
            return name;
        }
    }

    public void testExtrnalPropertyDelegatingCreator() throws Exception {
        ObjectMapper __ins_v1 = null;
        __ins_v1 = new ObjectMapper();
        ObjectMapper mapper = __ins_v1;
        final String json = mapper.writeValueAsString(new HeroBattle(new Superman()));
        final HeroBattle battle = mapper.readValue(json, HeroBattle.class);
        assertTrue(battle.getHero() instanceof Superman);
        org.helper.Assertions.verify("var._deserializationContext._factory.DEFAULT_NO_DESER_CLASS_NAMES_1512_", __ins_v1);
    }
}
