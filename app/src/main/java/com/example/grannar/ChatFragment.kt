package com.example.grannar

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ChatFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChatFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val messages = mutableListOf<Message>()
    private lateinit var etvMessageText: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        createMessages()
    }

    private fun createMessages() {
        messages.add(Message("","KJOaLgBuHkW8YZcdmPA4qhifxED3", "toId", "this is the message from currentUser"))
        messages.add(Message("","fromID", "KJOaLgBuHkW8YZcdmPA4qhifxED3", "this is the message TO current User"))
        messages.add(Message("","KJOaLgBuHkW8YZcdmPA4qhifxED3", "toId", "this is the message FROM currentUser"))
        messages.add(Message("","fromID", "KJOaLgBuHkW8YZcdmPA4qhifxED3", "this is the message TO current user"))

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chat, container, false)


        val rvChatMessage = view.findViewById<RecyclerView>(R.id.rvChatMessages)
        rvChatMessage.layoutManager = LinearLayoutManager(view.context)
        val adapter = ChatAdapter(view.context, messages)
        rvChatMessage.adapter = adapter
        etvMessageText = view.findViewById(R.id.etvMessageText)
        val btnSend: Button = view.findViewById(R.id.btnSendMessage)
        btnSend.setOnClickListener {
            addNewMessage()

            hideKeyboard(view)
        }

        return view
    }


    private fun addNewMessage() {
        val newMessage = Message("", CurrentUser.userID.toString(), "to", etvMessageText.text.toString())
        messages.add(newMessage)
        etvMessageText.text?.clear()
        etvMessageText.isFocusable = false

    }
    fun hideKeyboard(view: View) {
        val inputMethodManager = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ChatFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChatFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}