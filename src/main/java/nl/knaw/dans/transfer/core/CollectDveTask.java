/*
 * Copyright (C) 2025 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.transfer.core;

import com.jayway.jsonpath.JsonPath;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * <p>
 * Determines the target NBN of a DVE. If there is no subdirectory for the target NBN yet, one is created. Then the DVE is moved to the subdirectory.
 * </p>
 * <p>
 * The target NBN must be specified in the file <code>DATASETDIR/metadata/oai-ore.jsonld</code> under the JSON Path <code>ore:describes/dansDataVaultMetadata:dansNbn</code>, in which
 * <code>DATASETDIR</code> is the single directory in the root of the DVE. If the task is unable to find the target NBN, it will move the DVE to a subdirectory of the inbox called "failed" and write
 * the stack.
 * </p>
 */
@Slf4j
@AllArgsConstructor
public class CollectDveTask implements Runnable {
    private static final String METADATA_PATH = "metadata/oai-ore.jsonld";
    private static final String NBN_PATH = "$.ore:describes.dansDataVaultMetadata:dansNbn";

    private final Path dve;
    private final Path failedOutbox;
    private final Path destinationRoot;

    @Override
    public void run() {
        var targetNbn = findTargetNbn();
        var targetDir = destinationRoot.resolve(targetNbn);
        ensureExists(targetDir);
        moveToTargetDir(targetDir);
    }

    private String findTargetNbn() {
        try (FileSystem zipFs = FileSystems.newFileSystem(dve, (ClassLoader) null)) {
            var rootDir = zipFs.getRootDirectories().iterator().next();
            try (var topLevelDirStream = Files.list(rootDir)) {
                var topLevelDir = topLevelDirStream.filter(Files::isDirectory)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No top-level directory found in DVE"));

                var metadataPath = topLevelDir.resolve(METADATA_PATH);
                if (!Files.exists(metadataPath)) {
                    throw new IllegalStateException("No metadata file found in DVE");
                }

                try (var is = Files.newInputStream(metadataPath)) {
                    return JsonPath.read(is, NBN_PATH);
                }
            }
        }
        catch (IOException e) {
            throw new RuntimeException("Error processing ZIP file", e);
        }
    }

    private void ensureExists(Path dir) {
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            }
            catch (IOException e) {
                log.error("Unable to create target directory: {}", dir, e);
                moveToFailedOutbox();
            }
        }
    }

    private void moveToTargetDir(Path targetDir) {
        try {
            Files.move(dve, targetDir.resolve(dve.getFileName()));
        }
        catch (IOException e) {
            log.error("Unable to move DVE to target directory: {}", targetDir, e);
            moveToFailedOutbox();
        }
    }

    private void moveToFailedOutbox() {
        try {
            Files.move(dve, failedOutbox.resolve(dve.getFileName()));
        }
        catch (IOException e) {
            log.error("Unable to move DVE to failed outbox: {}", failedOutbox, e);
        }
    }
}

