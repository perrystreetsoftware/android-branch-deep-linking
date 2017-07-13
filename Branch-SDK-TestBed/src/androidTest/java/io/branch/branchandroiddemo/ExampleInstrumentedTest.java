package io.branch.branchandroiddemo;

import android.app.Instrumentation;
import android.content.Context;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.Branch;
import io.branch.referral.BranchError;
import io.branch.referral.BranchShortLinkBuilder;
import io.branch.referral.PrefHelper;
import io.branch.referral.util.CurrencyType;
import io.branch.referral.util.LinkProperties;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.*;


/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SmallTest
public class ExampleInstrumentedTest {

    Context context_;
    boolean isInitialised_ = false;
    private static final String TAG = "BranchAndroidTestCase";
    BranchUniversalObject buo_;
    LinkProperties linkProperties_;
    private static String shortUrlCreated_;
    private static String errorMessage_;
    private static final int LINK_GEN_WAIT_TIME = 1000; // Link should be created in 1 second
    private static final int MAX_BRANCH_REQ_WAIT_TIME = 2000; // Wait time for a Branch Request to respond
    Instrumentation instrumentation_;
    Activity currActivity_;
    PrefHelper prefHelper_;
    boolean testFlag_; // General flag to be used with tests

    private static final String TEST_LINK_1 = "https://bnctestbed.test-app.link/rVmb127dZx";
    private static final String TEST_LINK_2 = "https://bnctestbed.test-app.link/aPxlUcgeZx";

    private final String CHROME_PACKAGE_NAME = "com.android.chrome";
    private final String FIREFOX_PACKAGE_NAME = "org.mozilla.firefox";

    public static final String NO_STRING_VALUE = "bnc_no_value";

    public static final String TEST_KEY = "key_live_cgECtP02LMsvu8sN8h0urojoDrdWA30G";

    @Before
    public void createBranchInstance() {
        Log.d(TAG, "\n---- @Test::createBranchInstance() ----");

        context_ = InstrumentationRegistry.getTargetContext().getApplicationContext();
        instrumentation_ = getInstrumentation();
        Instrumentation.ActivityMonitor monitor = instrumentation_.addMonitor(MainActivity.class.getName(), null, false);

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClassName(instrumentation_.getTargetContext(), MainActivity.class.getName());
        instrumentation_.startActivitySync(intent);

        currActivity_ = instrumentation_.waitForMonitor(monitor);
        assertNotNull(currActivity_);

        buo_ = new BranchUniversalObject()
                .setCanonicalIdentifier("item/1000")
                .setCanonicalUrl("https://branch.io/deepviews")
                .setTitle("Test_Title")
                .setContentDescription("Test_Description ")
                .setContentImageUrl("https://example.com/mycontent-12345.png")
                .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                .setContentType("application/vnd.businessobjects")
                //.setContentExpiration(new Date(1476566432000L)) // set contents expiration time if applicable
                .setPrice(5.00, CurrencyType.USD)
                .addKeyWord("Test_Keyword1")
                .addKeyWord("Test_Keyword2")
                .addContentMetadata("Test_Metadata_Key1", "Metadata_value1")
                .addContentMetadata("Test_Metadata_Key2", "Metadata_value2");

        linkProperties_ = new LinkProperties()
                .addTag("myShareTag1")
                .addTag("myShareTag2")
                .setChannel("myShareChannel2")
                .setFeature("mySharefeature2")
                .setStage("10")
                .setCampaign("Android campaign")
                .addControlParameter("$android_deeplink_path", "custom/path/*")
                .addControlParameter("$ios_url", "http://example.com/ios")
                .setDuration(100);

        prefHelper_ = PrefHelper.getInstance(context_);
    }

    @Test
    public void test00InitSession() {
        Log.d(TAG, "\n---- @Test::initSession() ----");
        final String[] initErrorMsg = {""};
        final CountDownLatch latch = new CountDownLatch(1);
        Branch.getInstance().initSession(new Branch.BranchReferralInitListener() {
            @Override
            public void onInitFinished(JSONObject referringParams, BranchError error) {
                if (error != null) {
                    initErrorMsg[0] = error.getMessage();
                } else {
                    isInitialised_ = true;
                }
                latch.countDown();
            }
        });
        try {
            latch.await(MAX_BRANCH_REQ_WAIT_TIME, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            isInitialised_ = false;
        }
        assertTrue("Branch is not initialised " + initErrorMsg[0], isInitialised_);
    }

    @Test
    public void test01ShortLinkCreation() {
        Log.d(TAG, "\n---- @Test::getShortUrl ----");
        shortUrlCreated_ = buo_.getShortUrl(context_, linkProperties_);
        Log.d(TAG, "Short url created " + shortUrlCreated_);

        boolean isShortLinkCreated = (!TextUtils.isEmpty(shortUrlCreated_)) && (!shortUrlCreated_.contains("/a/"));
        assertTrue("Branch short link creation failed. Url created is " + shortUrlCreated_, isShortLinkCreated);
    }

    @Test
    public void test02LinkCache() {
        Log.d(TAG, "\n---- @Test::LinkCache ----");
        String url = buo_.getShortUrl(context_, linkProperties_);
        assertEquals("Link is not retrieved from cache ", shortUrlCreated_, url);
    }

    @Test
    public void test03ShortLinkGeneration() {
        Log.d(TAG, "\n---- @Test::ShortLinkGeneration ----");
        linkProperties_.setFeature("TestAsyncLink creation");
        boolean timedOut = false;
        final CountDownLatch latch = new CountDownLatch(1);
        final long startTime = System.currentTimeMillis();
        buo_.generateShortUrl(context_, linkProperties_, new Branch.BranchLinkCreateListener() {
            @Override
            public void onLinkCreate(String url, BranchError error) {
                latch.countDown();
                if (error != null) {
                    errorMessage_ = error.getMessage();
                }
                boolean isShortLinkGenerated = (!TextUtils.isEmpty(url)) && (!url.contains("/a/"));
                assertTrue("Branch short link generation failed . Url created is " + shortUrlCreated_ + "\n" + errorMessage_, isShortLinkGenerated);
                Log.d(TAG, "Link creation completed in " + (System.currentTimeMillis() - startTime) + " milli seconds");
            }
        });
        try {
            latch.await(LINK_GEN_WAIT_TIME, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            timedOut = true;
        }
        assertFalse("Async link creation timed out. Waited for " + LINK_GEN_WAIT_TIME + "milli seconds", timedOut);
    }

    /**
     * Check if share sheet is shown
     */
    @Test
    public void test04ShareSheet() {
        Log.d(TAG, "\n---- @Test::ShareSheet ----");
        onView(withId(R.id.share_btn)).perform(click());
        // Check if share dialog is up
        onView(withText("Share With")).check(ViewAssertions.matches(isDisplayed()));
    }

    @Test
    public void test05SetIdentity() {
        Log.d(TAG, "\n---- @Test::SetIdentity ----");
        final CountDownLatch latch = new CountDownLatch(1);
        prefHelper_.setIdentity(PrefHelper.NO_STRING_VALUE);
        Branch.getInstance().setIdentity("test_user_1", new Branch.BranchReferralInitListener() {
            @Override
            public void onInitFinished(JSONObject referringParams, BranchError error) {
                assertNull(error);
                assertNotNull(referringParams);
                assertTrue(prefHelper_.getIdentity().equals("test_user_1")); //"test_user_1" user id
                latch.countDown();
            }
        });
        try {
            latch.await(MAX_BRANCH_REQ_WAIT_TIME, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            assertTrue("setIdentity timed out ", false);
        }
    }

    @Test
    public void test06BuyCredit() {
        Log.d(TAG, "\n---- @Test::BuyCredit ----");
        final CountDownLatch latch = new CountDownLatch(1);
        final int[] availableCredits = new int[1];
        Branch.getInstance().loadRewards(new Branch.BranchReferralStateChangedListener() {
            @Override
            public void onStateChanged(boolean changed, BranchError error) {
                assertNull("Error while loading rewards ", error);
                availableCredits[0] = prefHelper_.getCreditCount();
                Log.d("BranchSDK", "available credits: " + prefHelper_.getCreditCount());
            }
        });

        try {
            Thread.sleep(4000); // Allow the browser to load the url
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.cmdCommitBuyAction)).perform(click());

        try {
            Thread.sleep(4000); // Allow the browser to load the url
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Branch.getInstance().loadRewards(new Branch.BranchReferralStateChangedListener() {
            @Override
            public void onStateChanged(boolean changed, BranchError error) {
                assertNull("Error while loading rewards ", error);
                int updatedCredits = prefHelper_.getCreditCount();
                Log.d("BranchSDKTest", "updated credits:" + updatedCredits);
                assertTrue(updatedCredits == (availableCredits[0] + 5)); // 5 credits are add in one buy
                latch.countDown();
            }
        });

        try {
            latch.await(MAX_BRANCH_REQ_WAIT_TIME, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            assertTrue("Loading rewards timed out ", false);
        }

    }

    @Test
    public void test07BuyCreditsWithMetaData() {
        Log.d(TAG, "\n---- @Test::LoadRewards ----");
        final CountDownLatch latch = new CountDownLatch(1);
        final int[] availableCredits = new int[1];
        Branch.getInstance().loadRewards(new Branch.BranchReferralStateChangedListener() {
            @Override
            public void onStateChanged(boolean changed, BranchError error) {
                assertNull("Error while loading rewards ", error);
                availableCredits[0] = prefHelper_.getCreditCount();
            }
        });

        onView(withId(R.id.cmdCommitBuyMetadataAction)).perform(click());
        Branch.getInstance().loadRewards(new Branch.BranchReferralStateChangedListener() {
            @Override
            public void onStateChanged(boolean changed, BranchError error) {
                assertNull("Error while loading rewards ", error);
                int updatedCredits = prefHelper_.getCreditCount();
                assertTrue(updatedCredits == (availableCredits[0] + 5)); // 5 credits are add in one buy
                latch.countDown();
            }
        });

        try {
            latch.await(MAX_BRANCH_REQ_WAIT_TIME, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            assertTrue("Loading rewards timed out ", false);
        }

    }

    @Test
    public void test08RedeemRewards() {
        Log.d(TAG, "\n---- @Test::RedeemRewards ----");
        final CountDownLatch latch = new CountDownLatch(1);
        final int availableCredits = prefHelper_.getCreditCount();
        Branch.getInstance().redeemRewards(5, new Branch.BranchReferralStateChangedListener() {
            @Override
            public void onStateChanged(boolean changed, BranchError error) {
                assertNull("Error while redeeming rewards", error);
                assertTrue("Local credit does not changed on redeeming rewards", changed);
            }
        });

        Branch.getInstance().loadRewards(new Branch.BranchReferralStateChangedListener() {
            @Override
            public void onStateChanged(boolean changed, BranchError error) {
                assertNull("Error while loading rewards ", error);
                int updatedCredits = prefHelper_.getCreditCount();
                assertTrue(updatedCredits == (availableCredits - 5));
                latch.countDown();
            }
        });
        try {
            latch.await(MAX_BRANCH_REQ_WAIT_TIME * 2, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            assertTrue("Redeeming rewards timed out ", false);
        }
    }

    @Test
    public void test09RegisterViewTest() {
        Log.d(TAG, "\n---- @Test::RegisterView ----");
        final CountDownLatch latch = new CountDownLatch(1);
        buo_.registerView(new BranchUniversalObject.RegisterViewStatusListener() {
            @Override
            public void onRegisterViewFinished(boolean registered, BranchError error) {
                assertNull("Error while registering view ", error);
                assertTrue("Registering  view failed", registered);
                latch.countDown();
            }
        });

        try {
            latch.await(MAX_BRANCH_REQ_WAIT_TIME, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            assertTrue("Registering view timed out ", false);
        }
    }

    @Test
    public void test10AppLinkAssociation() {
        Log.d(TAG, "\n---- @Test::AppLinkAssociation ----");
        if (android.os.Build.VERSION.SDK_INT >= 23) { // App links are supported only from Android M+
            Intent deepLinkIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(TEST_LINK_1));
            PackageManager packageManager = context_.getPackageManager();
            List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(deepLinkIntent, PackageManager.MATCH_DEFAULT_ONLY);
            boolean foundApplication = false;
            for (ResolveInfo resolveInfo : resolveInfos) {
                foundApplication = (resolveInfo.activityInfo).packageName.equals("io.branch.branchandroiddemo");
                if (foundApplication) {
                    break;
                }
            }
            assertTrue("Applink failed to open the app", foundApplication);
        }
    }

//    @Test
//    public void test12DeferredDeepLinkWithChrome() {
//        Log.d(TAG, "\n---- @Test::DeferredDeepLinkWithChrome ----");
//        if (isAppAvailable(CHROME_PACKAGE_NAME)) {
//            // Test Link 1
//            simulateDeferredDeepLinking(CHROME_PACKAGE_NAME, TEST_LINK_1);
//
//            verifyLink1DeepLink();
//
//            // Test Link 2
//            //simulateDeferredDeepLinking(CHROME_PACKAGE_NAME, TEST_LINK_2);
//            //verifyLink2DeepLink();
//        }
//    }
//
//    @Test
//    public void test13DeferredDeepLinkWithFireFox() {
//        Log.d(TAG, "\n---- @Test::DeferredDeepLinkWithFireFox ----");
//        if (isAppAvailable(FIREFOX_PACKAGE_NAME)) {
//            // Test Link 1
//            simulateDeferredDeepLinking(FIREFOX_PACKAGE_NAME, TEST_LINK_1);
//            verifyLink1DeepLink();
//
//            // Test Link 2
//            simulateDeferredDeepLinking(FIREFOX_PACKAGE_NAME, TEST_LINK_2);
//            verifyLink2DeepLink();
//        }
//    }


    ///------------------------- Support methods------------------------------------------//

    /**
     * Open the given link in the specified browser and opens the app after that to test deferred deep linking
     *
     * @param browserPackageName Package name for the browser for teh browser to be opened
     * @param branchLink         Branch link to be opened in the browser
     */
    private void simulateDeferredDeepLinking(String browserPackageName, String branchLink) {
        Intent deepLinkIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(branchLink));
        deepLinkIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        deepLinkIntent.setPackage(browserPackageName);
        context_.startActivity(deepLinkIntent);
        try {
            Thread.sleep(4000); // Allow the browser to load the url
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClassName(instrumentation_.getTargetContext(), MainActivity.class.getName());
        instrumentation_.startActivitySync(intent);
        try {
            Thread.sleep(2500);  // Allow the app to finish init Session
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void verifyLink1DeepLink() {
        try {
            JSONObject latestParams = Branch.getInstance().getLatestReferringParams();
            assertTrue("Link click not received by application", latestParams.getBoolean("+clicked_branch_link"));
            assertTrue("Received incorrect referring link", latestParams.getString("~referring_link").equals(TEST_LINK_1));
            assertTrue("Title is not set correctly", latestParams.getString("$og_title").equals("Test_Title_1"));
            assertTrue("Description is not set correctly", latestParams.getString("$og_description").equals("Test_Description_1"));
            assertTrue("Incorrect metadata received", latestParams.getString("Metadata_Key_Test11").equals("Test_Metadata_11"));
            assertTrue("Incorrect metadata received", latestParams.getString("Metadata_Key_Test12").equals("Test_Metadata_12"));
            assertTrue("Incorrect Channel value", latestParams.getString("~channel").equals("Gmail"));
            assertTrue("Incorrect feature value", latestParams.getString("~feature").equals("mySharefeature2"));
        } catch (JSONException e) {
            e.printStackTrace();
            assertTrue("Unable to retrieve the deep link data", false);
        }
    }

    private void verifyLink2DeepLink() {
        try {
            JSONObject latestParams = Branch.getInstance().getLatestReferringParams();
            assertTrue("Link click not received by application", latestParams.getBoolean("+clicked_branch_link"));
            assertTrue("Received incorrect referring link", latestParams.getString("~referring_link").equals(TEST_LINK_2));
            assertTrue("Title is not set correctly", latestParams.getString("$og_title").equals("Test_Title_2"));
            assertTrue("Description is not set correctly", latestParams.getString("$og_description").equals("Test_Description_2"));
            assertTrue("Incorrect metadata received", latestParams.getString("Metadata_Key_Test21").equals("Test_Metadata_21"));
            assertTrue("Incorrect metadata received", latestParams.getString("Metadata_Key_Test22").equals("Test_Metadata_22"));
            assertTrue("Incorrect Channel value", latestParams.getString("~channel").equals("Gmail"));
            assertTrue("Incorrect feature value", latestParams.getString("~feature").equals("mySharefeature2"));
        } catch (JSONException e) {
            e.printStackTrace();
            assertTrue("Unable to retrieve the deep link data", false);
        }
    }

    private boolean isAppAvailable(String appPackageName) {
        PackageManager pm = context_.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(appPackageName, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }

    @Test
    public void test11useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals("io.branch.branchandroiddemo", appContext.getPackageName());
    }

    @Test
    public void test12NetworkSettings() {
        prefHelper_.setRetryCount(3);
        prefHelper_.setRetryInterval(0);
        prefHelper_.setTimeout(1500);

        assertEquals(prefHelper_.getRetryCount(), 3);
        assertEquals(prefHelper_.getRetryInterval(), 0);
        assertEquals(prefHelper_.getTimeout(), 1500);
    }

    @Test
    public void test13ParamEncoding() {
        String regularString = "{\"$keywords\":[\"My_Keyword1\",\"My_Keyword2\"],\"$og_title\":\"Test_Title_1\",\"source\":\"android\",\"$identity_id\":\"322473644083563886\",\"~stage\":\"10\",\"~feature\":\"mySharefeature2\",\"$canonical_identifier\":\"item/TestID\",\"Metadata_Key_Test12\":\"Test_Metadata_12\",\"Metadata_Key_Test11\":\"Test_Metadata_11\",\"~id\":\"322502302261215595\",\"~campaign\":\"Android campaign\",\"+is_first_session\":false,\"~channel\":\"Gmail\",\"$ios_url\":\"http://example.com/ios\",\"$currency\":\"USD\",\"$publicly_indexable\":\"true\",\"$content_type\":\"application/vnd.businessobjects\",\"~creation_source\":2,\"$amount\":\"5.0\",\"$og_description\":\"Test_Description_1\",\"+click_timestamp\":1498596342,\"$match_duration\":100,\"$og_image_url\":\"https://example.com/mycontent-12345.png\",\"+match_guaranteed\":true,\"~tags\":[\"myShareTag1\",\"myShareTag2\"],\"$android_deeplink_path\":\"custom/path/*\",\"+clicked_branch_link\":true,\"$one_time_use\":false,\"$canonical_url\":\"https://branch.io/deepviews\",\"~referring_link\":\"https://bnctestbed.test-app.link/rVmb127dZx\"}";
        String convertedString = convertParamsStringToDictionary(regularString).toString();
        String correctResult = "{\"$keywords\":[\"My_Keyword1\",\"My_Keyword2\"],\"$og_title\":\"Test_Title_1\",\"source\":\"android\",\"$identity_id\":\"322473644083563886\",\"~stage\":\"10\",\"~feature\":\"mySharefeature2\",\"$canonical_identifier\":\"item\\/TestID\",\"Metadata_Key_Test12\":\"Test_Metadata_12\",\"Metadata_Key_Test11\":\"Test_Metadata_11\",\"~id\":\"322502302261215595\",\"~campaign\":\"Android campaign\",\"+is_first_session\":false,\"~channel\":\"Gmail\",\"$ios_url\":\"http:\\/\\/example.com\\/ios\",\"$currency\":\"USD\",\"$publicly_indexable\":\"true\",\"$content_type\":\"application\\/vnd.businessobjects\",\"~creation_source\":2,\"$amount\":\"5.0\",\"$og_description\":\"Test_Description_1\",\"+click_timestamp\":1498596342,\"$match_duration\":100,\"$og_image_url\":\"https:\\/\\/example.com\\/mycontent-12345.png\",\"+match_guaranteed\":true,\"~tags\":[\"myShareTag1\",\"myShareTag2\"],\"$android_deeplink_path\":\"custom\\/path\\/*\",\"+clicked_branch_link\":true,\"$one_time_use\":false,\"$canonical_url\":\"https:\\/\\/branch.io\\/deepviews\",\"~referring_link\":\"https:\\/\\/bnctestbed.test-app.link\\/rVmb127dZx\"}";
        assertEquals(convertedString, correctResult);
    }

    @Test
    public void test14RevenueEvent() {
        onView(withId(R.id.cmdRevenueEvent)).perform(click());
    }

    @Test
    public void test15FirstInstall() {
        String firstReferringParams = Branch.getInstance().getFirstReferringParams().toString();
        Log.d("BranchSDK", "firstReferringParamsBlocked: " + firstReferringParams);
        assertNotEquals(firstReferringParams, NO_STRING_VALUE);
    }

    @Test
    public void test16FirstInstallSync() {
        String firstReferringParams = Branch.getInstance().getFirstReferringParamsSync().toString();
        Log.d("BranchSDK", "firstReferringParamsBlocked: " + firstReferringParams);
        assertNotEquals(firstReferringParams, NO_STRING_VALUE);
    }

//    @Test
//    public void test17BUOLinkGen() throws InterruptedException {
//        final CountDownLatch signal = new CountDownLatch(1);
//        buo_.
//        buo_.generateShortUrl(getInstrumentation().getContext(), linkProperties_, new Branch.BranchLinkCreateListener() {
//            @Override
//            public void onLinkCreate(String url, BranchError error) {
//                assertNull(error);
//                assertNotNull(url);
//                Log.d("BranchSDK", url);
//                signal.countDown();
//            }
//        });
//        signal.await(10, TimeUnit.SECONDS);
//    }

//    @Test
//    public void test18BUORegisterView() throws InterruptedException {
//        final CountDownLatch signal = new CountDownLatch(1);
//        buo_.registerView(new BranchUniversalObject.RegisterViewStatusListener() {
//            @Override
//            public void onRegisterViewFinished(boolean registered, BranchError error) {
//                assertNull(error);
//                assertThat(registered, is(true));
//                signal.countDown();
//            }
//        });
//        signal.await(10, TimeUnit.SECONDS);
//    }

    @Test
    public void testLinkCreationLoad() throws InterruptedException {

        final CountDownLatch signalFinal = new CountDownLatch(1);
        final CountDownLatch signal = new CountDownLatch(1);
        final int LINK_TOTAL_CREATION_COUNT = 99;
        for (int i = 0; i < 100; i++) {
            final int idx = i;
            new BranchShortLinkBuilder(getInstrumentation().getContext())
                    .setChannel(i + "")
                    .generateShortUrl(new Branch.BranchLinkCreateListener() {
                        @Override
                        public void onLinkCreate(String url, BranchError error) {
                            assertNull(error);
                            assertNotNull(url);
                            Log.d("BranchSDK", "generating link... " + idx);
                            if (idx == LINK_TOTAL_CREATION_COUNT) {
                                signal.countDown();
                            }
                        }
                    });
            Thread.sleep(50);
        }
        signal.await(10, TimeUnit.SECONDS);

        new BranchShortLinkBuilder(getInstrumentation().getContext())
                .setFeature("loadTest")
                .generateShortUrl(new Branch.BranchLinkCreateListener() {
                    @Override
                    public void onLinkCreate(String url, BranchError error) {
                        assertNull(error);
                        assertNotNull(url);
                        signalFinal.countDown();
                    }
                });

        signalFinal.await(1, TimeUnit.SECONDS);
    }

    @Test
    public void test17IsInstantApp() {
        assertFalse(Branch.isInstantApp(context_));
    }

//    @Test
//    public void test18InitBranchWithKeyProgrammatically() {
//        final String[] initErrorMsg = {""};
//        final CountDownLatch latch = new CountDownLatch(1);
//        Branch.getInstance(currActivity_, TEST_KEY).initSession(new Branch.BranchReferralInitListener() {
//            @Override
//            public void onInitFinished(JSONObject referringParams, BranchError error) {
//                if (error != null) {
//                    initErrorMsg[0] = error.getMessage();
//                } else {
//                    isInitialised_ = true;
//                }
//                latch.countDown();
//            }
//        });
//        try {
//            latch.await(MAX_BRANCH_REQ_WAIT_TIME, TimeUnit.MILLISECONDS);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//            isInitialised_ = false;
//        }
//        assertTrue("Branch is not initialised " + initErrorMsg[0], isInitialised_);
//    }

    @Test
    public void test18LogoutUser() {
        Log.d(TAG, "\n---- @Test::LogoutUser ----");
        final CountDownLatch latch = new CountDownLatch(1);
        Branch.getInstance().logout(new Branch.LogoutStatusListener() {
            @Override
            public void onLogoutFinished(boolean loggedOut, BranchError error) {
                assertNull("Error while logging out user", error);
                assertTrue("Unable to logout user", loggedOut);
                assertTrue(prefHelper_.getIdentity().equals(PrefHelper.NO_STRING_VALUE));
                assertTrue(prefHelper_.getSessionParams().equals(PrefHelper.NO_STRING_VALUE));
                assertTrue(prefHelper_.getInstallParams().equals(PrefHelper.NO_STRING_VALUE));
                assertTrue(prefHelper_.getCreditCount() == 0);
                latch.countDown();
            }
        });

        try {
            latch.await(MAX_BRANCH_REQ_WAIT_TIME, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            assertTrue("Logging out user timed out ", false);
        }
    }

    private JSONObject convertParamsStringToDictionary(String paramString) {
        if (paramString.equals(PrefHelper.NO_STRING_VALUE)) {
            return new JSONObject();
        } else {
            try {
                return new JSONObject(paramString);
            } catch (JSONException e) {
                byte[] encodedArray = Base64.decode(paramString.getBytes(), Base64.NO_WRAP);
                try {
                    return new JSONObject(new String(encodedArray));
                } catch (JSONException ex) {
                    ex.printStackTrace();
                    return new JSONObject();
                }
            }
        }
    }
}
