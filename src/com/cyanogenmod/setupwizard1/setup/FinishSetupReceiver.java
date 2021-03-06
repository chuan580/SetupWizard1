package com.cyanogenmod.setupwizard1.setup;

import com.cyanogenmod.setupwizard1.SetupWizardApp;
import com.cyanogenmod.setupwizard1.util.SetupWizardUtils;

import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

public class FinishSetupReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (SetupWizardUtils.isDeviceLocked() || SetupWizardUtils.frpEnabled(context)) {
            return;
        }
        Settings.Global.putInt(context.getContentResolver(), Settings.Global.DEVICE_PROVISIONED, 1);
        Settings.Secure.putInt(context.getContentResolver(),
                Settings.Secure.USER_SETUP_COMPLETE, 1);
        ((StatusBarManager)context.getSystemService(Context.STATUS_BAR_SERVICE)).disable(
                StatusBarManager.DISABLE_NONE);
        Settings.Global.putInt(context.getContentResolver(),
                SetupWizardApp.KEY_DETECT_CAPTIVE_PORTAL, 1);
        SetupWizardUtils.disableGMSSetupWizard(context);
        SetupWizardUtils.disableSetupWizard(context);
    }
}
