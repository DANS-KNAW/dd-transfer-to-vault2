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

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.util.Optional;

/**
 * <p>
 * </p>
 * <p>
 * The CollectDveTask only requires the DVE:
 * <ul>
 *     <li>to be a ZIP file;</li>
 *     <li>contain a file <code>ROOTDIR/metadata/</code></li>
 * </ul>
 * </p>
 */
@Slf4j
@AllArgsConstructor
public class CollectDveTask implements Runnable {
    private final Path dve;
    private final Path failedOutbox;
    private final Path destinationRoot;

    @Override
    public void run() {

        // If there is, but it is empty, delete it and create a new one (with a new name)

        // Move the DVE to the target NBN subdirectory under destinationRoot

        // If anything goes wrong move the DVE to a subdirectory of the inbox called "failed" and write the stack trace to a text file with the same name as the DVE but with an -error.txt extension
    }

    private String findTargetNbn() {
        return null;
    }

    private Optional<Path> findSubdirFor(String nbn) {
        return Optional.empty();
    }
}
