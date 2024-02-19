package com.example.grannar

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject

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
    private val args: FriendProfileFragmentArgs by navArgs()
    private lateinit var adapter: ChatAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var tvName: TextView
    private lateinit var imImage: ImageView
    private var docID: String? = null
    private var docExist: Boolean = false

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
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        db = Firebase.firestore
        tvName = view.findViewById(R.id.tvChatName)
        imImage = view.findViewById(R.id.imChatImage)
        
        getDocID()

        getUserInfo(args.userID.toString())

        val rvChatMessage = view.findViewById<RecyclerView>(R.id.rvChatMessages)
        rvChatMessage.layoutManager = LinearLayoutManager(view.context)
        adapter = ChatAdapter(view.context, messages)
        rvChatMessage.adapter = adapter
        etvMessageText = view.findViewById(R.id.etvMessageText)
        val btnSend: Button = view.findViewById(R.id.btnSendMessage)
        btnSend.setOnClickListener {
            addNewMessage()

            hideKeyboard(view)
        }
        val appBar = view.findViewById<MaterialToolbar>(R.id.topChat)

        appBar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        return view
    }

    private fun getUserInfo(friendUid: String) {
        val db = Firebase.firestore
        db.collection("users").document(friendUid).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val selectedUser = document.toObject<User>()
                    if (selectedUser != null) {
                        showInfo(selectedUser)
                    }
                }
            }
    }

    private fun showInfo(selectedUser: User) {
        tvName.text = selectedUser.firstName
    }


    private fun getOldMessages() {
        Log.d("!!!", docID.toString())
        docID?.let {
            db.collection("messages").document(it).collection("message")
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        return@addSnapshotListener
                    } else {
                        if (snapshot != null) {
                            messages.clear()
                            for (document in snapshot.documents) {
                                val newMessage = document?.toObject<Message>()
                                if (newMessage != null) {
                                    messages.add(newMessage)
                                }
                            }
                            messages.sortBy { it.timeStamp }
                            adapter.notifyDataSetChanged()

                        }

                    }

                }

        }

    }


    private fun addNewMessage() {
        saveMessageToDataBase()

    }

    private fun getDocID() {

        db.collection("messages")
            .document(CurrentUser.userID.toString() + "_" + args.userID.toString()).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    docExist = true
                    docID = CurrentUser.userID.toString() + "_" + args.userID.toString()
                    Log.d("!!!", "here")
                    getOldMessages()
                }
                Log.d("!!!", "Doc exists: $docExist")
            }
        if (!docExist) {
            db.collection("messages")
                .document(args.userID.toString() + "_" + CurrentUser.userID.toString()).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        docExist = true
                        docID = args.userID.toString() + "_" + CurrentUser.userID.toString()
                        getOldMessages()
                    }
                }
        }
    }

    private fun createDoc() {
        val participants = listOf(CurrentUser.userID.toString(), args.userID.toString())
        val data = mapOf(
            "participants" to participants
        )
        db.collection("messages")
            .document(CurrentUser.userID.toString() + "_" + args.userID.toString()).set(data)
            .addOnSuccessListener {
                docID = CurrentUser.userID.toString() + "_" + args.userID.toString()
                saveMessage()

            }
    }


    private fun saveMessageToDataBase() {
        Log.d("!!!", "docexist : $docExist")
        if (docExist) {
            saveMessage()
        } else {
            createDoc()
        }

    }

    private fun saveMessage() {
        Log.d("!!!", docID.toString())
        docID?.let {
            db.collection("messages").document(it).collection("message")
                .add(
                    Message(
                        fromID = CurrentUser.userID.toString(),
                        toID = args.userID.toString(),
                        unread = true,
                        text = etvMessageText.text.toString()
                    )
                ).addOnSuccessListener {
                    docExist = true
                    getOldMessages()
                }
        }
    }


    fun hideKeyboard(view: View) {
        val inputMethodManager =
            view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
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