package com.example.shopService.support;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Basic in-memory repository based on a ConcurrentHashMap
 * Could have gone with an H2 DB but I guess this is enough for this test
 */
public class BasicRepository<T extends Versionable> {

    private Map<String, T> store = new ConcurrentHashMap<>();

    public T findOne(String id) {
        return store.get(id);
    }

    public List<T> findAll() {
        return new ArrayList<>(store.values());
    }

    public Optional<T> save(String id, T document) {
        if (document.getVersion() == null) {
            T existingDocument = this.store.get(id);
            if (existingDocument == null) {
                document.setVersion(0L);
            } else {
                document.setVersion(existingDocument.getVersion() + 1);
            }
        }
        return Optional.ofNullable(this.store.put(id, document));
    }

    public void deleteAll() {
        store = new HashMap<>();
    }
}
