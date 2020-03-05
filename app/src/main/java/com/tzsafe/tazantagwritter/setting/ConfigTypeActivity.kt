package com.tzsafe.tazantagwritter.setting

import android.content.Context
import android.content.Intent
import android.media.Image
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.tzsafe.tazantagwritter.R
import com.tzsafe.tazantagwritter.constant.Constant
import com.tzsafe.tazantagwritter.entity.ConfigTypeModel
import kotlinx.android.synthetic.main.activity_config_type.*
import kotlinx.android.synthetic.main.config_type_item.view.*

class ConfigTypeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config_type)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        initDatas();
    }

    private fun initDatas() {

        var types = ArrayList<ConfigTypeModel>()


        types.add(ConfigTypeModel(0, "文本"))
        types.add(ConfigTypeModel(1, "网址"))
        types.add(ConfigTypeModel(2, "应用程序"))

        lv_config_types.adapter = ConfigTypeAdapter(this, types)

        lv_config_types.setOnItemClickListener { adapterView, view, i, l ->
            val intent = Intent(this, AddConfigActivity::class.java)
            intent.putExtra("type", types[i].type)
            startActivity(intent)
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


}

class ConfigTypeAdapter(var context: Context, var list: List<ConfigTypeModel>) : BaseAdapter() {

    var inflater: LayoutInflater = LayoutInflater.from(context)


    override fun getView(p0: Int, convertView: View?, p2: ViewGroup?): View {

        var mConvertView = convertView

        mConvertView = inflater.inflate(R.layout.config_type_item, p2, false)

        mConvertView.config_type_image.setImageResource(Constant.TYPES_ICON[list[p0].type])
        mConvertView.config_type_name.text = Constant.TYPES[list[p0].type]
        mConvertView.config_type_subtext.text = Constant.TYPES_DESCRIBE[list[p0].type]

        return mConvertView!!;

    }

    override fun getItem(p0: Int): ConfigTypeModel {
        return list[p0]
    }

    override fun getItemId(p0: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return list.size ?: 0;
    }


}
