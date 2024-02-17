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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects

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
    private val args : FriendProfileFragmentArgs by navArgs()
    private lateinit var  adapter: ChatAdapter
    private lateinit var db: FirebaseFirestore
    private lateinit var tvName: TextView
    private lateinit var imImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        //createMessages()
    }

//    private fun createMessages() {
//        messages.add(Message("","KJOaLgBuHkW8YZcdmPA4qhifxED3", "toId", "this is the message from currentUser"))
//        messages.add(Message("","fromID", "KJOaLgBuHkW8YZcdmPA4qhifxED3", "this is the message TO current User"))
//        messages.add(Message("","KJOaLgBuHkW8YZcdmPA4qhifxED3", "toId", "this is the message FROM currentUser"))
//        messages.add(Message("","fromID", "KJOaLgBuHkW8YZcdmPA4qhifxED3", "this is the message TO current user"))
//
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        db = Firebase.firestore
        tvName = view.findViewById(R.id.tvChatName)
        imImage = view.findViewById(R.id.imChatImage)

        getMessages()
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

    private fun getMessages() {

        db.collection("messages").document(CurrentUser.userID.toString()).collection(args.userID.toString())
            .addSnapshotListener {snapshot, e ->
                if(e != null) {
                    Log.d("!!!", "error ", e)
                    return@addSnapshotListener
                }
                if(snapshot != null) {
                    messages.clear()
                    val chatMessage = snapshot.toObjects<Message>()
                    for ( message in chatMessage) {
                        messages.add(message)
                        adapter.notifyDataSetChanged()
                    }

                }
                messages.sortBy { it.timeStamp }


            }
    }


    private fun addNewMessage() {
//        val newMessage = Message(
//            docID = "",
//            fromID = CurrentUser.userID.toString(),
//            toID = args.userID.toString(),
//            message = etvMessageText.text.toString(),
//
//        )
        //messages.add(newMessage)
        saveMessageToDataBase()
        etvMessageText.text?.clear()
    }


    private fun saveMessageToDataBase() {

        val fromID = CurrentUser.userID.toString()
        val toID = args.userID.toString()
        val message = etvMessageText.text.toString()
        Log.d("!!!", message)
        val mapMessage = hashMapOf(
            "fromID" to fromID,
            "toID" to toID,
            "message" to message,
            "unread" to true,
            "timeStamp" to FieldValue.serverTimestamp()
        )

        val messageFromDocumentRef = db.collection("messages").document(fromID)
        val chatMessagesFromCollectionRef = messageFromDocumentRef.collection(toID)

        chatMessagesFromCollectionRef.add(mapMessage)
            .addOnSuccessListener { documentReference ->
                Log.d("!!!", "DocumentSnapshot written with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("!!!", "Error adding document", e)
            }
        val messageToDocumentRef =  db.collection("messages").document(toID)
        val chatMessagesToCollectionRef = messageToDocumentRef.collection(fromID)

        chatMessagesToCollectionRef.add(mapMessage)
            .addOnSuccessListener { documentReference ->
                Log.d("!!!", "DocumentSnapshot written with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("!!!", "Error adding document", e)
            }


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