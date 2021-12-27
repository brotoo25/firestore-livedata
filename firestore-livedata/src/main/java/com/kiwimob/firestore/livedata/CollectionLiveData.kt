package com.kiwimob.firestore.livedata

import androidx.lifecycle.LiveData
import android.util.Log
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import java.util.*

fun <T> CollectionReference.livedata(clazz: Class<T>): LiveData<QueryStatus<List<T>>> {
    return CollectionLiveDataNative(this, clazz)
}

fun <T> CollectionReference.livedata(parser: (documentSnapshot: DocumentSnapshot) -> T): LiveData<QueryStatus<List<T>>> {
    return CollectionLiveDataCustom(this, parser)
}

fun CollectionReference.livedata(): LiveData<QueryStatus<QuerySnapshot>> {
    return CollectionLiveDataRaw(this)
}

private class CollectionLiveDataNative<T>(private val collectionReference: CollectionReference,
                                          private val clazz: Class<T>) : LiveData<QueryStatus<List<T>>>() {

    private var listener: ListenerRegistration? = null

    override fun onActive() {
        super.onActive()

        listener = collectionReference.addSnapshotListener { querySnapshot, exception ->
            value = if (exception == null) {
                QueryStatus(querySnapshot?.documents?.map { it.toObject(clazz)!! })
            } else {
                Log.e("FireStoreLiveData", "", exception)
                QueryStatus(exception)
            }
        }
    }

    override fun onInactive() {
        super.onInactive()

        listener?.remove()
        listener = null
    }
}

private class CollectionLiveDataCustom<T>(private val collectionReference: CollectionReference,
                                          private val parser: (documentSnapshot: DocumentSnapshot) -> T) : LiveData<QueryStatus<List<T>>>() {

    private var listener: ListenerRegistration? = null

    override fun onActive() {
        super.onActive()

        listener = collectionReference.addSnapshotListener { querySnapshot, exception ->
            value = if (exception == null) {
                QueryStatus(
                    querySnapshot?.documents?.map { parser.invoke(it)!! }
                    ?: Collections.emptyList()
                )
            } else {
                Log.e("FireStoreLiveData", "", exception)
                QueryStatus(exception)
            }
        }
    }

    override fun onInactive() {
        super.onInactive()

        listener?.remove()
        listener = null
    }
}

private class CollectionLiveDataRaw(private val collectionReference: CollectionReference) : LiveData<QueryStatus<QuerySnapshot>>() {

    private var listener: ListenerRegistration? = null

    override fun onActive() {
        super.onActive()

        listener = collectionReference.addSnapshotListener { querySnapshot, exception ->
            value = if (exception == null) {
                QueryStatus(querySnapshot)
            } else {
                Log.e("FireStoreLiveData", "", exception)
                QueryStatus(exception)
            }
        }
    }

    override fun onInactive() {
        super.onInactive()

        listener?.remove()
        listener = null
    }
}