package com.azarenka.evebuilders.db.changelog;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class BaselineChangelogTest {

    @Test
    void baselineChangelogContainsAllPostgresTables() throws IOException {
        Path baseline = Path.of("src/main/resources/db/changelog/changeset-0-baseline-builders-schema.yaml");
        String content = Files.readString(baseline, StandardCharsets.UTF_8);

        assertThat(content).contains("CREATE SCHEMA IF NOT EXISTS builders;");
        assertThat(content).contains("CREATE TABLE builders.assets");
        assertThat(content).contains("CREATE TABLE builders.user_info");
        assertThat(content).contains("CREATE TABLE builders.request");
        assertThat(content).contains("CREATE TABLE builders.orders");
        assertThat(content).contains("CREATE TABLE builders.role_permissions");
        assertThat(content).contains("CREATE TABLE builders.user_permissions");
        assertThat(content).contains("CREATE TABLE builders.user_roles");
        assertThat(content).contains("CREATE TABLE builders.permissions");
        assertThat(content.split("CREATE TABLE builders.", -1).length - 1).isEqualTo(35);
    }

    @Test
    void masterChangelogIncludesBaselineFirst() throws IOException {
        Path master = Path.of("src/main/resources/db/changelog/db.changelog-master.yaml");
        String content = Files.readString(master, StandardCharsets.UTF_8);

        assertThat(content).containsSubsequence(
            "databaseChangeLog:",
            "- include:",
            "file: db/changelog/changeset-0-baseline-builders-schema.yaml",
            "- include:",
            "file: db/changelog/changeset-1.yaml"
        );
        assertThat(content).containsOnlyOnce("file: db/changelog/changeset-0-baseline-builders-schema.yaml");
        assertThat(content).containsOnlyOnce("file: db/changelog/changeset-1.yaml");
        assertThat(content).doesNotContain("add_roles.yaml");
        assertThat(content).doesNotContain("changeset-1.5.yaml");
        assertThat(content).doesNotContain("changeset-1.7.yaml");
        assertThat(content).doesNotContain("changeset-2026-06-14-acl.yaml");
        assertThat(content).doesNotContain("changeset-2026-06-17-acl-catalog.yaml");
        assertThat(content).doesNotContain("changeset-2026-06-17-acl-cleanup.yaml");
        assertThat(content).doesNotContain("changeset-2026-06-18-acl-admin.yaml");
        assertThat(content).doesNotContain("changeset-2026-07-04-fit-text-fit.yaml");
    }
}
