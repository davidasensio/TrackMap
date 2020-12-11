package com.handysparksoft.trackmap.core.data.server

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*
import javax.inject.Inject
import javax.inject.Singleton

//import com.handysparksoft.trackmap.model.TrackMap
//import com.handysparksoft.trackmap.model.TrackMarker
//import com.handysparksoft.trackmap.model.TrackWayPoint
//import com.handysparksoft.trackmap.util.Utils
//import com.handysparksoft.trackmap.util.debug
//import com.handysparksoft.trackmap.util.error

/**
 * Singleton class for handle Firebase Realtime Database Actions
 */
@Singleton
class FirebaseHandler @Inject constructor() {

    interface OnTrackMapListener {
        fun onTrackMapChanges(dataSnapshot: DataSnapshot)
    }

    companion object {


        private val TAG = FirebaseHandler::class.java.simpleName
        private const val PATH_ROOT = "/trackmaps/"
        private const val PATH_TRACK_MARKERS = "/trackMarkers/"
        private const val PATH_TRACK_WAY_POINTS = "/trackWayPoints/"
        private const val PATH_POSITION = "/position/"
        private const val PATH_ACTIVE = "/active/"

    }

    val database = FirebaseDatabase.getInstance()
    val rootRef = database.reference
    private val trackMapSubscriptors = mutableListOf<OnTrackMapListener>()

    fun getChildTrackMapId(trackMapId: String): DatabaseReference {
        return rootRef.child("trackMaps/$trackMapId/participantIds")
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
                throw error.toException()
            }
        })
    }

    fun subscribeToTrackMap(listener: OnTrackMapListener) {
        trackMapSubscriptors.add(listener)
    }

    fun unsubscribeFromTrackMap(listener: OnTrackMapListener) {
        trackMapSubscriptors.remove(listener)
    }

//    fun addMap(name: String, description: String): String {
//        val trackMapCode = Utils.getShortUUID("trackMap")
//        val trackMap = TrackMap(trackMapCode, name, description, "admin", true)
//
//        val trackMapChild = getRootChild().child(trackMapCode)
//        trackMapChild.setValue(trackMap)
//
//        // Add TrackMap ValueEventListener
//        addValueEventListenerToTrackMap(
//            trackMapChild
//        )
//
//        // Clear past subscriptors
//        trackMapSubscriptors.clear()
//
//        return trackMapCode
//    }
//
//    private fun addValueEventListenerToTrackMap(trackMapChild: DatabaseReference) {
//        trackMapChild.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                this.debug("Changes in Firebase Realtime database: " + dataSnapshot.toString())
//
//                trackMapSubscriptors.forEach {
//                    it.onTrackMapChanges(dataSnapshot)
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                this.error("Failed Firebase operation.")
//                throw error.toException()
//            }
//        })
//    }
//
//    fun addWayPoint(mapCode: String, name: String, description: String, position: String) {
//        val trackWayPointCode = Utils.getShortUUID("trackWayPoint")
//        val trackWayPoint =
//            TrackWayPoint(trackWayPointCode, name, description, position)
//
//        getRootChild()
//            .child(mapCode).child(PATH_TRACK_WAY_POINTS)
//            .child(trackWayPoint.code)
//            .setValue(trackWayPoint)
//    }
//
//    fun joinToMap(mapCode: String, name: String, description: String, position: String): String {
//        val trackMarkerCode = Utils.getShortUUID("trackMarker")
//        val trackMarker =
//            TrackMarker(trackMarkerCode, name, description, Utils.now(), "", true, true, position)
//
//        val trackMapChild = getRootChild().child(mapCode)
//        trackMapChild.child(PATH_TRACK_MARKERS)
//            .child(trackMarker.code)
//            .setValue(trackMarker)
//
//        // Add TrackMap ValueEventListener
//        addValueEventListenerToTrackMap(
//            trackMapChild
//        )
//
//        return trackMarkerCode
//    }
//
//    fun updateMyPosition(mapCode: String, trackerMarkerCode: String, position: String) {
//        getRootChild()
//            .child(mapCode).child(PATH_TRACK_MARKERS)
//            .child(trackerMarkerCode)
//            .child(PATH_POSITION)
//            .setValue(position)
//    }
//
//    fun leaveTrackMap(mapCode: String, trackerMarkerCode: String) {
//        getRootChild()
//            .child(mapCode).child(PATH_TRACK_MARKERS)
//            .child(trackerMarkerCode)
//            .child(PATH_ACTIVE)
//            .setValue(false)
//    }
//
//    fun deactivateTrackMap(mapCode: String) {
//        getRootChild()
//            .child(mapCode).child(PATH_ACTIVE).setValue(false)
//    }
//
//
//    private fun getRootChild() = rootRef.child(
//        PATH_ROOT
//    )
}
