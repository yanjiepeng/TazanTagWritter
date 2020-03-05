package com.tzsafe.tazantagwritter.setting

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tzsafe.tazantagwritter.R
import com.tzsafe.tazantagwritter.app.ObjectBox
import com.tzsafe.tazantagwritter.appinfo.GetAppInfoActivity
import com.tzsafe.tazantagwritter.entity.ConfigItem
import io.objectbox.Box
import io.objectbox.kotlin.boxFor
import kotlinx.android.synthetic.main.activity_add_config.*


class AddConfigActivity : AppCompatActivity() {


    private lateinit var http_prefix: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_config)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val type = intent.getIntExtra("type", -1);

        if (type == 1) {

            iv_add_config_headimg.setImageResource(R.drawable.ic_http_black_24dp)
            iv_add_config_typename.text = "请输入URL"
            //是网址类型 则显示spinner 否则不显示
            spinner_http.visibility = View.VISIBLE
            tv_choose_app.visibility = View.GONE
            tv_add_content_text.hint = "输入网址"

            spinner_http.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?, view: View,
                    pos: Int, id: Long
                ) {
                    val httpPrefix = resources.getStringArray(R.array.http)
                    http_prefix = httpPrefix[pos]
                }

                override fun onNothingSelected(parent: AdapterView<*>?) { // Another interface callback
                    http_prefix = resources.getStringArray(R.array.http)[0]

                }
            }


        } else {
            spinner_http.visibility = View.GONE
            if (type == 0) {
                iv_add_config_typename.text = "请输入文本"
                iv_add_config_headimg.setImageResource(R.drawable.ic_text_fields_black_24dp)
                tv_add_content_text.hint = "输入文本"
                tv_choose_app.visibility = View.GONE

            } else if (type == 2) {
                iv_add_config_typename.text = "请输入应用程序包名"
                iv_add_config_headimg.setImageResource(R.drawable.ic_apps_black_24dp)
                tv_add_content_text.hint = "输入应用程序包名或点击右侧按钮选择"
                tv_choose_app.visibility = View.VISIBLE

                tv_choose_app.setOnClickListener {
                    startActivityForResult(Intent(this, GetAppInfoActivity::class.java), 100)
                }
            }
        }


        btn_confirm.setOnClickListener {

            //根据类型需要组装数据
            val text: String = if (type == 1) {
                //网址需要加上HTTP等前缀
                "$http_prefix${tv_add_content_text.text.toString().trim()}"

            } else {
                tv_add_content_text.text.toString().trim()
            }
            val configItem = ConfigItem()
            configItem.type = type
            configItem.text = text
            val configBox: Box<ConfigItem> = ObjectBox.boxStore.boxFor()
            configBox.put(configItem)
            Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show()
        }

        btn_cancel.setOnClickListener {

            this.onBackPressed()
        }


    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {

            android.R.id.home -> {
                onBackPressed()
            }

        }

        return super.onOptionsItemSelected(item)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            100 -> {

                if (resultCode == Activity.RESULT_OK) {
                    val pName = data?.getStringExtra("packageName")

                    tv_add_content_text.setText(pName)
                }
            }
        }

    }
}
