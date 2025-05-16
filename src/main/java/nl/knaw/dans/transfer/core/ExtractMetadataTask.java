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
import nl.knaw.dans.transfer.CreationTimeComparator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@AllArgsConstructor
public class ExtractMetadataTask implements Runnable {
    private final Path targetNbnDir;

    @Override
    public void run() {
        try {
            var dves = getDves();
            while (!dves.isEmpty()) {
                for (var dve : dves) {
                    // Process each DVE
                }
                // Get any new DVE files that may have been added while processing
                dves = getDves();
            }
        }
        catch (IOException e) {
        }
        /*
         * At this point, all DVE files in targetNbnDir have been processed. We are NOT sticking around to wait for more DVEs, as this would prevent our worker thread from taking on other tasks.
         * The transfer inbox will dete targetNbnDir if it is empty in the next polling cycle. If new DVEs are added for this same NBN, the inbox will create a new targetNbnDir (with a different name)
         * and create a new ExtractMetadataTask for it. It cannot have the same name, because there would be two tasks instances competing for the same targetNbnDir.
         */
    }

    private List<Path> getDves() throws IOException {
        try (var dirStream = Files.list(targetNbnDir)) {
            return dirStream.filter(Files::isRegularFile)
                .sorted(CreationTimeComparator.getInstance()).toList();
        }
    }

}
