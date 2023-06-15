package io.github.logtube.core.outputs;

import io.github.logtube.core.IEvent;
import io.github.logtube.core.IEventSerializer;
import io.github.logtube.core.serializers.EventConsoleSerializer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

public class EventConsoleOutput extends BaseEventOutput {

    private static final char[] NEW_LINE = new char[]{'\r', '\n'};

    private IEventSerializer serializer = new EventConsoleSerializer(false);

    private final Writer writerStdout = new PrintWriter(System.out);

    private final Writer writerStderr = new PrintWriter(System.err);

    public void setPretty(boolean pretty) {
        this.serializer = new EventConsoleSerializer(pretty);
    }

    @Override
    public synchronized void doAppendEvent(@NotNull IEvent e) {
        try {
            final Writer writer;
            switch (e.getTopic()) {
                case "error":
                case "err":
                case "fatal":
                    writer = this.writerStderr;
                    break;
                default:
                    writer = this.writerStdout;
            }
            serializer.serialize(e, writer);
            writer.write(NEW_LINE);
            writer.flush();
        } catch (IOException ignored) {
        }
    }

}
