package com.tzsafe.tazantagwritter.appinfo

import android.R
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.SimpleAdapter
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import java.util.*


class GetAppInfoActivity : AppCompatActivity() {

    private lateinit var data: MutableList<Map<String, Any>>
    private lateinit var item: Map<String, Any>
    private lateinit var listView: ListView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listView = ListView(this)

        data = ArrayList<Map<String, Any>>()


        val pd = ProgressDialog(this)
        pd.setTitle("读取中")
        pd.show()


        GlobalScope.launch(Dispatchers.Unconfined) {

            val deferred = GlobalScope.async {
                listPackages()
            }

            val result = deferred.await()


            runOnUiThread() {

                pd.dismiss()
                val adapter = SimpleAdapter(
                    this@GetAppInfoActivity,
                    result,
                    R.layout.simple_list_item_2,
                    arrayOf("appname", "pname"),
                    intArrayOf(R.id.text1, R.id.text2)
                )
                listView.adapter = adapter
                setContentView(listView)
                listView.setOnItemClickListener { adapterView, view, i, l ->

                    var intent = Intent()
                    intent.putExtra("packageName", result[i]["pname"] as String)
                    setResult(Activity.RESULT_OK, intent)
                    this@GetAppInfoActivity.finish()
                }

            }


        }


    }


    internal class PInfo {
        var appname = ""
        var pname = ""
        var versionName = ""
        var versionCode = 0
        var icon: Drawable? = null
        fun prettyPrint() {
            Log.i(
                "taskmanger", appname + "\t" + pname + "\t" + versionName
                        + "\t" + versionCode + "\t"
            )
        }
    }


    private suspend fun listPackages(): MutableList<Map<String, Any>> {
        val apps: ArrayList<PInfo>? = getInstalledApps(false)
        val size: Int = apps?.size ?: 0

        for (i in 0 until size) {
            apps?.get(i)?.prettyPrint()
            item = mutableMapOf("appname" to apps!![i].appname, "pname" to apps[i].pname)
            data.add(item)
        }

        return data
    }

    private fun getInstalledApps(getSysPackages: Boolean): ArrayList<PInfo>? {
        val res = ArrayList<PInfo>()
        val packs =
            packageManager.getInstalledPackages(0)
        for (i in packs.indices) {
            val p = packs[i]
            if (!getSysPackages && p.versionName == null) {
                continue
            }
            val newInfo = PInfo()
            newInfo.appname = p.applicationInfo.loadLabel(packageManager)
                .toString()
            newInfo.pname = p.packageName
            newInfo.versionName = p.versionName
            newInfo.versionCode = p.versionCode
            newInfo.icon = p.applicationInfo.loadIcon(packageManager)
            res.add(newInfo)
        }
        return res
    }

}