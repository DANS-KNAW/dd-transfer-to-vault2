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

package nl.knaw.dans.transfer;

import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import nl.knaw.dans.lib.util.inbox.Inbox;
import nl.knaw.dans.transfer.config.DdTransferToVaultConfiguration;
import nl.knaw.dans.transfer.core.CollectDveTaskFactory;
import nl.knaw.dans.transfer.core.RemoveEmptySubdirsTask;
import nl.knaw.dans.transfer.core.TransferInbox;

public class DdTransferToVaultApplication extends Application<DdTransferToVaultConfiguration> {

    public static void main(final String[] args) throws Exception {
        new DdTransferToVaultApplication().run(args);
    }

    @Override
    public String getName() {
        return "DD Transfer To Vault";
    }

    @Override
    public void initialize(final Bootstrap<DdTransferToVaultConfiguration> bootstrap) {
        // TODO: application initialization
    }

    @Override
    public void run(final DdTransferToVaultConfiguration configuration, final Environment environment) {
        environment.lifecycle().manage(new TransferInbox(
            Inbox.builder()
                .onPollingHandler(new RemoveEmptySubdirsTask(configuration.getTransfer().getExtractMetadata().getInbox().getPath(), "failed"))
                .taskFactory(new CollectDveTaskFactory(
                    configuration.getTransfer().getExtractMetadata().getInbox().getPath(),
                    configuration.getTransfer().getExtractMetadata().getInbox().getPath().resolve("failed")))
                .inbox(configuration.getTransfer().getInbox().getPath())
                .interval(Math.toIntExact(configuration.getTransfer().getInbox().getPollingInterval().toMilliseconds()))
                .inboxItemComparator(CreationTimeComparator.getInstance())
                .build()));

    }

}
