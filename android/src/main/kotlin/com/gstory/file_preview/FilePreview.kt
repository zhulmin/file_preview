package com.gstory.file_preview

import android.app.Activity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.example.flutter_pangrowth.utils.UIUtils
import com.gstory.file_preview.utils.FileUtils
import com.tencent.smtt.sdk.TbsReaderView
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.platform.PlatformView
import java.io.File


/**
 * @Author: gstory
 * @CreateDate: 2021/12/27 10:34 上午
 * @Description: 描述
 */

internal class FilePreview(
        var activity: Activity,
        messenger: BinaryMessenger?,
        id: Int,
        params: Map<String?, Any?>
) :
        PlatformView {

    private val TAG = "FilePreview"

    private var mContainer: FrameLayout = FrameLayout(activity)
    private var width: Double = params["width"] as Double
    private var height: Double = params["height"] as Double
    private var path: String = params["path"] as String

    private var tbsReaderView: TbsReaderView

    private var readerCallback = object : TbsReaderView.ReaderCallback {
        override fun onCallBackAction(p0: Int?, p1: Any?, p2: Any?) {

        }
    }

    init {
        mContainer.layoutParams?.width = (UIUtils.dip2px(activity, width.toFloat())).toInt()
        mContainer.layoutParams?.height = (UIUtils.dip2px(activity, height.toFloat())).toInt()
        tbsReaderView = TbsReaderView(activity, readerCallback)
        tbsReaderView.layoutParams?.width = ViewGroup.LayoutParams.MATCH_PARENT
        tbsReaderView.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
        mContainer.addView(tbsReaderView)
        loadFile()
    }

    override fun getView(): View {
        return mContainer
    }

    private fun loadFile() {
        //tbs只能加载本地文件 如果是网络文件则先下载
        if (path.startsWith("http")) {
            FileUtils.downLoadFile(activity, path, object : FileUtils.DownloadCallback {
                override fun onProgress(progress: Int) {
//                    Log.e(TAG, "文件下载进度$progress")
                }

                override fun onFail(msg: String) {
                    Log.e(TAG, "文件下载失败$msg")
                }

                override fun onFinish(file: File) {
                    Log.e(TAG, "文件下载完成！")
                    activity.runOnUiThread {
                        openFile(file)
                    }
                }

            })
        } else {
            openFile(File(path))
        }

    }

    /**
     * 打开文件
     */
    private fun openFile(file: File?) {
        if (file != null && !TextUtils.isEmpty(file.toString())) {
            //增加下面一句解决没有TbsReaderTemp文件夹存在导致加载文件失败
            val bsReaderTemp = FileUtils.getDir(activity).toString() + File.separator + "TbsReaderTemp"
            val bsReaderTempFile = File(bsReaderTemp)
            if (!bsReaderTempFile.exists()) {
                val mkdir: Boolean = bsReaderTempFile.mkdir()
                if (!mkdir) {
                    Log.e(TAG, "创建$bsReaderTemp 失败")
                }
            }
            //加载文件
            val localBundle = Bundle()
            Log.d(TAG, file.toString())
            localBundle.putString("filePath", file.toString())
            localBundle.putString("tempPath", bsReaderTemp)
            val bool = tbsReaderView.preOpen(FileUtils.getFileType(file.toString()), false)
            if (bool) {
                tbsReaderView.openFile(localBundle)
            }else{
                Log.e(TAG, "文件打开失败！")
            }
        } else {
            Log.e(TAG, "文件路径无效！")
        }
    }


    override fun dispose() {
        tbsReaderView.onStop()
    }
}