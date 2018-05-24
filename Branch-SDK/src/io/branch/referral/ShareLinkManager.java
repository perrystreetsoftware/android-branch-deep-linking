package io.branch.referral;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Class that provides a chooser dialog with customised share options to share a link.
 * Class provides customised and easy way of sharing a deep link with other applications. </p>
 */
class ShareLinkManager {
    /* The custom chooser dialog for selecting an application to share the link. */
    AnimatedDialog shareDlg_;
    Branch.BranchLinkShareListener callback_;
    Branch.IChannelProperties channelPropertiesCallback_;
    
    /* List of apps available for sharing. */
    private List<ResolveInfo> appList_;
    /* Intent for sharing with selected application.*/
    private Intent shareLinkIntent_;
    /* Background color for the list view in enabled state. */
    private final int BG_COLOR_ENABLED = Color.argb(60, 17, 4, 56);
    /* Background color for the list view in disabled state. */
    private final int BG_COLOR_DISABLED = Color.argb(20, 17, 4, 56);
    /* Current activity context.*/
    Context context_;
    /* Default height for the list item.*/
    private static int viewItemMinHeight_ = 100;
    /* Indicates whether a sharing is in progress*/
    private boolean isShareInProgress_ = false;
    /* Styleable resource for share sheet.*/
    private int shareDialogThemeID_ = -1;
    /* Size of app icons in share sheet */
    private int iconSize_ = 50;
    private Branch.ShareLinkBuilder builder_;
    final int padding = 5;
    final int leftMargin = 100;
    private List<String> includeInShareSheet = new ArrayList<>();
    private List<String> excludeFromShareSheet = new ArrayList<>();
    
    /**
     * Creates an application selector and shares a link on user selecting the application.
     *
     * @param builder A {@link io.branch.referral.Branch.ShareLinkBuilder} instance to build share link.
     * @return Instance of the {@link Dialog} holding the share view. Null if sharing dialog is not created due to any error.
     */
    public Dialog shareLink(Branch.ShareLinkBuilder builder) {
        builder_ = builder;
        context_ = builder.getActivity();
        callback_ = builder.getCallback();
        channelPropertiesCallback_ = builder.getChannelPropertiesCallback();
        shareLinkIntent_ = new Intent(Intent.ACTION_SEND);
        shareLinkIntent_.setType("text/plain");
        shareDialogThemeID_ = builder.getStyleResourceID();
        includeInShareSheet = builder.getIncludedInShareSheet();
        excludeFromShareSheet = builder.getExcludedFromShareSheet();
        iconSize_ = builder.getIconSize();
        try {
            createShareDialog(builder.getPreferredOptions());
        } catch (Exception e) {
            e.printStackTrace();
            if (callback_ != null) {
                callback_.onLinkShareResponse(null, null, new BranchError("Trouble sharing link", BranchError.ERR_BRANCH_NO_SHARE_OPTION));
            } else {
                Log.i("BranchSDK", "Unable create share options. Couldn't find applications on device to share the link.");
            }
        }
        return shareDlg_;
    }
    
    /**
     * Dismiss the share dialog if showing. Should be called on activity stopping.
     *
     * @param animateClose A {@link Boolean} to specify whether to close the dialog with an animation.
     *                     A value of true will close the dialog with an animation. Setting this value
     *                     to false will close the Dialog immediately.
     */
    public void cancelShareLinkDialog(boolean animateClose) {
        if (shareDlg_ != null && shareDlg_.isShowing()) {
            if (animateClose) {
                // Cancel the dialog with animation
                shareDlg_.cancel();
            } else {
                // Dismiss the dialog immediately
                shareDlg_.dismiss();
            }
        }
    }
    
    /**
     * Create a custom chooser dialog with available share options.
     *
     * @param preferredOptions List of {@link io.branch.referral.SharingHelper.SHARE_WITH} options.
     */
    private void createShareDialog(List<SharingHelper.SHARE_WITH> preferredOptions) {
        final PackageManager packageManager = context_.getPackageManager();
        final List<ResolveInfo> preferredApps = new ArrayList<>();
        final List<ResolveInfo> matchingApps = packageManager.queryIntentActivities(shareLinkIntent_, PackageManager.MATCH_DEFAULT_ONLY);
        List<ResolveInfo> cleanedMatchingApps = new ArrayList<>();
        final List<ResolveInfo> cleanedMatchingAppsFinal = new ArrayList<>();
        ArrayList<SharingHelper.SHARE_WITH> packagesFilterList = new ArrayList<>(preferredOptions);

        /* Get all apps available for sharing and the available preferred apps. */
        for (ResolveInfo resolveInfo : matchingApps) {
            SharingHelper.SHARE_WITH foundMatching = null;
            String packageName = resolveInfo.activityInfo.packageName;
            for (SharingHelper.SHARE_WITH PackageFilter : packagesFilterList) {
                if (resolveInfo.activityInfo != null && packageName.toLowerCase().contains(PackageFilter.toString().toLowerCase())) {
                    foundMatching = PackageFilter;
                    break;
                }
            }
            if (foundMatching != null) {
                preferredApps.add(resolveInfo);
                preferredOptions.remove(foundMatching);
            }
        }
        /* Create all app list with copy link item. */
        matchingApps.removeAll(preferredApps);
        matchingApps.addAll(0, preferredApps);
        
        //if apps are explicitly being included, add only those, otherwise at the else statement add them all
        if (includeInShareSheet.size() > 0) {
            for (ResolveInfo r : matchingApps) {
                if (includeInShareSheet.contains(r.activityInfo.packageName)) {
                    cleanedMatchingApps.add(r);
                }
            }
        } else {
            cleanedMatchingApps = matchingApps;
        }
        
        //does our list contain explicitly excluded items? do not carry them into the next list
        for (ResolveInfo r : cleanedMatchingApps) {
            if (!excludeFromShareSheet.contains(r.activityInfo.packageName)) {
                cleanedMatchingAppsFinal.add(r);
            }
        }
        
        //make sure our "show more" option includes preferred apps
        for (ResolveInfo r : matchingApps) {
            for (SharingHelper.SHARE_WITH shareWith : packagesFilterList)
                if (shareWith.toString().equalsIgnoreCase(r.activityInfo.packageName)) {
                    cleanedMatchingAppsFinal.add(r);
                }
        }
        
        cleanedMatchingAppsFinal.add(new CopyLinkItem());
        matchingApps.add(new CopyLinkItem());
        preferredApps.add(new CopyLinkItem());
        
        if (preferredApps.size() > 1) {
            /* Add more and copy link option to preferred app.*/
            if (matchingApps.size() > preferredApps.size()) {
                preferredApps.add(new MoreShareItem());
            }
            appList_ = preferredApps;
        } else {
            appList_ = cleanedMatchingAppsFinal;
        }

        /* Copy link option will be always there for sharing. */
        final ChooserArrayAdapter adapter = new ChooserArrayAdapter();
        final ListView shareOptionListView;
        if (shareDialogThemeID_ > 1 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            shareOptionListView = new ListView(context_, null, 0, shareDialogThemeID_);
        } else {
            shareOptionListView = new ListView(context_);
        }
        shareOptionListView.setHorizontalFadingEdgeEnabled(false);
        shareOptionListView.setBackgroundColor(Color.WHITE);
        shareOptionListView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        
        if (builder_.getSharingTitleView() != null) {
            shareOptionListView.addHeaderView(builder_.getSharingTitleView(), null, false);
        } else if (!TextUtils.isEmpty(builder_.getSharingTitle())) {
            TextView textView = new TextView(context_);
            textView.setText(builder_.getSharingTitle());
            textView.setBackgroundColor(BG_COLOR_DISABLED);
            textView.setTextColor(BG_COLOR_DISABLED);
            textView.setTextAppearance(context_, android.R.style.TextAppearance_Medium);
            textView.setTextColor(context_.getResources().getColor(android.R.color.darker_gray));
            textView.setPadding(leftMargin, padding, padding, padding);
            shareOptionListView.addHeaderView(textView, null, false);
        }
        
        shareOptionListView.setAdapter(adapter);
        
        if (builder_.getDividerHeight() >= 0) { //User set height
            shareOptionListView.setDividerHeight(builder_.getDividerHeight());
        } else if (builder_.getIsFullWidthStyle()) {
            shareOptionListView.setDividerHeight(0); // Default no divider for full width dialog
        }
        
        shareOptionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                if (view.getTag() instanceof MoreShareItem) {
                    appList_ = cleanedMatchingAppsFinal;
                    adapter.notifyDataSetChanged();
                } else {
                    if (callback_ != null) {
                        String selectedChannelName = "";
                        if (view.getTag() != null && context_ != null && ((ResolveInfo) view.getTag()).loadLabel(context_.getPackageManager()) != null) {
                            selectedChannelName = ((ResolveInfo) view.getTag()).loadLabel(context_.getPackageManager()).toString();
                        }
                        builder_.getShortLinkBuilder().setChannel(((ResolveInfo) view.getTag()).loadLabel(context_.getPackageManager()).toString());
                        callback_.onChannelSelected(selectedChannelName);
                    }
                    adapter.selectedPos = pos - shareOptionListView.getHeaderViewsCount();
                    adapter.notifyDataSetChanged();
                    invokeSharingClient((ResolveInfo) view.getTag());
                    if (shareDlg_ != null) {
                        shareDlg_.cancel();
                    }
                }
            }
        });
        if (builder_.getDialogThemeResourceID() > 0) {
            shareDlg_ = new AnimatedDialog(context_, builder_.getDialogThemeResourceID());
        } else {
            shareDlg_ = new AnimatedDialog(context_, builder_.getIsFullWidthStyle());
        }
        shareDlg_.setContentView(shareOptionListView);
        shareDlg_.show();
        if (callback_ != null) {
            callback_.onShareLinkDialogLaunched();
        }
        shareDlg_.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                if (callback_ != null) {
                    callback_.onShareLinkDialogDismissed();
                    callback_ = null;
                }
                // Release  context to prevent leaks
                if (!isShareInProgress_) {
                    context_ = null;
                    builder_ = null;
                }
                shareDlg_ = null;
            }
        });
    }
    
    
    /**
     * Invokes a sharing client with a link created by the given json objects.
     *
     * @param selectedResolveInfo The {@link ResolveInfo} corresponding to the selected sharing client.
     */
    private void invokeSharingClient(final ResolveInfo selectedResolveInfo) {
        isShareInProgress_ = true;
        final String channelName = selectedResolveInfo.loadLabel(context_.getPackageManager()).toString();
        BranchShortLinkBuilder shortLinkBuilder = builder_.getShortLinkBuilder();
        
        shortLinkBuilder.generateShortUrlInternal(new Branch.BranchLinkCreateListener() {
            @Override
            public void onLinkCreate(String url, BranchError error) {
                if (error == null) {
                    shareWithClient(selectedResolveInfo, url, channelName);
                } else {
                    //If there is a default URL specified share it.
                    String defaultUrl = builder_.getDefaultURL();
                    if (defaultUrl != null && defaultUrl.trim().length() > 0) {
                        shareWithClient(selectedResolveInfo, defaultUrl, channelName);
                    } else {
                        if (callback_ != null) {
                            callback_.onLinkShareResponse(url, channelName, error);
                        } else {
                            Log.i("BranchSDK", "Unable to share link " + error.getMessage());
                        }
                        if (error.getErrorCode() == BranchError.ERR_BRANCH_NO_CONNECTIVITY
                                || error.getErrorCode() == BranchError.ERR_BRANCH_TRACKING_DISABLED) {
                            shareWithClient(selectedResolveInfo, url, channelName);
                        } else {
                            cancelShareLinkDialog(false);
                            isShareInProgress_ = false;
                        }
                    }
                }
            }
        }, true);
    }
    
    private void shareWithClient(ResolveInfo selectedResolveInfo, String url, String channelName) {
        if (callback_ != null) {
            callback_.onLinkShareResponse(url, channelName, null);
        } else {
            Log.i("BranchSDK", "Shared link with " + channelName);
        }
        if (selectedResolveInfo instanceof CopyLinkItem) {
            addLinkToClipBoard(url, builder_.getShareMsg());
        } else {
            shareLinkIntent_.setPackage(selectedResolveInfo.activityInfo.packageName);
            String shareSub = builder_.getShareSub();
            String shareMsg = builder_.getShareMsg();
            
            if (channelPropertiesCallback_ != null) {
                String customShareSub = channelPropertiesCallback_.getSharingTitleForChannel(channelName);
                String customShareMsg = channelPropertiesCallback_.getSharingMessageForChannel(channelName);
                if (!TextUtils.isEmpty(customShareSub)) {
                    shareSub = customShareSub;
                }
                if (!TextUtils.isEmpty(customShareMsg)) {
                    shareMsg = customShareMsg;
                }
            }
            if (shareSub != null && shareSub.trim().length() > 0) {
                shareLinkIntent_.putExtra(Intent.EXTRA_SUBJECT, shareSub);
            }
            shareLinkIntent_.putExtra(Intent.EXTRA_TEXT, shareMsg + "\n" + url);
            context_.startActivity(shareLinkIntent_);
        }
    }
    
    /**
     * Adds a given link to the clip board.
     *
     * @param url   A {@link String} to add to the clip board
     * @param label A {@link String} label for the adding link
     */
    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    private void addLinkToClipBoard(String url, String label) {
        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context_.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(url);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context_.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText(label, url);
            clipboard.setPrimaryClip(clip);
        }
        Toast.makeText(context_, builder_.getUrlCopiedMessage(), Toast.LENGTH_SHORT).show();
    }
    
    /*
     * Adapter class for creating list of available share options
     */
    private class ChooserArrayAdapter extends BaseAdapter {
        public int selectedPos = -1;
        
        @Override
        public int getCount() {
            return appList_.size();
        }
        
        @Override
        public Object getItem(int position) {
            return appList_.get(position);
        }
        
        @Override
        public long getItemId(int position) {
            return position;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ShareItemView itemView;
            if (convertView == null) {
                itemView = new ShareItemView(context_);
            } else {
                itemView = (ShareItemView) convertView;
            }
            ResolveInfo resolveInfo = appList_.get(position);
            boolean setSelected = position == selectedPos;
            itemView.setLabel(resolveInfo.loadLabel(context_.getPackageManager()).toString(), resolveInfo.loadIcon(context_.getPackageManager()), setSelected);
            itemView.setTag(resolveInfo);
            itemView.setClickable(false);
            return itemView;
        }
        
        @Override
        public boolean isEnabled(int position) {
            return selectedPos < 0;
        }
    }
    
    /**
     * Class for sharing item view to be displayed in the list with Application icon and Name.
     */
    private class ShareItemView extends TextView {
        Context context_;
        int iconSizeDP_;
        
        public ShareItemView(Context context) {
            super(context);
            context_ = context;
            this.setPadding(leftMargin, padding, padding, padding);
            this.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            this.setMinWidth(context_.getResources().getDisplayMetrics().widthPixels);
            this.iconSizeDP_ = iconSize_ != 0 ? BranchUtil.dpToPx(context, iconSize_) : 0;
        }
        
        public void setLabel(String appName, Drawable appIcon, boolean isEnabled) {
            this.setText("\t" + appName);
            this.setTag(appName);
            if (appIcon == null) {
                this.setTextAppearance(context_, android.R.style.TextAppearance_Large);
                this.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            } else {
                if (iconSizeDP_ != 0) {
                    appIcon.setBounds(0, 0, iconSizeDP_, iconSizeDP_);
                    this.setCompoundDrawables(appIcon, null, null, null);
                } else {
                    this.setCompoundDrawablesWithIntrinsicBounds(appIcon, null, null, null);
                }
                this.setTextAppearance(context_, android.R.style.TextAppearance_Medium);
                viewItemMinHeight_ = Math.max(viewItemMinHeight_, (appIcon.getIntrinsicHeight() + padding));
            }
            this.setMinHeight(viewItemMinHeight_);
            this.setTextColor(context_.getResources().getColor(android.R.color.black));
            if (isEnabled) {
                this.setBackgroundColor(BG_COLOR_ENABLED);
            } else {
                this.setBackgroundColor(BG_COLOR_DISABLED);
            }
        }
    }
    
    /**
     * Class for sharing item more
     */
    private class MoreShareItem extends ResolveInfo {
        @SuppressWarnings("NullableProblems")
        @Override
        public CharSequence loadLabel(PackageManager pm) {
            return builder_.getMoreOptionText();
        }
        
        @SuppressWarnings("NullableProblems")
        @Override
        public Drawable loadIcon(PackageManager pm) {
            return builder_.getMoreOptionIcon();
        }
    }
    
    /**
     * Class for Sharing Item copy URl
     */
    private class CopyLinkItem extends ResolveInfo {
        @SuppressWarnings("NullableProblems")
        @Override
        public CharSequence loadLabel(PackageManager pm) {
            return builder_.getCopyURlText();
        }
        
        @SuppressWarnings("NullableProblems")
        @Override
        public Drawable loadIcon(PackageManager pm) {
            return builder_.getCopyUrlIcon();
        }
        
    }
    
}
