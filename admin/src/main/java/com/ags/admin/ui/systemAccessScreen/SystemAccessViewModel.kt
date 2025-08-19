package com.ags.admin.ui.systemAccessScreen

import androidx.lifecycle.ViewModel
import com.ags.admin.R
import com.ags.admin.model.SystemAccess

class SystemAccessViewModel: ViewModel() {

    val features = listOf(
        SystemAccess("Read Contacts", "Access phone contacts", R.drawable.ic_contacts),
    )
}