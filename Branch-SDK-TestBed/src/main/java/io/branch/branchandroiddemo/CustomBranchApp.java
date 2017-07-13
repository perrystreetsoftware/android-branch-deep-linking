
package io.branch.branchandroiddemo;

//import com.squareup.leakcanary.LeakCanary;

import android.app.Application;

import io.branch.referral.Branch;
import io.branch.referral.PrefHelper;

public final class CustomBranchApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Branch.getAutoInstance(this, PrefHelper.getInstance(this).getBranchKey());
        // Uncomment to test memory leak
        // LeakCanary.install(this);
    }
}
