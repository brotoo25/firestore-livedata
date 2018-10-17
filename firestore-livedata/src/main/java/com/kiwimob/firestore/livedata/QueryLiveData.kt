package com.kiwimob.firestore.livedata

import android.arch.lifecycle.LiveData
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.launch

fun <T> Query.livedata(clazz: Class<T>): LiveData<List<T>> {
    return QueryLiveDataNative(this, clazz)
}

fun <T> Query.livedata(parser: suspend (documentSnapshot: DocumentSnapshot) -> T): LiveData<List<T>> {
    return QueryLiveDataCustom(this, parser)
}

fun Query.livedata(): LiveData<QuerySnapshot> {
    return QueryLiveDataRaw(this)
}

private class QueryLiveDataNative<T>(private val query: Query,
                                     private val clazz: Class<T>) : LiveData<List<T>>() {

    private var listener: ListenerRegistration? = null

    override fun onActive() {
        super.onActive()

        listener = query.addSnapshotListener { querySnapshot, exception ->
            if (exception == null) {
                value = querySnapshot?.documents?.map { it.toObject(clazz)!! }
            } else {
                Log.e("FireStoreLiveData", "", exception)
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
                                     private val parser: suspend (documentSnapshot: DocumentSnapshot) -> T) : LiveData<List<T>>() {

    private var listener: ListenerRegistration? = null

    override fun onActive() {
        super.onActive()

        listener = query.addSnapshotListener { querySnapshot, exception ->
            if (exception == null) {
                GlobalScope.launch { value = querySnapshot?.documents?.map { parser.invoke(it) } }
            } else {
                Log.e("FireStoreLiveData", "", exception)
            }
        }
    }

    override fun onInactive() {
        super.onInactive()

        listener?.remove()
        listener = null
    }
}

private class QueryLiveDataRaw(private val query: Query) : LiveData<QuerySnapshot>() {

    private var listener: ListenerRegistration? = null

    override fun onActive() {
        super.onActive()

        listener = query.addSnapshotListener { querySnapshot, exception ->
            if (exception == null) {
                value = querySnapshot
            } else {
                Log.e("FireStoreLiveData", "", exception)
            }
        }
    }

    override fun onInactive() {
        super.onInactive()

        listener?.remove()
        listener = null
    }
}