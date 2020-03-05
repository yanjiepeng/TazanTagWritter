package com.tzsafe.tazantagwritter.setting

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.tzsafe.tazantagwritter.R
import kotlinx.android.synthetic.main.activity_password.*

class PasswordActivity : AppCompatActivity() {

    lateinit var sp: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        sp = getSharedPreferences("pwd", Context.MODE_PRIVATE)

        val pwd = sp.getString("pwd", "");
        if (pwd != null && pwd.isNotBlank()) {
            editText_password.setText(pwd)
        }

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                saveConfig()

            }

        }

        return super.onOptionsItemSelected(item)

    }


    //保存密码
    private fun saveConfig() {
        val pwd = editText_password.text.toString().trim()
        val editor = sp.edit()
        editor.putString("pwd", pwd)
        editor.apply()
    }
}
