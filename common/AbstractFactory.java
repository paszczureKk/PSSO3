package common;

public interface AbstractFactory<T> {
    T create(String type) ;
}