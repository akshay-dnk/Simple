package com.ags.admin.ui.contactsScreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ags.core.model.ContactInfo
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ContactsViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _contactsUiState = MutableStateFlow<ContactsUiState>(ContactsUiState.Loading)
    val contactsUiState: StateFlow<ContactsUiState> = _contactsUiState

    fun loadUserContacts(email: String) {
        viewModelScope.launch {
            _contactsUiState.value = ContactsUiState.Loading
            try {
                val contactsSnapshot = firestore.collection("users")
                    .document(email)
                    .collection("permissions")
                    .document("read_contacts")
                    .collection("contacts")
                    .get()
                    .await()

                val contacts = contactsSnapshot.toObjects(ContactInfo::class.java)
                _contactsUiState.value = ContactsUiState.Success(contacts)
            } catch (e: Exception) {
                e.printStackTrace()
                _contactsUiState.value = ContactsUiState.Error(e.message ?: "Something went wrong")
            }
        }
    }
}