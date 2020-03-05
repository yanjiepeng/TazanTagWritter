package com.tzsafe.tazantagwritter.setting

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.BaseAdapter
import androidx.appcompat.app.AlertDialog
import com.tzsafe.tazantagwritter.R
import com.tzsafe.tazantagwritter.app.ObjectBox
import com.tzsafe.tazantagwritter.constant.Constant
import com.tzsafe.tazantagwritter.entity.ConfigItem
import com.tzsafe.tazantagwritter.entity.ConfigTypeModel
import io.objectbox.Box
import io.objectbox.kotlin.boxFor
import kotlinx.android.synthetic.main.activity_config.*
import kotlinx.android.synthetic.main.config_type_item.view.*

class ConfigActivity : AppCompatActivity() {


    lateinit var box: Box<ConfigItem>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_config)


        box = ObjectBox.boxStore.boxFor()

        tv_add_config.setOnClickListener {
            startActivity(Intent(this, ConfigTypeActivity::class.java))
        }


    }


    override fun onResume() {
        super.onResume()


        val data = box.all

        val adapter = ConfigListAdapter(this, data)

        lv_config_items.adapter = adapter


        lv_config_items.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, pos, _ ->

                val titles = arrayOf("删除", "编辑", "取消")

                AlertDialog.Builder(this, R.style.materialAlertDialogTheme)
                    .setTitle("选择")
                    .setItems(titles, DialogInterface.OnClickListener { dialogInterface, i ->

                        when (i) {

                            0 -> {
                                box.remove(data[pos])
                                data.clear()
                                data.addAll(box.all)
                                adapter.notifyDataSetChanged()
                            }

                            1 -> {
                                //编辑


                            }

                            2 -> dialogInterface.dismiss()
                        }


                    }).create().show()


            }


    }
}

class ConfigListAdapter(var context: Context, var list: List<ConfigItem>) : BaseAdapter() {

    var inflater: LayoutInflater = LayoutInflater.from(context)


    override fun getView(p0: Int, convertView: View?, p2: ViewGroup?): View {

        var mConvertView = convertView

        mConvertView = inflater.inflate(R.layout.config_type_item, p2, false)

        mConvertView.config_type_image.setImageResource(Constant.TYPES_ICON[list[p0].type])
        mConvertView.config_type_name.text = Constant.TYPES[list[p0].type]
        mConvertView.config_type_subtext.text = list[p0].text

        return mConvertView!!;

    }

    override fun getItem(p0: Int): ConfigItem {
        return list[p0]
    }

    override fun getItemId(p0: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return list.size ?: 0;
    }


}
