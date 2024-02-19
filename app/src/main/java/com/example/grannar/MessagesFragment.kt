package com.example.grannar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MessagesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MessagesFragment : Fragment(), MessageAdapter.MyAdapterListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val chats = mutableListOf<Chats>()
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: MessageAdapter
    private val args: FriendProfileFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        db = Firebase.firestore
        val view = inflater.inflate(R.layout.fragment_messages, container, false)
        val rvMessageList = view.findViewById<RecyclerView>(R.id.rvMessageList)
        rvMessageList.layoutManager = LinearLayoutManager(view.context)
        adapter = MessageAdapter(view.context, chats, this)
        rvMessageList.adapter = adapter

//        if(args.userID != null) {
//            val action =
//                MessagesFragmentDirections.actionMessagesFragmentToChatFragment(args.userID.toString())
//            findNavController().navigate(action)
//        }



        val btnSearch: Button = view.findViewById(R.id.btnChatSearch)
        getMessagesForUser()

        adapter.onUserClick = {


        }




        return view
    }


    private fun getMessagesForUser() {
        db.collection("messages").whereArrayContains("participants", CurrentUser.userID.toString())
            .get()
            .addOnSuccessListener {documents ->
                for(document in documents) {
                    Log.d("!!!", document.id)
                    val participants = document["participants"] as List<String>
                    val fromID = participants.filterNot { it == CurrentUser.userID.toString() }.first()
                    db.collection("users").document(fromID).get()
                        .addOnSuccessListener {userDoc->
                            val userInfo = userDoc.toObject<User>()

                            db.collection("messages").document(document.id).collection("message")
                                .orderBy("timeStamp", Query.Direction.DESCENDING)
                                .limit(1)
                                .get()
                                .addOnSuccessListener {messages->
                                    Log.d("!!!", messages.toString())
                                    for(message in messages) {
                                        val lastMessage = message.toObject<Message>()
                                        userInfo?.let {
                                            Chats(
                                                fromUser = it,
                                                lastMessage = lastMessage
                                            )
                                        }?.let { chats.add(it) }
                                    }


                                    adapter.notifyDataSetChanged()
                                }

                        }

                    Log.d("!!!", fromID.toString())
                    Log.d("!!!", chats.size.toString())
                    //chats.add(document)
                }

            }
    }
    fun getUserData(userID: String) {
        val db = Firebase.firestore
        var selectedUser: User? = null
        db.collection("users").document(userID).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val selectedUser = document.toObject<User>()

                }
            }

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

    override fun goToMessage(user: User) {
        Log.d("!!!", user.firstName.toString())
        val uid = user.userID
        if(uid != null) {
            val action =
                MessagesFragmentDirections.actionMessagesFragmentToChatFragment(uid)
            findNavController().navigate(action)
        }
    }
}