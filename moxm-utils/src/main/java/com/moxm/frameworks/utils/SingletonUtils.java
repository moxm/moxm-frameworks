package com.moxm.frameworks.utils;

/**
 * Singleton helper class for lazily initialization.
 * 
 * @author <a href="http://www.moxm.com/" target="_blank">Moxm</a>
 * 
 * @param <T>
 */
public abstract class SingletonUtils<T> {

    private T instance;

    protected abstract T newInstance();

    public final T getInstance() {
        if (instance == null) {
            synchronized (SingletonUtils.class) {
                if (instance == null) {
                    instance = newInstance();
                }
            }
        }
        return instance;
    }
}
