package io.navendra.retrofitkotlindeferred.utils;

public interface IStorageHelper {
    void saveDataStorage(String var1, String var2);

    String loadDataStorage(String var1);

    void clearDataStorage();
}