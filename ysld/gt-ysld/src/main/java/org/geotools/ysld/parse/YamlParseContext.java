package org.geotools.ysld.parse;

import org.geotools.ysld.YamlMap;
import org.geotools.ysld.YamlObject;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class YamlParseContext {
    /*
     * Handlers may handle a YAML object as a whole, or they may handle some specific subset of 
     * properties for one.  The former delegates the the latter by pushing the latter on to the 
     * stack with the current object.  Example: TextHandler handling X pushes a FontHandler with X 
     * as the current node.
     * 
     * Odd design resulted from initially planning to do stream parsing then recycling the code for
     * a in memory parser.
     */

    Deque<Entry> stack;
    Entry curr;
    
    Map<String, Object> docHints = new HashMap<>();;

    public YamlParseContext() {
        stack = new ArrayDeque<Entry>();
    }

    /**
     * Parse a child of the current object if present
     * @param key key of the child entry
     * @param handler handler to use
     * @return self
     */
    public YamlParseContext push(String key, YamlParseHandler handler) {
        return push(curr.obj, key, handler);
    }

    /**
     * Parse a child of the specified object if present
     * @param scope object to start from
     * @param key key of the child entry
     * @param handler handler to use
     * @return self
     */
    public YamlParseContext push(YamlObject scope, String key, YamlParseHandler handler) {
        YamlMap map = scope.map();
        if (map.has(key)) {
            return doPush(scope.map().obj(key), handler);
        }
        return this;
    }

    /**
     * Add a handler to the stack handling the current object.  Used for "inlined"/common properties.
     * @param handler handler to use
     * @return self
     */
    public YamlParseContext push(YamlParseHandler handler) {
        return doPush(curr.obj, handler);
    }

    /**
     * Add a handler to the stack handling the specified object
     * @param obj the object to parse
     * @param handler handler to use
     * @return self
     */
    public YamlParseContext push(YamlObject obj, YamlParseHandler handler) {
        return doPush(obj, handler);
    }

    YamlParseContext doPush(YamlObject obj, YamlParseHandler handler) {
        stack.push(new Entry(obj, handler));
        return this;
    }

    public YamlParseContext pop() {
        stack.pop();
        return this;
    }

    public boolean next() {
        curr = stack.pop();
        curr.handler.handle(curr.obj, this);
        return !stack.isEmpty();
    }

    public @Nullable Object getDocHint(String key) {
        return docHints.get(key);
    }
    
    public void setDocHint(String key, Object value) {
        docHints.put(key, value);
    }
    
    public void mergeDocHints(Map<String, Object> hints) {
        docHints.putAll(hints);
    }
    
    static class Entry {
        YamlObject obj;
        YamlParseHandler handler;
        
        Entry(YamlObject obj, YamlParseHandler handler) {
            this.obj = obj;
            this.handler = handler;
        }
    }
}
