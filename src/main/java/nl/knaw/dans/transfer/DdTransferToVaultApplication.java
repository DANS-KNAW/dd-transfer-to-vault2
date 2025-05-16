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
import nl.knaw.dans.lib.util.ClientProxyBuilder;
import nl.knaw.dans.lib.util.inbox.Inbox;
import nl.knaw.dans.transfer.client.VaultCatalogClient;
import nl.knaw.dans.transfer.client.VaultCatalogClientImpl;
import nl.knaw.dans.transfer.config.DdTransferToVaultConfiguration;
import nl.knaw.dans.transfer.core.CollectDveTaskFactory;
import nl.knaw.dans.transfer.core.CreationTimeComparator;
import nl.knaw.dans.transfer.core.DataFileAttributesReader;
import nl.knaw.dans.transfer.core.ExtractMetadataTaskFactory;
import nl.knaw.dans.transfer.core.FileContentAttributesReader;
import nl.knaw.dans.transfer.core.FileService;
import nl.knaw.dans.transfer.core.FileServiceImpl;
import nl.knaw.dans.transfer.core.RemoveEmptySubdirsTask;
import nl.knaw.dans.transfer.core.oaiore.OaiOreMetadataReader;
import nl.knaw.dans.vaultcatalog.client.invoker.ApiClient;
import nl.knaw.dans.vaultcatalog.client.resources.DefaultApi;
import org.apache.commons.io.filefilter.FileFilterUtils;

import java.util.concurrent.CountDownLatch;

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
        FileService fileService = new FileServiceImpl();
        VaultCatalogClient vaultCatalogClient = createVaultCatalogClient(configuration);
        CountDownLatch startCollectInbox = new CountDownLatch(1);
        environment.lifecycle().manage(
            Inbox.builder()
                .onPollingHandler(new ReleaseLatch(startCollectInbox))
                .fileFilter(FileFilterUtils.directoryFileFilter())
                .taskFactory(
                    ExtractMetadataTaskFactory.builder()
                        .outboxProcessed(configuration.getTransfer().getExtractMetadata().getOutbox().getProcessed())
                        .outboxFailed(configuration.getTransfer().getExtractMetadata().getOutbox().getFailed())
                        .outboxRejected(configuration.getTransfer().getExtractMetadata().getOutbox().getRejected())
                        .fileContentAttributesReader(new FileContentAttributesReader(
                            fileService,
                            new OaiOreMetadataReader(),
                            new DataFileAttributesReader(fileService)))
                        .vaultCatalogClient(vaultCatalogClient).build())
                .inbox(configuration.getTransfer().getExtractMetadata().getInbox().getPath())
                .executorService(configuration.getTransfer().getExtractMetadata().getTaskQueue().build(environment))
                .interval(Math.toIntExact(configuration.getTransfer().getExtractMetadata().getInbox().getPollingInterval().toMilliseconds()))
                .inboxItemComparator(CreationTimeComparator.getInstance())
                .build());

        environment.lifecycle().manage(
            Inbox.builder()
                .awaitLatch(startCollectInbox)
                .onPollingHandler(new RemoveEmptySubdirsTask(configuration.getTransfer().getCollectDve().getOutbox().getProcessed()))
                .fileFilter(FileFilterUtils.fileFileFilter())
                .taskFactory(
                    CollectDveTaskFactory.builder()
                        .destinationRoot(configuration.getTransfer().getCollectDve().getOutbox().getProcessed())
                        .failedOutbox(configuration.getTransfer().getCollectDve().getOutbox().getFailed()).build())
                .inbox(configuration.getTransfer().getCollectDve().getInbox().getPath())
                // N.B. this MUST be a single-threaded executor to prevent DVEs from out-racing each other via parallel processing, which would mess up the order of the DVEs.
                .executorService(environment.lifecycle().executorService("transfer-inbox").maxThreads(1).minThreads(1).build())
                .interval(Math.toIntExact(configuration.getTransfer().getCollectDve().getInbox().getPollingInterval().toMilliseconds()))
                .inboxItemComparator(CreationTimeComparator.getInstance())
                .build());


    }

    private VaultCatalogClient createVaultCatalogClient(DdTransferToVaultConfiguration configuration) {
        final var vaultCatalogProxy = new ClientProxyBuilder<ApiClient, DefaultApi>()
            .apiClient(new nl.knaw.dans.vaultcatalog.client.invoker.ApiClient())
            .basePath(configuration.getVaultCatalog().getUrl())
            .httpClient(configuration.getVaultCatalog().getHttpClient())
            .defaultApiCtor(nl.knaw.dans.vaultcatalog.client.resources.DefaultApi::new)
            .build();
        return new VaultCatalogClientImpl(vaultCatalogProxy);
    }

}
