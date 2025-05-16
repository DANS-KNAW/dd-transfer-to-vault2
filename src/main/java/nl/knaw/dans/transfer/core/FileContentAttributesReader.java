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
import nl.knaw.dans.transfer.core.oaiore.OaiOreMetadataReader;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

@AllArgsConstructor
public class FileContentAttributesReader {
    private final FileService fileService;
    private final OaiOreMetadataReader oaiOreMetadataReader;
    private final DataFileAttributesReader dataFileAttributesReader;

    public FileContentAttributes getFileContentAttributes(Path path)  {

        try {
            var datasetVersionExport = fileService.openZipFile(path);

            var metadataContent = fileService.getEntryUnderBaseFolder(datasetVersionExport, Path.of("metadata/oai-ore.jsonld"));
            var oaiOre = IOUtils.toString(metadataContent, StandardCharsets.UTF_8);
            var fileContentAttributes = oaiOreMetadataReader.readMetadata(oaiOre);
            fileContentAttributes.setMetadata(oaiOre);

            var dataFileAttributes = dataFileAttributesReader.readDataFileAttributes(path);
            fileContentAttributes.setDataFileAttributes(dataFileAttributes);

            return fileContentAttributes;
        }
        catch (IOException e) {
            throw new RuntimeException("unable to read metadata from file", e);
        }
    }
}
