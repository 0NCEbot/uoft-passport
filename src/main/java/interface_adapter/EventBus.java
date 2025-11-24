package interface_adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class EventBus {

    private static final Map<String, List<Consumer<Object>>> globalListeners = new ConcurrentHashMap<>();

    public static void subscribe(String eventName, Consumer<Object> listener) {
        globalListeners.computeIfAbsent(eventName, k -> new ArrayList<>()).add(listener);
    }

    public static void publish(String eventName, Object payload) {
        List<Consumer<Object>> listeners = globalListeners.get(eventName);
        if (listeners != null) {
            for (Consumer<Object> listener : listeners) {
                listener.accept(payload);
            }
        }
    }

    public static void publish(String eventName) {
        publish(eventName, null);
    }

    public static void unsubscribe(String eventName, Consumer<Object> listener) {
        List<Consumer<Object>> eventListeners = globalListeners.get(eventName);
        if (eventListeners != null) {
            eventListeners.remove(listener);
        }
    }
}
