package com.ags.user.features.readSMS

import android.content.Context
import com.ags.core.model.SMSInfo
import com.google.firebase.firestore.FirebaseFirestore
import androidx.core.net.toUri
import kotlinx.coroutines.tasks.await

class SmsUploader(private val firestore: FirebaseFirestore) {

    suspend fun uploadSMS(context: Context, userEmail: String): String {

        val smsList = mutableListOf<SMSInfo>()

        val cursor = context.contentResolver.query(
            "content://sms/inbox".toUri(),
            arrayOf("_id", "address", "date", "body"),
            null, null, "date DESC LIMIT 10"
        )

        cursor?.use {
            val addressIdx = it.getColumnIndexOrThrow("address")
            val dateIdx = it.getColumnIndexOrThrow("date")
            val bodyIdx = it.getColumnIndexOrThrow("body")

            while (it.moveToNext()) {
                val address = it.getString(addressIdx)
                val date = it.getLong(dateIdx)
                val body = it.getString(bodyIdx)

                smsList.add(SMSInfo(address, body, date))
            }
        }

        if (smsList.isEmpty()) return "No SMS found"

        // Deduplicate (by address + body + date)
        val uniqueSms = smsList.distinctBy { "${it.address}_${it.body}_${it.date}" }

        val userSmsRef = firestore.collection("users")
            .document(userEmail)
            .collection("permissions")
            .document("read_sms")
            .collection("sms")

        uniqueSms.chunked(100).forEach { chunk ->
            val batch = firestore.batch()
            chunk.forEach { sms ->
                val docId = "${sms.date}_${sms.address}" // unique doc ID
                val docRef = userSmsRef.document(docId)
                batch.set(docRef, sms)
            }
            batch.commit().await()
        }

        return "SMS uploaded successfully ✅"
    }
}