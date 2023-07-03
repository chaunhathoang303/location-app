package com.udacity.project4.locationreminders.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment.Companion.ACTION_GEOFENCE_EVENT
import com.udacity.project4.utils.sendNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.getKoin

/**
 * Triggered by the Geofence.  Since we can have many Geofences at once, we pull the request
 * ID from the first Geofence, and locate it within the cached data in our Room DB
 *
 * Or users can add the reminders and then close the app, So our app has to run in the background
 * and handle the geofencing in the background.
 * To do that you can use https://developer.android.com/reference/android/support/v4/app/JobIntentService to do that.
 *
 */
class GeofenceBroadcastReceiver : BroadcastReceiver() {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {

        val remindersLocalRepository = getKoin().get<ReminderDataSource>()

        if (intent.action == ACTION_GEOFENCE_EVENT) {
            val geofencingEvent = GeofencingEvent.fromIntent(intent)

            if (geofencingEvent?.hasError() == true) {
                Log.e(TAG, context.getString(R.string.geofence_unknown_error))
                return
            }

            if (geofencingEvent?.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                Log.v(TAG, context.getString(R.string.geofence_entered))

                val fenceId = when {
                    geofencingEvent.triggeringGeofences!!.isNotEmpty() ->
                        geofencingEvent.triggeringGeofences!![0].requestId

                    else -> {
                        Log.e(TAG, "No Geofence Trigger Found! Abort mission!")
                        return
                    }
                }

                coroutineScope.launch {
                    //get the reminder with the request id
                    val result = remindersLocalRepository.getReminder(fenceId)
                    if (result is Result.Success<ReminderDTO>) {
                        val reminderDTO = result.data
                        Log.e("data", "$reminderDTO")
                        sendNotification(
                            context, ReminderDataItem(
                                reminderDTO.title,
                                reminderDTO.description,
                                reminderDTO.location,
                                reminderDTO.latitude,
                                reminderDTO.longitude,
                                reminderDTO.id
                            )
                        )
                    }
                }
            }
        }
    }
}

private const val TAG = "GeofenceReceiver"