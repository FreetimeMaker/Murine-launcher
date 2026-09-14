package app.murinelauncher.settings.hiddenapps

import android.app.ActivityOptions
import android.app.PendingIntent
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
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
    private const val PERMISSION_LOCK_APPS = "android.permission.LOCK_APPS"

    /** TODO use comnstant field once building with new stubs */
    private val GET_APP_LOCK_INFO = runCatching {
        (PackageManager::class.java.getField("GET_APP_LOCK_INFO").get(null) as Number).toLong()
    }.getOrDefault(0L)

    private val isAppLockSupportedField = runCatching { ApplicationInfo::class.java.getField("isAppLockSupported") }.getOrNull()
    private val isAppLockEnabledField = runCatching { ApplicationInfo::class.java.getField("isAppLockEnabled") }.getOrNull()
    private val getEnableAppLockIntent = runCatching {
        PackageManager::class.java.getMethod("getEnableAppLockIntentForPackage", String::class.java, Boolean::class.javaPrimitiveType)
    }.getOrNull()

    /**
     * True only when the framework has the App Lock API and the LOCK_APPS permission is held;
     * The launcher must be set as default home app.
     */
    @JvmStatic
    fun isAvailable(context: Context): Boolean =
        getEnableAppLockIntent != null && isAppLockSupportedField != null &&
            context.checkSelfPermission(PERMISSION_LOCK_APPS) == PackageManager.PERMISSION_GRANTED

    /**
     * The system marks exempt apps (and every app, while the feature flag is off) as unsupported.
     */
    @JvmStatic
    fun isSupported(appInfo: ApplicationInfo): Boolean =
        runCatching { isAppLockSupportedField?.getBoolean(appInfo) == true }.getOrDefault(false)

    /**
     * Current lock state for a specific app; false on any error or when the feature is absent.
     */
    @JvmStatic
    fun isLocked(appInfo: ApplicationInfo): Boolean =
        runCatching { isAppLockEnabledField?.getBoolean(appInfo) == true }.getOrDefault(false)

    @JvmStatic
    fun isLocked(context: Context, packageName: String): Boolean = runCatching {
        if (!Utilities.ATLEAST_T) return false
        isLocked(context.packageManager.getApplicationInfo(
            packageName, PackageManager.ApplicationInfoFlags.of(GET_APP_LOCK_INFO)))
    }.getOrDefault(false)

    /**
     * Toggles App Lock for [packageName], the system asks for user authentication.
     */
    @JvmStatic
    fun requestSetAppLock(context: Context, packageName: String) {
        try {
            val pendingIntent = getEnableAppLockIntent?.invoke(
                context.packageManager, packageName, !isLocked(context, packageName)
            ) as? PendingIntent
            val options = Utilities.allowBGLaunch(ActivityOptions.makeBasic()).toBundle()
            pendingIntent?.send(null, 0, null, null, null, null, options)
                ?: Log.w(TAG, "No App Lock PendingIntent for $packageName")
        } catch (e: Exception) {
            Log.w(TAG, "App Lock toggle failed for $packageName", e)
        }
    }
}
