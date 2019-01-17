package com.effective.android.wxrp.view

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.util.LruCache
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import com.cunoraz.tagview.Tag
import com.cunoraz.tagview.TagView
import com.effective.android.wxrp.Constants

import com.effective.android.wxrp.R
import com.effective.android.wxrp.store.Config
import com.effective.android.wxrp.utils.ToolUtil
import com.effective.android.wxrp.view.dialog.AddTagDialog
import kotlinx.android.synthetic.main.fragment_setting.*

class SettingFragment : Fragment() {

    val tagCache = LruCache<String, Tag>(99)
    var tagDialg: AddTagDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tagDialg = AddTagDialog(context!!, object : AddTagDialog.CommitListener {

            override fun commit(tag: String) {
                if (TextUtils.isEmpty(tag)) {
                    ToolUtil.toast(context!!, context!!.getString(R.string.tag_empty_tip))
                    return
                }
                tag_container.addTag(getTag(tag))
            }
        });
        initListener()
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

        accessibility_select.setOnClickListener {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        getSelf_select.setOnClickListener {
            val selectStatus = getSelf_select.isSelected
            Config.openGetSelfPacket(!selectStatus)
            getSelf_select.isSelected = !selectStatus
        }

        filter_select.setOnClickListener {
            val filterStatus = filter_select.isSelected
            initFilterTags(!filterStatus)
            Config.openFilterTag(!filterStatus)
            filter_select.isSelected = !filterStatus
        }

        tag_commit.setOnClickListener {
            tagDialg?.show()
        }

        tag_container.setOnTagDeleteListener(object : TagView.OnTagDeleteListener {

            override fun onTagDeleted(p0: TagView?, p1: Tag?, p2: Int) {
                tag_container.remove(p2)
            }
        })


        delay_select.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val options = context!!.resources.getStringArray(R.array.delay_action)
                ToolUtil.toast(context!!, "选中了" + options[position] + "")
                Config.setDelayOption(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }
    }


    private fun initState() {
        notification_select.isSelected = ToolUtil.isServiceRunning(context!!, Constants.PACKAGE_SELF_APPLICATION + "." + Constants.CLASS_NOTIFICATION)
        accessibility_select.isSelected = ToolUtil.isServiceRunning(context!!, Constants.PACKAGE_SELF_APPLICATION + "." + Constants.CLASS_ACCESSBILITY)
        getSelf_select.isSelected = Config.isOpenGetSelfPacket()
        filter_select.isSelected = Config.isOpenFilterTag()
        initFilterTags(filter_select.isSelected)
    }

    private fun getTag(key: String): Tag {
        var tag = tagCache[key]
        if (tag == null) {
            tag = Tag(key)
            tag.background = ColorDrawable(ContextCompat.getColor(context!!, R.color.colorPrimary))
            tag.isDeletable = true
            tagCache.put(key, tag)
        }
        return tag;
    }

    private fun initFilterTags(visible: Boolean) {
        filer_tag_container.visibility = if (visible) View.VISIBLE else View.GONE
        if (visible) {
            val tagStrings = Config.filterTags
            val tags = ArrayList<Tag>()
            tagStrings.map {
                var tag = tagCache[it]
                if (tag == null) {
                    tag = Tag(it)
                    tag.background = ColorDrawable(ContextCompat.getColor(context!!, R.color.colorPrimary))
                    tag.isDeletable = true
                    tagCache.put(it, tag)
                }
                tags.add(tag)
            }
            tag_container.addTags(tags)
        }
    }

    override fun onResume() {
        super.onResume()
        initState()
    }
}
