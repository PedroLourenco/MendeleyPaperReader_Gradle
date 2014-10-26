package tests;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import com.mendeleypaperreader.MainActivity;
import com.mendeleypaperreader.MainMenuActivity;
import com.robotium.solo.Solo;
import com.mendeleypaperreader.R;
import com.robotium.solo.WebElement;
import android.webkit.WebView;
import com.robotium.solo.By;


public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity>{

    private Solo solo;
    private Activity activity;


    public MainActivityTest() {
        super(MainActivity.class);

    }


    @Override
    public void setUp() throws Exception {
        //setUp() is run before a test case is started.
        //This is where the solo object is created.
        activity = this.getActivity();
        solo = new Solo(getInstrumentation(), this.getActivity());
    }



    public void testFail() throws Exception {
            final int expected = 5;
            final int reality = 5;
            assertEquals(expected, reality);
        }



    public void testLogin() throws Exception {

        solo.clickOnView(solo.getCurrentActivity().findViewById(R.id.auth));

        WebView view = (WebView) solo.getView(R.id.webview);

        solo.waitForView(view, 2000, false);
        solo.clickOnWebElement(By.id("username"));
        solo.enterTextInWebElement(By.id("username"), "pdrolourenco@gmail.com");
        solo.clickOnWebElement(By.id("password"));
        solo.enterTextInWebElement(By.id("password"), "000000");
        solo.clickOnText("Authorize");

        assertTrue(solo.waitForActivity("com.medeleypaperreader.MainMenuActivity", 2000));







    }

}