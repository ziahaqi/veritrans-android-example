import android.os.SystemClock;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.espresso.web.webdriver.Locator;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import id.co.veritrans.sdk.sample.MainActivity;
import id.co.veritrans.sdk.sample.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.espresso.web.sugar.Web.onWebView;
import static android.support.test.espresso.web.webdriver.DriverAtoms.clearElement;
import static android.support.test.espresso.web.webdriver.DriverAtoms.findElement;
import static android.support.test.espresso.web.webdriver.DriverAtoms.webClick;
import static android.support.test.espresso.web.webdriver.DriverAtoms.webKeys;
import static org.hamcrest.Matchers.not;

/**
 * Created by rakawm on 5/20/16.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityBehaviorTest {
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);
    String cardNo, cardCVV, cardExpired;
    String cardNoExpected, cardCVVExcpected, cardExpiredExpected;
    String email, emailExpected;

    @Before
    public void initString() {
        cardNo = "4811111111111114";
        cardNoExpected = "4811 1111 1111 1114";
        cardCVV = "123";
        cardCVVExcpected = "123";
        cardExpired = "0120";
        cardExpiredExpected = "01/20";
        email = "sreekanth@veritrans.co.id";
        emailExpected = "sreekanth@veritrans.co.id";
    }

    @Test
    public void creditCardFlowTest() {
        // Initializing credit card payment
        onView(ViewMatchers.withId(R.id.btn_credit_ui)).perform(click());
        onView(withId(R.id.rv_payment_methods)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        // Fill credit card data
        onView(withId(R.id.et_card_no)).perform(typeText(cardNo), closeSoftKeyboard()).check(matches(withText(cardNoExpected)));
        onView(withId(R.id.et_exp_date)).perform(typeText(cardExpired), closeSoftKeyboard()).check(matches(withText(cardExpiredExpected)));
        onView(withId(R.id.et_cvv)).perform(typeText(cardCVV), closeSoftKeyboard()).check(matches(withText(cardCVVExcpected)));

        onView(withId(R.id.btn_pay_now)).perform(click());

        // Load 3DS
        SystemClock.sleep(10000);

        // Fill 3DS
        onWebView().forceJavascriptEnabled();
        // Check for webview and fill the code with default `112233` string
        onWebView()
                .withElement(findElement(Locator.ID, "PaRes"))
                .perform(clearElement())
                .perform(webKeys("112233"))
                .withElement(findElement(Locator.NAME, "ok"))
                .perform(webClick());

        // Load charging request
        SystemClock.sleep(5000);
        onView(withId(R.id.text_payment_status)).check(matches(withText(R.string.payment_successful)));

    }

    @Test
    public void bankTransferFlowTest() {
        //Initializing bank transfer payment
        onView(withId(R.id.btn_bca_va_ui)).perform(scrollTo(), click());
        onView(withId(R.id.rv_payment_methods)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.rv_bank_list)).perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));

        //Check email address text
        onView(withId(R.id.et_email)).perform(clearText(), typeText(email), closeSoftKeyboard()).check(matches(withText(emailExpected)));
        onView(withId(R.id.btn_confirm_payment)).perform(click());

        // Wait for request
        SystemClock.sleep(3000);

        // Check for virtual account
        onView(withId(R.id.text_virtual_account_number)).check(matches(not(withText(""))));
        onView(withId(R.id.text_virtual_account_number)).check(matches(not(withText(R.string.virtual_account_number))));
        onView(withId(R.id.btn_confirm_payment)).perform(click());

        // Check transaction status
        onView(withId(R.id.text_transaction_status)).check(matches(withText(R.string.payment_pending)));
    }

    @Test
    public void bcaFlowTest() {
        //Initializing bank transfer payment
        onView(withId(R.id.btn_bca_va_ui)).perform(scrollTo(), click());
        onView(withId(R.id.rv_payment_methods)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withId(R.id.rv_bank_list)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));

        //Check email address text
        onView(withId(R.id.et_email)).perform(clearText(), typeText(email), closeSoftKeyboard()).check(matches(withText(emailExpected)));
        onView(withId(R.id.btn_confirm_payment)).perform(click());

        // Wait for request
        SystemClock.sleep(3000);

        // Check for virtual account
        onView(withId(R.id.text_virtual_account_number)).check(matches(not(withText(""))));
        onView(withId(R.id.text_virtual_account_number)).check(matches(not(withText(R.string.virtual_account_number))));
        onView(withId(R.id.btn_confirm_payment)).perform(click());

        // Check transaction status
        onView(withId(R.id.text_transaction_status)).check(matches(withText(R.string.payment_pending)));
    }
}