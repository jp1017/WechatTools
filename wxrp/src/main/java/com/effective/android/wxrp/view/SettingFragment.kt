package com.effective.android.wxrp.view

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.LruCache
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cunoraz.tagview.Tag
import com.cunoraz.tagview.TagView
import com.effective.android.wxrp.Constants

import com.effective.android.wxrp.R
import com.effective.android.wxrp.store.Config
import com.effective.android.wxrp.utils.Logger
import com.effective.android.wxrp.utils.ToolUtil
import com.effective.android.wxrp.view.dialog.AddTagDialog
import kotlinx.android.synthetic.main.fragment_setting.*

class SettingFragment : Fragment() {

    companion object {
        private const val TAG = "SettingFragment"
    }

    val tagCache = LruCache<String, Tag>(99)
    val currentTag = ArrayList<Tag>()
    var tagDialg: AddTagDialog? = null
    var currentDelayNum: String = "-1"
    var currentUserName: String = ""
    var isFixationDelay = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
        initListener()
    }

    private fun initData() {
        //编辑tag对话框
        tagDialg = AddTagDialog(context!!, object : AddTagDialog.CommitListener {
            override fun commit(tag: String) {
                if (TextUtils.isEmpty(tag)) {
                    ToolUtil.toast(context!!, context!!.getString(R.string.tag_empty_tip))
                    return
                }
                tag_container.addTag(getTag(tag))
            }
        })
        //tag容器
        val tagStrings = Config.filterTags
        tagStrings.map {
            var tag = tagCache[it]
            if (tag == null) {
                tag = Tag(it)
                tag.background = ColorDrawable(ContextCompat.getColor(context!!, R.color.colorPrimary))
                tag.isDeletable = true
                tagCache.put(it, tag)
            }
            currentTag.add(tag)
        }
    }


    private fun initListener() {
        notification_select.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            } else {
                ToolUtil.toast(context!!, "当前安卓系统版本过低，请手动设置本应用的通知读取权限")
                notification_select.isSelected = ToolUtil.isServiceRunning(context!!, Constants.PACKAGE_SELF_APPLICATION + "." + Constants.CLASS_NOTIFICATION)
            }
        }

        getSelf_select.setOnClickListener {
            val selectStatus = getSelf_select.isSelected
            Config.openGetSelfPacket(!selectStatus)
            getSelf_select.isSelected = !selectStatus
        }

        filter_select.setOnClickListener {
            val filterStatus = filter_select.isSelected
            filer_tag_container.visibility = if (!filterStatus) View.VISIBLE else View.GONE
            if (filter_select.isSelected) {
                tag_container.addTags(currentTag)
            }
            Config.openFilterTag(!filterStatus)
            filter_select.isSelected = !filterStatus
        }

        tag_commit.setOnClickListener {
            tagDialg?.show()
        }

        tag_container.setOnTagDeleteListener(object : TagView.OnTagDeleteListener {

            override fun onTagDeleted(p0: TagView?, p1: Tag?, p2: Int) {
                tag_container.remove(p2)
                Config.filterTags.remove(p1?.text)
            }
        })

        delay_none.setOnClickListener {
            Config.openDelay(true)
            initDelayState(Config.isOpenDelay(), Config.isFixationDelay())
        }

        delay_random.setOnClickListener {
            Config.openDelay(false)
            Config.openFixationDelay(false)
            initDelayState(Config.isOpenDelay(), Config.isFixationDelay())
        }

        delay_fixation.setOnClickListener {
            Config.openDelay(false)
            Config.openFixationDelay(true)
            initDelayState(Config.isOpenDelay(), Config.isFixationDelay())
        }

        delay_num.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {
                if (s.toString().isEmpty()) {
                    delay_commit.text = context!!.getString(R.string.delay_back)
                } else {
                    currentDelayNum = s.toString()
                    if (currentDelayNum.toInt() > 0 && currentDelayNum.toInt() != Config.getDelayTime(true)) {
                        delay_commit.text = context!!.getString(R.string.delay_edit)
                    } else {
                        delay_commit.text = context!!.getString(R.string.delay_back)
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        delay_commit.setOnClickListener {
            if (delay_commit.text == context!!.getString(R.string.delay_edit)) {
                val time = currentDelayNum!!.toInt()
                Config.setDelayTime(time)
                Logger.i(TAG, "提交当前修改，是否是固定延迟 ：$isFixationDelay delayTime : $time")
                ToolUtil.toast(context!!, "已更新延迟时间")
            } else {
                Logger.i(TAG, "撤销当前时间修改")
                delay_num.setText(Config.getDelayTime(true).toString())
            }
        }
    }

    /**
     * 切换延迟
     */
    private fun initDelayState(openDelay: Boolean, isFixation: Boolean) {
        if (openDelay) {
            delay_none.isSelected = true
            delay_random.isSelected = false
            delay_fixation.isSelected = false
            delay_container.visibility = View.GONE
        } else {
            delay_none.isSelected = false
            delay_container.visibility = View.VISIBLE
            if (isFixation) {
                delay_random.isSelected = false
                delay_fixation.isSelected = true
                delay_message.text = context!!.getString(R.string.delay_fixation_message)
                currentDelayNum = Config.getDelayTime(true).toString()
                delay_num.setText(currentDelayNum)
                delay_commit.isEnabled = true
            } else {
                delay_random.isSelected = true
                delay_fixation.isSelected = false
                delay_message.text = context!!.getString(R.string.delay_random_message)
                currentDelayNum = Config.getDelayTime(true).toString()
                delay_num.setText(currentDelayNum)
                delay_commit.isEnabled = true
            }
        }
    }


    private fun initState() {
        notification_select.isSelected = ToolUtil.isServiceRunning(context!!, Constants.PACKAGE_SELF_APPLICATION + "." + Constants.CLASS_NOTIFICATION)
        getSelf_select.isSelected = Config.isOpenGetSelfPacket()
        filter_select.isSelected = Config.isOpenFilterTag()
        filer_tag_container.visibility = if (filter_select.isSelected) View.VISIBLE else View.GONE
        if (filter_select.isSelected) {
            tag_container.addTags(currentTag)
        }
        initDelayState(Config.isOpenDelay(), Config.isFixationDelay())
        currentUserName = Config.getUserWxName()
    }

    private fun getTag(key: String): Tag {
        var tag = tagCache[key]
        if (tag == null) {
            tag = Tag(key)
            tag.background = ColorDrawable(ContextCompat.getColor(context!!, R.color.colorPrimary))
            tag.isDeletable = true
            tagCache.put(key, tag)
        }
        return tag
    }

    override fun onResume() {
        super.onResume()
        initState()
    }
}
