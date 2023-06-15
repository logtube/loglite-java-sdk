package io.github.logtube.core.serializers;

import io.github.logtube.core.IEvent;
import io.github.logtube.core.IEventSerializer;
import io.github.logtube.utils.Dates;
import io.github.logtube.utils.ExtraJsonWriter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

public class EventConsoleSerializer implements IEventSerializer {

    private final boolean pretty;

    public EventConsoleSerializer(boolean pretty) {
        super();
        this.pretty = pretty;
    }

    public EventConsoleSerializer() {
        this(false);
    }

    @Override
    public void serialize(@NotNull IEvent e, @NotNull Writer w) throws IOException {
        if (this.pretty) {
            w.write(Dates.formatLineTimestamp(e.getTimestamp()));
            w.write(" [");
            w.write(e.getTopic());
            w.write("] {traceid = ");
            w.write(e.getCrid());
            boolean multiline = false;
            if (e.getExtra() != null) {
                for (Map.Entry<String, Object> entry : e.getExtra().entrySet()) {
                    String value = entry.getValue().toString().trim();
                    if (value.contains("\n")) {
                        multiline = true;
                        w.write("}\n  {");
                    } else {
                        w.write("; ");
                    }
                    w.write(entry.getKey());
                    w.write(" = ");
                    w.write(value);
                }
            }
            w.write('}');
            if (multiline) {
                w.write("\n  ");
            } else {
                w.write(' ');
            }
            if (e.getKeyword() != null) {
                w.write('(');
                w.write(e.getKeyword());
                w.write(") ");
            }
            if (e.getMessage() != null) {
                w.write(e.getMessage());
            }
        } else {
            ExtraJsonWriter j = new ExtraJsonWriter(w);
            j.beginObject();
            j.name("timestamp").value(Dates.formatISO(e.getTimestamp()));
            j.name("topic").value(e.getTopic());
            j.name("traceid").value(e.getCrid());
            if (e.getKeyword() != null) {
                j.name("keyword").value(e.getKeyword());
            }
            if (e.getMessage() != null) {
                j.name("message").value(e.getMessage());
            }
            if (e.getExtra() != null) {
                for (Map.Entry<String, Object> entry : e.getExtra().entrySet()) {
                    j.name("x_" + entry.getKey());
                    j.value(entry.getValue());
                }
            }
            j.endObject();
        }
    }
}
