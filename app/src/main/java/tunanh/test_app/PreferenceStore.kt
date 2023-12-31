package tunanh.test_app

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

val Context.dataStore by preferencesDataStore("settings")

class PreferenceStore {
    data class Preference<T>(
        val key: Preferences.Key<T>,
        val defaultValue: T
    )

    companion object {
        private infix fun <T> Preferences.Key<T>.defaultsTo(value: T) =
            Preference(this, value)

        // Connection
        val AUTO_CONNECT = booleanPreferencesKey("auto_connect") defaultsTo false
        val SHOW_UNNAMED = booleanPreferencesKey("show_unnamed") defaultsTo false
        val DEVICE = stringPreferencesKey("device") defaultsTo ""
//        val DEVICE_2 = stringPreferencesKey("device_2") defaultsTo ""
//        val DEVICE_3 = stringPreferencesKey("device_3") defaultsTo ""
//        val SEND_DELAY = floatPreferencesKey("send_delay") defaultsTo 10f
//        val KEYBOARD_LAYOUT = intPreferencesKey("keyboard_layout") defaultsTo 0
//        val EXTRA_KEYS = intPreferencesKey("extra_keys") defaultsTo 0 // None
//        val TEMPLATE_TEXT = stringPreferencesKey("template_text") defaultsTo ""
//
//        // Appearance
//        val ALLOW_SCREEN_ROTATION = booleanPreferencesKey("allow_screen_rotation") defaultsTo false
//        val THEME = intPreferencesKey("theme") defaultsTo 0 // System
//        val DYNAMIC_THEME = booleanPreferencesKey("dynamic_theme") defaultsTo false
//
//        // Camera
//        val AUTO_FOCUS = booleanPreferencesKey("auto_focus") defaultsTo true
//        val FRONT_CAMERA = booleanPreferencesKey("front_camera") defaultsTo false
//        val FIX_EXPOSURE = booleanPreferencesKey("fix_exposure") defaultsTo false
//        val FOCUS_MODE = intPreferencesKey("focus_mode") defaultsTo 0 // Auto
//
//        // Scanner
//        val SCAN_FREQUENCY = intPreferencesKey("scan_freq") defaultsTo 2 // Normal
//        val CODE_TYPES = stringSetPreferencesKey("code_types") defaultsTo setOf()
//        val SCAN_RESOLUTION = intPreferencesKey("scan_res") defaultsTo 0 // SD
//        val SCAN_REGEX = stringPreferencesKey("scan_regex") defaultsTo ""
//        val RESTRICT_AREA = booleanPreferencesKey("restrict_area") defaultsTo true
//        val FULL_INSIDE = booleanPreferencesKey("full_inside") defaultsTo true
//        val OVERLAY_TYPE = intPreferencesKey("overlay_type") defaultsTo 0 // Square
//        val AUTO_SEND = booleanPreferencesKey("auto_send") defaultsTo false
//        val PLAY_SOUND = booleanPreferencesKey("play_sound") defaultsTo false
//        val VIBRATE = booleanPreferencesKey("vibrate") defaultsTo false
//        val RAW_VALUE = booleanPreferencesKey("raw_value") defaultsTo false
//        val HIGHLIGHT_TYPE = intPreferencesKey("highlight") defaultsTo 0 // Box
    }
}


suspend fun <T> Context.setPreference(pref: PreferenceStore.Preference<T>, value: T) {
    dataStore.edit {
        it[pref.key] = value
    }
}

fun <T> Context.getPreference(pref: PreferenceStore.Preference<T>): Flow<T> = dataStore.data
    .catch { e ->
        Timber.tag("PreferenceStore").e(e, "Error readingpreference")
        emit(preferencesOf(pref.key to pref.defaultValue))
    }.map {
        it[pref.key] ?: pref.defaultValue
    }

@Composable
fun <T> Context.getPreferenceState(pref: PreferenceStore.Preference<T>, initial: T): State<T> {
    return remember { getPreference(pref) }.collectAsState(initial)
}

suspend fun <T> Context.getPreferenceValue(pref: PreferenceStore.Preference<T>): T {
    val deferred = CompletableDeferred<T>()
    dataStore.edit {
        deferred.complete(it[pref.key] ?: pref.defaultValue)
    }
    return deferred.await()
}

//@Composable
//fun <T> Context.getPreferenceState(pref: PreferenceStore.Preference<T>): State<T?> {
//    return remember { getPreference(pref) }.collectAsState(null)
//}
//
//@Composable
//fun <T> Context.getPreferenceStateBlocking(pref: PreferenceStore.Preference<T>): State<T> {
//    val flow = remember { getPreference(pref) }
//    return flow.collectAsState(runBlocking { flow.first() })
//}

//@Composable
//fun <T> rememberPreference(
//    pref: PreferenceStore.Preference<T>,
//): MutableState<T> {
//    val context = LocalContext.current
//    val scope = rememberCoroutineScope()
//    val state = context.getPreferenceStateBlocking(pref)
//
//    return remember {
//        object : MutableState<T> {
//            override var value: T
//                get() = state.value
//                set(value) {
//                    scope.launch {
//                        context.setPreference(pref, value)
//                    }
//                }
//
//            override fun component1(): T = value
//            override fun component2(): (T) -> Unit = { value = it }
//        }
//    }
//}
//
//@Composable
//fun <T> rememberPreferenceNull(
//    pref: PreferenceStore.Preference<T>,
//): MutableState<T?> {
//    val context = LocalContext.current
//    val scope = rememberCoroutineScope()
//    val state = context.getPreferenceState(pref)
//
//    return remember {
//        object : MutableState<T?> {
//            override var value: T?
//                get() = state.value
//                set(value) {
//                    scope.launch {
//                        context.setPreference(pref, value!!)
//                    }
//                }
//
//            override fun component1(): T? = value
//            override fun component2(): (T?) -> Unit = { value = it }
//        }
//    }
//}

@Composable
fun <T> rememberPreferenceDefault(
    pref: PreferenceStore.Preference<T>,
    initial: T = pref.defaultValue
): MutableState<T> {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val state = context.getPreferenceState(pref, initial)

    return remember {
        object : MutableState<T> {
            override var value: T
                get() = state.value
                set(value) {
                    scope.launch {
                        context.setPreference(pref, value)
                    }
                }

            override fun component1(): T = value
            override fun component2(): (T) -> Unit = { value = it }
        }
    }
}
