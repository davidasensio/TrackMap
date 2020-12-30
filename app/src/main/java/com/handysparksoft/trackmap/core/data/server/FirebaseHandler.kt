package com.handysparksoft.trackmap.core.data.server

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*
import javax.inject.Inject
import javax.inject.Singleton


/**
 * Singleton class for handle Firebase Realtime Database Actions
 */
@Singleton
class FirebaseHandler @Inject constructor() {

    val database = FirebaseDatabase.getInstance()
    val rootRef = database.reference

    fun getChildTrackMapId(trackMapId: String): DatabaseReference {
        return rootRef.child("trackMaps/$trackMapId/liveParticipantIds")
    }

    fun getChildUserId(userId: String): DatabaseReference {
        return rootRef.child("users/$userId")
    }

    fun init(context: Context) {
        FirebaseApp.initializeApp(context)

        // Add Global ValueEventListener
        rootRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                //this.debug("Changes in Firebase Realtime database: " + dataSnapshot.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                //this.error("Failed Firebase operation.")
                //throw error.toException()
            }
        })
    }
}
