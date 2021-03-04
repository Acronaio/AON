/*
 * Copyright © 2013-2016 The Nxt Core Developers.
 * Copyright © 2016-2018 Jelurida IP B.V.
 *
 * See the LICENSE.txt file at the top-level directory of this distribution
 * for licensing information.
 *
 * Unless otherwise agreed in a custom licensing agreement with Jelurida B.V.,
 * no part of the Nxt software, including this file, may be copied, modified,
 * propagated, or distributed except according to the terms contained in the
 * LICENSE.txt file.
 *
 * Removal or modification of this copyright notice is prohibited.
 *
 */

package nxt;

import nxt.crypto.Crypto;
import nxt.crypto.EncryptedData;
import nxt.util.Convert;
import nxt.util.bbh.LengthRwPrimitiveType;
import nxt.util.bbh.StringRw;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static nxt.util.bbh.LengthRwPrimitiveType.BYTE;
import static nxt.util.bbh.LengthRwPrimitiveType.SHORT;

public interface Attachment extends Appendix {

    TransactionType getTransactionType();

    abstract class AbstractAttachment extends Appendix.AbstractAppendix implements Attachment {

        private AbstractAttachment(ByteBuffer buffer, byte transactionVersion) {
            super(buffer, transactionVersion);
        }

        private AbstractAttachment(JSONObject attachmentData) {
            super(attachmentData);
        }

        private AbstractAttachment(int version) {
            super(version);
        }

        private AbstractAttachment() {}

        @Override
        final String getAppendixName() {
            return getTransactionType().getName();
        }

        @Override
        final void validate(Transaction transaction) throws NxtException.ValidationException {
            getTransactionType().validateAttachment(transaction);
        }

        @Override
        final void apply(Transaction transaction, Account senderAccount, Account recipientAccount) {
            getTransactionType().apply((TransactionImpl) transaction, senderAccount, recipientAccount);
        }

        @Override
        public final Fee getBaselineFee(Transaction transaction) {
            return getTransactionType().getBaselineFee(transaction);
        }

        @Override
        public final Fee getNextFee(Transaction transaction) {
            return getTransactionType().getNextFee(transaction);
        }

        @Override
        public final int getBaselineFeeHeight() {
            return getTransactionType().getBaselineFeeHeight();
        }

        @Override
        public final int getNextFeeHeight() {
            return getTransactionType().getNextFeeHeight();
        }

        @Override
        final boolean isPhasable() {
            return !(this instanceof Prunable) && getTransactionType().isPhasable();
        }

        final int getFinishValidationHeight(Transaction transaction) {
            return isPhased(transaction) ? transaction.getPhasing().getFinishHeight() - 1 : Nxt.getBlockchain().getHeight();
        }

    }

    abstract class EmptyAttachment extends AbstractAttachment {

        private EmptyAttachment() {
            super(0);
        }

        @Override
        final int getMySize() {
            return 0;
        }

        @Override
        final void putMyBytes(ByteBuffer buffer) {
        }

        @Override
        final void putMyJSON(JSONObject json) {
        }

        @Override
        final boolean verifyVersion(byte transactionVersion) {
            return getVersion() == 0;
        }

    }

    EmptyAttachment ORDINARY_PAYMENT = new EmptyAttachment() {

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.Payment.ORDINARY;
        }

    };

    // the message payload is in the Appendix
    EmptyAttachment ARBITRARY_MESSAGE = new EmptyAttachment() {

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.Messaging.ARBITRARY_MESSAGE;
        }

    };

    final class MessagingPollCreation extends AbstractAttachment {

        public final static class PollBuilder {
            private final String pollName;
            private final String pollDescription;
            private final String[] pollOptions;

            private final int finishHeight;
            private final byte votingModel;

            private long minBalance = 0;
            private byte minBalanceModel;

            private final byte minNumberOfOptions;
            private final byte maxNumberOfOptions;

            private final byte minRangeValue;
            private final byte maxRangeValue;

            private long holdingId;

            public PollBuilder(final String pollName, final String pollDescription, final String[] pollOptions,
                               final int finishHeight, final byte votingModel,
                               byte minNumberOfOptions, byte maxNumberOfOptions,
                               byte minRangeValue, byte maxRangeValue) {
                this.pollName = pollName;
                this.pollDescription = pollDescription;
                this.pollOptions = pollOptions;

                this.finishHeight = finishHeight;
                this.votingModel = votingModel;
                this.minNumberOfOptions = minNumberOfOptions;
                this.maxNumberOfOptions = maxNumberOfOptions;
                this.minRangeValue = minRangeValue;
                this.maxRangeValue = maxRangeValue;

                this.minBalanceModel = VoteWeighting.VotingModel.get(votingModel).getMinBalanceModel().getCode();
            }

            public PollBuilder minBalance(byte minBalanceModel, long minBalance) {
                this.minBalanceModel = minBalanceModel;
                this.minBalance = minBalance;
                return this;
            }

            public PollBuilder holdingId(long holdingId) {
                this.holdingId = holdingId;
                return this;
            }

            public MessagingPollCreation build() {
                return new MessagingPollCreation(this);
            }
        }

        private final String pollName;
        private final String pollDescription;
        private final String[] pollOptions;

        private final int finishHeight;

        private final byte minNumberOfOptions;
        private final byte maxNumberOfOptions;
        private final byte minRangeValue;
        private final byte maxRangeValue;
        private final VoteWeighting voteWeighting;

        MessagingPollCreation(ByteBuffer buffer, byte transactionVersion) throws NxtException.NotValidException {
            super(buffer, transactionVersion);
            this.pollName = Convert.readString(buffer, buffer.getShort(), Constants.MAX_POLL_NAME_LENGTH);
            this.pollDescription = Convert.readString(buffer, buffer.getShort(), Constants.MAX_POLL_DESCRIPTION_LENGTH);

            this.finishHeight = buffer.getInt();

            int numberOfOptions = buffer.get();
            if (numberOfOptions > Constants.MAX_POLL_OPTION_COUNT) {
                throw new NxtException.NotValidException("Invalid number of poll options: " + numberOfOptions);
            }

            this.pollOptions = new String[numberOfOptions];
            for (int i = 0; i < numberOfOptions; i++) {
                this.pollOptions[i] = Convert.readString(buffer, buffer.getShort(), Constants.MAX_POLL_OPTION_LENGTH);
            }

            byte votingModel = buffer.get();

            this.minNumberOfOptions = buffer.get();
            this.maxNumberOfOptions = buffer.get();

            this.minRangeValue = buffer.get();
            this.maxRangeValue = buffer.get();

            long minBalance = buffer.getLong();
            byte minBalanceModel = buffer.get();
            long holdingId = buffer.getLong();
            this.voteWeighting = new VoteWeighting(votingModel, holdingId, minBalance, minBalanceModel);
        }

        MessagingPollCreation(JSONObject attachmentData) {
            super(attachmentData);

            this.pollName = ((String) attachmentData.get("name")).trim();
            this.pollDescription = ((String) attachmentData.get("description")).trim();
            this.finishHeight = ((Long) attachmentData.get("finishHeight")).intValue();

            JSONArray options = (JSONArray) attachmentData.get("options");
            this.pollOptions = new String[options.size()];
            for (int i = 0; i < pollOptions.length; i++) {
                this.pollOptions[i] = ((String) options.get(i)).trim();
            }
            byte votingModel = ((Long) attachmentData.get("votingModel")).byteValue();

            this.minNumberOfOptions = ((Long) attachmentData.get("minNumberOfOptions")).byteValue();
            this.maxNumberOfOptions = ((Long) attachmentData.get("maxNumberOfOptions")).byteValue();
            this.minRangeValue = ((Long) attachmentData.get("minRangeValue")).byteValue();
            this.maxRangeValue = ((Long) attachmentData.get("maxRangeValue")).byteValue();

            long minBalance = Convert.parseLong(attachmentData.get("minBalance"));
            byte minBalanceModel = ((Long) attachmentData.get("minBalanceModel")).byteValue();
            long holdingId = Convert.parseUnsignedLong((String) attachmentData.get("holding"));
            this.voteWeighting = new VoteWeighting(votingModel, holdingId, minBalance, minBalanceModel);
        }

        private MessagingPollCreation(PollBuilder builder) {
            this.pollName = builder.pollName;
            this.pollDescription = builder.pollDescription;
            this.pollOptions = builder.pollOptions;
            this.finishHeight = builder.finishHeight;
            this.minNumberOfOptions = builder.minNumberOfOptions;
            this.maxNumberOfOptions = builder.maxNumberOfOptions;
            this.minRangeValue = builder.minRangeValue;
            this.maxRangeValue = builder.maxRangeValue;
            this.voteWeighting = new VoteWeighting(builder.votingModel, builder.holdingId, builder.minBalance, builder.minBalanceModel);
        }

        @Override
        int getMySize() {
            int size = 2 + Convert.toBytes(pollName).length + 2 + Convert.toBytes(pollDescription).length + 1;
            for (String pollOption : pollOptions) {
                size += 2 + Convert.toBytes(pollOption).length;
            }

            size += 4 + 1 + 1 + 1 + 1 + 1 + 8 + 1 + 8;

            return size;
        }

        @Override
        void putMyBytes(ByteBuffer buffer) {
            byte[] name = Convert.toBytes(this.pollName);
            byte[] description = Convert.toBytes(this.pollDescription);
            byte[][] options = new byte[this.pollOptions.length][];
            for (int i = 0; i < this.pollOptions.length; i++) {
                options[i] = Convert.toBytes(this.pollOptions[i]);
            }

            buffer.putShort((short) name.length);
            buffer.put(name);
            buffer.putShort((short) description.length);
            buffer.put(description);
            buffer.putInt(finishHeight);
            buffer.put((byte) options.length);
            for (byte[] option : options) {
                buffer.putShort((short) option.length);
                buffer.put(option);
            }
            buffer.put(this.voteWeighting.getVotingModel().getCode());

            buffer.put(this.minNumberOfOptions);
            buffer.put(this.maxNumberOfOptions);
            buffer.put(this.minRangeValue);
            buffer.put(this.maxRangeValue);

            buffer.putLong(this.voteWeighting.getMinBalance());
            buffer.put(this.voteWeighting.getMinBalanceModel().getCode());
            buffer.putLong(this.voteWeighting.getHoldingId());
        }

        @Override
        void putMyJSON(JSONObject attachment) {
            attachment.put("name", this.pollName);
            attachment.put("description", this.pollDescription);
            attachment.put("finishHeight", this.finishHeight);
            JSONArray options = new JSONArray();
            if (this.pollOptions != null) {
                Collections.addAll(options, this.pollOptions);
            }
            attachment.put("options", options);


            attachment.put("minNumberOfOptions", this.minNumberOfOptions);
            attachment.put("maxNumberOfOptions", this.maxNumberOfOptions);

            attachment.put("minRangeValue", this.minRangeValue);
            attachment.put("maxRangeValue", this.maxRangeValue);

            attachment.put("votingModel", this.voteWeighting.getVotingModel().getCode());

            attachment.put("minBalance", this.voteWeighting.getMinBalance());
            attachment.put("minBalanceModel", this.voteWeighting.getMinBalanceModel().getCode());
            attachment.put("holding", Long.toUnsignedString(this.voteWeighting.getHoldingId()));
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.Messaging.POLL_CREATION;
        }

        public String getPollName() {
            return pollName;
        }

        public String getPollDescription() {
            return pollDescription;
        }

        public int getFinishHeight() {
            return finishHeight;
        }

        public String[] getPollOptions() {
            return pollOptions;
        }

        public byte getMinNumberOfOptions() {
            return minNumberOfOptions;
        }

        public byte getMaxNumberOfOptions() {
            return maxNumberOfOptions;
        }

        public byte getMinRangeValue() {
            return minRangeValue;
        }

        public byte getMaxRangeValue() {
            return maxRangeValue;
        }

        public VoteWeighting getVoteWeighting() {
            return voteWeighting;
        }

    }


    final class MessagingHubAnnouncement extends AbstractAttachment {

        private final long minFeePerByteNQT;
        private final String[] uris;

        MessagingHubAnnouncement(ByteBuffer buffer, byte transactionVersion) throws NxtException.NotValidException {
            super(buffer, transactionVersion);
            this.minFeePerByteNQT = buffer.getLong();
            int numberOfUris = buffer.get();
            if (numberOfUris > Constants.MAX_HUB_ANNOUNCEMENT_URIS) {
                throw new NxtException.NotValidException("Invalid number of URIs: " + numberOfUris);
            }
            this.uris = new String[numberOfUris];
            for (int i = 0; i < uris.length; i++) {
                uris[i] = Convert.readString(buffer, buffer.getShort(), Constants.MAX_HUB_ANNOUNCEMENT_URI_LENGTH);
            }
        }

        MessagingHubAnnouncement(JSONObject attachmentData) throws NxtException.NotValidException {
            super(attachmentData);
            this.minFeePerByteNQT = (Long) attachmentData.get("minFeePerByte");
            try {
                JSONArray urisData = (JSONArray) attachmentData.get("uris");
                this.uris = new String[urisData.size()];
                for (int i = 0; i < uris.length; i++) {
                    uris[i] = (String) urisData.get(i);
                }
            } catch (RuntimeException e) {
                throw new NxtException.NotValidException("Error parsing hub terminal announcement parameters", e);
            }
        }

        public MessagingHubAnnouncement(long minFeePerByteNQT, String[] uris) {
            this.minFeePerByteNQT = minFeePerByteNQT;
            this.uris = uris;
        }

        @Override
        int getMySize() {
            int size = 8 + 1;
            for (String uri : uris) {
                size += 2 + Convert.toBytes(uri).length;
            }
            return size;
        }

        @Override
        void putMyBytes(ByteBuffer buffer) {
            buffer.putLong(minFeePerByteNQT);
            buffer.put((byte) uris.length);
            for (String uri : uris) {
                byte[] uriBytes = Convert.toBytes(uri);
                buffer.putShort((short)uriBytes.length);
                buffer.put(uriBytes);
            }
        }

        @Override
        void putMyJSON(JSONObject attachment) {
            attachment.put("minFeePerByteNQT", minFeePerByteNQT);
            JSONArray uris = new JSONArray();
            Collections.addAll(uris, this.uris);
            attachment.put("uris", uris);
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.Messaging.HUB_ANNOUNCEMENT;
        }

        public long getMinFeePerByteNQT() {
            return minFeePerByteNQT;
        }

        public String[] getUris() {
            return uris;
        }

    }

    final class MessagingAccountInfo extends AbstractAttachment {
        public static final StringRw NAME_RW = new StringRw(BYTE, Constants.MAX_ACCOUNT_NAME_LENGTH);
        public static final StringRw DESCRIPTION_RW = new StringRw(SHORT, Constants.MAX_ACCOUNT_DESCRIPTION_LENGTH);

        private final String name;
        private final String description;

        MessagingAccountInfo(ByteBuffer buffer, byte transactionVersion) throws NxtException.NotValidException {
            super(buffer, transactionVersion);
            this.name = NAME_RW.readFromBuffer(buffer);
            this.description = DESCRIPTION_RW.readFromBuffer(buffer);
        }

        MessagingAccountInfo(JSONObject attachmentData) {
            super(attachmentData);
            this.name = Convert.nullToEmpty((String) attachmentData.get("name"));
            this.description = Convert.nullToEmpty((String) attachmentData.get("description"));
        }

        public MessagingAccountInfo(String name, String description) {
            this.name = name;
            this.description = description;
        }

        @Override
        int getMySize() {
            return 1 + Convert.toBytes(name).length + 2 + Convert.toBytes(description).length;
        }

        @Override
        void putMyBytes(ByteBuffer buffer) {
            byte[] name = Convert.toBytes(this.name);
            byte[] description = Convert.toBytes(this.description);
            buffer.put((byte)name.length);
            buffer.put(name);
            buffer.putShort((short) description.length);
            buffer.put(description);
        }

        @Override
        void putMyJSON(JSONObject attachment) {
            attachment.put("name", name);
            attachment.put("description", description);
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.Messaging.ACCOUNT_INFO;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

    }

    final class MessagingAccountProperty extends AbstractAttachment {
        public static final StringRw PROPERTY_NAME_RW = new StringRw(BYTE, Constants.MAX_ACCOUNT_PROPERTY_NAME_LENGTH);
        public static final StringRw PROPERTY_VALUE_RW = new StringRw(BYTE, Constants.MAX_ACCOUNT_PROPERTY_VALUE_LENGTH);

        private final String property;
        private final String value;

        MessagingAccountProperty(ByteBuffer buffer, byte transactionVersion) throws NxtException.NotValidException {
            super(buffer, transactionVersion);
            this.property = PROPERTY_NAME_RW.readFromBuffer(buffer).trim();
            this.value = PROPERTY_VALUE_RW.readFromBuffer(buffer).trim();
        }

        MessagingAccountProperty(JSONObject attachmentData) {
            super(attachmentData);
            this.property = Convert.nullToEmpty((String) attachmentData.get("property")).trim();
            this.value = Convert.nullToEmpty((String) attachmentData.get("value")).trim();
        }

        public MessagingAccountProperty(String property, String value) {
            this.property = property.trim();
            this.value = Convert.nullToEmpty(value).trim();
        }

        @Override
        int getMySize() {
            return 1 + Convert.toBytes(property).length + 1 + Convert.toBytes(value).length;
        }

        @Override
        void putMyBytes(ByteBuffer buffer) {
            byte[] property = Convert.toBytes(this.property);
            byte[] value = Convert.toBytes(this.value);
            buffer.put((byte)property.length);
            buffer.put(property);
            buffer.put((byte)value.length);
            buffer.put(value);
        }

        @Override
        void putMyJSON(JSONObject attachment) {
            attachment.put("property", property);
            attachment.put("value", value);
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.Messaging.ACCOUNT_PROPERTY;
        }

        public String getProperty() {
            return property;
        }

        public String getValue() {
            return value;
        }

    }

    final class MessagingAccountPropertyDelete extends AbstractAttachment {

        private final long propertyId;

        MessagingAccountPropertyDelete(ByteBuffer buffer, byte transactionVersion) {
            super(buffer, transactionVersion);
            this.propertyId = buffer.getLong();
        }

        MessagingAccountPropertyDelete(JSONObject attachmentData) {
            super(attachmentData);
            this.propertyId = Convert.parseUnsignedLong((String)attachmentData.get("property"));
        }

        public MessagingAccountPropertyDelete(long propertyId) {
            this.propertyId = propertyId;
        }

        @Override
        int getMySize() {
            return 8;
        }

        @Override
        void putMyBytes(ByteBuffer buffer) {
            buffer.putLong(propertyId);
        }

        @Override
        void putMyJSON(JSONObject attachment) {
            attachment.put("property", Long.toUnsignedString(propertyId));
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.Messaging.ACCOUNT_PROPERTY_DELETE;
        }

        public long getPropertyId() {
            return propertyId;
        }

    }

    final class AccountControlEffectiveBalanceLeasing extends AbstractAttachment {

        private final int period;

        AccountControlEffectiveBalanceLeasing(ByteBuffer buffer, byte transactionVersion) {
            super(buffer, transactionVersion);
            this.period = Short.toUnsignedInt(buffer.getShort());
        }

        AccountControlEffectiveBalanceLeasing(JSONObject attachmentData) {
            super(attachmentData);
            this.period = ((Long) attachmentData.get("period")).intValue();
        }

        public AccountControlEffectiveBalanceLeasing(int period) {
            this.period = period;
        }

        @Override
        int getMySize() {
            return 2;
        }

        @Override
        void putMyBytes(ByteBuffer buffer) {
            buffer.putShort((short)period);
        }

        @Override
        void putMyJSON(JSONObject attachment) {
            attachment.put("period", period);
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.AccountControl.EFFECTIVE_BALANCE_LEASING;
        }

        public int getPeriod() {
            return period;
        }
    }

    interface MonetarySystemAttachment {

        long getCurrencyId();

    }

    interface ShufflingAttachment extends Attachment {

        long getShufflingId();

        byte[] getShufflingStateHash();

    }

    abstract class AbstractShufflingAttachment extends AbstractAttachment implements ShufflingAttachment {

        private final long shufflingId;
        private final byte[] shufflingStateHash;

        private AbstractShufflingAttachment(ByteBuffer buffer, byte transactionVersion) {
            super(buffer, transactionVersion);
            this.shufflingId = buffer.getLong();
            this.shufflingStateHash = new byte[32];
            buffer.get(this.shufflingStateHash);
        }

        private AbstractShufflingAttachment(JSONObject attachmentData) {
            super(attachmentData);
            this.shufflingId = Convert.parseUnsignedLong((String) attachmentData.get("shuffling"));
            this.shufflingStateHash = Convert.parseHexString((String) attachmentData.get("shufflingStateHash"));
        }

        private AbstractShufflingAttachment(long shufflingId, byte[] shufflingStateHash) {
            this.shufflingId = shufflingId;
            this.shufflingStateHash = shufflingStateHash;
        }

        @Override
        int getMySize() {
            return 8 + 32;
        }

        @Override
        void putMyBytes(ByteBuffer buffer) {
            buffer.putLong(shufflingId);
            buffer.put(shufflingStateHash);
        }

        @Override
        void putMyJSON(JSONObject attachment) {
            attachment.put("shuffling", Long.toUnsignedString(shufflingId));
            attachment.put("shufflingStateHash", Convert.toHexString(shufflingStateHash));
        }

        @Override
        public final long getShufflingId() {
            return shufflingId;
        }

        @Override
        public final byte[] getShufflingStateHash() {
            return shufflingStateHash;
        }

    }



    abstract class TaggedDataAttachment extends AbstractAttachment implements Prunable {

        private final String name;
        private final String description;
        private final String tags;
        private final String type;
        private final String channel;
        private final boolean isText;
        private final String filename;
        private final byte[] data;
        private volatile TaggedData taggedData;

        private TaggedDataAttachment(ByteBuffer buffer, byte transactionVersion) {
            super(buffer, transactionVersion);
            this.name = null;
            this.description = null;
            this.tags = null;
            this.type = null;
            this.channel = null;
            this.isText = false;
            this.filename = null;
            this.data = null;
        }

        private TaggedDataAttachment(JSONObject attachmentData) {
            super(attachmentData);
            String dataJSON = (String) attachmentData.get("data");
            if (dataJSON != null) {
                this.name = (String) attachmentData.get("name");
                this.description = (String) attachmentData.get("description");
                this.tags = (String) attachmentData.get("tags");
                this.type = (String) attachmentData.get("type");
                this.channel = Convert.nullToEmpty((String) attachmentData.get("channel"));
                this.isText = Boolean.TRUE.equals(attachmentData.get("isText"));
                this.data = isText ? Convert.toBytes(dataJSON) : Convert.parseHexString(dataJSON);
                this.filename = (String) attachmentData.get("filename");
            } else {
                this.name = null;
                this.description = null;
                this.tags = null;
                this.type = null;
                this.channel = null;
                this.isText = false;
                this.filename = null;
                this.data = null;
            }

        }

        private TaggedDataAttachment(String name, String description, String tags, String type, String channel, boolean isText, String filename, byte[] data) {
            this.name = name;
            this.description = description;
            this.tags = tags;
            this.type = type;
            this.channel = channel;
            this.isText = isText;
            this.data = data;
            this.filename = filename;
        }

        @Override
        final int getMyFullSize() {
            if (getData() == null) {
                return 0;
            }
            return Convert.toBytes(getName()).length + Convert.toBytes(getDescription()).length + Convert.toBytes(getType()).length
                    + Convert.toBytes(getChannel()).length + Convert.toBytes(getTags()).length + Convert.toBytes(getFilename()).length + getData().length;
        }

        @Override
        void putMyJSON(JSONObject attachment) {
            if (taggedData != null) {
                attachment.put("name", taggedData.getName());
                attachment.put("description", taggedData.getDescription());
                attachment.put("tags", taggedData.getTags());
                attachment.put("type", taggedData.getType());
                attachment.put("channel", taggedData.getChannel());
                attachment.put("isText", taggedData.isText());
                attachment.put("filename", taggedData.getFilename());
                attachment.put("data", taggedData.isText() ? Convert.toString(taggedData.getData()) : Convert.toHexString(taggedData.getData()));
            } else if (data != null) {
                attachment.put("name", name);
                attachment.put("description", description);
                attachment.put("tags", tags);
                attachment.put("type", type);
                attachment.put("channel", channel);
                attachment.put("isText", isText);
                attachment.put("filename", filename);
                attachment.put("data", isText ? Convert.toString(data) : Convert.toHexString(data));
            }
        }

        @Override
        public byte[] getHash() {
            if (data == null) {
                return null;
            }
            MessageDigest digest = Crypto.sha256();
            digest.update(Convert.toBytes(name));
            digest.update(Convert.toBytes(description));
            digest.update(Convert.toBytes(tags));
            digest.update(Convert.toBytes(type));
            digest.update(Convert.toBytes(channel));
            digest.update((byte)(isText ? 1 : 0));
            digest.update(Convert.toBytes(filename));
            digest.update(data);
            return digest.digest();
        }

        public final String getName() {
            if (taggedData != null) {
                return taggedData.getName();
            }
            return name;
        }

        public final String getDescription() {
            if (taggedData != null) {
                return taggedData.getDescription();
            }
            return description;
        }

        public final String getTags() {
            if (taggedData != null) {
                return taggedData.getTags();
            }
            return tags;
        }

        public final String getType() {
            if (taggedData != null) {
                return taggedData.getType();
            }
            return type;
        }

        public final String getChannel() {
            if (taggedData != null) {
                return taggedData.getChannel();
            }
            return channel;
        }

        public final boolean isText() {
            if (taggedData != null) {
                return taggedData.isText();
            }
            return isText;
        }

        public final String getFilename() {
            if (taggedData != null) {
                return taggedData.getFilename();
            }
            return filename;
        }

        public final byte[] getData() {
            if (taggedData != null) {
                return taggedData.getData();
            }
            return data;
        }

        @Override
        void loadPrunable(Transaction transaction, boolean includeExpiredPrunable) {
            if (data == null && taggedData == null && shouldLoadPrunable(transaction, includeExpiredPrunable)) {
                taggedData = TaggedData.getData(getTaggedDataId(transaction));
            }
        }

        @Override
        public boolean hasPrunableData() {
            return (taggedData != null || data != null);
        }

        abstract long getTaggedDataId(Transaction transaction);

    }

    final class TaggedDataUpload extends TaggedDataAttachment {

        static TaggedDataUpload parse(JSONObject attachmentData) {
            if (!Appendix.hasAppendix(TransactionType.Data.TAGGED_DATA_UPLOAD.getName(), attachmentData)) {
                return null;
            }
            return new TaggedDataUpload(attachmentData);
        }

        private final byte[] hash;

        TaggedDataUpload(ByteBuffer buffer, byte transactionVersion) {
            super(buffer, transactionVersion);
            this.hash = new byte[32];
            buffer.get(hash);
        }

        TaggedDataUpload(JSONObject attachmentData) {
            super(attachmentData);
            String dataJSON = (String) attachmentData.get("data");
            if (dataJSON == null) {
                this.hash = Convert.parseHexString(Convert.emptyToNull((String)attachmentData.get("hash")));
            } else {
                this.hash = null;
            }
        }

        public TaggedDataUpload(String name, String description, String tags, String type, String channel, boolean isText,
                                String filename, byte[] data) throws NxtException.NotValidException {
            super(name, description, tags, type, channel, isText, filename, data);
            this.hash = null;
            if (isText && !Arrays.equals(data, Convert.toBytes(Convert.toString(data)))) {
                throw new NxtException.NotValidException("Data is not UTF-8 text");
            }
        }

        @Override
        int getMySize() {
            return 32;
        }

        @Override
        void putMyBytes(ByteBuffer buffer) {
            buffer.put(getHash());
        }

        @Override
        void putMyJSON(JSONObject attachment) {
            super.putMyJSON(attachment);
            attachment.put("hash", Convert.toHexString(getHash()));
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.Data.TAGGED_DATA_UPLOAD;
        }

        @Override
        public byte[] getHash() {
            if (hash != null) {
                return hash;
            }
            return super.getHash();
        }

        @Override
        long getTaggedDataId(Transaction transaction) {
            return transaction.getId();
        }

        @Override
        public void restorePrunableData(Transaction transaction, int blockTimestamp, int height) {
            TaggedData.restore(transaction, this, blockTimestamp, height);
        }

    }

    final class TaggedDataExtend extends TaggedDataAttachment {

        static TaggedDataExtend parse(JSONObject attachmentData) {
            if (!Appendix.hasAppendix(TransactionType.Data.TAGGED_DATA_EXTEND.getName(), attachmentData)) {
                return null;
            }
            return new TaggedDataExtend(attachmentData);
        }

        private volatile byte[] hash;
        private final long taggedDataId;
        private final boolean jsonIsPruned;

        TaggedDataExtend(ByteBuffer buffer, byte transactionVersion) {
            super(buffer, transactionVersion);
            this.taggedDataId = buffer.getLong();
            this.jsonIsPruned = false;
        }

        TaggedDataExtend(JSONObject attachmentData) {
            super(attachmentData);
            this.taggedDataId = Convert.parseUnsignedLong((String)attachmentData.get("taggedData"));
            this.jsonIsPruned = attachmentData.get("data") == null;
        }

        public TaggedDataExtend(TaggedData taggedData) {
            super(taggedData.getName(), taggedData.getDescription(), taggedData.getTags(), taggedData.getType(),
                    taggedData.getChannel(), taggedData.isText(), taggedData.getFilename(), taggedData.getData());
            this.taggedDataId = taggedData.getId();
            this.jsonIsPruned = false;
        }

        @Override
        int getMySize() {
            return 8;
        }

        @Override
        void putMyBytes(ByteBuffer buffer) {
            buffer.putLong(taggedDataId);
        }

        @Override
        void putMyJSON(JSONObject attachment) {
            super.putMyJSON(attachment);
            attachment.put("taggedData", Long.toUnsignedString(taggedDataId));
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.Data.TAGGED_DATA_EXTEND;
        }

        public long getTaggedDataId() {
            return taggedDataId;
        }

        @Override
        public byte[] getHash() {
            if (hash == null) {
                hash = super.getHash();
            }
            if (hash == null) {
                TaggedDataUpload taggedDataUpload = (TaggedDataUpload)TransactionDb.findTransaction(taggedDataId).getAttachment();
                hash = taggedDataUpload.getHash();
            }
            return hash;
        }

        @Override
        long getTaggedDataId(Transaction transaction) {
            return taggedDataId;
        }

        boolean jsonIsPruned() {
            return jsonIsPruned;
        }

        @Override
        public void restorePrunableData(Transaction transaction, int blockTimestamp, int height) {
        }

    }

    final class SetPhasingOnly extends AbstractAttachment {

        private final PhasingParams phasingParams;
        private final long maxFees;
        private final short minDuration;
        private final short maxDuration;

        public SetPhasingOnly(PhasingParams params, long maxFees, short minDuration, short maxDuration) {
            phasingParams = params;
            this.maxFees = maxFees;
            this.minDuration = minDuration;
            this.maxDuration = maxDuration;
        }

        SetPhasingOnly(ByteBuffer buffer, byte transactionVersion) {
            super(buffer, transactionVersion);
            phasingParams = new PhasingParams(buffer);
            maxFees = buffer.getLong();
            minDuration = buffer.getShort();
            maxDuration = buffer.getShort();
        }

        SetPhasingOnly(JSONObject attachmentData) {
            super(attachmentData);
            JSONObject phasingControlParams = (JSONObject) attachmentData.get("phasingControlParams");
            phasingParams = new PhasingParams(phasingControlParams);
            maxFees = Convert.parseLong(attachmentData.get("controlMaxFees"));
            minDuration = ((Long)attachmentData.get("controlMinDuration")).shortValue();
            maxDuration = ((Long)attachmentData.get("controlMaxDuration")).shortValue();
        }

        @Override
        public TransactionType getTransactionType() {
            return TransactionType.AccountControl.SET_PHASING_ONLY;
        }

        @Override
        int getMySize() {
            return phasingParams.getMySize() + 8 + 2 + 2;
        }

        @Override
        void putMyBytes(ByteBuffer buffer) {
            phasingParams.putMyBytes(buffer);
            buffer.putLong(maxFees);
            buffer.putShort(minDuration);
            buffer.putShort(maxDuration);
        }

        @Override
        void putMyJSON(JSONObject json) {
            JSONObject phasingControlParams = new JSONObject();
            phasingParams.putMyJSON(phasingControlParams);
            json.put("phasingControlParams", phasingControlParams);
            json.put("controlMaxFees", maxFees);
            json.put("controlMinDuration", minDuration);
            json.put("controlMaxDuration", maxDuration);
        }

        public PhasingParams getPhasingParams() {
            return phasingParams;
        }

        public long getMaxFees() {
            return maxFees;
        }

        public short getMinDuration() {
            return minDuration;
        }

        public short getMaxDuration() {
            return maxDuration;
        }

    }
}
