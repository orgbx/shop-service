package com.example.shopService.support;

import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


public class BasicRepositoryTest {

    private BasicRepository<VersionableDocument> repository;

    @Before
    public void setUp() throws Exception {
        repository = new BasicRepository<>();
    }

    @Test
    public void save() throws Exception {
        String ID = "X";
        VersionableDocument document = new VersionableDocument("first");
        Optional<VersionableDocument> saveResult = repository.save(ID, document);
        assertThat(saveResult.isPresent()).as("Saving a new element should return null").isFalse();

        document = new VersionableDocument("second");
        saveResult = repository.save(ID, document);
        assertThat(saveResult.isPresent()).as("Saving an existing element should return something").isTrue();
        assertThat(saveResult.get().description).as("Saving an existing element should return the previous one").isEqualTo("first");
        assertThat(repository.findOne(ID).getVersion()).as("Saving an existing element should have increment the version").isEqualTo(1);
    }

    class VersionableDocument extends Versionable {
        private static final long serialVersionUID = -3801321484887482586L;
        final String description;

        VersionableDocument(String description) {
            super();
            this.description = description;
        }
    }
}