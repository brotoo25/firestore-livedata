package com.kiwimob.firestore.livedata

/**
 * QueryStatus Class can be used to communicate the state of a query.
 */
class QueryStatus<R>() {
    var state:States = States.Created
    var answer: R? = null
    var msg: String? = null
    var error: Throwable? = null

    constructor(result: R?) : this() {
        this.setSuccessfulResult(result)
    }

    constructor(error: Throwable): this() {
        this.setError(error)
    }


    fun setSuccessfulResult(result: R?, msg: String = "Query Successful"): QueryStatus<R> {
        return this.apply {
            state = States.Done
            this.msg = msg
            answer = result
        }
    }

    fun setError(e: Throwable, msg: String = "Error"): QueryStatus<R> {
        return this.apply {
            state = States.Error
            this.msg = msg
            answer = null
            error = e
        }
    }

    fun isSuccessful() = state == States.Done
    fun isFailed() = state == States.Error

}

enum class States {
    Created,
    Done,
    Error
}