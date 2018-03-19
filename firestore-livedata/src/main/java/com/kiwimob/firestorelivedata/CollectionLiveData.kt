package com.kiwimob.firestorelivedata

import android.arch.lifecycle.LiveData
import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot

fun <T> CollectionReference.livedata(clazz: Class<T>): LiveData<List<T>> {
    return CollectionLiveData(this, { documentSnapshot -> documentSnapshot.toObject(clazz) })
}

fun <T> CollectionReference.livedata(parser: (documentSnapshot: DocumentSnapshot) -> T): LiveData<List<T>> {
    return CollectionLiveData(this, parser)
}

fun CollectionReference.livedata() : LiveData<QuerySnapshot> {
    return CollectionLiveDataRaw(this)
}

class CollectionLiveData<T>(private val collectionReference: CollectionReference,
                            private val parser: (documentSnapshot: DocumentSnapshot) -> T) : LiveData<List<T>>() {

    private var listener: ListenerRegistration? = null

    override fun onActive() {
        super.onActive()

        listener = collectionReference.addSnapshotListener({ querySnapshot, exception ->
            if (exception == null) {
                value = querySnapshot.documents.map { parser.invoke(it) }
            } else {
                Log.e("FireStoreLiveData", "", exception)
            }
        })
    }

    override fun onInactive() {
        super.onInactive()

        listener?.remove()
        listener = null
    }
}

class CollectionLiveDataRaw(private val collectionReference: CollectionReference) : LiveData<QuerySnapshot>() {

    private var listener: ListenerRegistration? = null

    override fun onActive() {
        super.onActive()

        listener = collectionReference.addSnapshotListener({ querySnapshot, exception ->
            if (exception == null) {
                value = querySnapshot
            } else {
                Log.e("FireStoreLiveData", "", exception)
            }
        })
    }

    override fun onInactive() {
        super.onInactive()

        listener?.remove()
        listener = null
    }
}