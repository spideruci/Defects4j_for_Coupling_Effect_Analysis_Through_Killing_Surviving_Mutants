// Instrumented at 2025-12-13 09:03:42
package com.fasterxml.jackson.databind;

public class RoundtripTest extends BaseMapTest {

    private final ObjectMapper MAPPER = new ObjectMapper();

    public void testMedaItemRoundtrip() throws Exception {
        com.fasterxml.jackson.databind.ObjectWriter __ins_v1 = null;
        MediaItem.Content c = new MediaItem.Content();
        c.setBitrate(9600);
        c.setCopyright("none");
        c.setDuration(360000L);
        c.setFormat("lzf");
        c.setHeight(640);
        c.setSize(128000L);
        c.setTitle("Amazing Stuff For Something Or Oth\u00CBr!");
        c.setUri("http://multi.fario.us/index.html");
        c.setWidth(1400);
        c.addPerson("Joe Sixp\u00e2ck");
        c.addPerson("Ezekiel");
        c.addPerson("Sponge-Bob Squarepant\u00DF");
        MediaItem input = new MediaItem(c);
        input.addPhoto(new MediaItem.Photo());
        input.addPhoto(new MediaItem.Photo());
        input.addPhoto(new MediaItem.Photo());
        __ins_v1 = MAPPER.writerWithDefaultPrettyPrinter();
        String json = __ins_v1.writeValueAsString(input);
        MediaItem output = MAPPER.readValue(new java.io.StringReader(json), MediaItem.class);
        assertNotNull(output);
        assertNotNull(output.getImages());
        assertEquals(input.getImages().size(), output.getImages().size());
        assertNotNull(output.getContent());
        assertEquals(input.getContent().getTitle(), output.getContent().getTitle());
        assertEquals(input.getContent().getUri(), output.getContent().getUri());
        // compare re-serialization as a simple check as well
        assertEquals(json, MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(output));
        org.helper.Assertions.verify("var._serializerProvider._serializerCache._sharedMap_1224_223", __ins_v1);
    }
}
