package testing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * JUnit Test Suite Runner
 * Runs all JUnit test classes together
 * 
 * To run this suite:
 * 1. Make sure JUnit 4 is in your classpath
 * 2. Run: java org.junit.runner.JUnitCore testing.AllJUnitTestsRunner
 * 3. Or use your IDE's built-in test runner
 */
@RunWith(Suite.class)
@SuiteClasses({
    ModelClassesJUnitTest.class,
    StorageZoneJUnitTest.class,
    ManagersJUnitTest.class,
    ConcurrencyJUnitTest.class
})
public class AllJUnitTestsRunner {
    // This class remains empty, it's used only as a holder for the above annotations
}