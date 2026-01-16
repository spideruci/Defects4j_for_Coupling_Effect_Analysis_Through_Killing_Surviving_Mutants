// Instrumented at 2025-12-01 00:16:58
package com.fasterxml.jackson.databind.jsontype;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.*;

public class ExternalTypeId198Test extends BaseMapTest {

    public enum Attacks {

        KICK, PUNCH
    }

    static class Character {

        public String name;

        public Attacks preferredAttack;

        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, defaultImpl = Kick.class, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "preferredAttack")
        @JsonSubTypes({ @JsonSubTypes.Type(value = Kick.class, name = "KICK"), @JsonSubTypes.Type(value = Punch.class, name = "PUNCH") })
        public Attack attack;
    }

    public static abstract class Attack {

        public String side;

        @JsonCreator
        public Attack(String side) {
            this.side = side;
        }
    }

    public static class Kick extends Attack {

        @JsonCreator
        public Kick(String side) {
            super(side);
        }
    }

    public static class Punch extends Attack {

        @JsonCreator
        public Punch(String side) {
            super(side);
        }
    }

    final ObjectMapper MAPPER = new ObjectMapper();

    public void testFails() throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper __ins_v1 = null;
        String json = "{ \"name\": \"foo\", \"attack\":\"right\" } }";
        __ins_v1 = MAPPER;
        Character character = __ins_v1.readValue(json, Character.class);
        assertNotNull(character);
        assertNotNull(character.attack);
        assertEquals("foo", character.name);
        org.helper.Assertions.verify("var._deserializationContext._factory.DEFAULT_NO_DESER_CLASS_NAMES_262_32", __ins_v1);
    }

    public void testWorks() throws Exception {
        String json = "{ \"name\": \"foo\", \"preferredAttack\": \"KICK\", \"attack\":\"right\" } }";
        Character character = MAPPER.readValue(json, Character.class);
        assertNotNull(character);
        assertNotNull(character.attack);
        assertEquals("foo", character.name);
    }
}
