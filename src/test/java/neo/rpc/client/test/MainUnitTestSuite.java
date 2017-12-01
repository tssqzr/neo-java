package neo.rpc.client.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * the main test suite.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
		/** */
		TestLoadSave.class,
		/** */
		TestPayload.class,
		/** */
		TestBlockSerialization.class,
		/** */
})

public class MainUnitTestSuite {
}
