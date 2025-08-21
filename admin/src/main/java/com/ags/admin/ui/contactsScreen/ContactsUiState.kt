package com.ags.admin.ui.contactsScreen

import com.ags.core.model.ContactInfo

sealed class ContactsUiState {
    object Loading : ContactsUiState()
    data class Success(val contacts: List<ContactInfo>) : ContactsUiState()
    data class Error(val message: String) : ContactsUiState()
}