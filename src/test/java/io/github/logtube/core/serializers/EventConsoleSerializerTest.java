package io.github.logtube.core.serializers;

import io.github.logtube.core.events.Event;
import junit.framework.TestCase;

import java.io.IOException;
import java.io.StringWriter;

public class EventConsoleSerializerTest extends TestCase {

    public void testSerialize() throws IOException {
        Event event = new Event();
        event.setCrid("5c9597f3c8245907ea71a89d9d39d08e");
        event.setTopic("info");
        event.setKeyword("aaa,bbb,ccc");
        event.setMessage("you bing ba");
        event.extra("hello", "world");
        EventConsoleSerializer serializer = new EventConsoleSerializer();
        StringWriter stringWriter = new StringWriter();
        serializer.serialize(event, stringWriter);
        stringWriter.write("\n");
        serializer.serialize(event, stringWriter);
        System.out.println(stringWriter);
    }

}