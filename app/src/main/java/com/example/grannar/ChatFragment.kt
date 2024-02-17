package com.example.grannar

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
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
    private var docExist: Boolean = false

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
        Log.d("!!!", args.userID.toString())
        getMessages()

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

        return view
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
        val newMessage = Message(
            docID = "",
            fromID = CurrentUser.userID.toString(),
            toID = args.userID.toString(),
            message = etvMessageText.text.toString()
        )
        messages.add(newMessage)
        etvMessageText.text?.clear()
        saveMessageToDataBase(newMessage)
    }


    private fun saveMessageToDataBase(newMessage: Message) {

        val fromID = CurrentUser.userID.toString()
        val toID = args.userID.toString()

        val mapMessage = hashMapOf(
            "fromID" to fromID,
            "toID" to toID,
            "message" to newMessage.message,
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
//
//        val fromToCollectionRef = db.collection("message").document(newMessage.fromID)
//        val toFromCollectionRef = db.collection("message").document(newMessage.toID).collection(newMessage.fromID)
//
//        fromToCollectionRef.get().addOnSuccessListener {documents->
//            if(documents.exists()) {
//                fromToCollectionRef.collection(newMessage.fromID).add(mapMessageFrom).addOnSuccessListener {
//                    Log.d("!!!", "Success fromUser")
//                }
//                    .addOnFailureListener { e ->
//                        Log.e("!!!", "Error fromUser", e)
//                    }
//            } else {
//                Log.d("!!!", "here we go")
//
//                fromToCollectionRef.set(newMessage.fromID).addOnSuccessListener {
//                    fromToCollectionRef.collection(newMessage.fromID).add(mapMessageFrom).addOnSuccessListener {
//                        Log.d("!!!", "Success fromUser")
//                    }
//                        .addOnFailureListener { e ->
//                            Log.e("!!!", "Error fromUser", e)
//                        }
//                }
//            }

//        }
//
//
//        toFromCollectionRef.add(mapMessageFrom)
//            .addOnSuccessListener {
//                Log.d("!!!", "Success toUser")
//            }
//            .addOnFailureListener { e ->
//                Log.e("!!!", "Error toUser", e)
//            }
//
//        db.collection("message").document(newMessage.fromID)
//            .collection(newMessage.toID)
//            .add(mapMessageFrom).addOnSuccessListener {
//                Log.d("!!!", "success fromUser")
//            }
//        db.collection("message").document(newMessage.toID)
//            .collection(newMessage.fromID)
//            .add(mapMessageFrom).addOnSuccessListener {
//                Log.d("!!!", "success fromUser")
//            }



//        val data = mapOf("messages" to FieldValue.arrayUnion(mapMessageFrom))
//
//
//        val docFromRef = db.collection("messages").document(newMessage.fromID)
//        docFromRef.get().addOnSuccessListener {
//            if(it.data == null) {
//                docFromRef.set(data)
//                    .addOnSuccessListener {
//                        Log.d("!!!", "saved in new CurrentUser")
//                    }
//            } else {
//                docFromRef.update(data)
//                    .addOnSuccessListener {
//                        Log.d("!!!", "Saved in old currentuser ${newMessage.fromID}")
//
//                    }
//            }
//        }
//        val docToRef = db.collection("messages").document(newMessage.toID)
//        docToRef.get().addOnSuccessListener {
//            if(it.data == null) {
//                docToRef.set(data)
//                    .addOnSuccessListener {
//                        Log.d("!!!", "saved in new reciever")
//                    }
//            } else {
//                docToRef.update(data)
//                    .addOnSuccessListener {
//                        Log.d("!!!", "Saved in old reciever ${newMessage.toID}")
//
//                    }
//            }
//        }

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