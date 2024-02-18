package com.example.grannar

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchFragment : Fragment(), SearchListAdapter.MyAdapterListener,  SignInResultListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var searchList = mutableListOf<User>()
    private var listInRecyclerView = mutableListOf<User>()
    private lateinit var db : FirebaseFirestore
    private lateinit var adapter: SearchListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        db = Firebase.firestore
        adapter = SearchListAdapter(requireContext(), listInRecyclerView, this)
        getUsersList()
    }



    private fun getUsersList() {
        searchList.clear()
        Log.d("!!!", db.toString())

        db.collection("users").get().addOnSuccessListener { result ->
            for ((i, document) in result.withIndex()) {
                Log.d("!!!", "${document.id} => ${document.data}")
                val user = document.toObject<User>()
                searchList.add(user)
                adapter.notifyItemChanged(i)
            }
            listInRecyclerView.addAll(searchList)
           // filterListOnInterestCategory(listOf("Sport", "Animals"))
           // filterListOnInterestName("Fotboll")

        }

            .addOnFailureListener { exception ->
                Log.d("!!!", "Error getting documents: ", exception)
            }

    }

    private fun filterListOnInterestCategory(categoryNames: List<String>){
        val filteredList = searchList.filter { user ->
            user.interests?.any {interest ->
                interest.category?.name in categoryNames
            } == true
        }
        setListInRecyclerView(filteredList)
    }

    private fun filterListOnInterestName(interestName: String){
        val filteredList = searchList.filter { user ->
            user.interests?.any {interest ->
                interest?.name?.lowercase() == interestName.lowercase()
            } == true
        }
        setListInRecyclerView(filteredList)
    }

    private fun setListInRecyclerView(users: List<User>){
        listInRecyclerView.clear()
        listInRecyclerView.addAll(users)
        adapter.notifyDataSetChanged()
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        val btnGetuser = view.findViewById<Button>(R.id.btnGetUser)
        val rvSearchList = view.findViewById<RecyclerView>(R.id.rvSearchList)
        rvSearchList.layoutManager = LinearLayoutManager(view.context)
        rvSearchList.adapter = adapter
        //getUsersList()
        rvSearchList.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        adapter.onUserClick = {
            val uid = it.userID
            if(uid != null) {
                val action =
                    SearchFragmentDirections.actionSearchFragmentToFriendProfileFragment(uid)
                findNavController().navigate(action)
            }

        }
        btnGetuser.setOnClickListener {
            //adapter.notifyDataSetChanged()
            //getUsersList()
            val auth = Firebase.auth
            CurrentUser.clearUser()
            auth.signOut()
            adapter.notifyDataSetChanged()
            Log.d("!!!", CurrentUser.friendsList?.size.toString())
        }
        val etvSearch = view.findViewById<EditText>(R.id.etvSearchInterest)
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        view.findViewById<Button>(R.id.btnSearch).setOnClickListener {
            val searchString = etvSearch.text.toString()
            if (searchString.isNotEmpty()){
                filterListOnInterestName(searchString)


            }
            else{
                setListInRecyclerView(searchList)
            }
            etvSearch.clearFocus()
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

            return view
    }

    private fun openDialogFragment() {
        val dialogFragment = SignInDialogFragment()
        dialogFragment.show(parentFragmentManager, "SignInDialogFragment")
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SeachFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SearchFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onAddFriendsListener(user: User) {
        openDialogFragment()
    }

    override fun onSignInSuccess() {
        adapter.notifyDataSetChanged()
        Log.d("!!!", "onSigntInSuccess @ SearchFragment")
    }

    override fun onSignInFailure() {

    }

    override fun onSignUpPress() {

        Log.d("!!!", "SignUpPress")

    }
}