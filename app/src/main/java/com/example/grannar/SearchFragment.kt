package com.example.grannar

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Filter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
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
    private lateinit var etvSearch: EditText
    private lateinit var fabFilter: FloatingActionButton
    private var selectedCategories = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        db = Firebase.firestore
        adapter = SearchListAdapter(requireContext(), listInRecyclerView, this)

    }



    private fun getUsersList() {
        searchList.clear()
        Log.d("!!!", db.toString())

        db.collection("users").get().addOnSuccessListener { result ->
            for ((i, document) in result.withIndex()) {
                Log.d("!!!", "${document.id} => ${document.data}")
                val user = document.toObject<User>()
                searchList.add(user)
            }

            setListInRecyclerView(searchList)
        }
            .addOnFailureListener { exception ->
                Log.d("!!!", "Error getting documents: ", exception)
            }

    }

    private fun filterListOnInterestCategory(listToFilter: MutableList<User>):List<User>{
        if (selectedCategories.isEmpty()){
            fabFilter.setImageResource(R.drawable.baseline_filter_alt_off_24)
            fabFilter.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.md_theme_primaryContainer))
            return listToFilter
        }else {
            val filteredList = listToFilter.filter { user ->
                user.interests?.any {interest ->
                    interest.category in selectedCategories
                } == true
            }
            fabFilter.setImageResource(R.drawable.baseline_filter_list_alt_242)
            fabFilter.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E6FF00"))
            return filteredList
        }
    }

    private fun filterListOnInterestName(listToFilter: List<User>): List<User>{

        val interestName = etvSearch.text.toString()
        if (interestName.isEmpty()){
            return listToFilter
        }
        else {
            val filteredList = listToFilter.filter { user ->
                user.interests?.any {interest ->
                    interest?.name?.lowercase() == interestName.lowercase()
                } == true
            }
            return filteredList
        }
    }

    private fun setListInRecyclerView(users: MutableList<User>){
        listInRecyclerView.clear()

        val categoryFilteredList = filterListOnInterestCategory(users)
        val nameFilteredList = filterListOnInterestName(categoryFilteredList)

        listInRecyclerView.addAll(nameFilteredList)
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
        getUsersList()
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

            val auth = Firebase.auth
            CurrentUser.clearUser()
            auth.signOut()
            adapter.notifyDataSetChanged()
            Log.d("!!!", CurrentUser.friendsList?.size.toString())
        }
        etvSearch = view.findViewById<EditText>(R.id.etvSearchInterest)
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        view.findViewById<Button>(R.id.btnSearch).setOnClickListener {
            setListInRecyclerView(searchList)
            etvSearch.clearFocus()
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        fabFilter = view.findViewById(R.id.fabFilter)
        fabFilter.setOnClickListener {

            openCategoryFilterDialog()
        }



        return view
    }


    private fun openDialogFragment() {
        val dialogFragment = SignInDialogFragment()
        dialogFragment.show(parentFragmentManager, "SignInDialogFragment")
    }

    private fun openCategoryFilterDialog(){
//        val categories = listOf(
//         Category("Sport", R.color.sportCategory),
//         Category("Nature", R.color.natureCategory),
//         Category("Animals", R.color.animalsCategory),
//         Category("Music", R.color.musicCategory),
//         Category("Literature", R.color.literatureCategory),
//         Category("Travel", R.color.travelCategory),
//         Category("Games", R.color.gamesCategory),
//         Category("Exercise", R.color.exerciseCategory),
//         Category("Other", R.color.otherCategory),
//    )

        val dialog = Dialog(requireContext())
         dialog.setContentView(R.layout.dialog_category_filter)
        val container = dialog.findViewById<LinearLayout>(R.id.categoryLinearLayout)
        val checkBoxes = mutableListOf<CheckBox>()
        CategoryManager.categories.forEach { (category, colorID) ->
        //categories.forEach { category ->
            val checkBox = CheckBox(requireContext())
            checkBox.text = category
            checkBox.setBackgroundColor(ContextCompat.getColor(requireContext(), colorID))

            if (selectedCategories.contains(category)){
                checkBox.isChecked = true
            }
            checkBoxes.add(checkBox)
            container.addView(checkBox)
        }
        container.requestLayout()
        dialog.findViewById<Button>(R.id.btnCancelAddFilter).setOnClickListener {
            dialog.dismiss()
        }

        dialog.findViewById<Button>(R.id.btnAddCategoryFilter).setOnClickListener {
            setCategoryFilter(checkBoxes, dialog)
        }
        dialog.findViewById<TextView>(R.id.tvClearFilter).setOnClickListener {
            selectedCategories.clear()
            checkBoxes.forEach { checkBox ->
                checkBox.isChecked = false
            }
            setListInRecyclerView(searchList)
            dialog.dismiss()
        }


        dialog.show()
    }

    private fun setCategoryFilter(checkBoxes: List<CheckBox>, dialog: Dialog){
        selectedCategories.clear()
        checkBoxes.forEach { checkBox ->
            if (checkBox.isChecked)
                selectedCategories.add(checkBox.text.toString())
        }
        Log.d("!!!", selectedCategories.toString())
        setListInRecyclerView(searchList)

        dialog.dismiss()

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