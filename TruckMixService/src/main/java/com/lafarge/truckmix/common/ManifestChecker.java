package com.lafarge.truckmix.common;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.lafarge.truckmix.service.TruckMixService;

import java.util.List;

public final class ManifestChecker {

    //
    // API
    //

    public static void checkManifestConfiguration(final Context context) {
        checkServiceDeclaration(context);
        checkPermissions(context);
    }

    //
    // Private
    //

    private static void checkPermissions(final Context context) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(context, TruckMixService.class);
        List resolveInfo = packageManager.queryIntentServices(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfo.size() == 0) {
            throw new ServiceNotDeclaredException();
        }
    }

    private static void checkServiceDeclaration(final Context context) {
        if (context.checkCallingOrSelfPermission(Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_DENIED) {
            throw new PermissionNotDeclaredException(Manifest.permission.BLUETOOTH);
        }
        if (context.checkCallingOrSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_DENIED) {
            throw new PermissionNotDeclaredException(Manifest.permission.BLUETOOTH_ADMIN);
        }
    }

    //
    // Exceptions classes
    //

    private static class ServiceNotDeclaredException extends RuntimeException {

        public ServiceNotDeclaredException() {
            super("The TruckMixService is not properly declared in your AndroidManifest.xml.  If using Eclipse," +
                    " please verify that your project.properties has manifestmerger.enabled=true");
        }
    }

    private static class PermissionNotDeclaredException extends RuntimeException {

        public PermissionNotDeclaredException(String permission) {
            super("The " + permission + " is not properly declared in your AndroidManifest.xml");
        }
    }
}
