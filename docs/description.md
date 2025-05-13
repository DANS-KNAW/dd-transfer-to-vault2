DESCRIPTION
===========

Overview
--------

This service is responsible for taking dataset version exports, cataloging them and transferring them to the DANS data vault. If the dataset version export is
the first version of a dataset, an NBN persistent identifier is minted for the dataset and registered in the NBN database. For more information about the
context of this service, see the [Data Station architecture overview]{:target="_blank"}.

[Data Station architecture overview]: https://dans-knaw.github.io/dans-datastation-architecture/datastation/

Interfaces
----------

![Interfaces](img/overview.png)

### Provided

#### Inbox

* _Protocol type_: Shared filesystem
* _Internal or external_: **internal**
* _Purpose_: to receive dataset version exports from the Data Stations and other services

#### Admin console

* _Protocol type_: HTTP
* _Internal or external_: **internal**
* _Purpose_: application monitoring and management

### Consumed

#### Data Vault Catalog

* _Protocol type_: HTTP
* _Internal or external_: **internal**
* _Purpose_: to maintain information about the datasets and their versions that are stored in the DANS data vault

#### NBN Database

* _Protocol type_: HTTP
* _Internal or external_: **internal**
* _Purpose_: to mint and register NBN persistent identifiers for datasets

#### Data Vault import inbox

* _Protocol type_: Shared filesystem
* _Internal or external_: **internal**
* _Purpose_: to import dataset version exports into the DANS data vault

###### Data Vault API

* _Protocol type_: HTTP
* _Internal or external_: **internal**
* _Purpose_: to issue commands to the DANS data vault and retrieve information from it

Processing
----------
This service is best viewed as a processing pipeline for dataset version exports. It connects a source that produces dataset version exports to a DANS data
Vault Storage Root, which stores the dataset version exports as OCFL object versions. A source can be a Data Station or a Vault as a Service client. The service
takes care of cataloging the dataset version exports and ensuring that the dataset is resolvable via the NBN persistent identifier. It attempts to do this in an
efficient way, by processing multiple dataset version exports in parallel, while ensuring that the order of the dataset version exports is preserved.
Furthermore, the service will attempt to resume processing of dataset version exports that were left unfinished in the event of a crash or restart.

### Inbox

### Validation

The first step in the processing pipeline is to validate the dataset version export. Currently, the only layout that is supported is the [bagpack] layout. If
the dataset version export is not a bagpack, it will be rejected.

### Metadata extraction

The next step is to extract the metadata from the dataset version export in order to create or update the dataset version in the DANS data vault catalog. The
main source of metadata is the `metadata/oai-ore.jsonld` file in the dataset version export.

### NBN registration

After the Vault Catalog has been updated, the NBN persistent identifier is minted and scheduled for registration in the NBN database. This is done in a separate
thread which uses a database table as a queue, so that the registration can be retried in case of a restart or crash.

### Transfer to vault and layer management

Finally, the dataset version export is extracted to the current DANS data vault import inbox batch for this instance of `dd-transfer-to-vault`. If the size of
the batch exceeds a certain threshold, the service will issue a command to the DANS data vault to import the batch. The import command will return a tracking
URL which can

### Confirmation of archiving









