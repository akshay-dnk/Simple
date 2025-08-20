package com.ags.user.data

import android.content.Context
import android.provider.ContactsContract
import com.ags.core.model.ContactInfo
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ContactsUploader(private val firestore: FirebaseFirestore) {

    suspend fun uploadContacts(context: Context, userEmail: String): String {

        val contacts = mutableListOf<ContactInfo>()

        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER
            ),
            null, null, null
        )

        cursor?.use {
            val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (it.moveToNext()) {
                val name = it.getString(nameIdx)
                val phone = it.getString(numberIdx)?.replace("\\D".toRegex(), "")
                if (!phone.isNullOrEmpty()) {
                    contacts.add(ContactInfo(name, phone))
                }
            }
        }

        if (contacts.isEmpty()) return "No contacts found"

        // Deduplicate by phone number
        val uniqueContacts = contacts.distinctBy { it.phone }

        // Upload contacts in Firestore
        val userContactsRef = firestore.collection("users")
            .document(userEmail)
            .collection("permissions")
            .document("read_contacts")
            .collection("contacts")

        // Firestore batch limit = 500 writes
        uniqueContacts.chunked(500).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { contact ->
                val docRef = userContactsRef.document(contact.phone!!)
                batch.set(docRef, contact)
            }
            batch.commit().await()
        }

        return "Contacts uploaded successfully ✅"
    }
}