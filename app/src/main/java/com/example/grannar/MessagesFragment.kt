package com.example.grannar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MessagesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MessagesFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val chats = mutableListOf<Chats>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    fun createDummyData() {
//        val messages = mutableListOf<Message>()
//        val user1 = User(firstName = "Jonas")
//        val user2 = User(firstName = "Pelle")
//        val user3 = User(firstName = "Elisabet")
//        messages.add(Message("", user1, toID = user2, message = "hej hej"))
//        messages.add(Message("", user2, toID = user1, message = "hej2"))
//        messages.add(Message("", user1, toID = user2, message = "hej3"))
//
//        val participants = mutableListOf<User>()
//        participants.add(user1)
//        participants.add(user2)
//        chats.add(Chats("", participants, true, messages))
//        participants.clear()
//        participants.add(user1)
//        participants.add(user3)
//        messages.clear()
//        messages.add(Message("", user1, toID = user3, message = "hej hej"))
//        messages.add(Message("", user3, toID = user1, message = "hej2"))
//        messages.add(Message("", user1, toID = user3, message = "hej3"))
//        chats.add(Chats("", participants, true, messages))
//        participants.clear()
//        participants.add(user2)
//        participants.add(user3)
//        messages.clear()
//        messages.add(Message("", user2, toID = user3, message = "hej hej"))
//        messages.add(Message("", user3, toID = user2, message = "hej2"))
//        messages.add(Message("", user2, toID = user3, message = "hej3"))
//        chats.add(Chats("", participants, true, messages))
//        Log.d("!!!", chats.size.toString())
//
//
//        getMessagesForUser()
//        messages.add(Message(docID = "", fromUser = User(firstName = "ElisabetFrom"), toUser = User(firstName = "JonasTO"), message = "Hej min fina"))
//        messages.add(Message(docID = "", fromUser = User(firstName = "ElisabetFrom"), toUser = User(firstName = "ElisabetTo"), message = "Hej min fina"))
//        messages.add(Message(docID = "", fromUser = User(firstName = "JonasFrom"), toUser = User(firstName = "ElisabetTo"), message = "Hej min fina"))

    }

    private fun getMessagesForUser() {
        val user = User(firstName = "Jonas")
        Log.d("!!!", chats.size.toString())
        val chat = chats.filter { chat -> chat.participants.any { it.firstName == user.firstName } }
        Log.d("!!!", chat.toString())

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_messages, container, false)
        val rvMessageList = view.findViewById<RecyclerView>(R.id.rvMessageList)
        createDummyData()
        rvMessageList.layoutManager = LinearLayoutManager(view.context)
        val adapter = MessageAdapter(view.context, chats)
        rvMessageList.adapter = adapter

        val btnSearch: Button = view.findViewById(R.id.btnChatSearch)
        btnSearch.setOnClickListener {
            findNavController().navigate(R.id.action_messagesFragment_to_chatFragment)
        }

        return view
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MessagesFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MessagesFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}