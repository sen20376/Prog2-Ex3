package at.ac.fhcampuswien.fhmdb.util;

@FunctionalInterface
public interface ClickEventHandler<T> {
    void onClick(T item);
}
