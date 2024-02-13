package com.example.grannar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
 * Use the [SearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var searchList = mutableListOf<User>()
    private lateinit var db : FirebaseFirestore
    private lateinit var adapter: SearchListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        db = Firebase.firestore
        adapter = SearchListAdapter(requireContext(), searchList)
        getUsersList()


    }
//    private fun createDummySearchList() {
//
//        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
//        searchList.add(User(firstName = "Jonas", userID = "userID1", age = "1950/01/12"))
//        Log.d("!!!", Date(1990, 0, 12).toString())
//        searchList.add(User(firstName = "Frida", userID = "userID2", age = "1970/01/12"))
//        searchList.add(User(firstName = "Kristian", userID = "userID3", age = "1990/01/12"))
//        searchList.add(User(firstName = "Wed", userID = "userID4", age = "1990/01/12"))
//
//        CurrentUser.firstName = "Jonas"
//        CurrentUser.surname = "Bondesson"
//
//
//    }

    private fun getUsersList() {
        searchList.clear()
        Log.d("!!!", db.toString())
        db.collection("users").get().addOnSuccessListener { result ->
            for ((i, document) in result.withIndex()) {
                Log.d("!!!", "${document.id} => ${document.data}")
                val user = document.toObject<User>()
                searchList.add(user)
                adapter.notifyItemInserted(i)
            }

        }

            .addOnFailureListener { exception ->
                Log.d("!!!", "Error getting documents: ", exception)
            }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search, container, false)


        val rvSearchList = view.findViewById<RecyclerView>(R.id.rvSearchList)
        rvSearchList.layoutManager = LinearLayoutManager(view.context)

        rvSearchList.adapter = adapter


        adapter.onUserClick = {

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
}