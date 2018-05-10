package com.kiwimob.firestore.livedata

import android.arch.lifecycle.LiveData
import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch

fun <T> DocumentReference.livedata(clazz: Class<T>): LiveData<T> {
    return DocumentLiveDataNative(this, clazz)
}

fun <T> DocumentReference.livedata(parser: suspend (documentSnapshot: DocumentSnapshot) -> T): LiveData<T> {
    return DocumentLiveDataCustom(this, parser)
}

fun DocumentReference.livedata(): LiveData<DocumentSnapshot> {
    return DocumentLiveDataRaw(this)
}

private class DocumentLiveDataNative<T>(private val documentReference: DocumentReference,
                                        private val clazz: Class<T>) : LiveData<T>() {

    private var listener: ListenerRegistration? = null

    override fun onActive() {
        super.onActive()

        listener = documentReference.addSnapshotListener({ documentSnapshot, exception ->
            if (exception == null) {
                documentSnapshot?.let {
                    value = it.toObject(clazz)
                }
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

class DocumentLiveDataCustom<T>(private val documentReference: DocumentReference,
                                private val parser: suspend (documentSnapshot: DocumentSnapshot) -> T) : LiveData<T>() {

    private var listener: ListenerRegistration? = null

    override fun onActive() {
        super.onActive()

        listener = documentReference.addSnapshotListener({ documentSnapshot, exception ->
            if (exception == null) {
                documentSnapshot?.let {
                    launch(UI) { value = parser.invoke(it) }
                }
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