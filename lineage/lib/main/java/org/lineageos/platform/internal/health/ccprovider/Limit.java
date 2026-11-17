/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.platform.internal.health.ccprovider;

import static lineageos.health.HealthInterface.MODE_AUTO;
import static lineageos.health.HealthInterface.MODE_LIMIT;
import static lineageos.health.HealthInterface.MODE_MANUAL;

import android.content.Context;
import android.util.Log;

import vendor.lineage.health.ChargingControlSupportedMode;
import vendor.lineage.health.ChargingLimitInfo;
import vendor.lineage.health.IChargingControl;

import java.io.PrintWriter;

public class Limit extends ChargingControlProvider {
    private final boolean mIsBypassSupported;

    public Limit(IChargingControl chargingControl, Context context) {
        super(context, chargingControl);

        mIsBypassSupported = isHALModeSupported(ChargingControlSupportedMode.BYPASS);
        Log.i(TAG, "isBypassSupported: " + mIsBypassSupported);
    }

    @Override
    protected boolean onBatteryChanged(float currentPct, int targetPct, int rechargePct) {
        Log.i(TAG, "Current battery level: " + currentPct + ", target: " + targetPct
                + ", recharge: " + rechargePct);
        return setChargingLimit(targetPct, rechargePct);
    }

    @Override
    protected void onEnabled() {
        onReset();
    }

    @Override
    protected void onDisable() {
        onReset();
    }

    @Override
    protected void onReset() {
        setChargingLimit(100, 0);
    }

    private boolean setChargingLimit(int targetPct, int rechargePct) {
        try {
            final int min;
            if (targetPct == 100) {
                min = 0;
            } else if (mIsBypassSupported) {
                // The battery stays at the limit when bypassing, so keep the old window
                min = targetPct - 1;
            } else {
                min = Math.max(0, Math.min(rechargePct, targetPct - 1));
            }

            ChargingLimitInfo current = mChargingControl.getChargingLimit();
            if (current.max != targetPct || current.min != min) {
                ChargingLimitInfo limit = new ChargingLimitInfo();
                limit.min = min;
                limit.max = targetPct;
                mChargingControl.setChargingLimit(limit);
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to set charging limit", e);
            return false;
        }
    }

    @Override
    public boolean isSupported() {
        return isHALModeSupported(ChargingControlSupportedMode.LIMIT);
    }

    @Override
    public boolean requiresBatteryLevelMonitoring() {
        return !mIsBypassSupported;
    }

    @Override
    public boolean isChargingControlModeSupported(int mode) {
        return mode == MODE_AUTO || mode == MODE_MANUAL || mode == MODE_LIMIT;
    }

    @Override
    public void dump(PrintWriter pw) {
        pw.println("Provider: " + getClass().getName());
    }
}
