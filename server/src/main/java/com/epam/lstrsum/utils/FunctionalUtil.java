package com.epam.lstrsum.utils;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FunctionalUtil {
    private static final Random RANDOM = new Random();
    private static final int MAX_COLLECTION_SIZE = 10;
    private static final int MAX_BITS = 15;

    public static <T> List<T> getList(Supplier<T> supplier) {
        return getList(supplier, 1 + RANDOM.nextInt(MAX_COLLECTION_SIZE));
    }

    public static <T> List<T> getListWithSize(Supplier<T> supplier, int size) {
        return Stream.generate(supplier)
                .limit(size)
                .collect(Collectors.toList());
    }

    public static <T> List<T> getList(Supplier<T> supplier, int maxSize) {
        return getListWithSize(supplier, 1 + RANDOM.nextInt(maxSize));
    }

    public static <K, V> Map<K, V> getMap(Supplier<K> keySupplier, Supplier<V> valueSupplier) {
        return Stream.generate(() -> ImmutablePair.of(keySupplier.get(), valueSupplier.get()))
                .limit(MAX_COLLECTION_SIZE)
                .collect(Collectors.toMap(
                        ImmutablePair::getLeft,
                        ImmutablePair::getRight,
                        (v1, v2) -> v1
                ));

    }

    public static String getEmailPostfix() {
        return new StringBuilder(100)
                .append("@")
                .append(getRandomString(10))
                .append(".")
                .append(getRandomString(3))
                .toString();
    }

    public static String getRandomString() {
        return getRandomString(RANDOM.nextInt(MAX_BITS));
    }

    public static String getRandomString(int length) {
        return RandomStringUtils.randomAlphanumeric(length);
    }
}
