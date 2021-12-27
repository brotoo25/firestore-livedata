package com.kiwimob.firestore.livedata

import androidx.lifecycle.LiveData
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

fun <T> Query.livedata(clazz: Class<T>): LiveData<QueryStatus<List<T>>> {
    return QueryLiveDataNative(this, clazz)
}

fun <T> Query.livedata(parser: (documentSnapshot: DocumentSnapshot) -> T): LiveData<QueryStatus<List<T>>> {
    return QueryLiveDataCustom(this, parser)
}

fun Query.livedata(): LiveData<QueryStatus<QuerySnapshot>> {
    return QueryLiveDataRaw(this)
}

private class QueryLiveDataNative<T>(private val query: Query,
                                     private val clazz: Class<T>) : LiveData<QueryStatus<List<T>>>() {

    private var listener: ListenerRegistration? = null

    override fun onActive() {
        super.onActive()

        listener = query.addSnapshotListener { querySnapshot, exception ->
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

private class QueryLiveDataCustom<T>(private val query: Query,
                                     private val parser: (documentSnapshot: DocumentSnapshot) -> T) : LiveData<QueryStatus<List<T>>>() {

    private var listener: ListenerRegistration? = null

    override fun onActive() {
        super.onActive()

        listener = query.addSnapshotListener { querySnapshot, exception ->
            value = if (exception == null) {
                QueryStatus(querySnapshot?.documents?.map { parser.invoke(it) })
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

private class QueryLiveDataRaw(private val query: Query) : LiveData<QueryStatus<QuerySnapshot>>() {

    private var listener: ListenerRegistration? = null

    override fun onActive() {
        super.onActive()

        listener = query.addSnapshotListener { querySnapshot, exception ->
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