package neo.rpc.client.test.util;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONObject;

import neo.model.bytes.Fixed8;
import neo.model.bytes.UInt160;
import neo.model.bytes.UInt256;
import neo.model.core.Block;
import neo.model.core.Transaction;
import neo.model.core.TransactionOutput;
import neo.model.db.BlockDb;
import neo.model.util.ModelUtil;

/**
 * the mock block database.
 *
 * @author coranos
 *
 */
public abstract class AbstractJsonMockBlockDb implements BlockDb {

	/**
	 * the hash JSON key.
	 */
	private static final String HASH = "hash";

	/**
	 * the block JSON key.
	 */
	private static final String BLOCK = "block";

	/**
	 * the index JSON key.
	 */
	private static final String INDEX = "index";

	/**
	 * return the Block in the given mock block.
	 *
	 * @param mockBlock
	 *            the mock block to use.
	 * @return the Block in the given mock block.
	 */
	private static Block getBlock(final JSONObject mockBlock) {
		final String blockHex = mockBlock.getString(BLOCK);
		final Block block = new Block(ByteBuffer.wrap(ModelUtil.decodeHex(blockHex)));
		return block;
	}

	/**
	 * the constructor.
	 */
	public AbstractJsonMockBlockDb() {
	}

	@Override
	public final void close() {
	}

	@Override
	public final boolean containsBlockWithHash(final UInt256 hash) {
		final String hashHex = hash.toHexString();
		final JSONArray mockBlockDb = getMockBlockDb();
		for (int ix = 0; ix < mockBlockDb.length(); ix++) {
			final JSONObject mockBlock = mockBlockDb.getJSONObject(ix);
			if (mockBlock.getString(HASH).equals(hashHex)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public final Map<UInt160, Map<UInt256, Fixed8>> getAccountAssetValueMap() {
		final Map<UInt160, Map<UInt256, Fixed8>> accountAssetValueMap = new TreeMap<>();
		final JSONArray mockBlockDb = getMockBlockDb();
		for (int ix = 0; ix < mockBlockDb.length(); ix++) {
			final JSONObject mockBlock = mockBlockDb.getJSONObject(ix);
			final Block block = getBlock(mockBlock);
			for (final Transaction transaction : block.getTransactionList()) {
				for (final TransactionOutput output : transaction.outputs) {
					if (!accountAssetValueMap.containsKey(output.scriptHash)) {
						accountAssetValueMap.put(output.scriptHash, new TreeMap<>());
					}
					final Map<UInt256, Fixed8> assetValueMap = accountAssetValueMap.get(output.scriptHash);
					if (assetValueMap.containsKey(output.assetId)) {
						final Fixed8 oldValue = assetValueMap.get(output.assetId);
						final BigInteger oldBi = new BigInteger(1, oldValue.toByteArray());
						final BigInteger valBi = new BigInteger(1, output.value.toByteArray());
						final BigInteger newBi = oldBi.add(valBi);
						final Fixed8 newValue = new Fixed8(ByteBuffer.wrap(newBi.toByteArray()));
						assetValueMap.put(output.assetId, newValue);
					} else {
						assetValueMap.put(output.assetId, output.value);
					}
				}
			}
		}
		return accountAssetValueMap;
	}

	@Override
	public final Block getBlock(final long blockHeight) {
		final JSONArray mockBlockDb = getMockBlockDb();
		for (int ix = 0; ix < mockBlockDb.length(); ix++) {
			final JSONObject mockBlock = mockBlockDb.getJSONObject(ix);
			if (mockBlock.getLong(INDEX) == blockHeight) {
				final Block block = getBlock(mockBlock);
				return block;
			}
		}
		throw new RuntimeException("no block at height:" + blockHeight);
	}

	@Override
	public final Block getBlock(final UInt256 hash) {
		final String hashHex = hash.toHexString();
		final JSONArray mockBlockDb = getMockBlockDb();
		for (int ix = 0; ix < mockBlockDb.length(); ix++) {
			final JSONObject mockBlock = mockBlockDb.getJSONObject(ix);
			if (mockBlock.getString(HASH).equals(hashHex)) {
				final Block block = getBlock(mockBlock);
				return block;
			}
		}
		throw new RuntimeException("no block at hash:" + hash);
	}

	@Override
	public final long getBlockCount() {
		return getMockBlockDb().length();
	}

	@Override
	public final Block getBlockWithMaxIndex() {
		Block maxBlock = null;
		final JSONArray mockBlockDb = getMockBlockDb();
		for (int ix = 0; ix < mockBlockDb.length(); ix++) {
			final JSONObject mockBlock = mockBlockDb.getJSONObject(ix);
			if (maxBlock == null) {
				maxBlock = getBlock(mockBlock);
			} else {
				if (mockBlock.getLong(INDEX) > maxBlock.getIndexAsLong()) {
					maxBlock = getBlock(mockBlock);
				}
			}
		}
		return maxBlock;
	}

	@Override
	public final long getFileSize() {
		return 0;
	}

	/**
	 * return the mock block database.
	 *
	 * @return the mock block database.
	 */
	public abstract JSONArray getMockBlockDb();

	@Override
	public final Transaction getTransactionWithHash(final UInt256 hash) {
		final JSONArray mockBlockDb = getMockBlockDb();
		for (int ix = 0; ix < mockBlockDb.length(); ix++) {
			final JSONObject mockBlock = mockBlockDb.getJSONObject(ix);
			final Block block = getBlock(mockBlock);
			for (final Transaction transaction : block.getTransactionList()) {
				if (transaction.hash.equals(hash)) {
					return transaction;
				}
			}
		}
		throw new RuntimeException("no transaction with hash:" + hash);
	}

	@Override
	public final void put(final Block block) {
		if (containsBlockWithHash(block.hash)) {
			return;
		}
		final JSONObject mockBlock = new JSONObject();
		mockBlock.put(HASH, block.hash.toHexString());
		mockBlock.put(INDEX, block.getIndexAsLong());
		mockBlock.put(BLOCK, ModelUtil.toHexString(block.toByteArray()));
		getMockBlockDb().put(mockBlock);
	}
}
