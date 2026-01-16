// Instrumented at 2025-11-28 09:38:30
package com.fasterxml.jackson.databind.jsontype;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.BaseMapTest;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestDefaultWithCreators extends BaseMapTest {

    /*
    /**********************************************************
    /* Helper types
    /**********************************************************
     */
    static abstract class Job {

        public long id;
    }

    static class UrlJob extends Job {

        private final String url;

        private final int count;

        @JsonCreator
        public UrlJob(@JsonProperty("id") long id, @JsonProperty("url") String url, @JsonProperty("count") int count) {
            this.id = id;
            this.url = url;
            this.count = count;
        }

        public String getUrl() {
            return url;
        }

        public int getCount() {
            return count;
        }
    }

    /*
    /**********************************************************
    /* Unit tests
    /**********************************************************
     */
    public void testWithCreators() throws Exception {
        ObjectMapper __ins_v1 = null;
        __ins_v1 = new ObjectMapper();
        ObjectMapper mapper = __ins_v1;
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        UrlJob input = new UrlJob(123L, "http://foo", 3);
        String json = mapper.writeValueAsString(input);
        assertNotNull(json);
        Job output = mapper.readValue(json, Job.class);
        assertNotNull(output);
        assertSame(UrlJob.class, output.getClass());
        UrlJob o2 = (UrlJob) output;
        assertEquals(123L, o2.id);
        assertEquals("http://foo", o2.getUrl());
        assertEquals(3, o2.getCount());
        org.helper.Assertions.verify("var._deserializationContext._factory.DEFAULT_NO_DESER_CLASS_NAMES_184_", __ins_v1);
    }
}
