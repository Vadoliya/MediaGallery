package com.mediagallery.hashone.gallery

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class CoroutineAsyncTask<Params, Progress, Result> {

    open fun onPreExecute() {}

    abstract fun doInBackground(vararg params: Params?): Result?

    open fun onPostExecute(result: Result?) {}

    protected var isCancelled = false

    fun execute(vararg params: Params?) {

        onPreExecute()

        GlobalScope.launch(Dispatchers.Default) {
            val result = doInBackground(*params)

            withContext(Dispatchers.Main) {
                onPostExecute(result)
            }
        }
    }

}