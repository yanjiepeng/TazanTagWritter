package com.tzsafe.tazantagwritter

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.nfc.*
import android.nfc.tech.MifareUltralight
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.king.zxing.CaptureActivity
import com.king.zxing.Intents
import com.tbruyelle.rxpermissions2.RxPermissions
import com.tzsafe.tazantagwritter.app.ObjectBox
import com.tzsafe.tazantagwritter.entity.ConfigItem
import com.tzsafe.tazantagwritter.setting.ConfigActivity
import com.tzsafe.tazantagwritter.setting.PasswordActivity
import io.objectbox.Box
import io.objectbox.kotlin.boxFor
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.nio.charset.Charset
import java.util.*
import kotlin.experimental.and
import kotlin.experimental.or


/**
 * 主界面
 */
class MainActivity : AppCompatActivity() {


    lateinit var mNfcAdapter: NfcAdapter
    lateinit var mPendingIntent: PendingIntent
    lateinit var box: Box<ConfigItem>
    var dialog: AlertDialog? = null

    val ACTION_WRITE = 1000;
    val ACTION_DELETE_PWD = 1001;
    var NFC_ACTION: Int = 0;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkPermissions()
        box = ObjectBox.boxStore.boxFor()

//        initTapLinxLib()

        btn_scan_qr.setOnClickListener {
            val intent = Intent(this, CaptureActivity::class.java)
            startActivityForResult(intent, 102)
        }

        btn_write_nfc.setOnClickListener {
            NFC_ACTION = ACTION_WRITE
            showWriteDialog()
        }

        btn_password_manage.setOnClickListener {
            startActivity(Intent(this, PasswordActivity::class.java))
        }

        btn_delete_pwd.setOnClickListener {
            NFC_ACTION = ACTION_DELETE_PWD
            showDeleteDialog()

        }
    }

    private fun showDeleteDialog() {

        dialog =
            AlertDialog.Builder(this).setTitle("靠近要删除密码的NFC卡片").setView(R.layout.write_nfc_layout)
                .setPositiveButton(
                    "取消",
                    DialogInterface.OnClickListener { dialogInterface, _ -> dialogInterface.dismiss() })
                .create()
        dialog!!.show()

    }


    private fun showWriteDialog() {

        dialog = AlertDialog.Builder(this).setTitle("在NFC Tag上写").setView(R.layout.write_nfc_layout)
            .setPositiveButton(
                "取消",
                DialogInterface.OnClickListener { dialogInterface, _ -> dialogInterface.dismiss() })
            .create()
        dialog!!.show()

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        val tag: Tag? = intent!!.getParcelableExtra(NfcAdapter.EXTRA_TAG)

        if (NFC_ACTION == ACTION_WRITE) {

            if (tag != null && dialog != null && dialog!!.isShowing) {
                //写入ndef数据
                writeNfcTag(tag)
            } else {
                Toast.makeText(this, "卡片不支持", Toast.LENGTH_SHORT).show()
            }

        } else if (NFC_ACTION == ACTION_DELETE_PWD) {
            //删除NFC密码
            val mfc = MifareUltralight.get(tag);

            deletePassword(mfc)
        }
    }


    /**
     * 删除NFC设置的密码保护
     */
    private fun deletePassword(mfc: MifareUltralight) {

        val sp = getSharedPreferences("pwd", Context.MODE_PRIVATE)

        val pwdstr = sp.getString("pwd", "");
        //创建默认为0的4字节数组
        val pwd = Array<Byte>(4) { ((0).toByte()) }
        val temp = pwdstr?.toByteArray()
        if (temp != null) {
            for ((index, e) in temp.withIndex()) {
                pwd[index] = temp[index]
            }
        }


        //得出的PWD即用户设置的密码


        mfc.connect()

        val pwd_default = byteArrayOf(0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte())
        val pack = byteArrayOf(0.toByte(), 0.toByte())

        try {
            //用用户设置的密码询问登录
            val response = mfc.transceive(
                byteArrayOf(
                    0x1B, pwd[0], pwd[1], pwd[2], pwd[3]
                )
            )

            // Check if PACK is matching expected PACK
            // This is a (not that) secure method to check if tag is genuine
            if ((response != null) && (response.size >= 2)) {
                val packResponse = Arrays.copyOf(response, 2);
                if (!(pack[0] == packResponse[0] && pack[1] == packResponse[1])) {
                    Toast.makeText(
                        this@MainActivity,
                        "Tag could not be authenticated:\n$packResponse≠$pack",
                        Toast.LENGTH_LONG
                    ).show();
                } else {
                    Toast.makeText(this@MainActivity, "密码校验正确", Toast.LENGTH_LONG).show()
                }
            } else {

            }

            //pack置为默认
            mfc.transceive(
                byteArrayOf(
                    0xA2.toByte(),
                    0x2C, /*PAGE 44*/
                    pack[0], pack[1], 0, 0  // Write PACK into first 2 Bytes and 0 in RFUI bytes
                )
            )

            //pwd置为默认
            mfc.transceive(
                byteArrayOf(
                    0xA2.toByte(),
                    0x2B,  /*PAGE 43*/
                    pwd_default[0],
                    pwd_default[1],
                    pwd_default[2],
                    pwd_default[3]  // Write PACK into first 2 Bytes and 0 in RFUI bytes
                )
            )

            // set AUTHLIM:
            //将AUTHLIM（第42页，字节0，位2-0）设置为失败的最大密码验证尝试次数
            val responseAuthLim = mfc.readPages(42)
            if (responseAuthLim != null && responseAuthLim.size >= 16) {
                val prot =
                    false  // false = PWD_AUTH for write only, true = PWD_AUTH for read and write
                val authLim = 0;  //0-7

                mfc.transceive(
                    byteArrayOf(
                        0xA2.toByte(),
                        42,
                        (responseAuthLim[0] and 0x078 or (if (prot) 0x080.toByte() else 0x000) or ((authLim and 0x007).toByte())).toByte(),
                        responseAuthLim[1],
                        responseAuthLim[2],
                        responseAuthLim[3]

                        //将1-3位按原数据写会
                    )
                )
            }

            //设置Auth0 如果auth0设置为FF则为禁用密码保护
            val responseAuth0 = mfc.readPages(41)

            if (responseAuth0 != null && responseAuth0.size >= 16) {

                mfc.transceive(
                    byteArrayOf(
                        0xA2.toByte(),
                        41,
                        responseAuthLim[0],
                        responseAuthLim[1],
                        responseAuthLim[2],

                        //将0-2位按原数据写会
                        0x0ff.toByte()
                    )
                )
            }

            if (dialog != null && dialog!!.isShowing) {
                dialog!!.dismiss()
            }
            Toast.makeText(this, "清除密码成功", Toast.LENGTH_LONG).show()

        } catch (e: IOException) {
            e.printStackTrace()
            if (dialog != null && dialog!!.isShowing) {
                dialog!!.dismiss()
            }

        } catch (e: FormatException) {
            e.printStackTrace()
            if (dialog != null && dialog!!.isShowing) {
                dialog!!.dismiss()
            }
        } finally {
            mfc.close()

        }

    }


    /**
     * 写入NFC设置密码
     */

    private fun writePassword(mfc: MifareUltralight) {


        val sp = getSharedPreferences("pwd", Context.MODE_PRIVATE)
        val pwdstr = sp.getString("pwd", "");
        //创建默认为0的4字节数组
        val pwd = Array<Byte>(4) { ((0).toByte()) }
        val temp = pwdstr?.toByteArray()
        if (temp != null) {
            for ((index, e) in temp.withIndex()) {
                pwd[index] = temp[index]
            }
        }
        //得出的PWD即用户设置的密码


        mfc.connect()

        val pwd_default = byteArrayOf(0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte())
        val pack = byteArrayOf(0.toByte(), 0.toByte())


        try {

            //先用默认密码进行询问
            val response = mfc.transceive(
                byteArrayOf(
                    0x1B  //PWD_AUTH
                    , pwd_default[0], pwd_default[1], pwd_default[2], pwd_default[3]
                )
            )

            // Check if PACK is matching expected PACK
            // This is a (not that) secure method to check if tag is genuine
            if ((response != null) && (response.size >= 2)) {
                val packResponse = Arrays.copyOf(response, 2);
                if (!(pack[0] == packResponse[0] && pack[1] == packResponse[1])) {
                    Toast.makeText(
                        this@MainActivity,
                        "Tag could not be authenticated:\n$packResponse≠$pack",
                        Toast.LENGTH_LONG
                    ).show();
                }
            }

            // set PACK:
            mfc.transceive(
                byteArrayOf(
                    0xA2.toByte(),
                    0x2C, /*PAGE 44*/
                    pack[0], pack[1], 0, 0  // Write PACK into first 2 Bytes and 0 in RFUI bytes
                )
            )

            // set PWD:  设置密码为用户设置的密码
            mfc.transceive(
                byteArrayOf(
                    0xA2.toByte(),
                    0x2B,  /*PAGE 43*/
                    pwd[0],
                    pwd[1],
                    pwd[2],
                    pwd[3]  // Write PACK into first 2 Bytes and 0 in RFUI bytes
                )
            )


            // set AUTHLIM: 设置错误次数限制
            val responseAuthLim = mfc.readPages(42)
            if (responseAuthLim != null && responseAuthLim.size >= 16) {
                val prot =
                    false  // false = PWD_AUTH for write only, true = PWD_AUTH for read and write
                val authLim = 0;  //0-7

                mfc.transceive(
                    byteArrayOf(
                        0xA2.toByte(),
                        42,
                        (responseAuthLim[0] and 0x078 or (if (prot) 0x080.toByte() else 0x000) or ((authLim and 0x007).toByte())).toByte()
                        ,
                        responseAuthLim[1],
                        responseAuthLim[2],
                        responseAuthLim[3]

                        //将1-3位按原数据写会
                    )
                )
            }

            //设置Auth0  auth0实际控制是否启用密码保护
            val responseAuth0 = mfc.readPages(41)

            if (responseAuth0 != null && responseAuth0.size >= 16) {
                val prot =
                    false;  // false = PWD_AUTH for write only, true = PWD_AUTH for read and write
                val auth0 = 0;


                mfc.transceive(
                    byteArrayOf(
                        0xA2.toByte(),
                        41,
                        responseAuthLim[0],
                        responseAuthLim[1],
                        responseAuthLim[2],

                        //将0-2位按原数据写会
                        (auth0 and 0x0ff).toByte()
                    )
                )
            }

            Log.e("写密码完成", "写密码完成")
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: FormatException) {
            e.printStackTrace()
        } finally {
            mfc.close()

        }


    }


    /**
     * 写入NFC数据
     */
    private fun writeNfcTag(tag: Tag) {
        if (tag == null) {
            return
        }

        //拿出数据库中全部数据
        val data = box.all

        if (data.size == 0) {
            Toast.makeText(this, "至少需要一条记录", Toast.LENGTH_SHORT).show()
            return
        }

        //配置文件加扫码内容
        val ndefArray = arrayOfNulls<NdefRecord>(data.size + 1)

        for ((index, e) in data.withIndex()) {

            if (e.type == 0) {
                //文本
                ndefArray[index] = createTextRecord(e.text.toString().trim())
            } else if (e.type == 1) {
                //网址
                ndefArray[index] = NdefRecord.createUri(Uri.parse(e.text.toString().trim()))
            } else if (e.type == 2) {
                //包名
                ndefArray[index] = NdefRecord.createApplicationRecord(e.text.toString().trim())
            }
        }

        ndefArray[ndefArray.size - 1] = createTextRecord(tv_qr_result.text.toString().trim())

        val ndefMessage =
            NdefMessage(ndefArray)
        val size = ndefMessage.toByteArray().size
        val ndef = Ndef.get(tag)
        try {
            if (ndef != null) {
                ndef.connect()
                if (!ndef.isWritable) {
                    return
                }
                if (ndef.maxSize < size) {
                    return
                }
                ndef.writeNdefMessage(ndefMessage)
                Toast.makeText(this, "写入完成", Toast.LENGTH_SHORT).show()

            } else {
                val ndefFormatable = NdefFormatable.get(tag)
                if (ndefFormatable != null) {
                    ndefFormatable.connect()
                    ndefFormatable.format(ndefMessage)
                    Toast.makeText(this, "写入完成", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "编码错误", Toast.LENGTH_SHORT).show()
                }
            }


        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "连接失败，该卡可能已加密", Toast.LENGTH_SHORT).show()
        } catch (e: FormatException) {
            e.printStackTrace()
        } finally {
            ndef.close()
        }


        val mfc = MifareUltralight.get(tag);
        if (mfc != null) {
            writePassword(mfc)
        } else {
            Toast.makeText(this, "此NFC不支持加密", Toast.LENGTH_SHORT).show()
        }

        if (dialog != null && dialog!!.isShowing)
            dialog!!.dismiss()
    }

    //创建一个封装要写入的文本的NdefRecord对象
    private fun createTextRecord(text: String): NdefRecord {

        var langBytes = Locale.CHINA.language.toByteArray(
            Charset.forName("US-ASCII")
        );
        //将要写入的文本以UTF_8格式进行编码
        var utfEncoding = Charset.forName("UTF-8");
        //因为已经确定文本的格式编码为UTF_8。所以直接将payload的第1个字节的第7位设为0
        var textBytes = text.toByteArray(utfEncoding);
        var utfBit = 0;
        //定义和初始化状态字节
        var status = (utfBit + langBytes.size).toChar();
        //创建存储payload的字节数组
        var data = ByteArray(1 + langBytes.size + textBytes.size);
        //设置状态字节
        data[0] = status.toByte();
        //设置语言编码
        System.arraycopy(langBytes, 0, data, 1, langBytes.size);
        //设置实际要写入的文本
        System.arraycopy(
            textBytes, 0, data, 1 + langBytes.size,
            textBytes.size
        );
        //依据前面设置的payload创建NdefRecord对象
        val record = NdefRecord(
            NdefRecord.TNF_WELL_KNOWN,
            NdefRecord.RTD_TEXT, ByteArray(0), data
        );
        return record;

    }

    override fun onStart() {
        super.onStart()

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this)
        // 这里建议将处理NFC的子类的launchMode设置成singleTop模式，这样感应到标签时就会回调onNewIntent，而不会重复打开页面
        mPendingIntent = PendingIntent.getActivity(this, 0, Intent(this, this::class.java), 0);

    }


    override fun onResume() {
        super.onResume()
        // 设置当该页面处于前台时，NFC标签会直接交给该页面处理
        if (mNfcAdapter != null) {
            mNfcAdapter.enableForegroundDispatch(this, mPendingIntent, null, null);
        }

    }


    override fun onPause() {
        super.onPause()
        // 当页面不可见时，NFC标签不交给当前页面处理
        if (mNfcAdapter != null) {
            mNfcAdapter.disableForegroundDispatch(this);
        }
    }

    override fun onStop() {
        super.onStop()

    }

    /**
     * 照相机权限
     */
    private fun checkPermissions() {

        val rxPermissions = RxPermissions(this)

        rxPermissions
            .request(Manifest.permission.CAMERA)
            .subscribe { granted: Boolean ->
                if (granted) {

                } else {

                }
            }


    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {

            R.id.menu_setting -> {
                //进入设置
                startActivity(Intent(this, ConfigActivity::class.java))
            }

        }



        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                102 -> {
                    val result: String? = data.getStringExtra(Intents.Scan.RESULT)
//                    Toast.makeText(this, result, Toast.LENGTH_SHORT).show()
                    tv_qr_result.text = result
                }
            }
        }
    }
}
