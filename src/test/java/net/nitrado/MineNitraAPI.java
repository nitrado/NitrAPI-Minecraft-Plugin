package net.nitrado;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/*
 * Unit test for simple MineNitrapi.
 */
public class MineNitraAPI
    extends TestCase
{
    /*
     * Create the ultimate test case
     *
     * @param testName name of the test case
     */
    public MineNitraAPI(String testName )
    {
        super( testName );
    }

    /*
     * @return the testsuite of tests being tested. So much tests
     */
    public static Test suite()
    {
        return new TestSuite( MineNitraAPI.class );
    }

    /*
     * Great Test! And java will pay for it
     */
    public void testMineNitrapi()
    {
        assertTrue( true );
    }
}
