package app.murinelauncher.settings.hiddenapps

import android.content.Context
import android.view.View
import android.widget.ImageView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.android.launcher3.R

class HiddenAppPreference(context: Context) : Preference(context) {
    var isAppHidden: Boolean = false; set(value) { if (field != value) { field = value; notifyChanged() } }
    var isAppLocked: Boolean = false; set(value) { if (field != value) { field = value; notifyChanged() } }
    /** Set only when the native App Lock feature is available; see [AppLock.isAvailable]. */
    var onLockClick: (() -> Unit)? = null
    /** Protected tab: the row itself toggles the lock, so no eye and no separate lock button. */
    var lockedMode: Boolean = false; set(value) { if (field != value) { field = value; notifyChanged() } }

    /** ?android:attr/selectableItemBackgroundBorderless, resolved once. */
    private val rippleBg by lazy {
        val ta = context.obtainStyledAttributes(
            intArrayOf(android.R.attr.selectableItemBackgroundBorderless))
        ta.getResourceId(0, 0).also { ta.recycle() }
    }

    init {
        widgetLayoutResource = R.layout.hidden_app_eye_widget
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        (holder.findViewById(R.id.app_visibility_icon) as? ImageView)?.apply {
            visibility = if (lockedMode) View.GONE else View.VISIBLE
            setImageResource(if (isAppHidden) R.drawable.ic_eye_hidden else R.drawable.ic_eye_visible)
        }
        (holder.findViewById(R.id.app_lock_icon) as? ImageView)?.apply {
            visibility = if (onLockClick != null || isAppLocked) View.VISIBLE else View.GONE
            setImageResource(
                if (isAppLocked) R.drawable.ic_app_lock_locked else R.drawable.ic_app_lock_unlocked
            )
            val interactive = onLockClick != null && !lockedMode
            setOnClickListener { onLockClick?.invoke() }
            isClickable = interactive
            setBackgroundResource(if (interactive) rippleBg else 0)
        }
    }
}
