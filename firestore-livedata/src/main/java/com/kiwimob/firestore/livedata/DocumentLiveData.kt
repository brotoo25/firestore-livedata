package com.kiwimob.firestore.livedata

import android.arch.lifecycle.LiveData
import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration

fun <T> DocumentReference.livedata(clazz: Class<T>): LiveData<T> {
    return DocumentLiveData(this, { documentSnapshot -> documentSnapshot.toObject(clazz) })
}

fun <T> DocumentReference.livedata(parser: (documentSnapshot: DocumentSnapshot) -> T): LiveData<T> {
    return DocumentLiveData(this, parser)
}

fun DocumentReference.livedata() : LiveData<DocumentSnapshot> {
    return DocumentLiveDataRaw(this)
}

class DocumentLiveData<T>(private val documentReference: DocumentReference,
                            private val parser: (documentSnapshot: DocumentSnapshot) -> T) : LiveData<T>() {

    private var listener: ListenerRegistration? = null

    override fun onActive() {
        super.onActive()

        listener = documentReference.addSnapshotListener({ documentSnapshot, exception ->
            if (exception == null) {
                value = parser.invoke(documentSnapshot)
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

class DocumentLiveDataRaw(private val documentReference: DocumentReference) : LiveData<DocumentSnapshot>() {

    private var listener: ListenerRegistration? = null

    override fun onActive() {
        super.onActive()

        listener = documentReference.addSnapshotListener({ documentSnapshot, exception ->
            if (exception == null) {
                value = documentSnapshot
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