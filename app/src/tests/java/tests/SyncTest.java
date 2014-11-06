package tests;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.SharedPreferences;
import android.test.ActivityInstrumentationTestCase2;
import android.webkit.WebView;

import com.mendeleypaperreader.R;
import com.mendeleypaperreader.activities.MainActivity;
import com.robotium.solo.By;
import com.robotium.solo.Solo;

/**
 * Created by pedro on 03/11/14.
 */
public class SyncTest extends ActivityInstrumentationTestCase2<MainActivity> {

    private Solo solo;
    private Activity activity;


    public SyncTest() {
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




    public void testAllDocs() throws Exception {

        solo.clickOnView(solo.getCurrentActivity().findViewById(R.id.auth));

        WebView view = (WebView) solo.getView(R.id.webview);

        solo.waitForView(view, 2000, false);
        solo.enterTextInWebElement(By.id("username"), "pdrolourenco@gmail.com");
        solo.enterTextInWebElement(By.id("password"), "000000");
        solo.clickOnText("Authorize");
        solo.waitForActivity("MainMenuActivity", 20000);
        solo.getActivityMonitor();
        solo.waitForText("Sync data... (100%)", 1, 9999);
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