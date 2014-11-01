package tests;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Created by pedro on 01/11/14.
 */
public class AllTests extends  ActivityInstrumentationTestCase2<Activity> {



    public AllTests(Class<Activity> activityClass) {
        super(activityClass);
    }

    public static TestSuite suite() {
        TestSuite t = new TestSuite();
        t.addTestSuite(LoginTest.class);

        return t;
    }

    @Override
    public void setUp() throws Exception {

    }


    @Override
    public void tearDown() throws Exception {
    }

}