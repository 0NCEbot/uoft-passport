package interface_adapter;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * The ViewModel for our CA implementation.
 * This class delegates work to a PropertyChangeSupport object for
 * managing the property change events.
 *
 * @param <T> The type of state object contained in the model.
 */
public class ViewModel<T> {

    private final String viewName;

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    private T state;

    // ===== GLOBAL EVENT BUS (static, shared across all ViewModels) =====
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

    public ViewModel(String viewName) {
        this.viewName = viewName;
    }

    public String getViewName() {
        return this.viewName;
    }

    public T getState() {
        return this.state;
    }

    public void setState(T state) {
        this.state = state;
    }

    /**
     * Fires a property changed event for the state of this ViewModel.
     */
    public void firePropertyChange() {
        this.support.firePropertyChange("state", null, this.state);
    }

    /**
     * Fires a property changed event for the state of this ViewModel, which
     * allows the user to specify a different propertyName. This can be useful
     * when a class is listening for multiple kinds of property changes.
     * <p/>
     * For example, the LoggedInView listens for two kinds of property changes;
     * it can use the property name to distinguish which property has changed.
     * @param propertyName the label for the property that was changed
     */
    public void firePropertyChange(String propertyName) {
        this.support.firePropertyChange(propertyName, null, this.state);
    }

    /**
     * Adds a PropertyChangeListener to this ViewModel.
     * @param listener The PropertyChangeListener to be added
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.support.addPropertyChangeListener(listener);
    }
}
