package com.minecraftly.core.bukkit.config;

import static com.google.common.base.Preconditions.checkNotNull;

import com.minecraftly.core.ContentOwner;
import com.minecraftly.core.bukkit.utilities.BukkitUtilities;

/**
 * Represents a data value of which we can track the content owner and it's original value.
 */
public class DataValue<T> {

    private ContentOwner contentOwner;
    private T def;
    private T value;
    private T untouchedValue;
    private Class<T> typeClass;

    private Object handler = null;

    public DataValue(ContentOwner contentOwner, T def, Class<T> typeClass) {
        checkNotNull(contentOwner);
        checkNotNull(def);

        this.contentOwner = contentOwner;
        this.def = def;
        this.typeClass = typeClass;
        setValue(def);
    }

    public ContentOwner getContentOwner() {
        return contentOwner;
    }

    public T getDefaultValue() {
        return def;
    }

    public T getValue() {
        return value;
    }

    @SuppressWarnings("unchecked")
    public void setValue(T value) {
        if (value == null) {
            this.untouchedValue = getDefaultValue();
        } else {
            this.untouchedValue = value;
        }

        if (this.untouchedValue instanceof String) {
            this.value = (T) BukkitUtilities.translateAlternateColorCodes('&', (String) this.untouchedValue); // this is legal
        } else {
            this.value = value;
        }
    }

    public T getUntouchedValue() {
        return untouchedValue;
    }

    public Class<T> getTypeClass() {
        return typeClass;
    }

    // very internal methods

    protected Object getHandler() {
        return handler;
    }

    protected void setHandler(Object handler) {
        this.handler = handler;
    }
}
