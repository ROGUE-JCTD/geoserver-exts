package org.geotools.ysld.transform.sld;

import org.yaml.snakeyaml.emitter.Emitable;
import org.yaml.snakeyaml.events.*;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public class TracingEmitter implements Emitable {

    Emitable delegate;

    List<Pair> events = new ArrayList();
    int stack = 0;

    public TracingEmitter(Emitable delegate) {
        this.delegate = delegate;
    }

    @Override
    public void emit(Event event) throws IOException {
        if (event instanceof StreamStartEvent) {
            events.add(new Pair(event, stack++));
        }
        else if (event instanceof StreamEndEvent) {
            events.add(new Pair(event, --stack));
        }
        else if (event instanceof DocumentStartEvent) {
            events.add(new Pair(event, stack++));
        }
        else if (event instanceof DocumentEndEvent) {
            events.add(new Pair(event, --stack));
        }
        else if (event instanceof ScalarEvent) {
            events.add(new Pair(event, stack));
        }
        else if (event instanceof MappingStartEvent) {
            events.add(new Pair(event, stack++));
        }
        else if (event instanceof MappingEndEvent) {
            events.add(new Pair(event, --stack));
        }
        else if (event instanceof SequenceStartEvent) {
            events.add(new Pair(event, stack++));
        }
        else if (event instanceof SequenceEndEvent) {
            events.add(new Pair(event, --stack));
        }

        delegate.emit(event);
    }

    public void dump(PrintStream out) {
        for (Pair p : events) {
            for (int i = 0; i < p.stack; i++) {
                out.print("\t");
            }
            out.println(p.event);
        }
    }

    static class Pair {
        Event event;
        int stack;

        Pair(Event event, int stack) {
            this.event = event;
            this.stack = stack;
        }
    }
}
