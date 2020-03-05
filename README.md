# TazanTagWritter
NFC写入工具，包括对NTAG213加密解密的功能
NTAG213密码保护开启(采用MifareUltralight配置)：
```
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


```
NTAG213密码保护关闭(采用MifareUltralight配置)：
```
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


```


