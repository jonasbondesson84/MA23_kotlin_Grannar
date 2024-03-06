package com.example.grannar

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.transition.MaterialContainerTransform
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
    private lateinit var rvChatMessage: RecyclerView
    private var fromProfileImageURL = ""
    private lateinit var constMessage: ConstraintLayout

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
        val bottomNavigationView: BottomNavigationView = requireActivity().findViewById(R.id.bottomNavigationView)
        bottomNavigationView.menu.findItem(R.id.messagesFragment).isChecked = true
        constMessage = view.findViewById(R.id.constMessage)


            constMessage.transitionName = args.userID

//            image = view.findViewById(R.id.friendProfileImageView)
//        image.transitionName = friendUid
//
//        val animation = TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)
//        sharedElementEnterTransition = animation
//        sharedElementReturnTransition = animation
        sharedElementEnterTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.nav_host_fragment
            duration = 500
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(resources.getColor(R.color.md_theme_background))

        }
        sharedElementReturnTransition = MaterialContainerTransform().apply {
            drawingViewId = R.id.nav_host_fragment
            duration = 500
            scrimColor = Color.TRANSPARENT
            setAllContainerColors(resources.getColor(R.color.md_theme_background))
        }
        getDocID()

        getUserInfo(args.userID.toString(), view)

        rvChatMessage = view.findViewById(R.id.rvChatMessages)
        rvChatMessage.layoutManager = LinearLayoutManager(view.context)
        adapter = ChatAdapter(view.context, messages, fromProfileImageURL)
        rvChatMessage.adapter = adapter
//        adapter = ChatAdapter(view.context, messages, fromProfileImageURL)
//        rvChatMessage.adapter = adapter
        etvMessageText = view.findViewById(R.id.etvMessageText)
        val btnSend: Button = view.findViewById(R.id.btnSendMessage)
        btnSend.setOnClickListener {
            addNewMessage()

            hideKeyboard(view)
        }
        val appBar = view.findViewById<MaterialToolbar>(R.id.topChat)

        tvName.setOnClickListener {
            goToProfile()

        }
        imImage.setOnClickListener {
            goToProfile()
        }

        appBar.setNavigationOnClickListener {
            findNavController().navigateUp()
//            val action = ChatFragmentDirections.actionChatFragmentToMessagesFragment()
//            findNavController().navigate(action)
        }
        val scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val firstVisibleItemPosition = (recyclerView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
                val lastVisibleItemPosition = (recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition()

                updateLastRead(firstVisibleItemPosition, lastVisibleItemPosition)
            }
        }

        rvChatMessage.addOnScrollListener(scrollListener)

        return view
    }

    private fun goToProfile() {
        val action = ChatFragmentDirections.actionChatFragmentToFriendProfileFragment(args.userID.toString())
        findNavController().navigate(action)
    }

    fun updateLastRead(firstVisibleItemPosition: Int, lastVisibleItemPosition: Int) {

        for (i in firstVisibleItemPosition..lastVisibleItemPosition) {

            var item = messages[i]
            if(item.unread && item.toID == CurrentUser.userID.toString()) {
                docID?.let { item.docID?.let { it1 ->
                    db.collection("messages").document(it).collection("message").document(
                        it1
                    ).update("unread", false).addOnSuccessListener {
                        item.unread = false
                    }

                } }
            }
            if(i == messages.size-1) {
                markAsRead()
            }

        }
    }


//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        // Skapa en callback för hantering av bakåtknappen
//        val callback = object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                // Lägg till argument och navigera tillbaka
//                val action = ChatFragmentDirections.actionChatFragmentToMessagesFragment()
//                findNavController().navigate(action)
//                //findNavController().popBackStack()
//            }
//        }
//
//        // Lägg till callbacken till FragmentActivity
//        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
//    }




    private fun getUserInfo(friendUid: String, view: View) {
        val db = Firebase.firestore
        db.collection("users").document(friendUid).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val selectedUser = document.toObject<User>()
                    if (selectedUser != null) {
                        showInfo(selectedUser)
                        fromProfileImageURL = selectedUser.profileImageURL.toString()
                        adapter = ChatAdapter(view.context, messages, fromProfileImageURL)
                        rvChatMessage.adapter = adapter
                    }
                }
            }
            .addOnFailureListener {
                adapter = ChatAdapter(view.context, messages, fromProfileImageURL)
                rvChatMessage.adapter = adapter
            }
    }

    private fun showInfo(selectedUser: User) {
        tvName.text = selectedUser.firstName
        Glide
            .with(requireContext())
            .load(selectedUser?.profileImageURL)
            .centerCrop()
            .placeholder(R.drawable.img_album)
            .error(R.drawable.img_album)
            .into(imImage)
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
//                            for(document in snapshot.documentChanges) {
//                                if(document.type == DocumentChange.Type.ADDED) {
//                                    val newMessage = document.document.toObject<Message>()
//                                    Log.d("!!!", newMessage.toString())
////                                    if(newMessage != null) {
//
//                                    messages.add(newMessage)
//                                    messages.sortBy { it.timeStamp }
//                                        Log.d("!!!", "new message added")
//                                        adapter.notifyItemInserted(messages.size-1)
//                                        rvChatMessage.scrollToPosition(messages.size-1)
//                                //rvChatMessage.scrollToPosition(getFirstUnread()-1)
//                                        markAsRead()
////                                    }
//                                }
//                            }
                            for(document in snapshot) {
                                messages.clear()
                                for (document in snapshot.documents) {
                                    val newMessage = document?.toObject<Message>()
                                    if (newMessage != null) {
                                        //set lastRead position
                                        messages.add(newMessage)
                                    }

                                }
                            }
                            messages.sortBy { it.timeStamp }
                            adapter.notifyDataSetChanged()
                            Log.d("!!!", "new message")
                            rvChatMessage.scrollToPosition(messages.size - 1)
                            //markAsRead()
                        }

                    }

                }

        }

    }

    private fun markAsRead() {
        var newHashMap = mutableMapOf<String, Int>()
        for(unreadMessage in CurrentUser.unreadMessageIDs) {
            if(unreadMessage.key != args.userID.toString()) {
                newHashMap[unreadMessage.key] = unreadMessage.value
            }

        }
        db.collection("users").document(CurrentUser.userID.toString())
            .update("unreadMessages", newHashMap).addOnSuccessListener {
                Log.d("!!!", "hurra")
            }
        Log.d("!!!", newHashMap.toString())
    }

    private fun getFirstUnread(): Int {
        for(message in messages) {
            if(message.toID == CurrentUser.userID.toString() && message.unread) {
                return messages.indexOf(message)
            }

        }
        return messages.size

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
                    hideKeyboard(requireView())
                    etvMessageText.text?.clear()
                    addToUnreadMessage()
                }
        }
    }

    private fun addToUnreadMessage() {
        val updates = hashMapOf<String, Any>(
            "unreadMessages.${CurrentUser.userID}" to 1
        )
        Log.d("!!!", updates.toString())
            db.collection("users").document(args.userID.toString())
                .update(updates).addOnSuccessListener {
                    Log.d("!!!", "added unread")
                }
                .addOnFailureListener {
                    Log.d("!!!", it.toString())
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