package app.murinelauncher.settings.hiddenapps

import android.Manifest
import android.annotation.ChecksSdkIntAtLeast
import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.android.launcher3.Utilities

/**
 * Thin wrapper around Android's native App Lock
 *
 * [isAvailable] is the master gate: when it returns false the lock UI is never shown
 * and no other member of this object is reached.
 */
object AppLock {
    private const val TAG = "MurineAppLock"

    /**
     * Feature probe: App Lock API support.
     */
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.CINNAMON_BUN)
    private val hasAppLockApi = runCatching { ApplicationInfo::class.java.getField("isAppLockSupported") }.isSuccess

    /**
     * True only when the framework has the App Lock API and the LOCK_APPS permission is held;
     * The launcher must be set as default home app.
     */
    @JvmStatic
    fun isAvailable(context: Context): Boolean = hasAppLockApi &&
            context.checkSelfPermission(Manifest.permission.LOCK_APPS) == PackageManager.PERMISSION_GRANTED

    /**
     * The system marks exempt apps (and every app, while the feature flag is off) as unsupported.
     */
    @JvmStatic @SuppressLint("NewApi")
    fun isSupported(appInfo: ApplicationInfo): Boolean = runCatching { appInfo.isAppLockSupported }.getOrDefault(false)

    /**
     * Current lock state for a specific app; false on any error or when the feature is absent.
     */
    @JvmStatic @SuppressLint("NewApi")
    fun isLocked(appInfo: ApplicationInfo): Boolean = runCatching { appInfo.isAppLockEnabled }.getOrDefault(false)

    /** Without GET_APP_LOCK_INFO the isAppLock* fields come back unset (false). */
    @JvmStatic @SuppressLint("NewApi")
    fun isLocked(context: Context, packageName: String): Boolean = runCatching {
        if (!Utilities.ATLEAST_T) return false
        isLocked(context.packageManager.getApplicationInfo(packageName,
            PackageManager.ApplicationInfoFlags.of(PackageManager.GET_APP_LOCK_INFO)))
    }.getOrDefault(false)

    /**
     * Toggles App Lock for [packageName], the system asks for user authentication.
     */
    @JvmStatic @SuppressLint("NewApi")
    fun requestSetAppLock(context: Context, packageName: String) {
        try {
            val pendingIntent = context.packageManager.getEnableAppLockIntentForPackage(
                packageName, !isLocked(context, packageName))
            val options = Utilities.allowBGLaunch(ActivityOptions.makeBasic()).toBundle()
            pendingIntent?.send(null, 0, null, null, null, null, options)
                ?: Log.w(TAG, "No App Lock PendingIntent for $packageName")
        } catch (e: Throwable) {
            Log.w(TAG, "App Lock toggle failed for $packageName", e)
        }
    }
}
