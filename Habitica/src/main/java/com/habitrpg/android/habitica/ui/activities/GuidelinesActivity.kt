package com.habitrpg.android.habitica.ui.activities

import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.common.habitica.helpers.setMarkdown
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class GuidelinesActivity : BaseActivity() {
    override fun getLayoutResId(): Int = R.layout.activity_guidelines

    override fun injectActivity(component: UserComponent?) {
        component?.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupToolbar(findViewById(R.id.toolbar))

        val client = OkHttpClient()
        val request = Request.Builder().url("https://s3.amazonaws.com/habitica-assets/mobileApp/endpoint/community-guidelines.md").build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                RxErrorHandler.reportError(e)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val `in` = response.body?.byteStream()
                val reader = BufferedReader(InputStreamReader(`in`))
                val text = reader.readText()
                response.body?.close()

                findViewById<TextView>(R.id.text_view).post {
                    findViewById<TextView>(R.id.text_view).setMarkdown(text)
                }
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            onBackPressed()
            true
        } else super.onOptionsItemSelected(item)
    }
}
