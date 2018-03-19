package uy.com.arara.data.datasource.firebase

import android.arch.lifecycle.LiveData
import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot

fun <T> Query.livedata(clazz: Class<T>): LiveData<List<T>> {
    return QueryLiveData(this, { documentSnapshot -> documentSnapshot.toObject(clazz) })
}

fun <T> Query.livedata(parser: (documentSnapshot: DocumentSnapshot) -> T): LiveData<List<T>> {
    return QueryLiveData(this, parser)
}

fun Query.livedata(): LiveData<QuerySnapshot> {
    return QueryLiveDataRaw(this)
}

class QueryLiveData<T>(private val query: Query,
                       private val parser: (documentSnapshot: DocumentSnapshot) -> T) : LiveData<List<T>>() {

    private var listener: ListenerRegistration? = null

    override fun onActive() {
        super.onActive()

        listener = query.addSnapshotListener({ querySnapshot, exception ->
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

class QueryLiveDataRaw(private val query: Query) : LiveData<QuerySnapshot>() {

    private var listener: ListenerRegistration? = null

    override fun onActive() {
        super.onActive()

        listener = query.addSnapshotListener({ querySnapshot, exception ->
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