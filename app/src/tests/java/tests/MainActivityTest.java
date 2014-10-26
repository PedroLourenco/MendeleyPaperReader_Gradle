package tests;

import com.mendeleypaperreader.MainActivity;
import android.test.ActivityInstrumentationTestCase2;

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity>{


    public MainActivityTest() {
        super(MainActivity.class);

    }


    public void testFail() throws Exception {
            final int expected = 1;
            final int reality = 5;
            assertEquals(expected, reality);
        }

    }