package com.udacity.project4.locationreminders.reminderslist

import com.udacity.project4.R
import androidx.databinding.base.BaseRecyclerViewAdapter

// Use data binding to show the reminder on the item
class RemindersListAdapter(callBack: (selectedReminder: ReminderDataItem) -> Unit) :
    BaseRecyclerViewAdapter<ReminderDataItem>(callBack) {
    override fun getLayoutRes(viewType: Int) = R.layout.it_reminder
}