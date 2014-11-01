package tests;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;

import com.mendeleypaperreader.activities.MainActivity;
import com.robotium.solo.Solo;
import com.mendeleypaperreader.R;

import android.webkit.WebView;
import com.robotium.solo.By;
import android.content.SharedPreferences;
import android.app.Instrumentation;


public class LoginTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private Solo solo;
    private Activity activity;


    public LoginTest() {
        super(MainActivity.class);

    }


    @Override
    public void setUp() throws Exception {
        //setUp() is run before a test case is started.
        //This is where the solo object is created.
        super.setUp();
        Instrumentation instrumentation = getInstrumentation();
        SharedPreferences preferences = instrumentation.getTargetContext().getSharedPreferences("MendeleyPaperReaderPREF", 0);
        preferences.edit().clear().commit();


        activity = this.getActivity();
        solo = new Solo(getInstrumentation(), this.getActivity());
    }


    public void testLogin() throws Exception {

        solo.clickOnView(solo.getCurrentActivity().findViewById(R.id.auth));

        WebView view = (WebView) solo.getView(R.id.webview);

        solo.waitForView(view, 2000, false);
        solo.enterTextInWebElement(By.id("username"), "pdrolourenco@gmail.com");
        solo.enterTextInWebElement(By.id("password"), "000000");
        solo.clickOnText("Authorize");

        assertTrue(solo.waitForActivity("MainMenuActivity", 20000));


    }


    public void testAllDocs() throws Exception {

        solo.clickOnView(solo.getCurrentActivity().findViewById(R.id.auth));

        WebView view = (WebView) solo.getView(R.id.webview);

        solo.waitForView(view, 2000, false);
        solo.enterTextInWebElement(By.id("username"), "pdrolourenco@gmail.com");
        solo.enterTextInWebElement(By.id("password"), "000000");
        solo.clickOnText("Authorize");
        solo.waitForActivity("MainMenuActivity", 2000000);
        solo.getActivityMonitor();
        solo.waitForText("Sync data... (100%)", 1, 999999);
        solo.waitForDialogToClose();

    }




    // Our tearDown...
    public void tearDown() throws Exception {
        try {
            solo.finalize();
        } catch (Throwable e) {
            e.printStackTrace();
            getActivity().finish();
            super.tearDown();
        }


    }

}