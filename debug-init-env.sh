#!/usr/bin/env bash
#
# Copyright (C) 2025 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

echo -n "Pre-creating log..."
TEMPDIR=data
TRANSFER_INBOX=$TEMPDIR/01_transfer-inbox
EXTRACT_METADATA_INBOX=$TEMPDIR/02_extract-metadata/inbox
EXTRACT_METADATA_OUTBOX=$TEMPDIR/02_extract-metadata/outbox
SEND_TO_VAULT_INBOX=$TEMPDIR/03_send-to-vault/inbox
SEND_TO_VAULT_OUTBOX=$TEMPDIR/03_send-to-vault/outbox
DATA_VAULT_INBOX=$TEMPDIR/04_data-vault/inbox
touch $TEMPDIR/dd-transfer-to-vault.log
echo "OK"
echo -n "Creating working directories..."
mkdir -p $TRANSFER_INBOX/inbox
mkdir -p $TRANSFER_INBOX/failed
mkdir -p $EXTRACT_METADATA_INBOX
mkdir -p $EXTRACT_METADATA_OUTBOX/rejected
mkdir -p $EXTRACT_METADATA_OUTBOX/failed
mkdir -p $SEND_TO_VAULT_INBOX
mkdir -p $SEND_TO_VAULT_OUTBOX/failed
mkdir -p $SEND_TO_VAULT_OUTBOX/processed
mkdir -p $DATA_VAULT_INBOX
echo "OK"

