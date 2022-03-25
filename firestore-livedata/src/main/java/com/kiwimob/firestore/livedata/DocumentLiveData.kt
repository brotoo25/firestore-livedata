package com.kiwimob.firestore.livedata

import androidx.lifecycle.LiveData
import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration

fun <T> DocumentReference.livedata(clazz: Class<T>): LiveData<QueryStatus<T>> {
    return DocumentLiveDataNative(this, clazz)
}

fun <T> DocumentReference.livedata(parser: (documentSnapshot: DocumentSnapshot) -> T): LiveData<QueryStatus<T>> {
    return DocumentLiveDataCustom(this, parser)
}

fun DocumentReference.livedata(): LiveData<QueryStatus<DocumentSnapshot>> {
    return DocumentLiveDataRaw(this)
}

private class DocumentLiveDataNative<T>(private val documentReference: DocumentReference,
                                        private val clazz: Class<T>) : LiveData<QueryStatus<T>>() {

    private var listener: ListenerRegistration? = null

    override fun onActive() {
        super.onActive()

        listener = documentReference.addSnapshotListener { documentSnapshot, exception ->
            if (exception == null) {
                documentSnapshot?.let {
                    value = QueryStatus(it.toObject(clazz))
                }
            } else {
                Log.e("FireStoreLiveData", "", exception)
                value = QueryStatus(exception)
            }
        }
    }

    override fun onInactive() {
        super.onInactive()

        listener?.remove()
        listener = null
    }
}

class DocumentLiveDataCustom<T>(private val documentReference: DocumentReference,
                                private val parser: (documentSnapshot: DocumentSnapshot) -> T) : LiveData<QueryStatus<T>>() {

    private var listener: ListenerRegistration? = null

    override fun onActive() {
        super.onActive()

        listener = documentReference.addSnapshotListener { documentSnapshot, exception ->
            if (exception == null) {
                documentSnapshot?.let {
                    value = QueryStatus(parser.invoke(it))
                }
            } else {
                Log.e("FireStoreLiveData", "", exception)
                value = QueryStatus(exception)
            }
        }
    }

    override fun onInactive() {
        super.onInactive()

        listener?.remove()
        listener = null
    }
}

class DocumentLiveDataRaw(private val documentReference: DocumentReference) : LiveData<QueryStatus<DocumentSnapshot>>() {

    private var listener: ListenerRegistration? = null

    override fun onActive() {
        super.onActive()

        listener = documentReference.addSnapshotListener { documentSnapshot, exception ->
            if (exception == null) {
                value = QueryStatus(documentSnapshot)
            } else {
                Log.e("FireStoreLiveData", "", exception)
                value = QueryStatus(exception)
            }
        }
    }

    override fun onInactive() {
        super.onInactive()

        listener?.remove()
        listener = null
    }
}