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

import nl.knaw.dans.transfer.TestDirFixture;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;

public class CollectDveTaskTest extends TestDirFixture {

    @Test
    public void shouldRejectNonZipFile() throws Exception {
        // given
        var destinationRoot = testDir.resolve("destination");
        var inbox = testDir.resolve("inbox");
        var failedOutbox = testDir.resolve("failed-outbox");
        var path = inbox.resolve("test.txt");
        Files.createFile(path);

        // when
        var task = new CollectDveTask(path, destinationRoot, failedOutbox);

        // then

    }

}
