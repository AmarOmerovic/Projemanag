package com.amaromerovic.projemanag.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.amaromerovic.projemanag.adapter.MemberListAdapterDialog
import com.amaromerovic.projemanag.databinding.DialogListBinding
import com.amaromerovic.projemanag.models.User

abstract class MembersListDialog(
    context: Context,
    private var list: ArrayList<User>,
    private val title: String = ""
) : Dialog(context) {

    private var adapter: MemberListAdapterDialog? = null
    private lateinit var binding: DialogListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogListBinding.inflate(layoutInflater)

        setContentView(binding.root)
        setCancelable(true)
        setCanceledOnTouchOutside(true)
        setUpRecyclerView()

    }

    private fun setUpRecyclerView() {
        binding.titleDialogList.text = title
        if (list.size > 0) {
            binding.recyclerViewDialogList.layoutManager = LinearLayoutManager(context)
            adapter = MemberListAdapterDialog(context, list)
            binding.recyclerViewDialogList.adapter = adapter

            adapter!!.onMemberClick(object : MemberListAdapterDialog.OnMemberClickListener {
                override fun onMemberClick(position: Int, user: User, action: String) {
                    dismiss()
                    onItemSelected(user, action)
                }
            })
        }
    }

    protected abstract fun onItemSelected(user: User, action: String)
}