/*
 * SPDX-FileCopyrightText: 2018-2019 The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package lineageos.trust;

/** @hide */
interface ITrustInterface {

    boolean postNotificationForFeature(int feature);
    boolean removeNotificationForFeature(int feature);
    boolean hasUsbRestrictor();
    int getLevelForFeature(int feature);
    void runTest();
}
