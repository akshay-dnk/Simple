package com.ags.admin.ui.contactsScreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ags.core.model.ContactInfo
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ContactsViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val _contacts = MutableLiveData<List<ContactInfo>>()
    val contacts: LiveData<List<ContactInfo>> = _contacts

    fun loadUserContacts(email: String) {
        viewModelScope.launch {
            val allContacts = mutableListOf<ContactInfo>()
            val contactsSnapshot = firestore.collection("users")
                .document(email)
                .collection("permissions")
                .document("read_contacts")
                .collection("contacts")
                .get()
                .await()

            for (contactDoc in contactsSnapshot.documents) {
                val contact = contactDoc.toObject(ContactInfo::class.java)
                if (contact != null) {
                    allContacts.add(contact.copy(userId = email)) // store email as userId
                }
            }
            _contacts.postValue(allContacts)
        }
    }
}